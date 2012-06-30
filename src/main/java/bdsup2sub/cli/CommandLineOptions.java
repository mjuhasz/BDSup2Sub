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

import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CommandLineOptions {

    static final String HELP = "h";
    static final String VERSION = "V";
    static final String OUTPUT_FILE = "o";
    static final String VERBOSE = "v";
    static final String LOAD_SETTINGS = "L";

    static final String RESOLUTION = "r";
    static final String CONVERT_FRAMERATE = "C";
    static final String DELAY = "d";
    static final String SCALING_FILTER = "f";
    static final String PALETTE_MODE = "p";
    static final String MIN_DISPLAY_TIME = "m";
    static final String MAX_TIME_DIFF = "x";
    static final String MOVE_IN = "I";
    static final String MOVE_OUT = "O";
    static final String MOVE_X = "X";
    static final String CROP_LINES = "c";
    static final String ALPHA_CROP_THRESHOLD = "a";
    static final String SCALE = "S";
    static final String EXPORT_PALETTE = "P";
    static final String EXPORT_FORCED_SUBTITLES_ONLY = "D";
    static final String FORCED_FLAG = "F";
    static final String SWAP_CR_CB = "w";
    static final String FIX_INVISIBLE_FRAMES = "i";

    static final String ALPHA_THRESHOLD = "A";
    static final String LUM_LOW_MID_THRESHOLD = "M";
    static final String LUM_MID_HIGH_THRESHOLD = "H";
    static final String LANGUAGE_CODE = "l";
    static final String PALETTE_FILE = "t";

    static final List<String> OPTION_ORDER = Arrays.asList(HELP, LOAD_SETTINGS, RESOLUTION, CONVERT_FRAMERATE, DELAY,
            SCALING_FILTER, PALETTE_MODE, MIN_DISPLAY_TIME, MAX_TIME_DIFF, MOVE_IN, MOVE_OUT, MOVE_X,
            CROP_LINES, ALPHA_CROP_THRESHOLD, SCALE, EXPORT_PALETTE, EXPORT_FORCED_SUBTITLES_ONLY, FORCED_FLAG,
            SWAP_CR_CB, FIX_INVISIBLE_FRAMES, ALPHA_THRESHOLD, LUM_LOW_MID_THRESHOLD, LUM_MID_HIGH_THRESHOLD,
            LANGUAGE_CODE, PALETTE_FILE, OUTPUT_FILE, VERBOSE, VERSION);

    private final Options options = new Options();

    public CommandLineOptions() {
        createAndAddOptions();
    }

    public Options getOptions() {
        return options;
    }

    private void createAndAddOptions() {
        Option help = OptionBuilder
                .withLongOpt("help")
                .withDescription("Show usage information and exit.")
                .hasArg(false).create(HELP);
        options.addOption(help);

        Option version = OptionBuilder
                .withLongOpt("version")
                .withDescription("Show version information and exit.")
                .hasArg(false).create(VERSION);
        options.addOption(version);

        Option output = OptionBuilder
                .withLongOpt("output")
                .withDescription("Specify output file.")
                .hasArg().create(OUTPUT_FILE);
        options.addOption(output);

        Option verbose = OptionBuilder
                .withLongOpt("verbose")
                .withDescription("Verbose console output mode.")
                .hasArg(false).create(VERBOSE);
        options.addOption(verbose);

        Option loadSettings = OptionBuilder
                .withLongOpt("load-settings")
                .withDescription("Load settings stored in configuration file.")
                .hasArg(false).create(LOAD_SETTINGS);
        options.addOption(loadSettings);

        Option resolution = OptionBuilder
                .withArgName("resolution")
                .withLongOpt("resolution")
                .withDescription("Set resolution to: keep, ntsc=480, pal=576, 720p=720, 1080p=1080, 1440x1080\nDefault: keep")
                .hasArg().create(RESOLUTION);
        options.addOption(resolution);

        Option sourceFrameRate = OptionBuilder
                .withArgName("src>, <trg")
                .withLongOpt("convertfps")
                .withDescription("Convert frame rate from <src> to <trg>\nSupported values: 24p=23.976, 25p=25, 30p=29.970\nauto,<trg> detects source frame rate.")
                .withValueSeparator(',')
                .hasArgs(2).create(CONVERT_FRAMERATE);
        options.addOption(sourceFrameRate);

        Option delay = OptionBuilder
                .withArgName("delay")
                .withLongOpt("delay")
                .withDescription("Set delay in ms\nDefault: 0.0")
                .hasArg().create(DELAY);
        options.addOption(delay);

        Option filter = OptionBuilder
                .withArgName("filter")
                .withLongOpt("filter")
                .withDescription("Set the filter to use for scaling.\nSupported values: bilinear, triangle, bicubic, bell, b-spline, hermite, lanczos3, mitchell\nDefault: bilinear")
                .hasArg().create(SCALING_FILTER);
        options.addOption(filter);

        Option paletteMode = OptionBuilder
                .withArgName("mode")
                .withLongOpt("palettemode")
                .withDescription("Set palette mode.\nSupported values: keep, create, dither\nDefault: create")
                .hasArg().create(PALETTE_MODE);
        options.addOption(paletteMode);

        Option minDisplayTime = OptionBuilder
                .withArgName("time")
                .withLongOpt("mindisptime")
                .withDescription("Set minimum display time in ms.\nDefault: 500")
                .hasArg().create(MIN_DISPLAY_TIME);
        options.addOption(minDisplayTime);

        Option maxTimeDiff = OptionBuilder
                .withArgName("time")
                .withLongOpt("maxtimediff")
                .withDescription("Set maximum time difference for merging subtitles in ms.\nDefault: 200")
                .hasArg().create(MAX_TIME_DIFF);
        options.addOption(maxTimeDiff);

        OptionGroup moveGroup = new OptionGroup();
        Option moveIn = OptionBuilder
                .withArgName("ratio, offset")
                .withLongOpt("movein")
                .withDescription("Move captions inside screen ratio <ratio>, +/- offset <offset>")
                .withValueSeparator(',')
                .hasArgs(2).create(MOVE_IN);
        moveGroup.addOption(moveIn);

        Option moveOut = OptionBuilder
                .withArgName("ratio, offset")
                .withLongOpt("moveout")
                .withDescription("Move captions outside screen ratio <ratio>, +/- offset <offset>")
                .withValueSeparator(',')
                .hasArgs(2).create(MOVE_OUT);
        moveGroup.addOption(moveOut);

        options.addOptionGroup(moveGroup);

        Option moveX = OptionBuilder
                .withArgName("pos[, offset]")
                .withLongOpt("movex")
                .withDescription("Move captions horizontally from specified position. <pos> may be left, right, center\n+/- optional offset <offset> (only if moving left or right)")
                .withValueSeparator(',')
                .hasOptionalArgs(2).create(MOVE_X);
        options.addOption(moveX);

        Option cropLines = OptionBuilder
                .withArgName("n")
                .withLongOpt("croplines")
                .withDescription("Crop the upper/lower n lines.\nDefault: 0")
                .hasArg().create(CROP_LINES);
        options.addOption(cropLines);

        Option alphaCropThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt("alphacropthr")
                .withDescription("Set the alpha cropping threshold.\nDefault: 10")
                .hasArg().create(ALPHA_CROP_THRESHOLD);
        options.addOption(alphaCropThreshold);

        Option scale = OptionBuilder
                .withArgName("x, y")
                .withLongOpt("scale")
                .withDescription("Scale captions horizontally and vertically.\nDefault: 1.0,1.0")
                .withValueSeparator(',')
                .hasArgs(2).create(SCALE);
        options.addOption(scale);

        Option exportPalette = OptionBuilder
                .withLongOpt("exppal")
                .withDescription("Export target palette in PGCEdit format.")
                .hasArg(false).create(EXPORT_PALETTE);
        options.addOption(exportPalette);

        Option exportForcedSubtitlesOnly = OptionBuilder
                .withLongOpt("forcedonly")
                .withDescription("Export only forced subtitles (when converting from BD-SUP).")
                .hasArg(false).create(EXPORT_FORCED_SUBTITLES_ONLY);
        options.addOption(exportForcedSubtitlesOnly);

        Option setForcedFlag = OptionBuilder
                .withArgName("state")
                .withLongOpt("forcedflag")
                .withDescription("Set or clear the forced flag for all subtitles.\nSupported values: set, clear")
                .hasArg().create(FORCED_FLAG);
        options.addOption(setForcedFlag);

        Option swapCrCb = OptionBuilder
                .withLongOpt("swap")
                .withDescription("Swap Cr/Cb components when loading a BD/HD-DVD sup file.")
                .hasArg(false).create(SWAP_CR_CB);
        options.addOption(swapCrCb);

        Option fixInvisibleFrames = OptionBuilder
                .withLongOpt("fixinv")
                .withDescription("Fix zero alpha frame palette for SUB/IDX and SUP/IFO.")
                .hasArg(false).create(FIX_INVISIBLE_FRAMES);
        options.addOption(fixInvisibleFrames);

        Option alphaThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt("alphathr")
                .withDescription("Set alpha threshold 0..255 for SUB/IDX conversion.\nDefault: 80")
                .hasArg().create(ALPHA_THRESHOLD);
        options.addOption(alphaThreshold);

        Option luminanceLowMidThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt("lumlowmidthr")
                .withDescription("Set luminance lo/mid threshold 0..255 for SUB/IDX conversion.\nDefault: auto")
                .hasArg().create(LUM_LOW_MID_THRESHOLD);
        options.addOption(luminanceLowMidThreshold);

        Option luminanceMidHighThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt("lummidhighthr")
                .withDescription("Set luminance mid/hi threshold 0..255 for SUB/IDX conversion.\nDefault: auto")
                .hasArg().create(LUM_MID_HIGH_THRESHOLD);
        options.addOption(luminanceMidHighThreshold);

        Option languageCode = OptionBuilder
                .withArgName("langcode")
                .withLongOpt("langcode")
                .withDescription("Set language for SUB/IDX export.\nDefault: en")
                .hasArg().create(LANGUAGE_CODE);
        options.addOption(languageCode);

        Option paletteFile = OptionBuilder
                .withArgName("file")
                .withLongOpt("palettefile")
                .withDescription("Load palette file for SUB/IDX conversion. Overrides default palette.")
                .hasArg().create(PALETTE_FILE);
        options.addOption(paletteFile);
    }
}
