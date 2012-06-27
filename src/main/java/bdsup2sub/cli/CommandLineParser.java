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
    private Double delay = Double.valueOf(DEFAULT_PTS_DELAY);
    private ScalingFilter scalingFilter = ScalingFilter.BILINEAR;
    private PaletteMode paletteMode = PaletteMode.CREATE_NEW;
    private Double minimumDisplayTime;
    private Double maximumTimeDifference = Double.valueOf(18000 / 90.0);
    private CaptionMoveModeY moveModeY;
    private Integer moveYOffset;
    private CaptionMoveModeX moveModeX;
    private Double screenRatio;
    private boolean verbose;
    private Integer alphaThreshold = 80;

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
            if (line.hasOption(ALPHA_THRESHOLD)) {
                String value = line.getOptionValue(ALPHA_THRESHOLD);
                alphaThreshold = ToolBox.getInt(value.trim());
                if (alphaThreshold < 0 || alphaThreshold > 255) {
                    throw new ParseException("Illegal number range for alpha threshold: " + value);
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

    public Double getDelay() {
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

    public Double getMaximumTimeDifference() {
        return maximumTimeDifference;
    }

    public CaptionMoveModeY getMoveModeY() {
        return moveModeY;
    }

    public Integer getMoveYOffset() {
        return moveYOffset;
    }

    public Double getScreenRatio() {
        return screenRatio;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public Integer getAlphaThreshold() {
        return alphaThreshold;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar BDSup2Sub <in> <out> [options]", options);
    }
}
