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
package bdsup2sub.supstream;

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.BitmapBounds;
import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Core;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.Framerate;
import bdsup2sub.core.Resolution;
import bdsup2sub.tools.QuantizeFilter;
import bdsup2sub.utils.FilenameUtils;
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

import static bdsup2sub.core.Constants.LANGUAGES;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStrXml;
import static bdsup2sub.utils.TimeUtils.timeStrXmlToPTS;

/**
 * Reading and writing of Blu-Ray captions in Xml/Png format.
 */
public class SupXml implements Substream {

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
    /** number of subtitles read from the xml */
    private int numToImport;

    /**
     * Constructor (for reading)
     * @param fn file name of Xml file to read
     * @throws CoreException
     */
    public SupXml(String fn) throws CoreException {
        this.pathName = FilenameUtils.addSeparator(FilenameUtils.getParent(fn));
        this.title = FilenameUtils.removeExtension(FilenameUtils.getName(fn));

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
            DefaultHandler handler = new XmlHandler();
            saxParser.parse(new File(fn), handler);
        } catch (ParserConfigurationException e) {
            throw new CoreException(e.getMessage());
        } catch (SAXException e) {
            throw new CoreException(e.getMessage());
        } catch (IOException e) {
            throw new CoreException(e.getMessage());
        }

        Core.print("\nDetected " + numForcedFrames + " forced captions.\n");
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
     * @see deadbeef.SupTools.Substream#close()
     */
    @Override
    public void close() {
    }

    /* (non-Javadoc)
     * @see deadbeef.SupTools.Substream#decode(int)
     */
    @Override
    public void decode(int index) throws CoreException {
        try {
            File f = new File(subPictures.get(index).fileName);
            if (!f.exists()) {
                throw new CoreException("file " + subPictures.get(index).fileName + " not found.");
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
                        if (alpha >= Core.getAlphaCrop()) {
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
                    Core.printWarn("Quantizer failed.\n");
                    size = 255;
                }
                // create palette
                palette = new Palette(256);
                for (int i=0; i < size; i++) {
                    int alpha = (ct[i] >> 24) & 0xff;
                    if (alpha >= Core.getAlphaCrop()) {
                        palette.setARGB(i, ct[i]);
                    } else {
                        palette.setARGB(i, 0);
                    }
                }
            }
            primaryColorIndex = bitmap.getPrimaryColorIndex(palette.getAlpha(), Core.getAlphaThr(), palette.getY());
            // crop
            BitmapBounds bounds = bitmap.getCroppingBounds(palette.getAlpha(), Core.getAlphaCrop());
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
                pic.setOfsX(pic.originalX + bounds.xMin);
                pic.setOfsY(pic.originalY + bounds.yMin);
            }
        } catch (IOException e) {
            throw new CoreException(e.getMessage());
        } catch (OutOfMemoryError e) {
            JOptionPane.showMessageDialog(Core.getMainFrame(),"Out of heap! Use -Xmx256m to increase heap!","Error!", JOptionPane.WARNING_MESSAGE);
            throw new CoreException("Out of heap! Use -Xmx256m to increase heap!");
        }
    }

    /**
     * Create Xml file
     * @param fname file name
     * @param pics array of SubPictures
     * @throws CoreException
     */
    public static void writeXml(final String fname, final SubPicture pics[]) throws CoreException {
        double fps = Core.getFPSTrg();
        double fpsXml = XmlFps(fps);
        long t;
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
            out.write("    <Language Code=\"" + LANGUAGES[Core.getLanguageIdx()][2] + "\"/>");
            out.newLine();
            String res = Core.getResolutionNameXml(Core.getOutputResolution());
            out.write("    <Format VideoFormat=\"" + res + "\" FrameRate=\"" + ToolBox.formatDouble(fps) + "\" DropFrame=\"False\"/>");
            out.newLine();
            t = pics[0].startTime;
            if (fps != fpsXml) {
                t = (t * 2000 + 1001) / 2002;
            }
            String ts = ptsToTimeStrXml(t,fpsXml);
            t = pics[pics.length-1].endTime;
            if (fps != fpsXml) {
                t = (t * 2000 + 1001) / 2002;
            }
            String te = ptsToTimeStrXml(t,fpsXml);
            out.write("    <Events Type=\"Graphic\" FirstEventInTC=\"" + ts + "\" LastEventOutTC=\"" + te + "\" NumberofEvents=\"" + pics.length + "\"/>");
            out.newLine();
            out.write("  </Description>");
            out.newLine();
            out.write("  <Events>");
            out.newLine();
            for (int idx=0; idx < pics.length; idx++) {
                SubPicture p = pics[idx];
                t = p.startTime;
                if (fps != fpsXml) {
                    t = (t * 2000 + 1001) / 2002;
                }
                ts = ptsToTimeStrXml(t,fpsXml);
                t = p.endTime;
                if (fps != fpsXml) {
                    t = (t * 2000 + 1001) / 2002;
                }
                te = ptsToTimeStrXml(t, fpsXml);
                String forced = p.isforced? "True": "False";
                out.write("    <Event InTC=\"" + ts + "\" OutTC=\"" + te + "\" Forced=\"" + forced + "\">");
                out.newLine();

                String pname = getPNGname(name, idx+1);
                out.write("      <Graphic Width=\"" + p.getImageWidth() + "\" Height=\"" + p.getImageHeight()
                        + "\" X=\"" + p.getOfsX() + "\" Y=\"" + p.getOfsY() + "\">" + pname + "</Graphic>");
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
     * @see Substream#getBitmap()
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /* (non-Javadoc)
     * @see Substream#getImage()
     */
    public BufferedImage getImage() {
        return bitmap.getImage(palette.getColorModel());
    }

    /* (non-Javadoc)
     * @see Substream#getImage(Bitmap)
     */
    public BufferedImage getImage(final Bitmap bm) {
        return bm.getImage(palette.getColorModel());
    }

    /* (non-Javadoc)
     * @see Substream#getNumForcedFrames()
     */
    public int getNumForcedFrames() {
        return numForcedFrames;
    }

    /* (non-Javadoc)
     * @see Substream#getNumFrames()
     */
    public int getNumFrames() {
        return subPictures.size();
    }

    /* (non-Javadoc)
     * @see Substream#getPalette()
     */
    public Palette getPalette() {
        return palette;
    }

    /* (non-Javadoc)
     * @see Substream#getPrimaryColorIndex()
     */
    public int getPrimaryColorIndex() {
        return primaryColorIndex;
    }

    /* (non-Javadoc)
     * @see deadbeef.SupTools.Substream#getStartOffset(int)
     */
    public long getStartOffset(int index) {
        // dummy
        return 0;
    }

    /* (non-Javadoc)
     * @see Substream#getSubPicture(int)
     */
    public SubPicture getSubPicture(int index) {
        return subPictures.get(index);
    }

    /* (non-Javadoc)
     * @see Substream#getEndTime(int)
     */
    public long getEndTime(int index) {
        return subPictures.get(index).endTime;
    }

    /* (non-Javadoc)
     * @see Substream#getStartTime(int)
     */
    public long getStartTime(final int index) {
        return subPictures.get(index).startTime;
    }

    /* (non-Javadoc)
     * @see Substream#isForced(int)
     */
    public boolean isForced(int index) {
        return subPictures.get(index).isforced;
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
                Core.printErr("BDN tag missing");
            }

            txt = null;

            switch (state) {
                case UNKNOWN:
                    Core.printErr("Unknown tag "+qName+"\n");
                    break;
                case BDN:
                    if (valid) {
                        Core.printErr("BDN must be used only once");
                    } else {
                        valid = true;
                    }
                    break;
                case NAME:
                    at = atts.getValue("Title");
                    if (at != null) {
                        title = at;
                        Core.print("Title: "+title+"\n");
                    }
                    break;
                case LANGUAGE:
                    at = atts.getValue("Code");
                    if (at != null) {
                        language = at;
                        Core.print("Language: "+language+"\n");
                    }
                    break;
                case FORMAT:
                    at = atts.getValue("FrameRate");
                    if (at != null) {
                        fps = Core.getFPS(at);
                        fpsXml = XmlFps(fps);
                        Core.print("fps: " + ToolBox.formatDouble(fps) + "\n");
                    }
                    at = atts.getValue("VideoFormat");
                    if (at != null) {
                        String res = at;
                        for (Resolution r : Resolution.values())  {
                            if (res.length() == 4 && res.charAt(0) != '7') { // hack to rename 480p/576p to 480i/576i
                                res = res.replace('p', 'i');
                            }
                            if (Core.getResolutionNameXml(r).equalsIgnoreCase(res)) {
                                resolution = r;
                                Core.print("Language: " + Core.getResolutionNameXml(r) + "\n");
                                break;
                            }
                        }
                    }
                    break;
                case EVENTS:
                    at = atts.getValue("NumberofEvents");
                    if (at != null) {
                        int n = ToolBox.getInt(at);
                        if (n> 0) {
                            numToImport = n;
                            Core.setProgressMax(numToImport);
                        }
                    }
                    break;
                case EVENT:
                    pic = new SubPictureXml();
                    subPictures.add(pic);
                    int num  = subPictures.size();
                    Core.printX("#"+num+"\n");
                    Core.setProgress(num);
                    at = atts.getValue("InTC");
                    if (at != null) {
                        pic.startTime = timeStrXmlToPTS(at, fpsXml);
                        if (pic.startTime == -1) {
                            pic.startTime = 0;
                            Core.printWarn("Invalid start time " + at + "\n");
                        }
                    }
                    at = atts.getValue("OutTC");
                    if (at != null) {
                        pic.endTime = timeStrXmlToPTS(at, fpsXml);
                        if (pic.endTime == -1) {
                            pic.endTime = 0;
                            Core.printWarn("Invalid end time " + at + "\n");
                        }
                    }
                    if (fps != fpsXml) {
                        pic.startTime = (pic.startTime * 1001 + 500) / 1000;
                        pic.endTime   = (pic.endTime * 1001 + 500) / 1000;
                    }
                    at = atts.getValue("Forced");
                    pic.isforced = at != null && at.equalsIgnoreCase("true");
                    if (pic.isforced) {
                        numForcedFrames++;
                    }
                    int dim[] = Core.getResolution(resolution);
                    pic.width  = dim[0];
                    pic.height = dim[1];
                    break;
                case GRAPHIC:
                    pic.setImageWidth(ToolBox.getInt(atts.getValue("Width")));
                    pic.setImageHeight(ToolBox.getInt(atts.getValue("Height")));
                    pic.setOfsX(ToolBox.getInt(atts.getValue("X")));
                    pic.setOfsY(ToolBox.getInt(atts.getValue("Y")));
                    pic.setOriginal();
                    txt = new StringBuffer();
                    break;
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName ) {
            XmlState endState = findState(qName);
            if (state == XmlState.GRAPHIC && endState == XmlState.GRAPHIC) {
                pic.fileName = pathName + txt.toString().trim();
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
