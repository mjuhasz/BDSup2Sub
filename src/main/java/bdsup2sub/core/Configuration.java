/*
 * Copyright 2014 Miklos Juhasz (mjuhasz)
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

import bdsup2sub.tools.Props;
import bdsup2sub.utils.PlatformUtils;
import bdsup2sub.utils.SubtitleUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;


public final class Configuration {

    public static final Color OK_BACKGROUND = new Color(0xffffffff);
    public static final Color WARN_BACKGROUND = new Color(0xffffffc0);
    public static final Color ERROR_BACKGROUND = new Color(0xffe1acac);

    public static final boolean CONVERT_RESOLUTION_BY_DEFAULT = false;
    public static final boolean CONVERT_FRAMERATE_BY_DEFAULT = false;
    public static final int DEFAULT_PTS_DELAY = 0;
    public static final boolean FIX_SHORT_FRAMES_BY_DEFAULT = false;
    public static final int DEFAULT_MIN_DISPLAY_TIME_PTS = 90 * 500;
    public static final boolean APPLY_FREE_SCALE_BY_DEFAULT = false;
    public static final double DEFAULT_FREE_SCALE_FACTOR_X = 1.0;
    public static final double DEFAULT_FREE_SCALE_FACTOR_Y = 1.0;
    public static final double MIN_FREE_SCALE_FACTOR = 0.5;
    public static final double MAX_FREE_SCALE_FACTOR = 2.0;
    public static final double DEFAULT_SOURCE_FRAMERATE = Framerate.FPS_23_976.getValue();  //TODO: dumb default
    public static final double DEFAULT_TARGET_FRAMERATE = Framerate.PAL.getValue(); //TODO: dumb default
    public static final Resolution DEFAULT_TARGET_RESOLUTION = Resolution.PAL; //TODO: dumb default
    public static final ForcedFlagState DEFAULT_FORCED_FLAG_STATE = ForcedFlagState.KEEP;
    public static final int DEFAULT_ALPHA_CROP_THRESHOLD = 14;
    public static final int DEFAULT_ALPHA_THRESHOLD = 80;
    public static final int DEFAULT_LUMINANCE_MED_HIGH_THRESHOLD = 210;
    public static final int DEFAULT_LUMINANCE_LOW_MED_THRESHOLD = 160;
    public static final int DEFAULT_MOVE_Y_OFFSET = 10;
    public static final int DEFAULT_MOVE_X_OFFSET = 10;
    public static final int DEFAULT_CROP_LINE_COUNT = 0;
    //Two equal captions are merged of they are closer than 200ms (0.2*90000 = 18000)
    public static final int DEFAULT_MERGE_PTS_DIFF = 18000;
    public static final OutputMode DEFAULT_OUTPUT_MODE = OutputMode.VOBSUB;

    private boolean convertResolution = CONVERT_RESOLUTION_BY_DEFAULT;
    private boolean convertFPS = CONVERT_FRAMERATE_BY_DEFAULT;
    private int delayPTS = DEFAULT_PTS_DELAY;
    private boolean cliMode = true;
    private boolean fixShortFrames = FIX_SHORT_FRAMES_BY_DEFAULT;
    private int minTimePTS = DEFAULT_MIN_DISPLAY_TIME_PTS;
    private boolean applyFreeScale = APPLY_FREE_SCALE_BY_DEFAULT;
    private double freeScaleFactorX = DEFAULT_FREE_SCALE_FACTOR_X;
    private double freeScaleFactorY = DEFAULT_FREE_SCALE_FACTOR_Y;
    private double fpsSrc = DEFAULT_SOURCE_FRAMERATE;
    private double fpsTrg = DEFAULT_TARGET_FRAMERATE;
    private boolean fpsSrcCertain;
    private Resolution outputResolution = DEFAULT_TARGET_RESOLUTION;
    private int languageIdx;
    private boolean exportForced;

    private int cropOffsetY = DEFAULT_CROP_LINE_COUNT;
    private ForcedFlagState forceAll = DEFAULT_FORCED_FLAG_STATE;
    private boolean swapCrCb;
    private CaptionMoveModeX moveModeX = CaptionMoveModeX.KEEP_POSITION;
    private CaptionMoveModeY moveModeY = CaptionMoveModeY.KEEP_POSITION;
    private int moveOffsetX = DEFAULT_MOVE_X_OFFSET;
    private int moveOffsetY = DEFAULT_MOVE_Y_OFFSET;
    private boolean moveCaptions;
    /** Factor to calculate height of one cinemascope bar from screen height */
    private double cineBarFactor = 5.0/42;
    private StreamID currentStreamID = StreamID.UNKNOWN;
    private boolean keepFps;

    private static final int RECENT_FILE_COUNT = 5;
    private static final String CONFIG_FILE = "bdsup2sup.ini";
    private static final Configuration INSTANCE = new Configuration();
    private final String configFilePath;
    private List<String> recentFiles;
    private int[] luminanceThreshold = {DEFAULT_LUMINANCE_MED_HIGH_THRESHOLD, DEFAULT_LUMINANCE_LOW_MED_THRESHOLD};
    private int alphaThreshold = DEFAULT_ALPHA_THRESHOLD;

    private Props props;

    private Configuration() {
        configFilePath = getConfigFilePath();
        props = new Props();
    }

    public void load() {
        readConfigFile();
        loadConfig();
    }

    private void readConfigFile() {
        props.clear();
        props.setHeader(Constants.APP_NAME + " " + Constants.APP_VERSION + " settings - don't modify manually");
        props.load(configFilePath);
    }

    private void loadConfig() {
        loadRecentFiles();

        convertResolution = loadConvertResolution();
        outputResolution = loadOutputResolution();
        convertFPS = loadConvertFPS();
        fpsSrc = loadFpsSrc();
        fpsTrg = loadFpsTrg();
        delayPTS = loadDelayPTS();
        fixShortFrames = loadFixShortFrames();
        minTimePTS = loadMinTimePTS();
        applyFreeScale = loadApplyFreeScale();
        freeScaleFactorX = loadFreeScaleFactorX();
        freeScaleFactorY = loadFreeScaleFactorY();
        forceAll = loadForceAll();
    }

    public void storeConfig() {
        if (!cliMode) {
            props.save(configFilePath);
        }
    }

    private void loadRecentFiles() {
        recentFiles = new ArrayList<String>();
        int i = 0;
        String filename;
        while (i < RECENT_FILE_COUNT && (filename = props.get("recent_" + i, "")).length() > 0) {
            if (new File(filename).exists()) {
                recentFiles.add(filename);
            }
            i++;
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getConfigFilePath() {
        if (PlatformUtils.isLinux()) {
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
            File configFileDir = new File((xdgConfigHome != null ? xdgConfigHome : System.getProperty("user.home") + "/.config") + "/bdsup2sub");
            if (!configFileDir.exists()) {
                configFileDir.mkdir();
            }
            return configFileDir + "/" + CONFIG_FILE;
        } else if (PlatformUtils.isMac()) {
            File configFileDir = new File(System.getProperty("user.home") + "/Library/Application Support/bdsup2sub");
            if (!configFileDir.exists()) {
                configFileDir.mkdir();
            }
            return configFileDir + "/" + CONFIG_FILE;
        } else {
            File configFileDir = new File(System.getProperty("user.home") + "/bdsup2sub");
            if (!configFileDir.exists()) {
                configFileDir.mkdir();
            }
            return configFileDir + "/" + CONFIG_FILE;
        }
    }
    
    public boolean isCliMode() {
        return cliMode;
    }

    public void setCliMode(boolean cliMode) {
        this.cliMode = cliMode;
    }

    public boolean isVerbose() {
        return props.get("verbose", false);
    }

    public void setVerbose(boolean verbose) {
        props.set("verbose", verbose);
    }

    /**
     * Write PGCEdit palette file on export.
     */
    public boolean getWritePGCEditPalette() {
        return props.get("writePGCEditPal", false);
    }

    public void setWritePGCEditPalette(boolean writePGCEditPalette) {
        props.set("writePGCEditPal", writePGCEditPalette);
    }

    /**
     * Get maximum time difference for merging captions.
     */
    public int getMergePTSdiff() {
        return props.get("mergePTSdiff", DEFAULT_MERGE_PTS_DIFF);
    }

    public void setMergePTSdiff(int mergePTSdiff) {
        props.set("mergePTSdiff", mergePTSdiff);
    }

    /**
     * Get alpha threshold for cropping.
     */
    public int getAlphaCrop() {
        return props.get("alphaCrop", DEFAULT_ALPHA_CROP_THRESHOLD);
    }

    public void setAlphaCrop(int alphaCrop) {
        props.set("alphaCrop", alphaCrop);
    }

    /**
     * Fix completely invisible subtitles due to alpha=0 (SUB/IDX and SUP/IFO import only).
     */
    public boolean getFixZeroAlpha() {
        return props.get("fixZeroAlpha", false);
    }

    public void setFixZeroAlpha(boolean fixZeroAlpha) {
        props.set("fixZeroAlpha", fixZeroAlpha);
    }

    public ScalingFilter getScalingFilter() {
        ScalingFilter defaultScalingFilter = ScalingFilter.BILINEAR;
        try {
            return ScalingFilter.valueOf(props.get("filter", defaultScalingFilter.name()));
        } catch (IllegalArgumentException ex) {
            return defaultScalingFilter;
        }
    }

    public void setScalingFilter(ScalingFilter filter) {
        props.set("filter", filter.name());
    }

    /**
     * Get palette creation mode.
     */
    public PaletteMode getPaletteMode() {
        PaletteMode defaultPaletteMode = PaletteMode.CREATE_NEW;
        try {
            return PaletteMode.valueOf(props.get("paletteMode", defaultPaletteMode.name()));
        } catch (IllegalArgumentException ex) {
            return defaultPaletteMode;
        }
    }

    public void setPaletteMode(PaletteMode paletteMode) {
        props.set("paletteMode", paletteMode.name());
    }

    public OutputMode getOutputMode() {
        try {
            return OutputMode.valueOf(props.get("outputMode", DEFAULT_OUTPUT_MODE.name()));
        } catch (IllegalArgumentException ex) {
            return DEFAULT_OUTPUT_MODE;
        }
    }
    
    public void setOutputMode(OutputMode outputMode) {
        props.set("outputMode", outputMode.name());
    }

    public String getLoadPath() {
        return props.get("loadPath", "");
    }

    public void setLoadPath(String loadPath) {
        props.set("loadPath", loadPath);
    }

    public String getColorProfilePath() {
        return props.get("colorPath", "");
    }

    public void setColorProfilePath(String colorProfilePath) {
        props.set("colorPath", colorProfilePath);
    }

    public Dimension getMainWindowSize() {
        return new Dimension(props.get("frameWidth", 800), props.get("frameHeight", 600));
    }

    public void setMainWindowSize(Dimension dimension) {
        props.set("frameWidth", dimension.width);
        props.set("frameHeight", dimension.height);
    }

    public Point getMainWindowLocation() {
        return new Point(props.get("framePosX", -1), props.get("framePosY", -1));
    }

    public void setMainWindowLocation(Point location) {
        props.set("framePosX", location.x);
        props.set("framePosY", location.y);
    }

    public List<String> getRecentFiles() {
        return recentFiles;
    }

    public void addToRecentFiles(String filename) {
        int index = recentFiles.indexOf(filename);
        if (index != -1) {
            recentFiles.remove(index);
            recentFiles.add(0, filename);
        } else {
            recentFiles.add(0, filename);
            if (recentFiles.size() > RECENT_FILE_COUNT) {
                recentFiles.remove(recentFiles.size() - 1);
            }
        }
        for (int i=0; i < recentFiles.size(); i++) {
            props.set("recent_" + i, recentFiles.get(i));
        }
    }

    public boolean getConvertResolution() {
        return convertResolution;
    }

    public void setConvertResolution(boolean convertResolution) {
        this.convertResolution = convertResolution;
    }

    public boolean loadConvertResolution() {
        return props.get("convertResolution", CONVERT_RESOLUTION_BY_DEFAULT);
    }

    public void storeConvertResolution(boolean convertResolution) {
        props.set("convertResolution", convertResolution);
    }

    /**
     * Get flag that tells whether or not to convert the frame rate.
     */
    public boolean getConvertFPS() {
        return convertFPS;
    }

    public void setConvertFPS(boolean convertFPS) {
        this.convertFPS = convertFPS;
    }

    public boolean loadConvertFPS() {
        return props.get("convertFPS", CONVERT_FRAMERATE_BY_DEFAULT);
    }

    public void storeConvertFPS(boolean convertFPS) {
        props.set("convertFPS", convertFPS);
    }

    /**
     * Get Delay to apply to target in 90kHz resolution.
     */
    public int getDelayPTS() {
        return delayPTS;
    }

    public void setDelayPTS(int delayPTS) {
        this.delayPTS = delayPTS;
    }

    public int loadDelayPTS() {
        return props.get("delayPTS", DEFAULT_PTS_DELAY);
    }

    public void storeDelayPTS(int delayPTS) {
        props.set("delayPTS", delayPTS);
    }

    /**
     * Set flag that tells whether to fix subpictures with a display time shorter than minTimePTS.
     */
    public boolean getFixShortFrames() {
        return fixShortFrames;
    }

    public void setFixShortFrames(boolean fixShortFrames) {
        this.fixShortFrames = fixShortFrames;
    }

    public boolean loadFixShortFrames() {
        return props.get("fixShortFrames", FIX_SHORT_FRAMES_BY_DEFAULT);
    }

    public void storeFixShortFrames(boolean fixShortFrames) {
        props.set("fixShortFrames",  fixShortFrames);
    }

    /**
     * Get minimum display time for subpictures in 90kHz resolution
     */
    public int getMinTimePTS() {
        return minTimePTS;
    }

    public void setMinTimePTS(int minTimePTS) {
        this.minTimePTS = minTimePTS;
    }

    public int loadMinTimePTS() {
        return props.get("minTimePTS", DEFAULT_MIN_DISPLAY_TIME_PTS);
    }

    public void storeMinTimePTS(int minTimePTS) {
        props.set("minTimePTS", minTimePTS);
    }

    public boolean getApplyFreeScale() {
        return applyFreeScale;
    }

    public void setApplyFreeScale(boolean applyFreeScale) {
        this.applyFreeScale = applyFreeScale;
    }

    public boolean loadApplyFreeScale() {
        return props.get("applyFreeScale", APPLY_FREE_SCALE_BY_DEFAULT);
    }

    public void storeApplyFreeScale(boolean applyFreeScale) {
        props.set("applyFreeScale", applyFreeScale);
    }

    public double getFreeScaleFactorX() {
        return freeScaleFactorX;
    }

    public double getFreeScaleFactorY() {
        return freeScaleFactorY;
    }

    public void setFreeScaleFactor(double x, double y) {
        if (x < MIN_FREE_SCALE_FACTOR) {
            x = MIN_FREE_SCALE_FACTOR;
        } else if (x > MAX_FREE_SCALE_FACTOR) {
            x = MAX_FREE_SCALE_FACTOR;
        }
        freeScaleFactorX = x;

        if (y < MIN_FREE_SCALE_FACTOR) {
            y = MIN_FREE_SCALE_FACTOR;
        } else if (y > MAX_FREE_SCALE_FACTOR) {
            y = MAX_FREE_SCALE_FACTOR;
        }
        freeScaleFactorY = y;
    }

    public double loadFreeScaleFactorX() {
        return props.get("freeScaleX", DEFAULT_FREE_SCALE_FACTOR_X);
    }

    public double loadFreeScaleFactorY() {
        return props.get("freeScaleY", DEFAULT_FREE_SCALE_FACTOR_Y);
    }

    public void storeFreeScaleFactor(double x, double y) {
        if (x < MIN_FREE_SCALE_FACTOR) {
            x = MIN_FREE_SCALE_FACTOR;
        } else if (x > MAX_FREE_SCALE_FACTOR) {
            x = MAX_FREE_SCALE_FACTOR;
        }
        props.set("freeScaleX", x);

        if (y < MIN_FREE_SCALE_FACTOR) {
            y = MIN_FREE_SCALE_FACTOR;
        } else if (y > MAX_FREE_SCALE_FACTOR) {
            y = MAX_FREE_SCALE_FACTOR;
        }
        props.set("freeScaleY", y);
    }

    public double getFPSSrc() {
        return fpsSrc;
    }

    public void setFpsSrc(double fpsSrc) {
        this.fpsSrc = fpsSrc;
    }

    public double loadFpsSrc() {
        return SubtitleUtils.getFps(props.get("fpsSrc", String.valueOf(DEFAULT_SOURCE_FRAMERATE)));
    }

    public void storeFPSSrc(double fpsSrc) {
        props.set("fpsSrc", fpsSrc);
    }

    public double getFpsTrg() {
        return fpsTrg;
    }

    public void setFpsTrg(double fpsTrg) {
        this.fpsTrg = fpsTrg;
        setDelayPTS((int)SubtitleUtils.syncTimePTS(getDelayPTS(), fpsTrg, fpsTrg));
        setMinTimePTS((int)SubtitleUtils.syncTimePTS(getMinTimePTS(), fpsTrg, fpsTrg));
    }

    public double loadFpsTrg() {
        return SubtitleUtils.getFps(props.get("fpsTrg", String.valueOf(DEFAULT_TARGET_FRAMERATE)));
    }

    public void storeFpsTrg(double fpsTrg) {
        props.set("fpsTrg", fpsTrg);
    }

    public boolean isFpsSrcCertain() {
        return fpsSrcCertain;
    }

    public void setFpsSrcCertain(boolean fpsSrcCertain) {
        this.fpsSrcCertain = fpsSrcCertain;
    }

    public Resolution getOutputResolution() {
        return outputResolution;
    }

    public void setOutputResolution(Resolution outputResolution) {
        this.outputResolution = outputResolution;
    }

    public Resolution loadOutputResolution() {
        try {
            return Resolution.valueOf(props.get("resolutionTrg", DEFAULT_TARGET_RESOLUTION.name()));
        } catch (IllegalArgumentException e) {
            return DEFAULT_TARGET_RESOLUTION;
        }
    }

    public void storeOutputResolution(Resolution outputResolution) {
        props.set("resolutionTrg", outputResolution.name());
    }

    public int getAlphaThreshold() {
        return alphaThreshold;
    }

    public void setAlphaThreshold(int alphaThreshold) {
        this.alphaThreshold = alphaThreshold;
    }

    /**
     * Array of luminance thresholds ( 0: med/high, 1: low/med )
     */
    public int[] getLuminanceThreshold() {
        return luminanceThreshold;
    }

    public void setLuminanceThreshold(int[] luminanceThreshold) {
        this.luminanceThreshold = luminanceThreshold;
    }

    /**
     * Index of language to be used for SUB/IDX export (also used for XML export)
     */
    public int getLanguageIdx() {
        return languageIdx;
    }

    public void setLanguageIdx(int languageIdx) {
        this.languageIdx = languageIdx;
    }

    /**
     * Flag that defines whether to export only subpictures marked as "forced"
     */
    public boolean isExportForced() {
        return exportForced;
    }

    public void setExportForced(boolean exportForced) {
        this.exportForced = exportForced;
    }

    public int getCropOffsetY() {
        return cropOffsetY;
    }

    public void setCropOffsetY(int cropOffsetY) {
        this.cropOffsetY = cropOffsetY;
    }

    public ForcedFlagState getForceAll() {
        return forceAll;
    }

    public ForcedFlagState loadForceAll() {
        try {
            return ForcedFlagState.valueOf(props.get("forceAll", DEFAULT_FORCED_FLAG_STATE.name()));
        } catch (IllegalArgumentException ex) {
            return DEFAULT_FORCED_FLAG_STATE;
        }
    }

    public void setForceAll(ForcedFlagState forceAll) {
        this.forceAll = forceAll;
    }

    public void storeForceAll(ForcedFlagState forceAll) {
        props.set("forceAll", forceAll.name());
    }

    public boolean isSwapCrCb() {
        return swapCrCb;
    }

    public void setSwapCrCb(boolean swapCrCb) {
        this.swapCrCb = swapCrCb;
    }

    public void setMoveModeY(CaptionMoveModeY moveModeY) {
        this.moveModeY = moveModeY;
        this.moveCaptions = (moveModeY != CaptionMoveModeY.KEEP_POSITION) || (moveModeX != CaptionMoveModeX.KEEP_POSITION);
    }

    public CaptionMoveModeY getMoveModeY() {
        return moveModeY;
    }

    public void setMoveModeX(CaptionMoveModeX moveModeX) {
        this.moveModeX = moveModeX;
        this.moveCaptions = (moveModeY != CaptionMoveModeY.KEEP_POSITION) || (moveModeX != CaptionMoveModeX.KEEP_POSITION);
    }

    public CaptionMoveModeX getMoveModeX() {
        return moveModeX;
    }

    public void setMoveOffsetY(int moveOffsetY) {
        this.moveOffsetY = moveOffsetY;
    }

    public int getMoveOffsetY() {
        return moveOffsetY;
    }

    public void setMoveOffsetX(int moveOffsetX) {
        this.moveOffsetX = moveOffsetX;
    }

    public int getMoveOffsetX() {
        return moveOffsetX;
    }

    /**
     * Get: keep move settings after loading a new stream
     * @return true: keep settings, false: ignore settings
     */
    public boolean getMoveCaptions() {
        return moveCaptions;
    }

    /**
     * Set: keep move settings after loading a new stream
     * @param moveCaptions true: keep settings, false; ignore settings
     */
    public void setMoveCaptions(boolean moveCaptions) {
        this.moveCaptions = moveCaptions;
    }

    public double getCineBarFactor() {
        return cineBarFactor;
    }

    public void setCineBarFactor(double cineBarFactor) {
        this.cineBarFactor = cineBarFactor;
    }

    public StreamID getCurrentStreamID() {
        return currentStreamID;
    }

    public void setCurrentStreamID(StreamID currentStreamID) {
        this.currentStreamID = currentStreamID;
    }

    public boolean isKeepFps() {
        return keepFps;
    }

    public void setKeepFps(boolean keepFps) {
        this.keepFps = keepFps;
    }
}
