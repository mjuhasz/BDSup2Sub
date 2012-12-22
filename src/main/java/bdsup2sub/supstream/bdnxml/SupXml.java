/*
 * Copyright 2012 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdsup2sub.supstream.bdnxml;

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.BitmapBounds;
import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.*;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.supstream.SubtitleStream;
import bdsup2sub.tools.QuantizeFilter;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static bdsup2sub.core.Constants.LANGUAGES;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStrXml;
import static bdsup2sub.utils.TimeUtils.timeStrXmlToPTS;

/**
 * Reading and writing of Blu-Ray captions in Xml/Png format.
 */
public class SupXml implements SubtitleStream {

    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = Logger.getInstance();

    /** ArrayList of captions contained in the current file */
    private List<SubPictureXml> subPictures = new ArrayList<SubPictureXml>();
    /** color palette of the last decoded caption  */
    private Palette palette;
    /** bitmap of the last decoded caption  */
    private Bitmap bitmap;
    /** index of dominant color for the current caption  */
    private int primaryColorIndex;
    /** number of forced captions in the current file  */
    private int numForcedFrames;

    /** path of the input stream */
    private String pathName;
    /** file name of XML file used as title */
    private String title;
    /** language id read from the xml */
    private String language = "eng";
    /** resolution read from the xml */
    private Resolution resolution = Resolution.HD_1080;
    /** frame rate read from the stream */
    private double fps = Framerate.FPS_23_976.getValue();
    /** converted xml frame rate read from the stream */
    private double fpsXml = XmlFps(fps);

    /**
     * Constructor (for reading)
     * @param filename file name of Xml file to read
     * @throws CoreException
     */
    public SupXml(String filename) throws CoreException {
        this.pathName = FilenameUtils.addSeparator(FilenameUtils.getParent(filename));
        this.title = FilenameUtils.removeExtension(FilenameUtils.getName(filename));

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
            DefaultHandler handler = new XmlHandler();
            saxParser.parse(new File(filename), handler);
        } catch (ParserConfigurationException e) {
            throw new CoreException(e.getMessage());
        } catch (SAXException e) {
            throw new CoreException(e.getMessage());
        } catch (IOException e) {
            throw new CoreException(e.getMessage());
        }

        logger.trace("\nDetected " + numForcedFrames + " forced captions.\n");
    }

    /**
     * Return an integer frame rate in BDN XML style
     * @param fps source frame rate
     * @return next integer frame rate (yet returned as double)
     */
    private static double XmlFps(double fps) {
        if (fps == Framerate.FPS_23_975.getValue()) {
            return Framerate.FPS_24.getValue();
        } else if (fps == Framerate.FPS_23_976.getValue()) {
            return Framerate.FPS_24.getValue();
        } else if (fps == Framerate.NTSC.getValue()) {
            return 30.0;
        } else if (fps == Framerate.NTSC_I.getValue()) {
            return 60.0;
        } else {
            return fps;
        }
    }

    /* (non-Javadoc)
     * @see deadbeef.SupTools.SubtitleStream#close()
     */
    @Override
    public void close() {
    }

    /* (non-Javadoc)
     * @see deadbeef.SupTools.SubtitleStream#decode(int)
     */
    @Override
    public void decode(int index) throws CoreException {
        try {
            File f = new File(subPictures.get(index).getFileName());
            if (!f.exists()) {
                throw new CoreException("file " + subPictures.get(index).getFileName() + " not found.");
            }
            BufferedImage img = ImageIO.read(f);
            int w = img.getWidth();
            int h = img.getHeight();

            this.palette = null;

            // first try to read image and palette directly from imported image
            if (img.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                IndexColorModel icm = (IndexColorModel)img.getColorModel();
                if (icm.getMapSize() < 255 || (icm.hasAlpha() && icm.getAlpha(255) == 0)) {
                    // create palette
                    palette = new Palette(256);
                    for (int i=0; i < icm.getMapSize(); i++) {
                        int alpha = (icm.getRGB(i) >> 24) & 0xff;
                        if (alpha >= configuration.getAlphaCrop()) {
                            palette.setARGB(i, icm.getRGB(i));
                        } else {
                            palette.setARGB(i, 0);
                        }
                    }
                    // copy pixels
                    WritableRaster raster = img.getRaster();
                    bitmap = new Bitmap(img.getWidth(), img.getHeight(), (byte[])raster.getDataElements( 0, 0, img.getWidth(), img.getHeight(), null ));
                }
            }

            // if this failed, assume RGB image and quantize palette
            if (palette == null) {
                // grab int array (ARGB)
                int[] pixels = new int[w * h];
                img.getRGB(0, 0, w, h, pixels, 0, w);
                // quantize image
                QuantizeFilter qf = new QuantizeFilter();
                bitmap = new Bitmap(img.getWidth(), img.getHeight());
                int ct[] = qf.quantize(pixels, bitmap.getInternalBuffer(), w, h, 255, false, false);
                int size = ct.length;
                if (size > 255) {
                    logger.warn("Quantizer failed.\n");
                    size = 255;
                }
                // create palette
                palette = new Palette(256);
                for (int i=0; i < size; i++) {
                    int alpha = (ct[i] >> 24) & 0xff;
                    if (alpha >= configuration.getAlphaCrop()) {
                        palette.setARGB(i, ct[i]);
                    } else {
                        palette.setARGB(i, 0);
                    }
                }
            }
            primaryColorIndex = bitmap.getPrimaryColorIndex(palette.getAlpha(), configuration.getAlphaThreshold(), palette.getY());
            // crop
            BitmapBounds bounds = bitmap.getCroppingBounds(palette.getAlpha(), configuration.getAlphaCrop());
            if (bounds.yMin>0 || bounds.xMin > 0 || bounds.xMax<bitmap.getWidth()-1 || bounds.yMax<bitmap.getHeight()-1) {
                w = bounds.xMax - bounds.xMin + 1;
                h = bounds.yMax - bounds.yMin + 1;
                if (w < 2) {
                    w = 2;
                }
                if (h < 2) {
                    h = 2;
                }
                bitmap = bitmap.crop(bounds.xMin, bounds.yMin, w, h);
                // update picture
                SubPictureXml pic = subPictures.get(index);
                pic.setImageWidth(w);
                pic.setImageHeight(h);
                pic.setOfsX(pic.getOriginalXOffset() + bounds.xMin);
                pic.setOfsY(pic.getOriginalYOffset() + bounds.yMin);
            }
        } catch (IOException e) {
            throw new CoreException(e.getMessage());
        } catch (OutOfMemoryError e) {
            JOptionPane.showMessageDialog(null,"Out of heap! Use -Xmx256m to increase heap!","Error!", JOptionPane.WARNING_MESSAGE);
            throw new CoreException("Out of heap! Use -Xmx256m to increase heap!");
        }
    }

    /**
     * Create Xml file
     *
     * @param fname file name
     * @param pics Map of SubPictures and their original indexes which were used to generate the png file names
     * @throws CoreException
     */
    public static void writeXml(String fname, SortedMap<Integer, SubPicture> pics) throws CoreException {
        double fps = configuration.getFpsTrg();
        double fpsXml = XmlFps(fps);
        BufferedWriter out = null;
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(fname));
        try {
            out = new BufferedWriter(new FileWriter(fname));
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.newLine();
            out.write("<BDN Version=\"0.93\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"BD-03-006-0093b BDN File Format.xsd\">");
            out.newLine();
            out.write("  <Description>");
            out.newLine();
            out.write("    <Name Title=\"" + name + "\" Content=\"\"/>");
            out.newLine();
            out.write("    <Language Code=\"" + LANGUAGES[configuration.getLanguageIdx()][2] + "\"/>");
            out.newLine();
            String res = configuration.getOutputResolution().getResolutionNameForXml();
            out.write("    <Format VideoFormat=\"" + res + "\" FrameRate=\"" + ToolBox.formatDouble(fps) + "\" DropFrame=\"False\"/>");
            out.newLine();
            long t = pics.get(pics.firstKey()).getStartTime();
            if (fps != fpsXml) {
                t = (t * 2000 + 1001) / 2002;
            }
            String ts = ptsToTimeStrXml(t,fpsXml);
            t = pics.get(pics.lastKey()).getEndTime();
            if (fps != fpsXml) {
                t = (t * 2000 + 1001) / 2002;
            }
            String te = ptsToTimeStrXml(t,fpsXml);
            out.write("    <Events Type=\"Graphic\" FirstEventInTC=\"" + ts + "\" LastEventOutTC=\"" + te + "\" NumberofEvents=\"" + pics.size() + "\"/>");
            out.newLine();
            out.write("  </Description>");
            out.newLine();
            out.write("  <Events>");
            out.newLine();

            for (int idx : pics.keySet()) {
                SubPicture p = pics.get(idx);
                t = p.getStartTime();
                if (fps != fpsXml) {
                    t = (t * 2000 + 1001) / 2002;
                }
                ts = ptsToTimeStrXml(t,fpsXml);
                t = p.getEndTime();
                if (fps != fpsXml) {
                    t = (t * 2000 + 1001) / 2002;
                }
                te = ptsToTimeStrXml(t, fpsXml);
                String forced = p.isForced() ? "True": "False";
                out.write("    <Event InTC=\"" + ts + "\" OutTC=\"" + te + "\" Forced=\"" + forced + "\">");
                out.newLine();

                String pname = getPNGname(name, idx+1);
                out.write("      <Graphic Width=\"" + p.getImageWidth() + "\" Height=\"" + p.getImageHeight()
                        + "\" X=\"" + p.getXOffset() + "\" Y=\"" + p.getYOffset() + "\">" + pname + "</Graphic>");
                out.newLine();
                out.write("    </Event>");
                out.newLine();
            }
            out.write("  </Events>");
            out.newLine();
            out.write("</BDN>");
            out.newLine();
        } catch (IOException ex) {
            throw new CoreException(ex.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getBitmap()
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getImage()
     */
    public BufferedImage getImage() {
        return bitmap.getImage(palette.getColorModel());
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getImage(Bitmap)
     */
    public BufferedImage getImage(final Bitmap bm) {
        return bm.getImage(palette.getColorModel());
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getForcedFrameCount()
     */
    public int getForcedFrameCount() {
        return numForcedFrames;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getNumFrames()
     */
    public int getFrameCount() {
        return subPictures.size();
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getPalette()
     */
    public Palette getPalette() {
        return palette;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getPrimaryColorIndex()
     */
    public int getPrimaryColorIndex() {
        return primaryColorIndex;
    }

    /* (non-Javadoc)
     * @see deadbeef.SupTools.SubtitleStream#getStartOffset(int)
     */
    public long getStartOffset(int index) {
        // dummy
        return 0;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getSubPicture(int)
     */
    public SubPicture getSubPicture(int index) {
        return subPictures.get(index);
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getEndTime(int)
     */
    public long getEndTime(int index) {
        return subPictures.get(index).getEndTime();
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getStartTime(int)
     */
    public long getStartTime(final int index) {
        return subPictures.get(index).getStartTime();
    }

    /* (non-Javadoc)
     * @see SubtitleStream#isForced(int)
     */
    public boolean isForced(int index) {
        return subPictures.get(index).isForced();
    }

    /**
     * Create PNG name from (xml) file name and index
     * @param fn file name
     * @param idx index
     * @return PNG name
     */
    public static String getPNGname(String fn, int idx) {
        return FilenameUtils.removeExtension(fn) + "_" + ToolBox.leftZeroPad(idx, 4) + ".png";
    }

    /**
     * get language read from Xml
     * @return language as String
     */
    public String getLanguage() {
        return language;
    }

    /**
     * get fps read from Xml
     * @return frame rate as double
     */
    public double getFps() {
        return fps;
    }

    enum XmlState { BDN, DESCRIPT, NAME, LANGUAGE, FORMAT, EVENTS, EVENT, GRAPHIC, UNKNOWN}

    private static final String xmlStates[] = { "bdn", "description", "name", "language", "format", "events", "event", "graphic"};

    class XmlHandler extends DefaultHandler {

        XmlState state;
        StringBuffer txt;
        boolean valid;
        SubPictureXml pic;

        private XmlState findState(final String s) {
            for (XmlState x : XmlState.values()) {
                if (s.toLowerCase().equals(xmlStates[x.ordinal()])) {
                    return x;
                }
            }
            return XmlState.UNKNOWN;
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts ) {
            state = findState(qName);
            String at;

            if (state != XmlState.BDN && !valid) {
                logger.error("BDN tag missing");
            }

            txt = null;

            switch (state) {
                case UNKNOWN:
                    logger.error("Unknown tag " + qName + "\n");
                    break;
                case BDN:
                    if (valid) {
                        logger.error("BDN must be used only once");
                    } else {
                        valid = true;
                    }
                    break;
                case NAME:
                    at = atts.getValue("Title");
                    if (at != null) {
                        title = at;
                        logger.trace("Title: " + title + "\n");
                    }
                    break;
                case LANGUAGE:
                    at = atts.getValue("Code");
                    if (at != null) {
                        language = at;
                        logger.trace("Language: " + language + "\n");
                    }
                    break;
                case FORMAT:
                    at = atts.getValue("FrameRate");
                    if (at != null) {
                        fps = SubtitleUtils.getFps(at);
                        fpsXml = XmlFps(fps);
                        logger.trace("fps: " + ToolBox.formatDouble(fps) + "\n");
                    }
                    at = atts.getValue("VideoFormat");
                    if (at != null) {
                        String res = at;
                        for (Resolution r : Resolution.values())  {
                            if (res.length() == 4 && res.charAt(0) != '7') { // hack to rename 480p/576p to 480i/576i
                                res = res.replace('p', 'i');
                            }
                            if (r.getResolutionNameForXml().equalsIgnoreCase(res)) {
                                resolution = r;
                                logger.trace("Language: " + r.getResolutionNameForXml() + "\n");
                                break;
                            }
                        }
                    }
                    break;
                case EVENTS:
                    at = atts.getValue("NumberofEvents");
                    if (at != null) {
                        int n = ToolBox.getInt(at);
                        if (n > 0) {
                            /* number of subtitles read from the xml */
                            Core.setProgressMax(n);
                        }
                    }
                    break;
                case EVENT:
                    pic = new SubPictureXml();
                    subPictures.add(pic);
                    int num  = subPictures.size();
                    logger.info("#" + num + "\n");
                    Core.setProgress(num);
                    at = atts.getValue("InTC");
                    if (at != null) {
                        pic.setStartTime(timeStrXmlToPTS(at, fpsXml));
                        if (pic.getStartTime() == -1) {
                            pic.setStartTime(0);
                            logger.warn("Invalid start time " + at + "\n");
                        }
                    }
                    at = atts.getValue("OutTC");
                    if (at != null) {
                        pic.setEndTime(timeStrXmlToPTS(at, fpsXml));
                        if (pic.getEndTime() == -1) {
                            pic.setEndTime(0);
                            logger.warn("Invalid end time " + at + "\n");
                        }
                    }
                    if (fps != fpsXml) {
                        pic.setStartTime((pic.getStartTime() * 1001 + 500) / 1000);
                        pic.setEndTime((pic.getEndTime() * 1001 + 500) / 1000);
                    }
                    at = atts.getValue("Forced");
                    pic.setForced(at != null && at.equalsIgnoreCase("true"));
                    if (pic.isForced()) {
                        numForcedFrames++;
                    }
                    int dim[] = resolution.getDimensions();
                    pic.setWidth(dim[0]);
                    pic.setHeight(dim[1]);
                    break;
                case GRAPHIC:
                    pic.setImageWidth(ToolBox.getInt(atts.getValue("Width")));
                    pic.setImageHeight(ToolBox.getInt(atts.getValue("Height")));
                    pic.setOfsX(ToolBox.getInt(atts.getValue("X")));
                    pic.setOfsY(ToolBox.getInt(atts.getValue("Y")));
                    pic.storeOriginalOffsets();
                    txt = new StringBuffer();
                    break;
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName ) {
            XmlState endState = findState(qName);
            if (state == XmlState.GRAPHIC && endState == XmlState.GRAPHIC) {
                pic.setFileName(pathName + txt.toString().trim());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length ) {
            if (txt != null) {
                txt.append(ch, start, length);
            }
        }
    }
}
