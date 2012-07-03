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
package bdsup2sub.core;

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.BitmapWithPalette;
import bdsup2sub.bitmap.ErasePatch;
import bdsup2sub.bitmap.Palette;
import bdsup2sub.gui.main.MainFrameView;
import bdsup2sub.gui.support.Progress;
import bdsup2sub.supstream.*;
import bdsup2sub.supstream.bd.SupBD;
import bdsup2sub.supstream.bdnxml.SupXml;
import bdsup2sub.supstream.dvd.*;
import bdsup2sub.supstream.hd.SupHD;
import bdsup2sub.tools.EnhancedPngEncoder;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;
import com.mortennobel.imagescaling.ResampleFilter;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static bdsup2sub.core.Constants.*;
import static bdsup2sub.utils.SubtitleUtils.getResolutionForDimension;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStr;
import static com.mortennobel.imagescaling.ResampleFilters.*;

/**
 * This class contains the core functionality of BDSup2Sub.<br>
 * It's meant to be used from the command line as well as from the GUI.
 */
public class Core extends Thread {

    private static final Configuration configuration = Configuration.getInstance();

    /** Enumeration of functionalities executed in the started thread */
    private enum RunType {
        /** read a SUP stream */
        READSUP,
        /** read a SUP stream */
        READXML,
        /** read a VobSub stream */
        READVOBSUB,
        /** read a SUP/IFO stream */
        READSUPIFO,
        /** write a VobSub stream */
        CREATESUB,
        /** write a BD-SUP stream */
        CREATESUP,
        /** move all captions */
        MOVEALL
    }

    /** Enumeration of caption types (used for moving captions) */
    private enum CaptionType {
        /** caption in upper half of the screen */
        UP,
        /** caption in lower half of the screen */
        DOWN,
        /** caption covering more or less the whole screen */
        FULL
    }

    /** Current DVD palette (for create mode) - initialized as default */
    private static Palette currentDVDPalette = new Palette(
            DEFAULT_PALETTE_RED, DEFAULT_PALETTE_GREEN, DEFAULT_PALETTE_BLUE, DEFAULT_PALETTE_ALPHA, true
    );

    private static final int MIN_IMAGE_DIMENSION = 8;

    /** Palette imported from SUB/IDX or SUP/IFO */
    private static Palette defaultSourceDVDPalette;
    /** Current palette based on the one imported from SUB/IDX or SUP/IFO */
    private static Palette currentSourceDVDPalette;
    /** Default alpha map */
    private static final int DEFAULT_ALPHA[] = { 0, 0xf, 0xf, 0xf};

    /** Converted unpatched target bitmap of current subpicture - just for display */
    private static Bitmap trgBitmapUnpatched;
    /** Converted target bitmap of current subpicture - just for display */
    private static Bitmap trgBitmap;
    /** Palette of target caption */
    private static Palette trgPal;
    /** Used for creating VobSub streams */
    private static SubPictureDVD subVobTrg;

    /** Used for handling BD SUPs */
    private static SupBD supBD;
    /** Used for handling HD-DVD SUPs */
    private static SupHD supHD;
    /** Used for handling Xmls */
    private static SupXml supXml;
    /** Used for handling VobSub */
    private static SubDvd subDVD;
    /** Used for handling SUP/IFO */
    private static SupDvd supDVD;
    /** Used for common handling of either SUPs */
    private static SubtitleStream subtitleStream;

    /** Array of subpictures used for editing and export */
    private static SubPicture subPictures[];

    /** Input mode used for last import */
    private static InputMode inMode = InputMode.VOBSUB;

    /** Use BT.601 color model instead of BT.709 */
    private static boolean useBT601;

    /** Full filename of current source SUP (needed for thread) */
    private static String fileName;

    /** Reference to main GUI class */
    private static MainFrameView mainFrame;
    /** Progress dialog for loading/exporting */
    private static Progress progress;
    /** Maximum absolute value for progress bar */
    private static int progressMax;
    /** Last relative value for progress bar */
    private static int progressLast;

    /** Number of errors */
    private static int errors;
    /** Number of warnings */
    private static int warnings;

    /** Functionality executed in the started thread */
    private static RunType runType;
    /** Thread state */
    private static CoreThreadState state = CoreThreadState.INACTIVE;
    /** Used to store exception thrown in the thread */
    private static Exception threadException;
    /** Semaphore to disable actions while changing component properties */
    private static volatile boolean ready;
    /** Semaphore for synchronization */
    private static final Object semaphore = new Object();

    /** Thread used for threaded import/export. */
    @Override
    public void run() {
        state = CoreThreadState.ACTIVE;
        threadException = null;
        try {
            switch (runType) {
                case CREATESUB:
                    writeSub(fileName);
                    break;
                case READSUP:
                    readSup(fileName);
                    break;
                case READVOBSUB:
                    readVobSub(fileName);
                    break;
                case READSUPIFO:
                    readSupIfo(fileName);
                    break;
                case READXML:
                    readXml(fileName);
                    break;
                case MOVEALL:
                    moveAllToBounds();
                    break;
            }
        } catch (Exception ex) {
            threadException = ex;
        } finally {
            state = CoreThreadState.INACTIVE;
        }
    }

    /**
     * Reset the core, close all files
     */
    public static void close() {
        ready = false;
        if (supBD != null) {
            supBD.close();
        }
        if (supHD != null) {
            supHD.close();
        }
        if (supXml != null) {
            supXml.close();
        }
        if (subDVD != null) {
            subDVD.close();
        }
        if (supDVD != null) {
            supDVD.close();
        }
    }

    /**
     * Shut down the Core (write properties, close files etc.).
     */
    public static void exit() {
        configuration.storeConfig();
        if (supBD != null) {
            supBD.close();
        }
        if (supHD != null) {
            supHD.close();
        }
        if (supXml != null) {
            supXml.close();
        }
        if (subDVD != null) {
            subDVD.close();
        }
        if (supDVD != null) {
            supDVD.close();
        }
    }

    /**
     * Read a subtitle stream in a thread and display the progress dialog.
     * @param fname		File name of subtitle stream to read
     * @param parent	Parent frame (needed for progress dialog)
     * @param sid       stream identifier
     * @throws Exception
     */
    public static void readStreamThreaded(String fname, JFrame parent, StreamID sid) throws Exception {
        boolean xml = FilenameUtils.getExtension(fname).equalsIgnoreCase("xml");
        boolean idx = FilenameUtils.getExtension(fname).equalsIgnoreCase("idx");
        boolean ifo = FilenameUtils.getExtension(fname).equalsIgnoreCase("ifo");

        fileName = fname;
        progressMax = (int)(new File(fname)).length();
        progressLast = 0;
        progress = new Progress(parent);
        progress.setTitle("Loading");
        progress.setText("Loading subtitle stream");
        if (xml || sid == StreamID.XML) {
            runType = RunType.READXML;
        } else if (idx || sid == StreamID.DVDSUB || sid == StreamID.IDX) {
            runType = RunType.READVOBSUB;
        } else if (ifo || sid == StreamID.IFO) {
            runType = RunType.READSUPIFO;
        } else {
			File ifoFile = new File(FilenameUtils.removeExtension(fname) + ".ifo");
            if (ifoFile.exists()) {
                runType = RunType.READSUPIFO;
            } else {
                runType = RunType.READSUP;
            }
		}

        configuration.setCurrentStreamID(sid);

        // start thread
        Thread t = new Thread(new Core());
        t.start();
        progress.setVisible(true);
        while (t.isAlive()) {
            try  {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
        }
        state = CoreThreadState.INACTIVE;
        Exception ex = threadException;
        if (ex != null) {
            throw ex;
        }
    }

    /**
     * Write a VobSub or BD-SUP in a thread and display the progress dialog.
     * @param fname		File name of subtitle stream to create
     * @param parent	Parent frame (needed for progress dialog)
     * @throws Exception
     */
    public static void createSubThreaded(String fname, JFrame parent) throws Exception {
        fileName = fname;
        progressMax = subtitleStream.getFrameCount();
        progressLast = 0;
        progress = new Progress(parent);
        progress.setTitle("Exporting");
        OutputMode outputMode = configuration.getOutputMode();
        if (outputMode == OutputMode.VOBSUB) {
            progress.setText("Exporting SUB/IDX");
        } else if (outputMode == OutputMode.BDSUP) {
            progress.setText("Exporting SUP(BD)");
        } else if (outputMode == OutputMode.XML) {
            progress.setText("Exporting XML/PNG");
        } else {
            progress.setText("Exporting SUP/IFO");
        }
        runType = RunType.CREATESUB;
        // start thread
        Thread t = new Thread(new Core());
        t.start();
        progress.setVisible(true);
        while (t.isAlive()) {
            try  {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
        }
        state = CoreThreadState.INACTIVE;
        Exception ex = threadException;
        if (ex != null) {
            throw ex;
        }
    }

    /**
     * Create the frame individual 4-color palette for VobSub mode.
     * @index Index of caption
     */
    private static void determineFramePal(int index) {
        if ((inMode != InputMode.VOBSUB && inMode != InputMode.SUPIFO) || configuration.getPaletteMode() != PaletteMode.KEEP_EXISTING) {
            // get the primary color from the source palette
            int rgbSrc[] = subtitleStream.getPalette().getRGB(subtitleStream.getPrimaryColorIndex());

            // match with primary color from 16 color target palette
            // note: skip index 0 , primary colors at even positions
            // special treatment for index 1:  white
            Palette trgPallete = currentDVDPalette;
            int minDistance = 0xffffff; // init > 0xff*0xff*3 = 0x02fa03
            int colIdx = 0;
            for (int idx=1; idx<trgPallete.getSize(); idx+=2 )  {
                int rgb[] = trgPallete.getRGB(idx);
                // distance vector (skip sqrt)
                int rd = rgbSrc[0]-rgb[0];
                int gd = rgbSrc[1]-rgb[1];
                int bd = rgbSrc[2]-rgb[2];
                int distance = rd*rd+gd*gd+bd*bd;
                // new minimum distance ?
                if ( distance < minDistance) {
                    colIdx = idx;
                    minDistance = distance;
                    if (minDistance == 0) {
                        break;
                    }
                }
                // special treatment for index 1 (white)
                if (idx == 1) {
                    idx--; // -> continue with index = 2
                }
            }

            // set new frame palette
            int palFrame[] = new int[4];
            palFrame[0] = 0;        // black - transparent color
            palFrame[1] = colIdx;   // primary color
            if (colIdx == 1) {
                palFrame[2] = colIdx+2; // special handling: white + dark grey
            } else {
                palFrame[2] = colIdx+1; // darker version of primary color
            }
            palFrame[3] = 0;        // black - opaque

            subVobTrg.setAlpha(DEFAULT_ALPHA);
            subVobTrg.setPal(palFrame);

            trgPal = SupDvdUtils.decodePalette(subVobTrg, trgPallete);
        } else {
            // use palette from loaded VobSub or SUP/IFO
            Palette miniPal = new Palette(4, true);
            int alpha[];
            int palFrame[];
            DvdSubtitleStream substreamDvd;

            if (inMode == InputMode.VOBSUB) {
                substreamDvd = subDVD;
            } else {
                substreamDvd = supDVD;
            }

            alpha = substreamDvd.getFrameAlpha(index);
            palFrame = substreamDvd.getFramePalette(index);

            for (int i=0; i < 4; i++) {
                int a = (alpha[i]*0xff)/0xf;
                if (a >= configuration.getAlphaCrop()) {
                    miniPal.setARGB(i, currentSourceDVDPalette.getARGB(palFrame[i]));
                    miniPal.setAlpha(i, a);
                } else {
                    miniPal.setARGB(i, 0);
                }
            }
            subVobTrg.setAlpha(alpha);
            subVobTrg.setPal(palFrame);
            trgPal = miniPal;
        }
    }

    /**
     * Read BD-SUP or HD-DVD-SUP.
     * @param fname File name
     * @throws CoreException
     */
    public static void readSup(String fname) throws CoreException {
        printX("Loading "+fname+"\n");
        resetErrors();
        resetWarnings();

        // try to find matching language idx if filename contains language string
        String fnl = FilenameUtils.getName(fname.toLowerCase());
        for (int i=0; i < LANGUAGES.length; i++) {
            if (fnl.contains(LANGUAGES[i][0].toLowerCase())) {
                configuration.setLanguageIdx(i);
                printX("Selected language '"+LANGUAGES[i][0]+" ("+LANGUAGES[i][1]+")' by filename\n");
                break;
            }
        }

        // close existing subtitleStream
        if (subtitleStream != null) {
            subtitleStream.close();
        }

        // check first two byte to determine whether this is a BD-SUP or HD-DVD-SUP
        byte id[] = ToolBox.getFileID(fname, 2);
        if (id != null && id[0] == 0x50 && id[1] == 0x47) {
            supBD = new SupBD(fname);
            subtitleStream = supBD;
            supHD = null;
            inMode = InputMode.BDSUP;
        } else {
            supHD = new SupHD(fname);
            subtitleStream = supHD;
            supBD = null;
            inMode = InputMode.HDDVDSUP;
        }

        // decode first frame
        subtitleStream.decode(0);
        subVobTrg = new SubPictureDVD();

        // automatically set luminance thresholds for VobSub conversion
        int maxLum = subtitleStream.getPalette().getY()[subtitleStream.getPrimaryColorIndex()] & 0xff;
        int[] luminanceThreshold = new int[2];
        configuration.setLuminanceThreshold(luminanceThreshold);
        if (maxLum > 30) {
            luminanceThreshold[0] = maxLum*2/3;
            luminanceThreshold[1] = maxLum/3;
        } else {
            luminanceThreshold[0] = 210;
            luminanceThreshold[1] = 160;
        }

        // try to detect source frame rate
        if (subtitleStream == supBD) {
            configuration.setFpsSrc(supBD.getFps(0));
            configuration.setFpsSrcCertain(true);
            if (configuration.isKeepFps()) {
                configuration.setFpsTrg(configuration.getFPSSrc());
            }
        } else {
            // for HD-DVD we need to guess
            useBT601 = false;
            configuration.setFpsSrcCertain(false);
            configuration.setFpsSrc(Framerate.FPS_23_976.getValue());
        }
    }

    /**
     * Read Sony BDN XML file.
     * @param fname File name
     * @throws CoreException
     */
    public static void readXml(String fname) throws CoreException {
        printX("Loading "+fname+"\n");
        resetErrors();
        resetWarnings();

        // close existing subtitleStream
        if (subtitleStream != null) {
            subtitleStream.close();
        }

        supXml = new SupXml(fname);
        subtitleStream = supXml;

        inMode = InputMode.XML;

        // decode first frame
        subtitleStream.decode(0);
        subVobTrg = new SubPictureDVD();

        // automatically set luminance thresholds for VobSub conversion
        int maxLum = subtitleStream.getPalette().getY()[subtitleStream.getPrimaryColorIndex()] & 0xff;
        int[] luminanceThreshold = new int[2];
        configuration.setLuminanceThreshold(luminanceThreshold);
        if (maxLum > 30) {
            luminanceThreshold[0] = maxLum*2/3;
            luminanceThreshold[1] = maxLum/3;
        } else {
            luminanceThreshold[0] = 210;
            luminanceThreshold[1] = 160;
        }

        // find language idx
        for (int i=0; i < LANGUAGES.length; i++) {
            if (LANGUAGES[i][2].equalsIgnoreCase(supXml.getLanguage())) {
                configuration.setLanguageIdx(i);
                break;
            }
        }

        // set frame rate
        configuration.setFpsSrc(supXml.getFps());
        configuration.setFpsSrcCertain(true);
        if (configuration.isKeepFps()) {
            configuration.setFpsTrg(configuration.getFPSSrc());
        }
    }

    /**
     * Read VobSub.
     * @param fname File name
     * @throws CoreException
     */
    public static void readVobSub(String fname) throws CoreException {
        readDVDSubstream(fname, true);
    }

    /**
     * Read SUP/IFO.
     * @param fname File name
     * @throws CoreException
     */
    public static void readSupIfo(String fname) throws CoreException {
        readDVDSubstream(fname, false);
    }

    /**
     * Read VobSub or SUP/IFO.
     * @param fname File name
     * @param isVobSub True if SUB/IDX, false if SUP/IFO
     * @throws CoreException
     */
    private static void readDVDSubstream(String fname, boolean isVobSub) throws CoreException {
        printX("Loading " + fname + "\n");
        resetErrors();
        resetWarnings();

        // close existing subtitleStream
        if (subtitleStream != null) {
            subtitleStream.close();
        }

        DvdSubtitleStream substreamDvd;
        String fnI;
        String fnS;

        if (isVobSub) {
            // SUB/IDX
            if (configuration.getCurrentStreamID() == StreamID.DVDSUB) {
                fnS = fname;
                fnI = FilenameUtils.removeExtension(fname) + ".idx";
            } else {
                fnI = fname;
                fnS = FilenameUtils.removeExtension(fname) + ".sub";
            }
            subDVD = new SubDvd(fnS, fnI);
            subtitleStream = subDVD;
            inMode = InputMode.VOBSUB;
            substreamDvd = subDVD;
        } else {
            // SUP/IFO
            if (FilenameUtils.getExtension(fname).equals("ifo")) {
                fnI = fname;
                fnS = FilenameUtils.removeExtension(fname) + ".sup";
            } else {
                fnI = FilenameUtils.removeExtension(fname) + ".ifo";
                fnS = fname;
            }
            supDVD = new SupDvd(fnS, fnI);
            subtitleStream = supDVD;
            inMode = InputMode.SUPIFO;
            substreamDvd = supDVD;
        }

        // decode first frame
        subtitleStream.decode(0);
        subVobTrg = new SubPictureDVD();
        defaultSourceDVDPalette = substreamDvd.getSrcPalette();
        currentSourceDVDPalette = new Palette(defaultSourceDVDPalette);

        // automatically set luminance thresholds for VobSub conversion
        int primColIdx = subtitleStream.getPrimaryColorIndex();
        int yMax = subtitleStream.getPalette().getY()[primColIdx] & 0xff;
        int[] luminanceThreshold = new int[2];
        configuration.setLuminanceThreshold(luminanceThreshold);
        if (yMax > 10) {
            // find darkest opaque color
            int yMin = yMax;
            for (int i=0; i < 4; i++) {
                int y = subtitleStream.getPalette().getY()[i] & 0xff;
                int a = subtitleStream.getPalette().getAlpha(i);
                if (y < yMin && a > configuration.getAlphaThreshold()) {
                    yMin = y;
                }
            }
            luminanceThreshold[0] = yMin + (yMax-yMin)*9/10;
            luminanceThreshold[1] = yMin + (yMax-yMin)*3/10;
        } else {
            luminanceThreshold[0] = 210;
            luminanceThreshold[1] = 160;
        }

        configuration.setLanguageIdx(substreamDvd.getLanguageIdx());

        // set frame rate
        int h = subtitleStream.getSubPicture(0).getHeight(); //subtitleStream.getBitmap().getHeight();
        switch (h) {
            case 480:
                configuration.setFpsSrc(Framerate.NTSC.getValue());
                useBT601 = true;
                configuration.setFpsSrcCertain(true);
                break;
            case 576:
                configuration.setFpsSrc(Framerate.PAL.getValue());
                useBT601 = true;
                configuration.setFpsSrcCertain(true);
                break;
            default:
                useBT601 = false;
                configuration.setFpsSrc(Framerate.FPS_23_976.getValue());
                configuration.setFpsSrcCertain(false);
        }
    }


    /**
     * Check start and end time, fix overlaps etc.
     * @param idx			Index of subpicture (just for display)
     * @param subPic		Subpicture to check/fix
     * @param subPicNext	Next subpicture
     * @param subPicPrev	Previous subpicture
     */
    private static void validateTimes(int idx, SubPicture subPic, SubPicture subPicNext, SubPicture subPicPrev) {
        //long tpf = (long)(90000/fpsTrg); // time per frame
        long ts = subPic.getStartTime();     // start time
        long te = subPic.getEndTime();       // end time
        long delay = 5000*90;            // default delay for missing end time (5 seconds)

        idx += 1; // only used for display

        // get end time of last frame
        long te_last;
        if (subPicPrev != null) {
            te_last = subPicPrev.getEndTime();
        } else {
            te_last = -1;
        }

        if (ts < te_last) {
            printWarn("start time of frame "+idx+" < end of last frame -> fixed\n");
            ts = te_last;
        }

        // get start time of next frame
        long ts_next;
        if (subPicNext != null) {
            ts_next = subPicNext.getStartTime();
        } else {
            ts_next = 0;
        }

        if (ts_next == 0) {
            if ( te > ts) {
                ts_next = te;
            } else {
                // completely messed up:
                // end time and next start time are invalid
                ts_next = ts+delay;
            }
        }

        if (te <= ts) {
            if (te == 0) {
                printWarn("missing end time of frame "+idx+" -> fixed\n");
            } else {
                printWarn("end time of frame "+idx+" <= start time -> fixed\n");
            }
            te = ts+delay;
            if (te > ts_next) {
                te = ts_next;
            }
        } else if (te > ts_next) {
            printWarn("end time of frame "+idx+" > start time of next frame -> fixed\n");
            te = ts_next;
        }

        int minTimePTS = configuration.getMinTimePTS();
        if (te - ts < minTimePTS) {
            if (configuration.getFixShortFrames()) {
                te = ts + minTimePTS;
                if (te > ts_next) {
                    te = ts_next;
                }
                printWarn("duration of frame " + idx + " was shorter than " + (ToolBox.formatDouble(minTimePTS / 90.0)) + "ms -> fixed\n");
            } else {
                printWarn("duration of frame " + idx + " is shorter than " + (ToolBox.formatDouble(minTimePTS / 90.0)) + "ms\n");
            }
        }

        if (subPic.getStartTime() != ts) {
            subPic.setStartTime(SubtitleUtils.syncTimePTS(ts, configuration.getFpsTrg(), configuration.getFpsTrg()));
        }
        if (subPic.getEndTime() != te) {
            subPic.setEndTime(SubtitleUtils.syncTimePTS(te, configuration.getFpsTrg(), configuration.getFpsTrg()));
        }
    }

    /**
     * Update width, height and offsets of target SubPicture.<br>
     * This is needed if cropping captions during decode (i.e. the source image size changes).
     * @param index Index of caption
     * @return true: image size has changed, false: image size didn't change.
     */
    private static boolean updateTrgPic(int index) {
        SubPicture picSrc = subtitleStream.getSubPicture(index);
        SubPicture picTrg = subPictures[index];
        double scaleX = (double) picTrg.getWidth() / picSrc.getWidth();
        double scaleY = (double) picTrg.getHeight() / picSrc.getHeight();
        double fx;
        double fy;
        if (configuration.getApplyFreeScale()) {
            fx = configuration.getFreeScaleFactorX();
            fy = configuration.getFreeScaleFactorY();
        } else {
            fx = 1.0;
            fy = 1.0;
        }

        int wOld = picTrg.getImageWidth();
        int hOld = picTrg.getImageHeight();
        int wNew = (int)(picSrc.getImageWidth()  * scaleX * fx + 0.5);
        if (wNew < MIN_IMAGE_DIMENSION) {
            wNew = picSrc.getImageWidth();
        } else if (wNew > picTrg.getWidth()) {
            wNew = picTrg.getWidth();
        }
        int hNew = (int)(picSrc.getImageHeight() * scaleY * fy + 0.5);
        if (hNew < MIN_IMAGE_DIMENSION) {
            hNew = picSrc.getImageHeight();
        } else if (hNew > picTrg.getHeight()) {
            hNew = picTrg.getHeight();
        }
        picTrg.setImageWidth(wNew);
        picTrg.setImageHeight(hNew);
        if (wNew != wOld) {
            int xOfs = (int)(picSrc.getXOffset() * scaleX + 0.5);
            int spaceSrc = (int)((picSrc.getWidth() -picSrc.getImageWidth())*scaleX + 0.5);
            int spaceTrg = picTrg.getWidth() - wNew;
            xOfs += (spaceTrg - spaceSrc) / 2;
            if (xOfs < 0) {
                xOfs = 0;
            } else if (xOfs+wNew > picTrg.getWidth()) {
                xOfs = picTrg.getWidth() - wNew;
            }
            picTrg.setOfsX(xOfs);
        }
        if (hNew != hOld) {
            int yOfs = (int)(picSrc.getYOffset() * scaleY + 0.5);
            int spaceSrc = (int)((picSrc.getHeight() -picSrc.getImageHeight())*scaleY + 0.5);
            int spaceTrg = picTrg.getHeight() - hNew;
            yOfs += (spaceTrg - spaceSrc) / 2;
            if (yOfs+hNew > picTrg.getHeight()) {
                yOfs = picTrg.getHeight() - hNew;
            }
            picTrg.setOfsY(yOfs);
        }
        // was image cropped?
        return (wNew != wOld) || (hNew != hOld);
    }

    /**
     * Apply the state of forceAll to all captions
     */
    public static void setForceAll() {
        if (subPictures != null) {
            for (SubPicture subPicture : subPictures) {
                switch (configuration.getForceAll()) {
                    case SET:
                        subPicture.setForced(true);
                        break;
                    case CLEAR:
                        subPicture.setForced(false);
                        break;
                }
            }
        }
    }

    /**
     * Create a copy of the loaded subpicture information frames.<br>
     * Apply scaling and speedup/delay to the copied frames.<br>
     * Sync frames to target fps.
     */
    public static void scanSubtitles() {
        boolean convertFPS = configuration.getConvertFPS();
        subPictures = new SubPicture[subtitleStream.getFrameCount()];
        double factTS = convertFPS ? configuration.getFPSSrc() / configuration.getFpsTrg() : 1.0;

        // change target resolution to source resolution if no conversion is needed
        if (!configuration.getConvertResolution() && getNumFrames() > 0) {
            configuration.setOutputResolution(getResolutionForDimension(getSubPictureSrc(0).getWidth(), getSubPictureSrc(0).getHeight()));
        }

        double fx;
        double fy;
        if (configuration.getApplyFreeScale()) {
            fx = configuration.getFreeScaleFactorX();
            fy = configuration.getFreeScaleFactorY();
        } else {
            fx = 1.0;
            fy = 1.0;
        }

        // first run: clone source subpics, apply speedup/down,
        SubPicture picSrc;
        for (int i=0; i<subPictures.length; i++) {
            picSrc = subtitleStream.getSubPicture(i);
            subPictures[i] = picSrc.copy();
            long ts = picSrc.getStartTime();
            long te = picSrc.getEndTime();
            // copy time stamps and apply speedup/speeddown
            int delayPTS = configuration.getDelayPTS();
            if (!convertFPS) {
                subPictures[i].setStartTime(ts + delayPTS);
                subPictures[i].setEndTime(te + delayPTS);
            } else {
                subPictures[i].setStartTime((long)(ts * factTS + 0.5) + delayPTS);
                subPictures[i].setEndTime((long)(te * factTS + 0.5) + delayPTS);
            }
            // synchronize to target frame rate
            subPictures[i].setStartTime(SubtitleUtils.syncTimePTS(subPictures[i].getStartTime(), configuration.getFpsTrg(), configuration.getFpsTrg()));
            subPictures[i].setEndTime(SubtitleUtils.syncTimePTS(subPictures[i].getEndTime(), configuration.getFpsTrg(), configuration.getFpsTrg()));

            // set forced flag
            SubPicture picTrg = subPictures[i];
            switch (configuration.getForceAll()) {
                case SET:
                    picTrg.setForced(true);
                    break;
                case CLEAR:
                    picTrg.setForced(false);
                    break;
            }

            double scaleX;
            double scaleY;
            if (configuration.getConvertResolution()) {
                // adjust image sizes and offsets
                // determine scaling factors
                picTrg.setWidth(configuration.getOutputResolution().getDimensions()[0]);
                picTrg.setHeight(configuration.getOutputResolution().getDimensions()[1]);
                scaleX = (double) picTrg.getWidth() / picSrc.getWidth();
                scaleY = (double) picTrg.getHeight() / picSrc.getHeight();
            } else {
                picTrg.setWidth(picSrc.getWidth());
                picTrg.setHeight(picSrc.getHeight());
                scaleX = 1.0;
                scaleY = 1.0;
            }
            int w = (int)(picSrc.getImageWidth()  * scaleX * fx + 0.5);
            if (w < MIN_IMAGE_DIMENSION) {
                w = picSrc.getImageWidth();
            } else if (w > picTrg.getWidth()) {
                w = picTrg.getWidth();
            }

            int h = (int)(picSrc.getImageHeight() * scaleY * fy + 0.5);
            if (h < MIN_IMAGE_DIMENSION) {
                h = picSrc.getImageHeight();
            } else if (h > picTrg.getHeight()) {
                h = picTrg.getHeight();
            }
            picTrg.setImageWidth(w);
            picTrg.setImageHeight(h);

            int xOfs = (int)(picSrc.getXOffset() * scaleX + 0.5);
            int spaceSrc = (int)((picSrc.getWidth() -picSrc.getImageWidth())*scaleX + 0.5);
            int spaceTrg = picTrg.getWidth() - w;
            xOfs += (spaceTrg - spaceSrc) / 2;
            if (xOfs < 0) {
                xOfs = 0;
            } else if (xOfs+w > picTrg.getWidth()) {
                xOfs = picTrg.getWidth() - w;
            }
            picTrg.setOfsX(xOfs);

            int yOfs = (int)(picSrc.getYOffset() * scaleY + 0.5);
            spaceSrc = (int)((picSrc.getHeight() -picSrc.getImageHeight())*scaleY + 0.5);
            spaceTrg = picTrg.getHeight() - h;
            yOfs += (spaceTrg - spaceSrc) / 2;
            if (yOfs+h > picTrg.getHeight()) {
                yOfs = picTrg.getHeight() - h;
            }
            picTrg.setOfsY(yOfs);
        }

        // 2nd run: validate times
        SubPicture picPrev = null;
        SubPicture picNext;
        for (int i=0; i<subPictures.length; i++) {
            if (i < subPictures.length-1) {
                picNext = subPictures[i+1];
            } else {
                picNext = null;
            }
            picSrc = subPictures[i];
            validateTimes(i, subPictures[i], picNext, picPrev);
            picPrev = picSrc;
        }
    }

    /**
     * Same as scanSubtitles, but consider existing frame copies.<br>
     * Times and X/Y offsets of existing frames are converted to new settings.
     * @param resOld        Resolution of existing frames
     * @param fpsTrgOld     Target fps of existing frames
     * @param delayOld      Delay of existing frames
     * @param convertFpsOld ConverFPS setting for existing frames
     * @param fsXOld        Old free scaling factor in X direction
     * @param fsYOld        Old free scaling factor in Y direction
     */
    public static void reScanSubtitles(Resolution resOld, double fpsTrgOld, int delayOld, boolean convertFpsOld, double fsXOld, double fsYOld) {
        //SubPicture subPicturesOld[] = subPictures;
        //subPictures = new SubPicture[sup.getNumFrames()];
        SubPicture picOld;
        SubPicture picSrc;
        double factTS;
        double factX;
        double factY;
        double fsXNew;
        double fsYNew;

        if (configuration.getApplyFreeScale()) {
            fsXNew = configuration.getFreeScaleFactorX();
            fsYNew = configuration.getFreeScaleFactorY();
        } else {
            fsXNew = 1.0;
            fsYNew = 1.0;
        }

        boolean convertFPS = configuration.getConvertFPS();
        double fpsTrg = configuration.getFpsTrg();
        double fpsSrc = configuration.getFPSSrc();
        if (convertFPS && !convertFpsOld) {
            factTS = fpsSrc / fpsTrg;
        } else if (!convertFPS && convertFpsOld) {
            factTS = fpsTrgOld / fpsSrc;
        } else if (convertFPS && convertFpsOld && (fpsTrg != fpsTrgOld)) {
            factTS = fpsTrgOld / fpsTrg;
        } else {
            factTS = 1.0;
        }

        // change target resolution to source resolution if no conversion is needed
        if (!configuration.getConvertResolution() && getNumFrames() > 0) {
            configuration.setOutputResolution(getResolutionForDimension(getSubPictureSrc(0).getWidth(), getSubPictureSrc(0).getHeight()));
        }

        if (resOld != configuration.getOutputResolution()) {
            int rOld[] = resOld.getDimensions();
            int rNew[] = configuration.getOutputResolution().getDimensions();
            factX = (double)rNew[0]/(double)rOld[0];
            factY = (double)rNew[1]/(double)rOld[1];
        } else {
            factX = 1.0;
            factY = 1.0;
        }

        // first run: clone source subpics, apply speedup/down,
        for (int i=0; i < subPictures.length; i++) {
            picOld = subPictures[i];
            picSrc = subtitleStream.getSubPicture(i);
            subPictures[i] = picOld.copy();

            // set forced flag
            switch (configuration.getForceAll()) {
                case SET:
                    subPictures[i].setForced(true);
                    break;
                case CLEAR:
                    subPictures[i].setForced(false);
                    break;
            }

            long ts = picOld.getStartTime();
            long te = picOld.getEndTime();
            // copy time stamps and apply speedup/speeddown
            int delayPTS = configuration.getDelayPTS();
            if (factTS == 1.0) {
                subPictures[i].setStartTime(ts - delayOld + delayPTS);
                subPictures[i].setEndTime(te - delayOld + delayPTS);
            } else {
                subPictures[i].setStartTime((long)(ts * factTS + 0.5) - delayOld + delayPTS);
                subPictures[i].setEndTime((long)(te * factTS + 0.5) - delayOld + delayPTS);
            }
            // synchronize to target frame rate
            subPictures[i].setStartTime(SubtitleUtils.syncTimePTS(subPictures[i].getStartTime(), fpsTrg, fpsTrg));
            subPictures[i].setEndTime(SubtitleUtils.syncTimePTS(subPictures[i].getEndTime(), fpsTrg, fpsTrg));
            // adjust image sizes and offsets
            // determine scaling factors
            double scaleX;
            double scaleY;
            if (configuration.getConvertResolution()) {
                subPictures[i].setWidth(configuration.getOutputResolution().getDimensions()[0]);
                subPictures[i].setHeight(configuration.getOutputResolution().getDimensions()[1]);
                scaleX = (double) subPictures[i].getWidth() / picSrc.getWidth();
                scaleY = (double) subPictures[i].getHeight() / picSrc.getHeight();
            } else {
                subPictures[i].setWidth(picSrc.getWidth());
                subPictures[i].setHeight(picSrc.getHeight());
                scaleX = 1.0;
                scaleY = 1.0;
            }

            int w = (int)(picSrc.getImageWidth()  * scaleX * fsXNew + 0.5);
            if (w < MIN_IMAGE_DIMENSION) {
                w = picSrc.getImageWidth();
            } else if (w > subPictures[i].getWidth()) {
                w = subPictures[i].getWidth();
                fsXNew = (double)w / (double)picSrc.getImageWidth() / scaleX;
            }
            int h = (int)(picSrc.getImageHeight() * scaleY * fsYNew + 0.5);
            if (h < MIN_IMAGE_DIMENSION) {
                h = picSrc.getImageHeight();
            } else if (h > subPictures[i].getHeight()) {
                h = subPictures[i].getHeight();
                fsYNew = (double)h / (double)picSrc.getImageHeight() / scaleY;
            }

            subPictures[i].setImageWidth(w);
            subPictures[i].setImageHeight(h);

            // correct ratio change
            int xOfs = (int)(picOld.getXOffset()*factX + 0.5);
            if (fsXNew != fsXOld) {
                int spaceTrgOld = (int)((picOld.getWidth() - picOld.getImageWidth())*factX + 0.5);
                int spaceTrg    = subPictures[i].getWidth() - w;
                xOfs += (spaceTrg - spaceTrgOld) / 2;
            }
            if (xOfs < 0) {
                xOfs = 0;
            } else if (xOfs+w > subPictures[i].getWidth()) {
                xOfs = subPictures[i].getWidth() - w;
            }
            subPictures[i].setOfsX(xOfs);

            int yOfs = (int)(picOld.getYOffset()*factY + 0.5);
            if (fsYNew != fsYOld) {
                int spaceTrgOld = (int)((picOld.getHeight() - picOld.getImageHeight())*factY + 0.5);
                int spaceTrg = subPictures[i].getHeight() - h;
                yOfs += (spaceTrg - spaceTrgOld) / 2;
            }
            if (yOfs < 0) {
                yOfs = 0;
            }
            if (yOfs+h > subPictures[i].getHeight()) {
                yOfs = subPictures[i].getHeight() - h;
            }
            subPictures[i].setOfsY(yOfs);

            // fix erase patches
            double fx = factX * fsXNew / fsXOld;
            double fy = factY * fsYNew / fsYOld;
            ArrayList<ErasePatch> erasePatches = subPictures[i].getErasePatch();
            if (erasePatches != null) {
                for (int j = 0; j < erasePatches.size(); j++) {
                    ErasePatch ep = erasePatches.get(j);
                    int x = (int)(ep.x * fx + 0.5);
                    int y = (int)(ep.y * fy + 0.5);
                    int width = (int)(ep.width * fx + 0.5);
                    int height = (int)(ep.height * fy + 0.5);
                    erasePatches.set(j, new ErasePatch(x, y, width, height));
                }
            }
        }

        // 2nd run: validate times (not fully necessary, but to avoid overlap due to truncation
        SubPicture subPicPrev = null;
        SubPicture subPicNext;

        for (int i=0; i<subPictures.length; i++) {
            if (i < subPictures.length-1) {
                subPicNext = subPictures[i+1];
            } else {
                subPicNext = null;
            }

            picOld = subPictures[i];
            validateTimes(i, subPictures[i], subPicNext, subPicPrev);
            subPicPrev = picOld;
        }
    }

    /**
     * Convert source subpicture image to target subpicture image.
     * @param index			Index of subtitle to convert
     * @param displayNum	Subtitle number to display (needed for forced subs)
     * @param displayMax	Maximum subtitle number to display (needed for forced subs)
     * @throws CoreException
     */
    public static void convertSup(int index, int displayNum, int displayMax) throws CoreException{
        convertSup(index, displayNum, displayMax, false);
    }

    /**
     * Convert source subpicture image to target subpicture image.
     * @param index			Index of subtitle to convert
     * @param displayNum	Subtitle number to display (needed for forced subs)
     * @param displayMax	Maximum subtitle number to display (needed for forced subs)
     * @param skipScaling   true: skip bitmap scaling and palette transformation (used for moving captions)
     * @throws CoreException
     */
    private static void convertSup(int index, int displayNum, int displayMax, boolean skipScaling) throws CoreException{
        int w,h;
        int startOfs = (int) subtitleStream.getStartOffset(index);
        SubPicture subPic = subtitleStream.getSubPicture(index);

        printX("Decoding frame "+displayNum+"/"+displayMax+((subtitleStream == supXml)?"\n":(" at offset "+ToolBox.toHexLeftZeroPadded(startOfs,8)+"\n")));

        synchronized (semaphore) {
            subtitleStream.decode(index);
            w = subPic.getImageWidth();
            h = subPic.getImageHeight();
            OutputMode outputMode = configuration.getOutputMode();
            if (outputMode == OutputMode.VOBSUB || outputMode == OutputMode.SUPIFO) {
                determineFramePal(index);
            }
            updateTrgPic(index);
        }
        SubPicture picTrg = subPictures[index];
        picTrg.setWasDecoded(true);

        int trgWidth = picTrg.getImageWidth();
        int trgHeight = picTrg.getImageHeight();
        if (trgWidth < MIN_IMAGE_DIMENSION || trgHeight < MIN_IMAGE_DIMENSION || w < MIN_IMAGE_DIMENSION || h < MIN_IMAGE_DIMENSION) {
            // don't scale to avoid division by zero in scaling routines
            trgWidth = w;
            trgHeight = h;
        }

        if (!skipScaling) {
            ResampleFilter f;
            switch (configuration.getScalingFilter()) {
                case BELL:
                    f = getBellFilter();
                    break;
                case BICUBIC:
                    f = getBiCubicFilter();
                    break;
                case BICUBIC_SPLINE:
                    f = getBSplineFilter();
                    break;
                case HERMITE:
                    f = getHermiteFilter();
                    break;
                case LANCZOS3:
                    f = getLanczos3Filter();
                    break;
                case TRIANGLE:
                    f = getTriangleFilter();
                    break;
                case MITCHELL:
                    f = getMitchellFilter();
                    break;
                default:
                    f = null;
            }

            Bitmap tBm;
            Palette tPal = trgPal;
            // create scaled bitmap
            OutputMode outputMode = configuration.getOutputMode();
            PaletteMode paletteMode = configuration.getPaletteMode();
            if (outputMode == OutputMode.VOBSUB || outputMode == OutputMode.SUPIFO) {
                // export 4 color palette
                if (w==trgWidth && h==trgHeight) {
                    // don't scale at all
                    if ( (inMode == InputMode.VOBSUB || inMode == InputMode.SUPIFO) && paletteMode == PaletteMode.KEEP_EXISTING) {
                        tBm = subtitleStream.getBitmap(); // no conversion
                    } else {
                        tBm = subtitleStream.getBitmap().getBitmapWithNormalizedPalette(subtitleStream.getPalette().getAlpha(), configuration.getAlphaThreshold(), subtitleStream.getPalette().getY(), configuration.getLuminanceThreshold()); // reduce palette
                    }
                } else {
                    // scale up/down
                    if ((inMode == InputMode.VOBSUB || inMode == InputMode.SUPIFO) && paletteMode == PaletteMode.KEEP_EXISTING) {
                        // keep palette
                        if (f != null) {
                            tBm = subtitleStream.getBitmap().scaleFilter(trgWidth, trgHeight, subtitleStream.getPalette(), f);
                        } else {
                            tBm = subtitleStream.getBitmap().scaleBilinear(trgWidth, trgHeight, subtitleStream.getPalette());
                        }
                    } else {
                        // reduce palette
                        if (f != null) {
                            tBm = subtitleStream.getBitmap().scaleFilterLm(trgWidth, trgHeight, subtitleStream.getPalette(), configuration.getAlphaThreshold(), configuration.getLuminanceThreshold(), f);
                        } else {
                            tBm = subtitleStream.getBitmap().scaleBilinearLm(trgWidth, trgHeight, subtitleStream.getPalette(), configuration.getAlphaThreshold(), configuration.getLuminanceThreshold());
                        }
                    }
                }
            } else {
                // export (up to) 256 color palette
                tPal = subtitleStream.getPalette();
                if (w==trgWidth && h==trgHeight) {
                    tBm = subtitleStream.getBitmap(); // no scaling, no conversion
                } else {
                    // scale up/down
                    if (paletteMode == PaletteMode.KEEP_EXISTING) {
                        // keep palette
                        if (f != null) {
                            tBm = subtitleStream.getBitmap().scaleFilter(trgWidth, trgHeight, subtitleStream.getPalette(), f);
                        } else {
                            tBm = subtitleStream.getBitmap().scaleBilinear(trgWidth, trgHeight, subtitleStream.getPalette());
                        }
                    } else {
                        // create new palette
                        boolean dither = paletteMode == PaletteMode.CREATE_DITHERED;
                        BitmapWithPalette pb;
                        if (f != null) {
                            pb = subtitleStream.getBitmap().scaleFilter(trgWidth, trgHeight, subtitleStream.getPalette(), f, dither);
                        } else {
                            pb = subtitleStream.getBitmap().scaleBilinear(trgWidth, trgHeight, subtitleStream.getPalette(), dither);
                        }
                        tBm = pb.bitmap;
                        tPal = pb.palette;
                    }
                }
            }
            if (picTrg.getErasePatch() != null) {
                trgBitmapUnpatched = new Bitmap(tBm);
                int col = tPal.getIndexOfMostTransparentPaletteEntry();
                for (ErasePatch ep : picTrg.getErasePatch()) {
                    tBm.fillRectangularWithColorIndex(ep.x, ep.y, ep.width, ep.height, (byte)col);
                }
            } else {
                trgBitmapUnpatched = tBm;
            }
            trgBitmap = tBm;
            trgPal = tPal;

        }

        if (configuration.isCliMode()) {
            moveToBounds(picTrg, displayNum, configuration.getCineBarFactor(), configuration.getMoveOffsetX(), configuration.getMoveOffsetY(), configuration.getMoveModeX(), configuration.getMoveModeY(), configuration.getCropOffsetY());
        }
    }

    /**
     * Create BD-SUP or VobSub or Xml.
     * @param fname File name of SUP/SUB/XML to create
     * @throws CoreException
     */
    public static void writeSub(String fname) throws CoreException {
        BufferedOutputStream out = null;
        ArrayList<Integer> offsets = null;
        ArrayList<Integer> timestamps = null;
        int frameNum = 0;
        int maxNum;
        String fn = "";

        // handling of forced subtitles
        if (configuration.isExportForced()) {
            maxNum = countForcedIncluded();
        } else {
            maxNum = countIncluded();
        }

        OutputMode outputMode = configuration.getOutputMode();
        try {
            // handle file name extensions depending on mode
            if (outputMode == OutputMode.VOBSUB) {
                fname = FilenameUtils.removeExtension(fname) + ".sub";
                out = new BufferedOutputStream(new FileOutputStream(fname));
                offsets = new ArrayList<Integer>();
                timestamps = new ArrayList<Integer>();
            } else if (outputMode == OutputMode.SUPIFO) {
                fname = FilenameUtils.removeExtension(fname) + ".sup";
                out = new BufferedOutputStream(new FileOutputStream(fname));
            } else if (outputMode == OutputMode.BDSUP) {
                fname = FilenameUtils.removeExtension(fname) + ".sup";
                out = new BufferedOutputStream(new FileOutputStream(fname));
            } else {
                fn = FilenameUtils.removeExtension(fname);
                fname = fn+".xml";
            }
            printX("\nWriting "+fname+"\n");
            resetErrors();
            resetWarnings();

            // main loop
            int offset = 0;
            for (int i=0; i < subtitleStream.getFrameCount(); i++) {
                // for threaded version
                if (isCancelled()) {
                    throw new CoreException("Cancelled by user!");
                }
                // for threaded version (progress bar);
                setProgress(i);
                //
                if (!subPictures[i].isExcluded() && (!configuration.isExportForced() || subPictures[i].isForced())) {
                    if (outputMode == OutputMode.VOBSUB) {
                        offsets.add(offset);
                        convertSup(i, frameNum/2+1, maxNum);
                        subVobTrg.copyInfo(subPictures[i]);
                        byte buf[] = SubDvd.createSubFrame(subVobTrg, trgBitmap);
                        out.write(buf);
                        offset += buf.length;
                        timestamps.add((int) subPictures[i].getStartTime());
                    } else if (outputMode == OutputMode.SUPIFO) {
                        convertSup(i, frameNum/2+1, maxNum);
                        subVobTrg.copyInfo(subPictures[i]);
                        byte buf[] = SupDvdWriter.createSupFrame(subVobTrg, trgBitmap);
                        out.write(buf);
                    } else if (outputMode == OutputMode.BDSUP) {
                        subPictures[i].setCompNum(frameNum);
                        convertSup(i, frameNum/2+1, maxNum);
                        byte buf[] = SupBD.createSupFrame(subPictures[i], trgBitmap, trgPal);
                        out.write(buf);
                    } else {
                        // Xml
                        convertSup(i, frameNum/2+1, maxNum);
                        String fnp = SupXml.getPNGname(fn,i+1);
                        //File file = new File(fnp);
                        //ImageIO.write(trgBitmap.getImage(trgPal), "png", file);
                        out = new BufferedOutputStream(new FileOutputStream(fnp));
                        EnhancedPngEncoder pngEncoder= new EnhancedPngEncoder(trgBitmap.getImage(trgPal.getColorModel()));
                        byte buf[] = pngEncoder.pngEncode(true);
                        out.write(buf);
                        out.close();

                    }
                    frameNum+=2;
                }
            }
        } catch (IOException ex) {
            throw new CoreException(ex.getMessage());
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
            }
        }

        boolean importedDVDPalette;
        importedDVDPalette = (inMode == InputMode.VOBSUB) || (inMode == InputMode.SUPIFO);

        Palette trgPallete = null;
        PaletteMode paletteMode = configuration.getPaletteMode();
        if (outputMode == OutputMode.VOBSUB) {
            // VobSub - write IDX
            /* return offets as array of ints */
            int ofs[] = new int[offsets.size()];
            for (int i=0; i<ofs.length; i++) {
                ofs[i] = offsets.get(i);
            }
            int ts[] = new int[timestamps.size()];
            for (int i=0; i<ts.length; i++) {
                ts[i] = timestamps.get(i);
            }
            fname = FilenameUtils.removeExtension(fname) + ".idx";
            printX("\nWriting "+fname+"\n");
            if (!importedDVDPalette || paletteMode != PaletteMode.KEEP_EXISTING) {
                trgPallete = currentDVDPalette;
            } else {
                trgPallete = currentSourceDVDPalette;
            }
            SubDvd.writeIdx(fname, subPictures[0], ofs, ts, trgPallete);
        } else if (outputMode == OutputMode.XML) {
            // XML - write ML
            printX("\nWriting "+fname+"\n");
            SupXml.writeXml(fname, subPictures);
        } else if (outputMode == OutputMode.SUPIFO) {
            // SUP/IFO - write IFO
            if (!importedDVDPalette || paletteMode != PaletteMode.KEEP_EXISTING) {
                trgPallete = currentDVDPalette;
            } else {
                trgPallete = currentSourceDVDPalette;
            }
            fname = FilenameUtils.removeExtension(fname) + ".ifo";
            printX("\nWriting "+fname+"\n");
            IfoWriter.writeIFO(fname, subPictures[0].getHeight(), trgPallete);
        }

        // only possible for SUB/IDX and SUP/IFO (else there is no public palette)
        if (trgPallete != null && configuration.getWritePGCEditPalette()) {
            String fnp = FilenameUtils.removeExtension(fname) + ".txt";
            printX("\nWriting "+fnp+"\n");
            writePGCEditPal(fnp, trgPallete);
        }

        state = CoreThreadState.FINISHED;
    }

    /**
     * Move all subpictures into or outside given bounds in a thread and display the progress dialog.
     * @param parent	Parent frame (needed for progress dialog)
     * @throws Exception
     */
    public static void moveAllThreaded(JFrame parent) throws Exception {
        progressMax = subtitleStream.getFrameCount();
        progressLast = 0;
        progress = new Progress(parent);
        progress.setTitle("Moving");
        progress.setText("Moving all captions");
        runType = RunType.MOVEALL;
        // start thread
        Thread t = new Thread(new Core());
        t.start();
        progress.setVisible(true);
        while (t.isAlive()) {
            try  {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
        }
        state = CoreThreadState.INACTIVE;
        Exception ex = threadException;
        if (ex != null) {
            throw ex;
        }
    }

    /**
     * Move all subpictures into or outside given bounds.
     * @throws CoreException
     */
    public static void moveAllToBounds() throws CoreException {
        String sy = null;
        switch (configuration.getMoveModeY()) {
            case MOVE_INSIDE_BOUNDS:
                sy = "inside";
                break;
            case MOVE_OUTSIDE_BOUNDS:
                sy = "outside";
                break;
        }
        String sx = null;
        switch (configuration.getMoveModeX()) {
            case CENTER:
                sx = "center vertically";
                break;
            case LEFT:
                sx = "left";
                break;
            case RIGHT:
                sx = "right";
        }
        String s = "Moving captions ";
        if (sy!= null) {
            s += sy + " cinemascope bars";
            if (sx != null) {
                 s += " and to the " + sx;
            }
            print(s+".\n");
        } else if (sx != null) {
            print(s+"to the "+sx+".\n");
        }

        if (!configuration.isCliMode()) {
            // in CLI mode, moving is done during export
            for (int idx=0; idx<subPictures.length; idx++) {
                setProgress(idx);
                if (!subPictures[idx].isWasDecoded()) {
                    convertSup(idx, idx+1, subPictures.length, true);
                }
                moveToBounds(subPictures[idx], idx+1, configuration.getCineBarFactor(), configuration.getMoveOffsetX(), configuration.getMoveOffsetY(), configuration.getMoveModeX(), configuration.getMoveModeY(), configuration.getCropOffsetY());
            }
        }
    }

    /**
     * Move subpicture into or outside given bounds.
     * @param pic         SubPicture object containing coordinates and size
     * @param idx         Index (only used for display)
     * @param barFactor   Factor to calculate cinemascope bar height from screen height
     * @param offsetX     X offset to consider when moving
     * @param offsetY     Y offset to consider when moving
     * @param mmx         Move mode in X direction
     * @param mmy         Move mode in Y direction
     * @param cropOffsetY Number of lines to crop from bottom and top
     */
    public static void moveToBounds(SubPicture pic, int idx, double barFactor, int offsetX, int offsetY,
            CaptionMoveModeX mmx, CaptionMoveModeY mmy, int cropOffsetY) {

        int barHeight = (int)(pic.getHeight() * barFactor + 0.5);
        int y1 = pic.getYOffset();
        int h = pic.getHeight();
        int w = pic.getWidth();
        int hi = pic.getImageHeight();
        int wi = pic.getImageWidth();
        int y2 = y1 + hi;
        CaptionType c;

        if (mmy != CaptionMoveModeY.KEEP_POSITION) {
            // move vertically
            if (y1 < h/2 && y2 < h/2) {
                c = CaptionType.UP;
            } else if (y1 > h/2 && y2 > h/2) {
                c = CaptionType.DOWN;
            } else {
                c = CaptionType.FULL;
            }

            switch (c) {
                case FULL:
                    // maybe add scaling later, but for now: do nothing
                    printWarn("Caption "+idx+" not moved (too large)\n");
                    break;
                case UP:
                    if (mmy == CaptionMoveModeY.MOVE_INSIDE_BOUNDS)
                        pic.setOfsY(barHeight+offsetY);
                    else
                        pic.setOfsY(offsetY);
                    print("Caption "+idx+" moved to y position "+pic.getYOffset()+"\n");
                    break;
                case DOWN:
                    if (mmy == CaptionMoveModeY.MOVE_INSIDE_BOUNDS) {
                        pic.setOfsY(h-barHeight-offsetY-hi);
                    } else {
                        pic.setOfsY(h-offsetY-hi);
                    }
                    print("Caption "+idx+" moved to y position "+pic.getYOffset()+"\n");
                    break;
            }
            if (pic.getYOffset() < cropOffsetY) {
                pic.getYOffset();
            } else {
                int yMax = pic.getHeight() - pic.getImageHeight() - cropOffsetY;
                if (pic.getYOffset() > yMax) {
                    pic.setOfsY(yMax);
                }
            }
        }
        // move horizontally
        switch (mmx) {
            case LEFT:
                if (w-wi >= offsetX) {
                    pic.setOfsX(offsetX);
                } else {
                    pic.setOfsX((w-wi)/2);
                }
                break;
            case RIGHT:
                if (w-wi >= offsetX) {
                    pic.setOfsX(w-wi-offsetX);
                } else {
                    pic.setOfsX((w-wi)/2);
                }
                break;
            case CENTER:
                pic.setOfsX((w-wi)/2);
                break;
        }
    }

    /**
     * Print string to console or console window (only printed in verbatim mode).
     * @param s String containing message to print
     */
    public static void print(String s) {
        if (configuration.isVerbatim()) {
            if (mainFrame != null) {
                mainFrame.printOut(s);
            } else {
                System.out.print(s);
            }
        }
    }

    /**
     * Print string to console or console window (always printed).
     * @param s String containing message to print
     */
    public static void printX(String s) {
        if (mainFrame != null) {
            mainFrame.printOut(s);
        } else {
            System.out.print(s);
        }
    }

    /**
     * Print error string to console or console window (always printed).
     * @param s String containing error message to print
     */
    public static void printErr(String s) {
        errors++;
        s = "ERROR: " + s;
        if (mainFrame != null) {
            mainFrame.printErr(s);
        } else {
            System.out.print(s);
        }
    }

    /**
     * Print warning string to console or console window (always printed).
     * @param s String containing warning message to print
     */
    public static void printWarn(String s) {
        warnings++;
        s = "WARNING: "+s;
        if (mainFrame != null) {
            mainFrame.printWarn(s);
        } else {
            System.out.print(s);
        }
    }

    /**
     * Create PGCEdit palette file from given Palette.
     * @param fname File name
     * @param p     Palette
     * @throws CoreException
     */
    private static void writePGCEditPal(String fname, Palette p) throws CoreException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(fname));
            out.write("# Palette file for PGCEdit - colors given as R,G,B components (0..255)");
            out.newLine();
            for (int i=0; i < p.getSize(); i++) {
                int rgb[] = p.getRGB(i);
                out.write("Color "+i+"="+rgb[0]+", "+rgb[1]+", "+rgb[2]);
                out.newLine();
            }
        } catch (IOException ex) {
            throw new CoreException(ex.getMessage());
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Count the number of forced subpictures to be exported.
     * @return Number of forced subpictures to be exported
     */
    private static int countForcedIncluded() {
        int n = 0;
        for (SubPicture pic : subPictures) {
            if (pic.isForced() && !pic.isExcluded()) {
                n++;
            }
        }
        return n;
    }

    /**
     * Count the number of subpictures to be exported.
     * @return Number of subpictures to be exported
     */
    private static int countIncluded() {
        int n = 0;
        for (SubPicture pic : subPictures) {
            if (!pic.isExcluded()) {
                n++;
            }
        }
        return n;
    }

    /**
     * Get Core ready state.
     * @return True if the Core is ready
     */
    public static boolean isReady() {
        return ready;
    }

    /**
     * Set Core ready state.
     * @param r true if the Core is ready
     */
    public static void setReady(boolean r) {
        ready = r;
    }

    /**
     *  Force Core to cancel current operation.
     */
    public static void cancel() {
        state = CoreThreadState.CANCELED;
    }

    /**
     * Get cancel state.
     * @return True if the current operation was canceled
     */
    public static boolean isCancelled() {
        return state == CoreThreadState.CANCELED;
    }

    /**
     * Get Core state.
     * @return Current Core state
     */
    public static CoreThreadState getStatus() {
        return state;
    }

    /**
     * Get reference to the main frame.
     * @return Reference to the main frame
     */
    public static MainFrameView getMainFrame() {
        return mainFrame;
    }

    /**
     * Set reference to the main frame.
     * @param mf Reference to the main frame
     */
    public static void setMainFrame(final MainFrameView mf) {
        mainFrame = mf;
    }

    /**
     * Get the number of errors.
     * @return Number of errors since last call to resetErrors
     */
    public static int getErrors() {
        return errors;
    }

    /**
     * Reset the number of errors.
     */
    public static void resetErrors() {
        errors = 0;
    }

    /**
     * Get the number of warnings.
     * @return Number of warnings since last call to resetWarnings
     */
    public static int getWarnings() {
        return warnings;
    }

    /**
     * Reset the number of warnings.
     */
    public static void resetWarnings() {
        warnings = 0;
    }

    /**
     * Set progress in progress bar.
     * @param p Subtitle index processed
     */
    public static void setProgress(int p) {
        if (progress != null) {
            int val = (int)(((long)p * 100) / progressMax);
            if (val > progressLast) {
                progressLast = val;
                progress.setProgress(val);
            }
        }
    }

    /**
     * Get input mode.
     * @return Current input mode
     */
    public static InputMode getInputMode() {
        return inMode;
    }

    /**
     * Get source image as BufferedImage.
     * @return Source image as BufferedImage
     */
    public static BufferedImage getSrcImage() {
        synchronized (semaphore) {
            return subtitleStream.getImage();
        }
    }

    /**
     * Get source image as BufferedImage.
     * @param idx	Index of subtitle
     * @return		Source image as BufferedImage
     * @throws CoreException
     */
    public static BufferedImage getSrcImage(int idx) throws CoreException {
        synchronized (semaphore) {
            subtitleStream.decode(idx);
            return subtitleStream.getImage();
        }
    }

    /**
     * Get target image as BufferedImage.
     * @return Target image as BufferedImage
     */
    public static BufferedImage getTrgImage() {
        synchronized (semaphore) {
            return trgBitmap.getImage(trgPal.getColorModel());
        }
    }

    /**
     * Get target image as BufferedImage.
     * @param pic SubPicture to use for applying erase patches
     * @return Target image as BufferedImage
     */
    public static BufferedImage getTrgImagePatched(SubPicture pic) {
        synchronized (semaphore) {
            if (pic.getErasePatch() != null) {
                Bitmap trgBitmapPatched = new Bitmap(trgBitmapUnpatched);
                int col = trgPal.getIndexOfMostTransparentPaletteEntry();
                for (ErasePatch ep : pic.getErasePatch()) {
                    trgBitmapPatched.fillRectangularWithColorIndex(ep.x, ep.y, ep.width, ep.height, (byte)col);
                }
                return trgBitmapPatched.getImage(trgPal.getColorModel());
            } else {
                return trgBitmapUnpatched.getImage(trgPal.getColorModel());
            }
        }
    }

    /**
     * Get screen width of target.
     * @param index Subtitle index
     * @return Screen width of target
     */
    public static int getTrgWidth(int index) {
        synchronized (semaphore) {
            return subPictures[index].getWidth();
        }
    }

    /**
     * Get screen height of target.
     * @param index Subtitle index
     * @return Screen height of target
     */
    public static int getTrgHeight(int index) {
        synchronized (semaphore) {
            return subPictures[index].getHeight();
        }
    }

    /**
     * Get subtitle width of target.
     * @param index Subtitle index
     * @return Subtitle width of target
     */
    public static int getTrgImgWidth(int index) {
        synchronized (semaphore) {
            return subPictures[index].getImageWidth();
        }
    }

    /**
     * Get subtitle height of target.
     * @param index Subtitle index
     * @return Subtitle height of target
     */
    public static int getTrgImgHeight(int index) {
        synchronized (semaphore) {
            return subPictures[index].getImageHeight();
        }
    }

    /**
     * Get exclude (from export) state of target.
     * @param index Subtitle index
     * @return Screen width of target
     */
    public static boolean getTrgExcluded(int index) {
        synchronized (semaphore) {
            return subPictures[index].isExcluded();
        }
    }

    /**
     * Get subtitle x offset of target.
     * @param index Subtitle index
     * @return Subtitle x offset of target
     */
    public static int getTrgOfsX(int index) {
        synchronized (semaphore) {
            return subPictures[index].getXOffset();
        }
    }

    /**
     * Get subtitle y offset of target.
     * @param index Subtitle index
     * @return Subtitle y offset of target
     */
    public static int getTrgOfsY(int index) {
        synchronized (semaphore) {
            return subPictures[index].getYOffset();
        }
    }

    /**
     * Get number of subtitles.
     * @return Number of subtitles
     */
    public static int getNumFrames() {
        return subtitleStream == null ? 0 : subtitleStream.getFrameCount();
    }

    /**
     * Get number of forced subtitles.
     * @return Number of forced subtitles
     */
    public static int getNumForcedFrames() {
        return subtitleStream == null ? 0 : subtitleStream.getForcedFrameCount();
    }

    /**
     * Create info string for target subtitle.
     * @param index Index of subtitle
     * @return Info string for target subtitle
     */
    public static String getTrgInfoStr(int index) {
        SubPicture pic = subPictures[index];
        String text = "screen size: "+getTrgWidth(index)+"x"+getTrgHeight(index)+"    ";
        text +=	"image size: "+getTrgImgWidth(index)+"x"+getTrgImgHeight(index)+"    ";
        text += "pos: ("+pic.getXOffset()+","+pic.getYOffset()+") - ("+(pic.getXOffset()+getTrgImgWidth(index))+","+(pic.getYOffset()+getTrgImgHeight(index))+")    ";
        text += "start: "+ptsToTimeStr(pic.getStartTime())+"    ";
        text += "end: "+ptsToTimeStr(pic.getEndTime())+"    ";
        text += "forced: "+((pic.isForced())?"yes":"no");
        return text;
    }

    /**
     * Create info string for source subtitle.
     * @param index Index of subtitle
     * @return Info string for source subtitle
     */
    public static String getSrcInfoStr(int index) {
        String text;

        SubPicture pic = subtitleStream.getSubPicture(index);
        text  = "screen size: "+ pic.getWidth() +"x"+ pic.getHeight() +"    ";
        text +=	"image size: "+pic.getImageWidth()+"x"+pic.getImageHeight()+"    ";
        text += "pos: ("+pic.getXOffset()+","+pic.getYOffset()+") - ("+(pic.getXOffset()+pic.getImageWidth())+","+(pic.getYOffset()+pic.getImageHeight())+")    ";
        text += "start: "+ptsToTimeStr(pic.getStartTime())+"    ";
        text += "end: "+ptsToTimeStr(pic.getEndTime())+"    ";
        text += "forced: "+((pic.isForced())?"yes":"no");
        return text;
    }

    /**
     * Get current DVD palette.
     * @return DVD palette
     */
    public static Palette getCurrentDVDPalette() {
        return currentDVDPalette;
    }

    /**
     * Set current DVD palette.
     * @param pal DVD palette
     */
    public static void setCurrentDVDPalette(Palette pal) {
        currentDVDPalette = pal;
    }

    /**
     * Get target subpicture.
     * @param index Index of subpicture
     * @return Target SubPicture
     */
    public static SubPicture getSubPictureTrg(int index) {
        synchronized (semaphore) {
            return subPictures[index];
        }
    }

    /**
     * Get source subpicture.
     * @param index Index of subpicture
     * @return Source SubPicture
     */
    public static SubPicture getSubPictureSrc(int index) {
        synchronized (semaphore) {
            return subtitleStream.getSubPicture(index);
        }
    }

    /**
     * Get: use of BT.601 color model instead of BT.709.
     * @return True if BT.601 is used
     */
    public static boolean usesBT601() {
        return useBT601;
    }

    /**
     * Set internal maximum for progress bar.
     * @param max Internal maximum for progress bar (e.g. number of subtitles)
     */
    public static void setProgressMax(int max) {
        progressMax = max;
    }

    /**
     * Get imported palette if input is DVD format.
     * @return Imported palette if input is DVD format, else null
     */
    public static Palette getDefSrcDVDPalette() {
        return defaultSourceDVDPalette;
    }

    /**
     * Get modified imported palette if input is DVD format.
     * @return Imported palette if input is DVD format, else null
     */
    public static Palette getCurSrcDVDPalette() {
        return currentSourceDVDPalette;
    }

    /**
     * Set modified imported palette.
     * @param pal Modified imported palette
     */
    public static void setCurSrcDVDPalette(Palette pal) {
        currentSourceDVDPalette = pal;

        DvdSubtitleStream substreamDvd = null;
        if (inMode == InputMode.VOBSUB) {
            substreamDvd = subDVD;
        } else if (inMode == InputMode.SUPIFO) {
            substreamDvd = supDVD;
        }

        substreamDvd.setSrcPalette(currentSourceDVDPalette);
    }

    /**
     * Return frame palette of given subtitle.
     * @param index Index of subtitle
     * @return Frame palette of given subtitle as array of int (4 entries)
     */
    public static int[] getFramePal(int index) {
        DvdSubtitleStream substreamDvd = null;

        if (inMode == InputMode.VOBSUB) {
            substreamDvd = subDVD;
        } else if (inMode == InputMode.SUPIFO) {
            substreamDvd = supDVD;
        }

        if (substreamDvd != null) {
            return substreamDvd.getFramePalette(index);
        } else {
            return null;
        }
    }

    /**
     * Return frame alpha values of given subtitle.
     * @param index Index of subtitle
     * @return Frame alpha values of given subtitle as array of int (4 entries)
     */
    public static int[] getFrameAlpha(int index) {
        DvdSubtitleStream substreamDvd = null;

        if (inMode == InputMode.VOBSUB) {
            substreamDvd = subDVD;
        } else if (inMode == InputMode.SUPIFO) {
            substreamDvd = supDVD;
        }

        if (substreamDvd != null) {
            return substreamDvd.getFrameAlpha(index);
        } else {
            return null;
        }
    }

    /**
     * Return original frame palette of given subtitle.
     * @param index Index of subtitle
     * @return Frame palette of given subtitle as array of int (4 entries)
     */
    public static int[] getOriginalFramePal(int index) {
        DvdSubtitleStream substreamDvd = null;

        if (inMode == InputMode.VOBSUB) {
            substreamDvd = subDVD;
        } else if (inMode == InputMode.SUPIFO) {
            substreamDvd = supDVD;
        }

        if (substreamDvd != null) {
            return substreamDvd.getOriginalFramePalette(index);
        } else {
            return null;
        }
    }

    /**
     * Return original frame alpha values of given subtitle.
     * @param index Index of subtitle
     * @return Frame alpha values of given subtitle as array of int (4 entries)
     */
    public static int[] getOriginalFrameAlpha(int index) {
        DvdSubtitleStream substreamDvd = null;

        if (inMode == InputMode.VOBSUB) {
            substreamDvd = subDVD;
        } else if (inMode == InputMode.SUPIFO) {
            substreamDvd = supDVD;
        }

        if (substreamDvd != null) {
            return substreamDvd.getOriginalFrameAlpha(index);
        } else {
            return null;
        }
    }
}
