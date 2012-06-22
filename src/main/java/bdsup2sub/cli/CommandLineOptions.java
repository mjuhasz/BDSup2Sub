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

public class CommandLineOptions {

    static final String HELP = "h";
    static final String VERSION = "V";
    static final String VERBOSE = "v";

    static final String RESOLUTION = "r";
    static final String CONVERT_FRAMERATE = "convertfps";
    static final String DELAY = "d";
    static final String SCALING_FILTER = "f";
    static final String PALETTE_MODE = "p";
    static final String MIN_DISPLAY_TIME = "m";
    static final String MAX_TIME_DIFF = "x";
    static final String MOVE_IN = "movein";
    static final String MOVE_OUT = "moveout";
    static final String MOVE_X = "movex";
    static final String CROP_LINES = "c";
    static final String ALPHA_CROP_THRESHOLD = "a";
    static final String SCALE = "scale";
    static final String EXPORT_PALETTE = "exppal";
    static final String EXPORT_FORCED_SUBTITLES_ONLY = "forcedonly";
    static final String FORCED_FLAG = "forcedflag";
    static final String SWAP_CR_CB = "swap";
    static final String FIX_INVISIBLE_FRAMES = "fixinv";

    static final String ALPHA_THRESHOLD = "alphathr";
    static final String LUM_LOW_MID_THRESHOLD = "lumlowmidthr";
    static final String LUM_MID_HIGH_THRESHOLD = "lummidhighthr";
    static final String LANGUAGE_CODE = "langcode";
    static final String PALETTE_FILE = "palettefile";

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

        Option verbose = OptionBuilder
                .withLongOpt("verbose")
                .withDescription("verbose console output mode")
                .hasArg(false).create(VERBOSE);
        options.addOption(verbose);

        Option resolution = OptionBuilder
                .withArgName("resolution")
                .withLongOpt("resolution")
                .withDescription("resolution\nSupported values: keep, 480 (ntsc), 576 (pal), 720 (720p), 1080 (1080p), \nDefault: keep")
                .hasArg().create(RESOLUTION);
        options.addOption(resolution);

        Option sourceFrameRate = OptionBuilder
                .withArgName("<src>, <trg>")
                .withLongOpt(CONVERT_FRAMERATE)
                .withDescription("convert frame rate from <src> to <trg>\nSupported values: 24p (23.976), 25p (25), 30p (29.970)\nauto,<trg> detects source frame rate automatically")
                .withValueSeparator(',')
                .hasArgs(2).create();
        options.addOption(sourceFrameRate);

        Option delay = OptionBuilder
                .withArgName("delay")
                .withLongOpt("delay")
                .withDescription("delay in ms\nDefault: 0.0")
                .hasArg().create(DELAY);
        options.addOption(delay);

        Option filter = OptionBuilder
                .withArgName("filter")
                .withLongOpt("filter")
                .withDescription("scaling filter\nSupported values: bilinear, triangle, bicubic, bell, b-spline, hermite, lanczos3, mitchell\nDefault: bilinear")
                .hasArg().create(SCALING_FILTER);
        options.addOption(filter);

        Option paletteMode = OptionBuilder
                .withArgName("mode")
                .withLongOpt("palettemode")
                .withDescription("palette mode\nSupported values: keep, create, dither\nDefault: create")
                .hasArg().create(PALETTE_MODE);
        options.addOption(paletteMode);

        Option minDisplayTime = OptionBuilder
                .withArgName("time")
                .withLongOpt("mindisptime")
                .withDescription("minimum display time (ms)\nDefault: 500")
                .hasArg().create(MIN_DISPLAY_TIME);
        options.addOption(minDisplayTime);

        Option maxTimeDiff = OptionBuilder
                .withArgName("time")
                .withLongOpt("maxtimediff")
                .withDescription("maximum time difference for merging subtitles (ms)\nDefault: 200")
                .hasArg().create(MAX_TIME_DIFF);
        options.addOption(maxTimeDiff);

        Option moveIn = OptionBuilder
                .withArgName("ratio, offset")
                .withLongOpt(MOVE_IN)
                .withDescription("move captions inside screen ratio <ratio>, +/- offset <offset>")
                .withValueSeparator(',')
                .hasArgs(2).create();
        options.addOption(moveIn);

        Option moveOut = OptionBuilder
                .withArgName("ratio, offset")
                .withLongOpt(MOVE_OUT)
                .withDescription("move captions outside screen ratio <ratio>, +/- offset <offset>")
                .withValueSeparator(',')
                .hasArgs(2).create();
        options.addOption(moveOut);

        Option moveX = OptionBuilder
                .withArgName("pos[, offset]")
                .withLongOpt(MOVE_X)
                .withDescription("move captions horizontally\n<pos> may be left, right, center\n+/- optional offset <offset> (only if moving left or right)")
                .withValueSeparator(',')
                .hasOptionalArgs(2).create();
        options.addOption(moveX);

        Option cropLines = OptionBuilder
                .withArgName("n")
                .withLongOpt("croplines")
                .withDescription("crop the upper/lower n lines\nDefault: 0")
                .hasArg().create(CROP_LINES);
        options.addOption(cropLines);

        Option alphaCropThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt("alphacropthr")
                .withDescription("alpha cropping threshold\nDefault: 10")
                .hasArg().create(ALPHA_CROP_THRESHOLD);
        options.addOption(alphaCropThreshold);

        Option scale = OptionBuilder
                .withArgName("x, y")
                .withLongOpt(SCALE)
                .withDescription("scale captions with free factors\nDefault: 1.0,1.0")
                .withValueSeparator(',')
                .hasArgs(2).create();
        options.addOption(scale);

        Option exportPalette = OptionBuilder
                .withLongOpt(EXPORT_PALETTE)
                .withDescription("export target palette in PGCEdit format")
                .hasArg(false).create();
        options.addOption(exportPalette);

        Option exportForcedSubtitlesOnly = OptionBuilder
                .withLongOpt(EXPORT_FORCED_SUBTITLES_ONLY)
                .withDescription("export only forced subtitles (when converting from BD-SUP)")
                .hasArg(false).create();
        options.addOption(exportForcedSubtitlesOnly);

        Option setForcedFlag = OptionBuilder
                .withArgName("state")
                .withLongOpt(FORCED_FLAG)
                .withDescription("set or clear forced flag for all subs\nSupported values: set, clear, keep\nDefault: keep")
                .hasArg().create();
        options.addOption(setForcedFlag);

        Option swapCrCb = OptionBuilder
                .withLongOpt(SWAP_CR_CB)
                .withDescription("swap Cr/Cb components")
                .hasArg(false).create();
        options.addOption(swapCrCb);

        Option fixInvisibleFrames = OptionBuilder
                .withLongOpt(FIX_INVISIBLE_FRAMES)
                .withDescription("fix zero alpha frame palette for SUB/IDX and SUP/IFO")
                .hasArg(false).create();
        options.addOption(fixInvisibleFrames);

        Option alphaThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt(ALPHA_THRESHOLD)
                .withDescription("alpha threshold 0..255 for SUB/IDX conversion\nDefault: 80")
                .hasArg().create();
        options.addOption(alphaThreshold);

        Option luminanceLowMidThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt(LUM_LOW_MID_THRESHOLD)
                .withDescription("luminance lo/mid threshold 0..255 for SUB/IDX conversion\nDefault: auto")
                .hasArg().create();
        options.addOption(luminanceLowMidThreshold);

        Option luminanceMidHighThreshold = OptionBuilder
                .withArgName("n")
                .withLongOpt(LUM_MID_HIGH_THRESHOLD)
                .withDescription("luminance mid/hi threshold 0..255 for SUB/IDX conversion\nDefault: auto")
                .hasArg().create();
        options.addOption(luminanceMidHighThreshold);

        Option languageCode = OptionBuilder
                .withArgName("langcode")
                .withLongOpt(LANGUAGE_CODE)
                .withDescription("language code used for SUB/IDX export\nDefault: de")
                .hasArg().create();
        options.addOption(languageCode);

        Option paletteFile = OptionBuilder
                .withArgName("file")
                .withLongOpt(PALETTE_FILE)
                .withDescription("load palette file for SUB/IDX conversion\nDefault: use builtin palette")
                .hasArg().create();
        options.addOption(paletteFile);
    }
}
