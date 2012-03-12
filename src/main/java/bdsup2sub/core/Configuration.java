/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
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
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.SubtitleUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

import static bdsup2sub.core.Constants.APP_NAME_AND_VERSION;

public final class Configuration {

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
    public static final double DEFAULT_SOURCE_FRAMERATE = Framerate.FPS_23_976.getValue();
    public static final double DEFAULT_TARGET_FRAMERATE = Framerate.PAL.getValue();
    public static final Resolution DEFAULT_TARGET_RESOLUTION = Resolution.PAL;

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

    private static final int RECENT_FILE_COUNT = 5;
    private static final String CONFIG_FILE = "bdsup2sup.ini";
    private static final Configuration INSTANCE = new Configuration();
    private final String configFilePath;
    private List<String> recentFiles;
    private int[] luminanceThreshold = {210, 160};
    private int alphaThreshold = 80;
    private Props props;

    private Configuration() {
        configFilePath = workOutConfigFilePath();
        readConfigFile();
        loadConfig();
    }

    private void readConfigFile() {
        props = new Props();
        props.setHeader(APP_NAME_AND_VERSION + " settings - don't modify manually");
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
    }

    public void storeConfig() {
        props.save(configFilePath);
    }

    private void loadRecentFiles() {
        recentFiles = new ArrayList<String>();
        int i = 0;
        String filename;
        while (i < RECENT_FILE_COUNT && (filename = props.get("recent_" + i, "")).length() > 0) {
            recentFiles.add(filename);
            i++;
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    private String workOutConfigFilePath() {
        String relPathToClassFile = Configuration.class.getName().replace('.','/') + ".class";
        String absPathToClassFile = Configuration.class.getClassLoader().getResource(relPathToClassFile).getPath();

        int pos = absPathToClassFile.toLowerCase().indexOf(relPathToClassFile.toLowerCase());
        String configFileDir = absPathToClassFile.substring(0, pos);

        if (configFileDir.startsWith("file:")) {
            configFileDir = configFileDir.substring("file:".length());
        }

        configFileDir = FilenameUtils.separatorsToUnix(configFileDir);
        pos = configFileDir.lastIndexOf(".jar");
        if (pos != -1) {
            pos = configFileDir.substring(0, pos).lastIndexOf('/');
            if (pos != -1) {
                configFileDir = configFileDir.substring(0, pos + 1);
            }
        }
        return configFileDir + CONFIG_FILE;
    }
    
    public boolean isCliMode() {
        return cliMode;
    }

    public void setCliMode(boolean cliMode) {
        this.cliMode = cliMode;
    }

    /**
     * Verbatim output
     */
    public boolean isVerbatim() {
        return props.get("verbatim", false);
    }

    public void setVerbatim(boolean verbatim) {
        props.set("verbatim", verbatim);
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
        //Two equal captions are merged of they are closer than 200ms (0.2*90000 = 18000)
        return props.get("mergePTSdiff", 18000);
    }

    public void setMergePTSdiff(int mergePTSdiff) {
        props.set("mergePTSdiff", mergePTSdiff);
    }

    /**
     * Get alpha threshold for cropping.
     */
    public int getAlphaCrop() {
        return props.get("alphaCrop", 14);
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
        OutputMode defaultOutputMode = OutputMode.VOBSUB;
        try {
            return OutputMode.valueOf(props.get("outputMode", defaultOutputMode.name()));
        } catch (IllegalArgumentException ex) {
            return defaultOutputMode;
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
        return props.get("convertFPS", convertFPS);
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
        return props.get("delayPTS", delayPTS);
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
        return props.get("fixShortFrames", fixShortFrames);
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
        return props.get("minTimePTS", minTimePTS);
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
        return props.get("applyFreeScale", applyFreeScale);
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
        return props.get("freeScaleX", freeScaleFactorX);
    }

    public double loadFreeScaleFactorY() {
        return props.get("freeScaleY", freeScaleFactorY);
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

    public void setFPSSrc(double fpsSrc) {
        this.fpsSrc = fpsSrc;
    }

    public double loadFpsSrc() {
        return SubtitleUtils.getFPS(props.get("fpsSrc", String.valueOf(fpsSrc)));
    }

    public void storeFPSSrc(double fpsSrc) {
        props.set("fpsSrc", fpsSrc);
    }

    public double getFPSTrg() {
        return fpsTrg;
    }

    public void setFPSTrg(double fpsTrg) {
        this.fpsTrg = fpsTrg;
        setDelayPTS((int)SubtitleUtils.syncTimePTS(getDelayPTS(), fpsTrg, fpsTrg));
        setMinTimePTS((int)SubtitleUtils.syncTimePTS(getMinTimePTS(), fpsTrg, fpsTrg));
    }

    public double loadFpsTrg() {
        return SubtitleUtils.getFPS(props.get("fpsTrg", String.valueOf(fpsTrg)));
    }

    public void storeFPSTrg(double fpsTrg) {
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
            return Resolution.valueOf(props.get("resolutionTrg", outputResolution.name()));
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
}
