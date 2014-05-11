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
package bdsup2sub.cli;

import bdsup2sub.core.*;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.optional.Optional;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static bdsup2sub.cli.CommandLineOptions.*;
import static bdsup2sub.core.Configuration.*;
import static bdsup2sub.core.Constants.LANGUAGES;

public class CommandLineParser {

    private boolean printHelpMode;
    private boolean printVersionMode;
    private boolean cliMode;
    private File inputFile;
    private File outputFile;
    private Optional<OutputMode> outputMode = Optional.absent();
    private boolean loadSettings;
    private Optional<Resolution> resolution = Optional.absent();
    private Optional<Double> sourceFrameRate = Optional.absent();
    private Optional<Double> targetFrameRate = Optional.absent();
    private boolean convertFpsMode;
    private boolean synchronizeFpsMode;
    private Optional<Double> delay = Optional.absent();
    private Optional<ScalingFilter> scalingFilter = Optional.absent();
    private Optional<PaletteMode> paletteMode = Optional.absent();
    private Optional<Double> minimumDisplayTime = Optional.absent();
    private Optional<Double> maximumTimeDifference = Optional.absent();
    private Optional<CaptionMoveModeY> moveModeY = Optional.absent();
    private double screenRatio;
    private int moveYOffset;
    private Optional<CaptionMoveModeX> moveModeX = Optional.absent();
    private Optional<Integer> moveXOffset = Optional.absent();
    private Optional<Integer> cropLines = Optional.absent();
    private Optional<Integer> alphaCropThreshold = Optional.absent();
    private Optional<Double> scaleX = Optional.absent();
    private Optional<Double> scaleY = Optional.absent();
    private Optional<Boolean> exportPalette = Optional.absent();
    private Optional<Boolean> exportForcedSubtitlesOnly = Optional.absent();
    private Optional<ForcedFlagState> forcedFlagState = Optional.absent();
    private Optional<Boolean> swapCrCb = Optional.absent();
    private Optional<Boolean> fixInvisibleFrames = Optional.absent();
    private Optional<Boolean> verbose = Optional.absent();
    private Optional<Integer> alphaThreshold = Optional.absent();
    private Optional<Integer> lumLowMedThreshold  = Optional.absent();
    private Optional<Integer> lumMedHighThreshold = Optional.absent();
    private Optional<Integer> languageIndex = Optional.absent();

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
            parseInputFileOption(line);
            parseOutputFileOption(line);
            cliMode = line.hasOption(OUTPUT_FILE);
            loadSettings = line.hasOption(LOAD_SETTINGS) || !cliMode;
            parseResolutionOption(line);
            parseTargetFramerateOption(line);
            parseConvertFramerateOption(line);
            parseDelayOption(line);
            parseScalingFilterOption(line);
            parsePaletteModeOption(line);
            parseMinimumDisplayTimeOption(line);
            parseMaxTimeDiffOption(line);
            parseMoveYOption(line);
            parseMoveXOption(line);
            parseCropLinesOption(line);
            parseAlphaCropThresholdOption(line);
            parseScaleOption(line);
            exportPalette = line.hasOption(EXPORT_PALETTE) ? Optional.of(Boolean.TRUE) : Optional.<Boolean>absent();
            exportForcedSubtitlesOnly = line.hasOption(EXPORT_FORCED_SUBTITLES_ONLY) ? Optional.of(Boolean.TRUE) : Optional.<Boolean>absent();
            parseForcedFlagOption(line);
            swapCrCb = line.hasOption(SWAP_CR_CB) ? Optional.of(Boolean.TRUE) : Optional.<Boolean>absent();
            fixInvisibleFrames = line.hasOption(FIX_INVISIBLE_FRAMES) ? Optional.of(Boolean.TRUE) : Optional.<Boolean>absent(); // TODO: accept only for SUB/IDX or SUP/IFO as target
            verbose = line.hasOption(VERBOSE) ? Optional.of(Boolean.TRUE) : Optional.<Boolean>absent();
            parseAlphaThresholdOption(line);
            parseLuminanceThresholdOption(line);
            parseLanguageCodeOption(line);
            parsePaletteFileOption(line);
        }
    }

    private void parseInputFileOption(CommandLine line) throws ParseException {
        if (line.getArgList().isEmpty() && line.hasOption(OUTPUT_FILE)) {
            throw new ParseException("Missing input file.");
        } else if (line.getArgList().size() > 1) {
            throw new ParseException("Too many input files.");
        } else if (line.getArgList().size() == 1) {
            inputFile = new File(line.getArgList().get(0).toString());
            if (!inputFile.exists()) {
                throw new ParseException("Input file not found: " + inputFile.getAbsolutePath());
            }
        }
    }

    private void parseOutputFileOption(CommandLine line) throws ParseException {
        if (line.hasOption(OUTPUT_FILE)) {
            String value = line.getOptionValue(OUTPUT_FILE);
            outputFile = new File(value);

            String extension = FilenameUtils.getExtension(value);
            if (extension.isEmpty()) {
                throw new ParseException("No extension given for output " + outputFile);
            }
            if (extension.equalsIgnoreCase("sup")) {
                outputMode = Optional.of(OutputMode.BDSUP);
            } else if (extension.equalsIgnoreCase("sub") || extension.equals("idx")) {
                outputMode = Optional.of(OutputMode.VOBSUB);
            } else if (extension.equalsIgnoreCase("xml")) {
                outputMode = Optional.of(OutputMode.XML);
            } else if (extension.equalsIgnoreCase("ifo")) {
                outputMode = Optional.of(OutputMode.SUPIFO);
            } else {
                throw new ParseException("Unknown extension of output " + outputFile);
            }
        }
    }

    private void parseResolutionOption(CommandLine line) throws ParseException {
        if (line.hasOption(RESOLUTION)) {
            String value = line.getOptionValue(RESOLUTION);
            if (value.equalsIgnoreCase("keep")) {
                // keep undefined
            } else if (value.equalsIgnoreCase("pal") || value.equalsIgnoreCase("576")) {
                resolution = Optional.of(Resolution.PAL);
            } else if (value.equalsIgnoreCase("ntsc") || value.equalsIgnoreCase("480")) {
                resolution = Optional.of(Resolution.NTSC);
            } else if (value.equalsIgnoreCase("720p") || value.equalsIgnoreCase("720")) {
                resolution = Optional.of(Resolution.HD_720);
            } else if (value.equalsIgnoreCase("1440x1080")) {
                resolution = Optional.of(Resolution.HD_1440x1080);
            } else if (value.equalsIgnoreCase("1080p") || value.equalsIgnoreCase("1080")) {
                resolution = Optional.of(Resolution.HD_1080);
            } else {
                throw new ParseException("Illegal resolution: " + value);
            }
        }
    }

    private void parseTargetFramerateOption(CommandLine line) throws ParseException {
        if (line.hasOption(TARGET_FRAMERATE)) {
            synchronizeFpsMode = true;
            String value = line.getOptionValue(TARGET_FRAMERATE);
            if (value.equalsIgnoreCase("keep")) {
                // keep undefined
            } else {
                targetFrameRate = Optional.of(SubtitleUtils.getFps(value));
                if (targetFrameRate.get() <= 0) {
                    throw new ParseException("Invalid target framerate: " + value);
                }
            }
        }
    }

    private void parseConvertFramerateOption(CommandLine line) throws ParseException {
        if (line.hasOption(CONVERT_FRAMERATE)) {
            convertFpsMode = true;
            if (line.getOptionValues(CONVERT_FRAMERATE).length != 2) {
                throw new ParseException("2 arguments needed for framerate conversion.");
            }
            String value = line.getOptionValues(CONVERT_FRAMERATE)[0];
            if (value.equalsIgnoreCase("auto")) {
                // keep undefined
            } else {
                sourceFrameRate = Optional.of(SubtitleUtils.getFps(value));
                if (sourceFrameRate.get() <= 0) {
                    throw new ParseException("Invalid source framerate: " + value);
                }
            }
            value = line.getOptionValues(CONVERT_FRAMERATE)[1];
            targetFrameRate = Optional.of(SubtitleUtils.getFps(value));
            if (targetFrameRate.get() <= 0) {
                throw new ParseException("Invalid target framerate: " + value);
            }
        }
    }

    private void parseDelayOption(CommandLine line) throws ParseException {
        if (line.hasOption(DELAY)) {
            String value = line.getOptionValue(DELAY);
            try {
                delay = Optional.of(Double.parseDouble(value.trim()));
            } catch (NumberFormatException ex) {
                throw new ParseException("Illegal delay value: " + value);
            }
        }
    }

    private void parseScalingFilterOption(CommandLine line) throws ParseException {
        if (line.hasOption(SCALING_FILTER)) {
            String value = line.getOptionValue(SCALING_FILTER);
            boolean found = false;
            for (ScalingFilter f : ScalingFilter.values()) {
                if (f.toString().equalsIgnoreCase(value)) {
                    scalingFilter = Optional.of(f);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ParseException("Illegal scaling filter value: " + value);
            }
        }
    }

    private void parsePaletteModeOption(CommandLine line) throws ParseException {
        if (line.hasOption(PALETTE_MODE)) {
            String value = line.getOptionValue(PALETTE_MODE);
            if (value.equalsIgnoreCase("keep")) {
                paletteMode = Optional.of(PaletteMode.KEEP_EXISTING);
            } else if (value.equalsIgnoreCase("create")) {
                paletteMode = Optional.of(PaletteMode.CREATE_NEW);
            } else if (value.equalsIgnoreCase("dither")) {
                paletteMode = Optional.of(PaletteMode.CREATE_DITHERED);
            } else {
                throw new ParseException("Invalid palette mode: " + value);
            }
        }
    }

    private void parseMinimumDisplayTimeOption(CommandLine line) throws ParseException {
        if (line.hasOption(MIN_DISPLAY_TIME)) {
            String value = line.getOptionValue(MIN_DISPLAY_TIME);
            try {
                minimumDisplayTime = Optional.of(Double.parseDouble(value.trim()));
            } catch (NumberFormatException ex) {
                throw new ParseException("Illegal minimum display time value: " + value);
            }
            if (minimumDisplayTime.get() <= 0) {
                throw new ParseException("Illegal minimum display time value: " + value);
            }
        }
    }

    private void parseMaxTimeDiffOption(CommandLine line) throws ParseException {
        if (line.hasOption(MAX_TIME_DIFF)) {
            String value = line.getOptionValue(MAX_TIME_DIFF);
            try {
                maximumTimeDifference = Optional.of(Double.parseDouble(value.trim()));
            } catch (NumberFormatException ex) {
                throw new ParseException("Illegal maximum merge time difference value: " + value);
            }
            if (maximumTimeDifference.get() < 0) {
                throw new ParseException("Illegal maximum merge time difference value: " + value);
            }
        }
    }

    private void parseMoveYOption(CommandLine line) throws ParseException {
        if (line.hasOption(MOVE_IN) || line.hasOption(MOVE_OUT)) {
            moveModeY = line.hasOption(MOVE_IN) ? Optional.of(CaptionMoveModeY.MOVE_INSIDE_BOUNDS) : Optional.of(CaptionMoveModeY.MOVE_OUTSIDE_BOUNDS);
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
    }

    private void parseMoveXOption(CommandLine line) throws ParseException {
        if (line.hasOption(MOVE_X)) {
            if (line.getOptionValues(MOVE_X) == null || line.getOptionValues(MOVE_X).length < 1) {
                throw new ParseException("Missing argument for moving captions.");
            }
            String value = line.getOptionValues(MOVE_X)[0];
            if (value.equalsIgnoreCase("left")) {
                moveModeX = Optional.of(CaptionMoveModeX.LEFT);
            } else if (value.equalsIgnoreCase("center")) {
                moveModeX = Optional.of(CaptionMoveModeX.CENTER);
            } else if (value.equalsIgnoreCase("right")) {
                moveModeX = Optional.of(CaptionMoveModeX.RIGHT);
            } else {
                throw new ParseException("Invalid move mode: " + value);
            }

            if ((moveModeX.get() == CaptionMoveModeX.LEFT || moveModeX.get() == CaptionMoveModeX.RIGHT) && line.getOptionValues(MOVE_X).length > 1) {
                value = line.getOptionValues(MOVE_X)[1];
                moveXOffset = Optional.of(ToolBox.getInt(value));
                if (moveXOffset.get() < 0) {
                    throw new ParseException("Invalid pixel offset: " + value);
                }
            }
        }
    }

    private void parseCropLinesOption(CommandLine line) throws ParseException {
        if (line.hasOption(CROP_LINES)) {
            String value = line.getOptionValue(CROP_LINES);
            cropLines = Optional.of(ToolBox.getInt(value.trim()));
            if (cropLines.get() < 0) {
                throw new ParseException("Invalid crop lines value: " + value);
            }
        }
    }

    private void parseAlphaCropThresholdOption(CommandLine line) throws ParseException {
        if (line.hasOption(ALPHA_CROP_THRESHOLD)) {
            String value = line.getOptionValue(ALPHA_CROP_THRESHOLD);
            alphaCropThreshold = Optional.of(ToolBox.getInt(value.trim()));
            if (alphaCropThreshold.get() < 0 || alphaCropThreshold.get() > 255) {
                throw new ParseException("Illegal number range for alpha cropping threshold: " + value);
            }
        }
    }

    private void parseScaleOption(CommandLine line) throws ParseException {
        if (line.hasOption(SCALE)) {
            if (line.getOptionValues(SCALE).length != 2) {
                throw new ParseException("2 arguments needed for scaling.");
            }
            String value = line.getOptionValues(SCALE)[0];
            scaleX = Optional.of(ToolBox.getDouble(value));
            if (scaleX.get() < MIN_FREE_SCALE_FACTOR || scaleX.get() > MAX_FREE_SCALE_FACTOR) {
                throw new ParseException("Invalid x scaling factor: " + value);
            }
            value = line.getOptionValues(SCALE)[1];
            scaleY = Optional.of(ToolBox.getDouble(value));
            if (scaleY.get() < MIN_FREE_SCALE_FACTOR || scaleY.get() > MAX_FREE_SCALE_FACTOR) {
                throw new ParseException("Invalid y scaling factor: " + value);
            }
        }
    }

    private void parseForcedFlagOption(CommandLine line) throws ParseException {
        if (line.hasOption(FORCED_FLAG)) {
            String value = line.getOptionValue(FORCED_FLAG);
            if (value.equalsIgnoreCase("set")) {
                forcedFlagState = Optional.of(ForcedFlagState.SET);
            } else if (value.equalsIgnoreCase("clear")) {
                forcedFlagState = Optional.of(ForcedFlagState.CLEAR);
            } else {
                throw new ParseException("Invalid forced flag state: " + value);
            }
        }
    }

    private void parseAlphaThresholdOption(CommandLine line) throws ParseException {
        if (line.hasOption(ALPHA_THRESHOLD)) { // TODO: accept only for SUB/IDX or SUP/IFO as target
            String value = line.getOptionValue(ALPHA_THRESHOLD);
            alphaThreshold = Optional.of(ToolBox.getInt(value.trim()));
            if (alphaThreshold.get() < 0 || alphaThreshold.get() > 255) {
                throw new ParseException("Illegal number range for alpha threshold: " + value);
            }
        }
    }

    private void parseLuminanceThresholdOption(CommandLine line) throws ParseException {
        if (line.hasOption(LUM_LOW_MED_THRESHOLD)) { // TODO: accept only for SUB/IDX or SUP/IFO as target
            String value = line.getOptionValue(LUM_LOW_MED_THRESHOLD);
            lumLowMedThreshold = Optional.of(ToolBox.getInt(value.trim()));
            if (lumLowMedThreshold.get() < 0 || lumLowMedThreshold.get() > 255) {
                throw new ParseException("Illegal number range for luminance threshold: " + value);
            }
        }
        if (line.hasOption(LUM_MED_HIGH_THRESHOLD)) { // TODO: accept only for SUB/IDX or SUP/IFO as target
            String value = line.getOptionValue(LUM_MED_HIGH_THRESHOLD);
            lumMedHighThreshold = Optional.of(ToolBox.getInt(value.trim()));
            if (lumMedHighThreshold.get() < 0 || lumMedHighThreshold.get() > 255) {
                throw new ParseException("Illegal number range for luminance threshold: " + value);
            }
        }
        if (lumLowMedThreshold.isPresent() || lumMedHighThreshold.isPresent()) {
            int lowMed = lumLowMedThreshold.isPresent() ? lumLowMedThreshold.get() : DEFAULT_LUMINANCE_LOW_MED_THRESHOLD;
            int medHigh = lumMedHighThreshold.isPresent() ? lumMedHighThreshold.get() : DEFAULT_LUMINANCE_MED_HIGH_THRESHOLD;
            if (lowMed > medHigh) {
                throw new ParseException("Invalid luminance threshold values: " + lumLowMedThreshold + ", " + lumMedHighThreshold);
            }
        }
    }

    private void parseLanguageCodeOption(CommandLine line) throws ParseException {
        if (line.hasOption(LANGUAGE_CODE)) { //TODO: only sub/idx
            String value = line.getOptionValue(LANGUAGE_CODE);
            boolean found = false;
            for (int i = 0; i < LANGUAGES.length; i++)
                if (LANGUAGES[i][1].equalsIgnoreCase(value)) {
                    languageIndex = Optional.of(i);
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
    }

    private void parsePaletteFileOption(CommandLine line) throws ParseException {
        if (line.hasOption(PALETTE_FILE)) { //TODO: only sub/idx
            String value = line.getOptionValue(PALETTE_FILE);
            paletteFile = new File(value);
            if (!paletteFile.exists()) {
                throw new ParseException("Palette file not found: " + value);
            } else {
                if (!Arrays.equals("#COL".getBytes(), ToolBox.getFileID(value, 4))) {
                    throw new ParseException("Invalid palette file: " + value);
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

    public boolean isCliMode() {
        return cliMode;
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public Optional<OutputMode> getOutputMode() {
        return outputMode;
    }

    public boolean isLoadSettings() {
        return loadSettings;
    }

    public Optional<Resolution> getResolution() {
        return resolution;
    }

    public Optional<Double> getSourceFrameRate() {
        return sourceFrameRate;
    }

    public Optional<Double> getTargetFrameRate() {
        return targetFrameRate;
    }

    public boolean isConvertFpsMode() {
        return convertFpsMode;
    }

    public boolean isSynchronizeFpsMode() {
        return synchronizeFpsMode;
    }

    public Optional<Double> getDelay() {
        return delay;
    }

    public Optional<ScalingFilter> getScalingFilter() {
        return scalingFilter;
    }

    public Optional<PaletteMode> getPaletteMode() {
        return paletteMode;
    }

    public Optional<Double> getMinimumDisplayTime() {
        return minimumDisplayTime;
    }

    public Optional<Double> getMaximumTimeDifference() {
        return maximumTimeDifference;
    }

    public Optional<CaptionMoveModeY> getMoveModeY() {
        return moveModeY;
    }

    public int getMoveYOffset() {
        return moveYOffset;
    }

    public Optional<CaptionMoveModeX> getMoveModeX() {
        return moveModeX;
    }

    public Optional<Integer> getMoveXOffset() {
        return moveXOffset;
    }

    public double getScreenRatio() {
        return screenRatio;
    }

    public Optional<Integer> getCropLines() {
        return cropLines;
    }

    public Optional<Integer> getAlphaCropThreshold() {
        return alphaCropThreshold;
    }

    public Optional<Double> getScaleX() {
        return scaleX;
    }

    public Optional<Double> getScaleY() {
        return scaleY;
    }

    public Optional<Boolean> isExportPalette() {
        return exportPalette;
    }

    public Optional<Boolean> isExportForcedSubtitlesOnly() {
        return exportForcedSubtitlesOnly;
    }

    public Optional<ForcedFlagState> getForcedFlagState() {
        return forcedFlagState;
    }

    public Optional<Boolean> isSwapCrCb() {
        return swapCrCb;
    }

    public Optional<Boolean> isFixInvisibleFrames() {
        return fixInvisibleFrames;
    }

    public Optional<Boolean> isVerbose() {
        return verbose;
    }

    public Optional<Integer> getAlphaThreshold() {
        return alphaThreshold;
    }

    public Optional<Integer> getLumLowMedThreshold() {
        return lumLowMedThreshold;
    }

    public Optional<Integer> getLumMedHighThreshold() {
        return lumMedHighThreshold;
    }

    public Optional<Integer> getLanguageIndex() {
        return languageIndex;
    }

    public File getPaletteFile() {
        return paletteFile;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;

                int opt1Index = OPTION_ORDER.indexOf(opt1.getOpt());
                int opt2Index = OPTION_ORDER.indexOf(opt2.getOpt());

                return (int) Math.signum(opt1Index - opt2Index);
            }
        });
        formatter.setWidth(79);
        String command = System.getProperty("wrapper") == null ? "java -jar BDSup2Sub" : "bdsup2sub";
        formatter.printHelp(command + " [options] -o <output> <input>", options);
    }
}
