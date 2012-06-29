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
package bdsup2sub.cli;

import bdsup2sub.core.*;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;
import org.apache.commons.cli.*;

import java.io.File;

import static bdsup2sub.cli.CommandLineOptions.*;
import static bdsup2sub.core.Configuration.*;
import static bdsup2sub.core.Constants.LANGUAGES;

public class CommandLineParser {

    private boolean printHelpMode;
    private boolean printVersionMode;
    private Resolution resolution;
    private Double sourceFrameRate;
    private Double targetFrameRate;
    private double delay = DEFAULT_PTS_DELAY;
    private ScalingFilter scalingFilter = ScalingFilter.BILINEAR;
    private PaletteMode paletteMode = PaletteMode.CREATE_NEW;
    private Double minimumDisplayTime;
    private double maximumTimeDifference = DEFAULT_MERGE_PTS_DIFF / 90.0;
    private CaptionMoveModeY moveModeY = CaptionMoveModeY.KEEP_POSITION;
    private int moveYOffset = DEFAULT_MOVE_Y_OFFSET;
    private CaptionMoveModeX moveModeX = CaptionMoveModeX.KEEP_POSITION;
    private Double screenRatio;
    private int moveXOffset = DEFAULT_MOVE_X_OFFSET;
    private int cropLines = DEFAULT_CROP_LINE_COUNT;
    private int alphaCropThreshold = DEFAULT_ALPHA_CROP_THRESHOLD;
    private double scaleX = DEFAULT_FREE_SCALE_FACTOR_X;
    private double scaleY = DEFAULT_FREE_SCALE_FACTOR_Y;
    private boolean exportPalette;
    private boolean exportForcedSubtitlesOnly;
    private ForcedFlagState forcedFlagState = ForcedFlagState.KEEP;
    private boolean swapCrCb;
    private boolean fixInvisibleFrames;
    private boolean verbose;
    private int alphaThreshold = DEFAULT_ALPHA_THRESHOLD;
    private int lumLowMidThreshold = DEFAULT_LUMINANCE_LOW_MID_THRESHOLD;
    private int lumMidHighThreshold =  DEFAULT_LUMINANCE_MID_HIGH_THRESHOLD;
    private int languageIndex;
    private File paletteFile;

    private Options options;

    public CommandLineParser() {
        this.options = new CommandLineOptions().getOptions();
    }

    public void parse(String... args) throws ParseException {
        CommandLine line = new PosixParser().parse(options, args);

        if (line.hasOption(HELP)) {
            printHelpMode = true;
        } else if (line.hasOption(VERSION)) {
            printVersionMode = true;
        } else {
            if (line.hasOption(RESOLUTION)) {
                String value = line.getOptionValue(RESOLUTION);
                if (value.equalsIgnoreCase("keep")) {
                    resolution = null;
                } else if (value.equalsIgnoreCase("pal") || value.equalsIgnoreCase("576")) {
                    resolution = Resolution.PAL;
                } else if (value.equalsIgnoreCase("ntsc") || value.equalsIgnoreCase("480")) {
                    resolution = Resolution.NTSC;
                } else if (value.equalsIgnoreCase("720p") || value.equalsIgnoreCase("720")) {
                    resolution = Resolution.HD_720;
                } else if (value.equalsIgnoreCase("1440x1080")) {
                    resolution = Resolution.HD_1440x1080;
                } else if (value.equalsIgnoreCase("1080p") || value.equalsIgnoreCase("1080")) {
                    resolution = Resolution.HD_1080;
                } else {
                    throw new ParseException("Illegal resolution: " + value);
                }
            }

            if (line.hasOption(CONVERT_FRAMERATE)) {
                if (line.getOptionValues(CONVERT_FRAMERATE).length != 2) {
                    throw new ParseException("2 arguments needed for framerate conversion.");
                }
                String value = line.getOptionValues(CONVERT_FRAMERATE)[0];
                if (value.equalsIgnoreCase("auto")) {
                    sourceFrameRate = null;
                } else {
                    sourceFrameRate = SubtitleUtils.getFps(value);
                    if (sourceFrameRate <= 0) {
                        throw new ParseException("Invalid source framerate: " + value);
                    }
                }
                value = line.getOptionValues(CONVERT_FRAMERATE)[1];
                targetFrameRate = SubtitleUtils.getFps(value);
                if (targetFrameRate <= 0) {
                    throw new ParseException("Invalid target framerate: " + value);
                }
            }

            if (line.hasOption(DELAY)) {
                String value = line.getOptionValue(DELAY);
                try {
                    delay = Double.parseDouble(value.trim());
                } catch (NumberFormatException ex) {
                    throw new ParseException("Illegal delay value: " + value);
                }
            }

            if (line.hasOption(SCALING_FILTER)) {
                String value = line.getOptionValue(SCALING_FILTER);
                boolean found = false;
                for (ScalingFilter f : ScalingFilter.values()) {
                    if (f.toString().equalsIgnoreCase(value)) {
                        scalingFilter = f;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new ParseException("Illegal scaling filter value: " + value);
                }
            }

            if (line.hasOption(PALETTE_MODE)) {
                String value = line.getOptionValue(PALETTE_MODE);
                if (value.equalsIgnoreCase("keep")) {
                    paletteMode = PaletteMode.KEEP_EXISTING;
                } else if (value.equalsIgnoreCase("create")) {
                    paletteMode = PaletteMode.CREATE_NEW;
                } else if (value.equalsIgnoreCase("dither")) {
                    paletteMode = PaletteMode.CREATE_DITHERED;
                } else {
                    throw new ParseException("Invalid palette mode: " + value);
                }
            }
            if (line.hasOption(MIN_DISPLAY_TIME)) {
                String value = line.getOptionValue(MIN_DISPLAY_TIME);
                try {
                    minimumDisplayTime = Double.parseDouble(value.trim());
                } catch (NumberFormatException ex) {
                    throw new ParseException("Illegal minimum display time value: " + value);
                }
                if (minimumDisplayTime <= 0) {
                    throw new ParseException("Illegal minimum display time value: " + value);
                }
            }
            if (line.hasOption(MAX_TIME_DIFF)) {
                String value = line.getOptionValue(MAX_TIME_DIFF);
                try {
                    maximumTimeDifference = Double.parseDouble(value.trim());
                } catch (NumberFormatException ex) {
                    throw new ParseException("Illegal maximum merge time difference value: " + value);
                }
                if (maximumTimeDifference < 0) {
                    throw new ParseException("Illegal maximum merge time difference value: " + value);
                }
            }
            if (line.hasOption(MOVE_IN) && line.hasOption(MOVE_OUT)) {
                throw new ParseException("Incompatible options: " + MOVE_IN + ", " + MOVE_OUT);
            }
            if (line.hasOption(MOVE_IN) || line.hasOption(MOVE_OUT)) {
                moveModeY = line.hasOption(MOVE_IN) ? CaptionMoveModeY.MOVE_INSIDE_BOUNDS : CaptionMoveModeY.MOVE_OUTSIDE_BOUNDS;
                String option = line.hasOption(MOVE_IN) ? MOVE_IN : MOVE_OUT;
                if (line.getOptionValues(option).length != 2) {
                    throw new ParseException("2 arguments needed for moving captions.");
                }
                screenRatio = ToolBox.getDouble(line.getOptionValues(option)[0]);
                if (screenRatio <= (16.0 / 9)) {
                    throw new ParseException("Invalid screen ratio: " + screenRatio);
                }
                moveYOffset = ToolBox.getInt(line.getOptionValues(option)[1]);
                if (moveYOffset < 0) {
                    throw new ParseException("Invalid pixel offset: " + moveYOffset);
                }
            }
            if (line.hasOption(MOVE_X)) {
                if (line.getOptionValues(MOVE_X) == null || line.getOptionValues(MOVE_X).length < 1) {
                    throw new ParseException("Missing argument for moving captions.");
                }
                String value = line.getOptionValues(MOVE_X)[0];
                if (value.equalsIgnoreCase("left")) {
                    moveModeX = CaptionMoveModeX.LEFT;
                } else if (value.equalsIgnoreCase("center")) {
                    moveModeX = CaptionMoveModeX.CENTER;
                } else if (value.equalsIgnoreCase("right")) {
                    moveModeX = CaptionMoveModeX.RIGHT;
                } else {
                    throw new ParseException("Invalid move mode: " + value);
                }

                if ((moveModeX == CaptionMoveModeX.LEFT || moveModeX == CaptionMoveModeX.RIGHT) && line.getOptionValues(MOVE_X).length > 1) {
                    value = line.getOptionValues(MOVE_X)[1];
                    moveXOffset = ToolBox.getInt(value);
                    if (moveXOffset < 0) {
                        throw new ParseException("Invalid pixel offset: " + value);
                    }
                }
            }
            if (line.hasOption(CROP_LINES)) {
                String value = line.getOptionValue(CROP_LINES);
                cropLines = ToolBox.getInt(value.trim());
                if (cropLines < 0) {
                    throw new ParseException("Invalid crop lines value: " + value);
                }
            }
            if (line.hasOption(ALPHA_CROP_THRESHOLD)) {
                String value = line.getOptionValue(ALPHA_CROP_THRESHOLD);
                alphaCropThreshold = ToolBox.getInt(value.trim());
                if (alphaCropThreshold < 0 || alphaCropThreshold > 255) {
                    throw new ParseException("Illegal number range for alpha cropping threshold: " + value);
                }
            }
            if (line.hasOption(SCALE)) {
                if (line.getOptionValues(SCALE).length != 2) {
                    throw new ParseException("2 arguments needed for scaling.");
                }
                String value = line.getOptionValues(SCALE)[0];
                scaleX = ToolBox.getDouble(value);
                if (scaleX < MIN_FREE_SCALE_FACTOR || scaleX > MAX_FREE_SCALE_FACTOR) {
                    throw new ParseException("Invalid x scaling factor: " + value);
                }
                value = line.getOptionValues(SCALE)[1];
                scaleY = ToolBox.getDouble(value);
                if (scaleY < MIN_FREE_SCALE_FACTOR || scaleY > MAX_FREE_SCALE_FACTOR) {
                    throw new ParseException("Invalid y scaling factor: " + value);
                }
            }
            exportPalette = line.hasOption(EXPORT_PALETTE);
            exportForcedSubtitlesOnly = line.hasOption(EXPORT_FORCED_SUBTITLES_ONLY);

            if (line.hasOption(FORCED_FLAG)) {
                String value = line.getOptionValue(FORCED_FLAG);
                if (value.equalsIgnoreCase("keep")) {
                    forcedFlagState = ForcedFlagState.KEEP;
                } else if (value.equalsIgnoreCase("set")) {
                    forcedFlagState = ForcedFlagState.SET;
                } else if (value.equalsIgnoreCase("clear")) {
                    forcedFlagState = ForcedFlagState.CLEAR;
                } else {
                    throw new ParseException("Invalid forced flag state: " + value);
                }
            }
            swapCrCb = line.hasOption(SWAP_CR_CB);
            fixInvisibleFrames = line.hasOption(FIX_INVISIBLE_FRAMES); // TODO: accept only for SUB/IDX or SUP/IFO as target
            verbose = line.hasOption(VERBOSE);
            if (line.hasOption(ALPHA_THRESHOLD)) { // TODO: accept only for SUB/IDX or SUP/IFO as target
                String value = line.getOptionValue(ALPHA_THRESHOLD);
                alphaThreshold = ToolBox.getInt(value.trim());
                if (alphaThreshold < 0 || alphaThreshold > 255) {
                    throw new ParseException("Illegal number range for alpha threshold: " + value);
                }
            }
            if (line.hasOption(LUM_LOW_MID_THRESHOLD)) { // TODO: accept only for SUB/IDX or SUP/IFO as target
                String value = line.getOptionValue(LUM_LOW_MID_THRESHOLD);
                lumLowMidThreshold = ToolBox.getInt(value.trim());
                if (lumLowMidThreshold < 0 || lumLowMidThreshold > 255) {
                    throw new ParseException("Illegal number range for luminance threshold: " + value);
                }
            }
            if (line.hasOption(LUM_MID_HIGH_THRESHOLD)) { // TODO: accept only for SUB/IDX or SUP/IFO as target
                String value = line.getOptionValue(LUM_MID_HIGH_THRESHOLD);
                lumMidHighThreshold = ToolBox.getInt(value.trim());
                if (lumMidHighThreshold < 0 || lumMidHighThreshold > 255) {
                    throw new ParseException("Illegal number range for luminance threshold: " + value);
                }
            }
            if (lumLowMidThreshold > lumMidHighThreshold) {
                throw new ParseException("Invalid luminance threshold values: " + lumLowMidThreshold + ", " + lumMidHighThreshold);
            }
            if (line.hasOption(LANGUAGE_CODE)) { //TODO: only sub/idx
                String value = line.getOptionValue(LANGUAGE_CODE);
                boolean found = false;
                for (int i = 0; i < LANGUAGES.length; i++)
                    if (LANGUAGES[i][1].equalsIgnoreCase(value)) {
                        languageIndex = i;
                        found = true;
                        break;
                    }
                if (!found) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unknown language code: " + value + "\n");
                    sb.append("Use one of the following 2 character codes:\n");
                    for (String[] language : LANGUAGES) {
                        sb.append("    " + language[1] + " - " + language[0] + "\n");
                    }
                    throw new ParseException(sb.toString());
                }
            }
            if (line.hasOption(PALETTE_FILE)) { //TODO: only sub/idx
                String value = line.getOptionValue(PALETTE_FILE);
                paletteFile = new File(value);
                if (!paletteFile.exists()) {
                    throw new ParseException("Palette file not found: " + value);
                } else {
                    byte id[] = ToolBox.getFileID(value, 4);
                    if (id == null || id[0] != 0x23 || id[1] != 0x43 || id[2] != 0x4F || id[3] != 0x4C) { //#COL
                        throw new ParseException("Invalid palette file: " + value);
                    }
                }
            }
        }
    }

    public boolean isPrintHelpMode() {
        return printHelpMode;
    }

    public boolean isPrintVersionMode() {
        return printVersionMode;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public Double getSourceFrameRate() {
        return sourceFrameRate;
    }

    public Double getTargetFrameRate() {
        return targetFrameRate;
    }

    public double getDelay() {
        return delay;
    }

    public ScalingFilter getScalingFilter() {
        return scalingFilter;
    }

    public PaletteMode getPaletteMode() {
        return paletteMode;
    }

    public Double getMinimumDisplayTime() {
        return minimumDisplayTime;
    }

    public double getMaximumTimeDifference() {
        return maximumTimeDifference;
    }

    public CaptionMoveModeY getMoveModeY() {
        return moveModeY;
    }

    public int getMoveYOffset() {
        return moveYOffset;
    }

    public CaptionMoveModeX getMoveModeX() {
        return moveModeX;
    }

    public int getMoveXOffset() {
        return moveXOffset;
    }

    public Double getScreenRatio() {
        return screenRatio;
    }

    public int getCropLines() {
        return cropLines;
    }

    public int getAlphaCropThreshold() {
        return alphaCropThreshold;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public boolean isExportPalette() {
        return exportPalette;
    }

    public boolean isExportForcedSubtitlesOnly() {
        return exportForcedSubtitlesOnly;
    }

    public ForcedFlagState getForcedFlagState() {
        return forcedFlagState;
    }

    public boolean isSwapCrCb() {
        return swapCrCb;
    }

    public boolean isFixInvisibleFrames() {
        return fixInvisibleFrames;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public int getAlphaThreshold() {
        return alphaThreshold;
    }

    public int getLumLowMidThreshold() {
        return lumLowMidThreshold;
    }

    public int getLumMidHighThreshold() {
        return lumMidHighThreshold;
    }

    public int getLanguageIndex() {
        return languageIndex;
    }

    public File getPaletteFile() {
        return paletteFile;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar BDSup2Sub <in> <out> [options]", options);
    }
}
