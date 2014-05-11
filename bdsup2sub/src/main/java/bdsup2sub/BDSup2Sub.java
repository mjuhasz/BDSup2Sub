/*
 * Copyright 2014 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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

import static bdsup2sub.gui.support.GuiUtils.applyGtkThemeWorkarounds;

import java.awt.Color;
import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.cli.ParseException;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;

import bdsup2sub.cli.CommandLineParser;
import bdsup2sub.core.CaptionMoveModeX;
import bdsup2sub.core.CaptionMoveModeY;
import bdsup2sub.core.Configuration;
import bdsup2sub.core.Constants;
import bdsup2sub.core.Core;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.Framerate;
import bdsup2sub.core.LibLogger;
import bdsup2sub.core.Logger;
import bdsup2sub.core.OutputMode;
import bdsup2sub.core.StreamID;
import bdsup2sub.gui.main.MainFrame;
import bdsup2sub.tools.Props;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.PlatformUtils;
import bdsup2sub.utils.StreamUtils;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;

public class BDSup2Sub {

    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = Logger.getInstance();

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
		LibLogger.getInstance().setObserver(Logger.getInstance());
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
            configuration.setCropOffsetY(options.getCropLines().get());
        }
        if (options.getAlphaCropThreshold().isPresent()) {
            configuration.setAlphaCrop(options.getAlphaCropThreshold().get());
        }
        if (options.getScaleX().isPresent() && options.getScaleY().isPresent()) {
            configuration.setApplyFreeScale(true);
            configuration.setFreeScaleFactor(options.getScaleX().get(), options.getScaleY().get());
        }
        if (options.isExportPalette().isPresent()) {
            configuration.setWritePGCEditPalette(options.isExportPalette().get());
        }
        if (options.isExportForcedSubtitlesOnly().isPresent()) {
            configuration.setExportForced(options.isExportForcedSubtitlesOnly().get());
        }
        if (options.getForcedFlagState().isPresent()) {
            configuration.setForceAll(options.getForcedFlagState().get());
        }
        if (options.isSwapCrCb().isPresent()) {
            configuration.setSwapCrCb(options.isSwapCrCb().get());
        }
        if (options.isFixInvisibleFrames().isPresent()) {
            configuration.setFixZeroAlpha(options.isFixInvisibleFrames().get());
        }
        if (options.isVerbose().isPresent()) {
            configuration.setVerbose(options.isVerbose().get());
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
        boolean targetFramerateDefined = false;
        if (options.isConvertFpsMode()) {
            if (!options.getSourceFrameRate().isPresent()) { // was set to "auto"
                // leave default value
            } else {
                configuration.setFpsSrc(options.getSourceFrameRate().get());
                configuration.setFpsTrg(options.getTargetFrameRate().get());
            }
            // convert framerate from <auto>/fpssrc to fpstrg
            configuration.setConvertFPS(true);
            targetFramerateDefined = true;
        } else if (options.isSynchronizeFpsMode()) {
            if (!options.getTargetFrameRate().isPresent()) { // was set to "keep"
                configuration.setKeepFps(true);
                // use source fps as target fps
            } else {
                // synchronize target framerate to fpstrg
                configuration.setFpsTrg(options.getTargetFrameRate().get());
                targetFramerateDefined = true;
            }
        }
        if (!targetFramerateDefined && options.getResolution().isPresent()) {
            switch(options.getResolution().get()) {
                case PAL: configuration.setFpsTrg(Framerate.PAL.getValue()); break;
                case NTSC: configuration.setFpsTrg(Framerate.NTSC.getValue()); break;
                case HD_720: configuration.setFpsTrg(Framerate.FPS_23_976.getValue()); break;
                case HD_1440x1080: configuration.setFpsTrg(Framerate.FPS_23_976.getValue()); break;
                case HD_1080: configuration.setFpsTrg(Framerate.FPS_23_976.getValue()); break;
            }
        }
        if (!configuration.isKeepFps() && !targetFramerateDefined) {
            configuration.setFpsTrg(SubtitleUtils.getDefaultFramerateForResolution(configuration.getOutputResolution()));
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
            configuration.setMoveModeY(options.getMoveModeY().get());
            configuration.setMoveOffsetY(options.getMoveYOffset());
        }
        if (options.getMoveModeX().isPresent()) {
            configuration.setMoveModeX(options.getMoveModeX().get());
            if (options.getMoveXOffset().isPresent()) {
                configuration.setMoveOffsetX(options.getMoveXOffset().get());
            }
        }
    }

    private void processLuminanceThreshold() {
        int lt[] = configuration.getLuminanceThreshold();
        if (options.getLumLowMedThreshold().isPresent()) {
            lt[1] = options.getLumLowMedThreshold().get();
        }
        if (options.getLumMedHighThreshold().isPresent()) {
            lt[0] = options.getLumMedHighThreshold().get();
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
            System.out.println(Constants.APP_NAME + " " + Constants.APP_VERSION);
        } else {
            if (!options.isCliMode()) {
                setupGUI();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
					public void run() {
                        configuration.setCliMode(false);
                        Application app = new DefaultApplication();
                        MainFrame mainFrame = new MainFrame(options.getInputFile());
                        app.addApplicationListener(mainFrame.getApplicationListener());
                        mainFrame.setVisible(true);
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
        } catch (Exception e) {
            // ignore
        }

        if (PlatformUtils.isLinux()) {
            applyGtkThemeWorkarounds();
        }

        if (PlatformUtils.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", Constants.APP_NAME);
        }
    }

    private void runCliLoop() {
        String inputFile = options.getInputFile().getAbsolutePath();
        String outputFile = options.getOutputFile().getAbsolutePath();
        try {
            boolean xml = FilenameUtils.getExtension(inputFile).equalsIgnoreCase("xml");
            boolean idx = FilenameUtils.getExtension(inputFile).equalsIgnoreCase("idx");
            boolean ifo = FilenameUtils.getExtension(inputFile).equalsIgnoreCase("ifo");
            byte id[] = ToolBox.getFileID(inputFile, 4);
            StreamID sid = (id == null) ? StreamID.UNKNOWN : StreamUtils.getStreamID(id);
            if (!idx && !xml && !ifo && sid == StreamID.UNKNOWN) {
                throw new CoreException("File '" + inputFile + "' is not a supported subtitle stream.");
            }
            configuration.setCurrentStreamID(sid);

            // check output file(s)
            File indexFile, subtitleFile;
            if (configuration.getOutputMode() == OutputMode.VOBSUB) {
                indexFile = new File(FilenameUtils.removeExtension(outputFile) + ".idx");
                subtitleFile = new File(FilenameUtils.removeExtension(outputFile) + ".sub");
            } else {
                subtitleFile = new File(FilenameUtils.removeExtension(outputFile) + ".sup");
                indexFile = null;
            }
            if ((indexFile != null && indexFile.exists() && !indexFile.canWrite()) || (subtitleFile.exists() && !subtitleFile.canWrite())) {
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
            logger.printWarningsAndErrorsAndResetCounters();
            // move captions
            if (configuration.getMoveModeX() != CaptionMoveModeX.KEEP_POSITION || configuration.getMoveModeY() != CaptionMoveModeY.KEEP_POSITION) {
                configuration.setCineBarFactor((1.0 - (16.0 / 9) / options.getScreenRatio()) / 2.0);
                Core.moveAllToBounds();
            }
            // set some values
            if (configuration.isExportForced() && Core.getNumForcedFrames() == 0) {
                throw new CoreException("No forced subtitles found.");
            }
            // write output
            Core.writeSub(outputFile);
        } catch (CoreException ex) {
            logger.error(ex.getMessage());
        } catch (Exception ex) {
            ToolBox.showException(ex);
            logger.error(ex.getMessage());
        }
        // clean up
        logger.printWarningsAndErrorsAndResetCounters();
        Core.exit();

        System.out.println("\nConversion finished.");
        System.exit(0);
    }

    private static void fatalError(String message) {
        Core.exit();
        System.out.println("ERROR: " + message);
        System.exit(1);
    }
}
