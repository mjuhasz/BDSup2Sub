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
package bdsup2sub;

import bdsup2sub.cli.CommandLineParser;
import bdsup2sub.core.*;
import bdsup2sub.gui.main.MainFrame;
import bdsup2sub.tools.Props;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;
import org.apache.commons.cli.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static bdsup2sub.core.Configuration.*;
import static bdsup2sub.gui.support.GuiUtils.applyGtkThemeWorkarounds;

public class BDSup2Sub {

    private static final Configuration configuration = getInstance();

    private CommandLineParser options;

    public static void main(String[] args) {
        try {
            new BDSup2Sub().run(args);
        } catch (Exception e) {
            fatalError(e.getMessage());
            System.exit(1);
        }
    }

    private void run(String[] args) {
        parseOptions(args);
        processOptions();
        execute();
    }

    private void parseOptions(String[] args) {
        options = new CommandLineParser();
        try {
            options.parse(args);
        } catch (ParseException e) {
            fatalError(e.getMessage());
            options.printHelp();
            System.exit(1);
        }
    }

    private void processOptions() {
        if (options.isLoadSettings()) {
            configuration.load();
        }
        if (options.getOutputMode().isPresent()) {
            configuration.setOutputMode(options.getOutputMode().get());
        }
        if (options.getResolution().isPresent()) {
            configuration.setConvertResolution(true);
            configuration.setOutputResolution(options.getResolution().get());
        }
        processFrameRate();
        if (options.getDelay().isPresent()) {
            configuration.setDelayPTS((int) SubtitleUtils.syncTimePTS((long) (options.getDelay().get() * 90.0), configuration.getFpsTrg(), configuration.getFpsTrg()));
        }
        if (options.getScalingFilter().isPresent()) {
            configuration.setScalingFilter(options.getScalingFilter().get());
        }
        if (options.getPaletteMode().isPresent()) {
            configuration.setPaletteMode(options.getPaletteMode().get());
        }
        processMinimumDisplayTime();
        if (options.getMaximumTimeDifference().isPresent()) {
            configuration.setMergePTSdiff((int) (options.getMaximumTimeDifference().get() * 90.0 + 0.5));
        }
        processMoveMode();
        if (options.getCropLines().isPresent()) {
            Core.setCropOfsY(options.getCropLines().get());
        }
        if (options.getAlphaCropThreshold().isPresent()) {
            configuration.setAlphaCrop(options.getAlphaCropThreshold().get());
        }
        if (options.getScaleX().isPresent() && options.getScaleY().isPresent()) {
            configuration.setFreeScaleFactor(options.getScaleX().get(), options.getScaleY().get());
        }
        if (options.isExportPalette().isPresent()) {
            configuration.setWritePGCEditPalette(options.isExportPalette().get());
        }
        if (options.isExportForcedSubtitlesOnly().isPresent()) {
            configuration.setExportForced(options.isExportForcedSubtitlesOnly().get());
        }
        if (options.getForcedFlagState().isPresent()) {
            Core.setForceAll(options.getForcedFlagState().get());
        }
        if (options.isSwapCrCb().isPresent()) {
            Core.setSwapCrCb(options.isSwapCrCb().get());
        }
        if (options.isFixInvisibleFrames().isPresent()) {
            configuration.setFixZeroAlpha(options.isFixInvisibleFrames().get());
        }
        if (options.isVerbose().isPresent()) {
            configuration.setVerbatim(options.isVerbose().get());
        }
        if (options.getAlphaThreshold().isPresent()) {
            configuration.setAlphaThreshold(options.getAlphaThreshold().get());
        }
        processLuminanceThreshold();
        if (options.getLanguageIndex().isPresent()) {
            configuration.setLanguageIdx(options.getLanguageIndex().get());
        }
        processPaletteFile();
    }

    private void processFrameRate() {
        if (options.getTargetFrameRate().isPresent() && !options.getSourceFrameRate().equals(options.getTargetFrameRate())) {
            configuration.setConvertFPS(true);
        } else {
            Core.setKeepFps(true);
        }
        if (options.getSourceFrameRate().isPresent()) {
            configuration.setFpsSrc(options.getSourceFrameRate().get());
        }
        if (options.getTargetFrameRate().isPresent()) {
            configuration.setFpsTrg(options.getTargetFrameRate().get());
        } else if (options.getResolution().isPresent()) {
            switch(options.getResolution().get()) {
                case PAL: configuration.setFpsTrg(Framerate.PAL.getValue()); break;
                case NTSC: configuration.setFpsTrg(Framerate.NTSC.getValue()); break;
                case HD_720: configuration.setFpsTrg(Framerate.FPS_23_976.getValue()); break;
                case HD_1440x1080: configuration.setFpsTrg(Framerate.FPS_23_976.getValue()); break;
                case HD_1080: configuration.setFpsTrg(Framerate.FPS_23_976.getValue()); break;
            }
        }
    }

    private void processMinimumDisplayTime() {
        if (options.getMinimumDisplayTime().isPresent()) {
            int tMin = (int) SubtitleUtils.syncTimePTS((long) (options.getMinimumDisplayTime().get() * 90.0), configuration.getFpsTrg(), configuration.getFpsTrg());
            configuration.setMinTimePTS(tMin);
            configuration.setFixShortFrames(true);
        }
    }

    private void processMoveMode() {
        if (options.getMoveModeY().isPresent()) {
            Core.setMoveModeY(options.getMoveModeY().get());
            Core.setMoveOffsetY(options.getMoveYOffset());
        }
        if (options.getMoveModeX().isPresent()) {
            Core.setMoveModeX(options.getMoveModeX().get());
            if (options.getMoveXOffset().isPresent()) {
                Core.setMoveOffsetX(options.getMoveXOffset().get());
            }
        }
    }

    private void processLuminanceThreshold() {
        int lt[] = configuration.getLuminanceThreshold();
        if (options.getLumLowMidThreshold().isPresent()) {
            lt[1] = options.getLumLowMidThreshold().get();
        }
        if (options.getLumMidHighThreshold().isPresent()) {
            lt[0] = options.getLumMidHighThreshold().get();
        }
        configuration.setLuminanceThreshold(lt);
    }

    private void processPaletteFile() {
        if (options.getPaletteFile() != null) {
            Props colProps = new Props();
            colProps.load(options.getPaletteFile().getAbsolutePath());
            for (int c = 0; c < 15; c++) {
                String s = colProps.get("Color_" + c, "0,0,0");
                String sp[] = s.split(",");
                if (sp.length >= 3) {
                    int red = Integer.valueOf(sp[0].trim()) & 0xff;
                    int green = Integer.valueOf(sp[1].trim()) & 0xff;
                    int blue = Integer.valueOf(sp[2].trim()) & 0xff;
                    Core.getCurrentDVDPalette().setColor(c + 1, new Color(red, green, blue));
                }
            }
        }
    }

    private void execute() {
        if (options.isPrintHelpMode()) {
            options.printHelp();
        } else if (options.isPrintVersionMode()) {
            System.out.println(Constants.APP_NAME_AND_VERSION);
        } else {
            if (!options.isCliMode()) {
                setupGUI();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        configuration.setCliMode(false);
                        new MainFrame(options.getInputFile()).setVisible(true);
                    }
                });
            } else {
                runCliLoop();
            }
        }
    }

    private static void setupGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            applyGtkThemeWorkarounds();
        } catch (Exception e) {
            // ignore
        }
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
    }

    private void runCliLoop() {
        String inputFile = options.getInputFile().getAbsolutePath();
        String outputFile = options.getOutputFile().getAbsolutePath();
        try {
            boolean xml = FilenameUtils.getExtension(inputFile).equalsIgnoreCase("xml");
            boolean idx = FilenameUtils.getExtension(inputFile).equalsIgnoreCase("idx");
            boolean ifo = FilenameUtils.getExtension(inputFile).equalsIgnoreCase("ifo");
            byte id[] = ToolBox.getFileID(inputFile, 4);
            StreamID sid = (id == null) ? StreamID.UNKNOWN : Core.getStreamID(id);
            if (!idx && !xml && !ifo && sid == StreamID.UNKNOWN) {
                throw new CoreException("File '" + inputFile + "' is not a supported subtitle stream.");
            }
            Core.setCurrentStreamID(sid);

            // check output file(s)
            File indexFile, subtitleFile;
            if (configuration.getOutputMode() == OutputMode.VOBSUB) {
                indexFile = new File(FilenameUtils.removeExtension(outputFile) + ".idx");
                subtitleFile = new File(FilenameUtils.removeExtension(outputFile) + ".sub");
            } else {
                subtitleFile = new File(FilenameUtils.removeExtension(outputFile) + ".sup");
                indexFile = null;
            }
            if ((indexFile != null && !indexFile.canWrite()) || (!subtitleFile.canWrite())) {
                throw new CoreException("Target file '" + outputFile + "' is write protected.");
            }

            // read input file
            if (xml || sid == StreamID.XML) {
                Core.readXml(inputFile);
            } else if (idx || sid == StreamID.DVDSUB || sid == StreamID.IDX) {
                Core.readVobSub(inputFile);
            } else if (ifo || sid == StreamID.IFO) {
                Core.readSupIfo(inputFile);
            } else {
                Core.readSup(inputFile);
            }

            Core.scanSubtitles();
            printWarnings();
            // move captions
            if (Core.getMoveModeX() != CaptionMoveModeX.KEEP_POSITION || Core.getMoveModeY() != CaptionMoveModeY.KEEP_POSITION) {
                Core.setCineBarFactor((1.0 - (16.0/9)/options.getScreenRatio())/2.0);
                Core.moveAllToBounds();
            }
            // set some values
            if (configuration.isExportForced() && Core.getNumForcedFrames() == 0) {
                throw new CoreException("No forced subtitles found.");
            }
            // write output
            Core.writeSub(outputFile);
        } catch (CoreException ex) {
            Core.printErr(ex.getMessage());
        } catch (Exception ex) {
            ToolBox.showException(ex);
            Core.printErr(ex.getMessage());
        }
        // clean up
        printWarnings();
        Core.exit();

        System.out.println("\nConversion finished.");
        System.exit(0);
    }

    private static void exit(int c) {
        Core.exit();
        System.exit(c);
    }

    private static void fatalError(String e) {
        Core.exit();
        System.out.println("ERROR: " + e);
        System.exit(1);
    }

    private static void printWarnings() {
        int w = Core.getWarnings();
        Core.resetWarnings();
        int e = Core.getErrors();
        Core.resetErrors();
        if (w+e > 0) {
            String s = "";
            if (w > 0) {
                if (w==1) {
                    s += w+" warning";
                } else {
                    s += w+" warnings";
                }
            }
            if (w>0 && e>0) {
                s += " and ";
            }
            if (e > 0) {
                if (e==1) {
                    s = e+" error";
                } else {
                    s = e+" errors";
                }
            }
            if (w+e < 3) {
                s = "There was " + s;
            } else {
                s = "There were " + s;
            }
            System.out.println(s);
        }
    }
}
