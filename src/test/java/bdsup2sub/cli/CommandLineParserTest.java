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
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static bdsup2sub.core.Configuration.MAX_FREE_SCALE_FACTOR;
import static bdsup2sub.core.Configuration.MIN_FREE_SCALE_FACTOR;
import static org.junit.Assert.*;

public class CommandLineParserTest {

    private CommandLineParser subject;

    @Before
    public void setUp() {
        subject = new CommandLineParser();
    }

    @Test
    public void shouldParseEmptyArgList() throws Exception {
        subject.parse();
    }

    @Test(expected = ParseException.class)
    public void shouldRejectArgListWithUnknownArgs() throws Exception {
        subject.parse("--help", "--foo");
    }

    @Test
    public void shouldParseHelp() throws Exception {
        subject.parse("--help");
        assertTrue(subject.isPrintHelpMode());
    }

    @Test
    public void shouldParseVersion() throws Exception {
        subject.parse("--version");
        assertTrue(subject.isPrintVersionMode());
    }

    @Test
    public void shouldPrintHelpIfHelpIsDefinedBesidesOtherArgs() throws Exception {
        subject.parse("--version", "--help");
        assertTrue(subject.isPrintHelpMode());
        assertFalse(subject.isPrintVersionMode());
    }

    @Test
    public void shouldPrintVersionIfVersionIsDefinedBesidesOtherArgs() throws Exception {
        subject.parse("--version", "--verbose");
        assertTrue(subject.isPrintVersionMode());
        assertFalse(subject.isVerbose().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNonExistentInputFileArg() throws Exception {
        subject.parse("in.sub");
        assertEquals(new File("in.sub"), subject.getInputFile());
    }

    @Test
    public void shouldParseInputFileArg() throws Exception {
        File infile = File.createTempFile("input", null);
        infile.deleteOnExit();
        subject.parse(infile.getAbsolutePath());
        assertEquals(infile, subject.getInputFile());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingOutputFileArg() throws Exception {
        File infile = File.createTempFile("input", null);
        infile.deleteOnExit();
        subject.parse("--output", infile.getAbsolutePath());
    }

    @Test
    public void shouldParseOutputFileArg() throws Exception {
        File infile = File.createTempFile("input", null);
        infile.deleteOnExit();
        subject.parse("--output", "out.sup", infile.getAbsolutePath());
        assertEquals(new File("out.sup"), subject.getOutputFile());
        assertEquals(OutputMode.BDSUP, subject.getOutputMode().orNull());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectUnknownOutputFileMode() throws Exception {
        File infile = File.createTempFile("input", null);
        infile.deleteOnExit();
        subject.parse("--output", "out.foobar", infile.getAbsolutePath());
    }

    @Test
    public void shouldOutputFileModeDefaultToVobSub() throws Exception {
        File infile = File.createTempFile("input", null);
        infile.deleteOnExit();
        subject.parse(infile.getAbsolutePath());
        assertFalse(subject.getOutputMode().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRequireInoutFileIfOutputFileGiven() throws Exception {
        File infile = File.createTempFile("input", null);
        infile.deleteOnExit();
        subject.parse("--output", "out.sub");
    }

    @Test
    public void shouldLoadSettingsDefaultToFalse() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isLoadSettings());
    }

    @Test
    public void shouldLoadSettingsIfNotDefinedAndNotInCliMode() throws Exception {
        subject.parse();
        assertTrue(subject.isLoadSettings());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectResolutionWithoutArg() throws Exception {
        subject.parse("--resolution");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectResolutionWithUnknownArg() throws Exception {
        subject.parse("--resolution", "foo");
    }

    @Test
    public void shouldParseNtscResolutionWithNumericArg() throws Exception {
        subject.parse("--resolution", "480");
        assertEquals(Resolution.NTSC, subject.getResolution().get());
    }

    @Test
    public void shouldParseNtscResolutionWithTextArg() throws Exception {
        subject.parse("--resolution", "ntsc");
        assertEquals(Resolution.NTSC, subject.getResolution().get());
    }

    @Test
    public void shouldParsePalResolutionWithNumericArg() throws Exception {
        subject.parse("--resolution", "576");
        assertEquals(Resolution.PAL, subject.getResolution().get());
    }

    @Test
    public void shouldParsePalResolutionWithTextArg() throws Exception {
        subject.parse("--resolution", "pal");
        assertEquals(Resolution.PAL, subject.getResolution().get());
    }

    @Test
    public void shouldParse720ResolutionArg() throws Exception {
        subject.parse("--resolution", "720");
        assertEquals(Resolution.HD_720, subject.getResolution().get());
    }

    @Test
    public void shouldParse720pResolutionArg() throws Exception {
        subject.parse("--resolution", "720p");
        assertEquals(Resolution.HD_720, subject.getResolution().get());
    }

    @Test
    public void shouldParse1080ResolutionArg() throws Exception {
        subject.parse("--resolution", "1080");
        assertEquals(Resolution.HD_1080, subject.getResolution().get());
    }

    @Test
    public void shouldParse1080pResolutionArg() throws Exception {
        subject.parse("--resolution", "1080p");
        assertEquals(Resolution.HD_1080, subject.getResolution().get());
    }

    @Test
    public void shouldParse1440x1080ResolutionArg() throws Exception {
        subject.parse("--resolution", "1440x1080");
        assertEquals(Resolution.HD_1440x1080, subject.getResolution().get());
    }

    @Test
    public void shouldParseKeepResolutionArg() throws Exception {
        subject.parse("--resolution", "keep");
        assertFalse(subject.getResolution().isPresent());
    }

    @Test
    public void shouldResolutionDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getResolution().isPresent());
    }

    @Test
    public void shouldTargetFrameRateDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getTargetFrameRate().isPresent());
    }

    @Test
    public void shouldSourceFrameRateDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getSourceFrameRate().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMissingTargetFramerate() throws Exception {
        subject.parse("--fps-target");
    }

    @Test
    public void shouldParseTargetFramerate() throws Exception {
        subject.parse("--fps-target", "24");
        assertEquals(24, subject.getTargetFrameRate().get().intValue());
        assertFalse(subject.isConvertFpsMode());
        assertTrue(subject.isSynchronizeFpsMode());
    }

    @Test
    public void shouldParseKeepTargetFramerateArg() throws Exception {
        subject.parse("--fps-target", "keep");
        assertFalse(subject.getTargetFrameRate().isPresent());
        assertFalse(subject.isConvertFpsMode());
        assertTrue(subject.isSynchronizeFpsMode());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectConvertAndTargetFramerateAtTheSameTime() throws Exception {
        subject.parse("--convert-fps", "15, 25", "--fps-target", "24");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectConvertFramerateIfTargetFramerateIsMissing() throws Exception {
        subject.parse("--convert-fps", "15");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingConvertFramerateArgs() throws Exception {
        subject.parse("--convert-fps");
    }

    @Test
    public void shouldAcceptValidConvertFramerateArgs() throws Exception {
        subject.parse("--convert-fps", "15, 25");
        assertEquals(15, subject.getSourceFrameRate().get().intValue());
        assertEquals(25, subject.getTargetFrameRate().get().intValue());
        assertTrue(subject.isConvertFpsMode());
        assertFalse(subject.isSynchronizeFpsMode());
    }

    @Test
    public void shouldAcceptAutoAsSourceForConvertFramerateArgs() throws Exception {
        subject.parse("--convert-fps", "auto, 25");
        assertFalse(subject.getSourceFrameRate().isPresent());
        assertEquals(25, subject.getTargetFrameRate().get().intValue());
        assertTrue(subject.isConvertFpsMode());
        assertFalse(subject.isSynchronizeFpsMode());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidSourceFramerateForConvertFramerateArgs() throws Exception {
        subject.parse("--convert-fps", "-1, 25");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidTargetFramerateForConvertFramerateArgs() throws Exception {
        subject.parse("--convert-fps", "15, -1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidDelayArg() throws Exception {
        subject.parse("--delay", "foo");
    }

    @Test
    public void shouldAcceptValidDelayArg() throws Exception {
        subject.parse("--delay", "-200");
        assertEquals(-200, subject.getDelay().get().intValue());
    }

    @Test
    public void shouldDelayDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getDelay().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingDelayArg() throws Exception {
        subject.parse("--delay");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidFilterArg() throws Exception {
        subject.parse("--filter", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingFilterArg() throws Exception {
        subject.parse("--filter");
    }

    @Test
    public void shouldFilterDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getScalingFilter().isPresent());
    }

    @Test
    public void shouldParseFilterWithValidName() throws Exception {
        subject.parse("--filter", "Bicubic-Spline");
        assertEquals(ScalingFilter.BICUBIC_SPLINE, subject.getScalingFilter().get());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingPaletteModeArg() throws Exception {
        subject.parse("--palette-mode");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidPaletteModeArg() throws Exception {
        subject.parse("--palette-mode", "foo");
    }

    @Test
    public void shouldPaletteModeDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getPaletteMode().isPresent());
    }

    @Test
    public void shouldParsePaletteModeWithValidArg() throws Exception {
        subject.parse("--palette-mode", "keep");
        assertEquals(PaletteMode.KEEP_EXISTING, subject.getPaletteMode().get());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMinimumDisplayTimeArg() throws Exception {
        subject.parse("--minimum-time", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMinimumDisplayTimeIfNotPositive() throws Exception {
        subject.parse("--minimum-time", "-100");
    }

    @Test
    public void shouldMinimumDisplayTimeDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getMinimumDisplayTime().isPresent());
    }

    @Test
    public void shouldAcceptValidMinimumDisplayTimeArg() throws Exception {
        subject.parse("--minimum-time", "900");
        assertEquals(900, subject.getMinimumDisplayTime().get().intValue());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMinimumDisplayTimeArg() throws Exception {
        subject.parse("--minimum-time");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMaximumMergeTimeDifferenceArg() throws Exception {
        subject.parse("--merge-time", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMaximumMergeTimeDifferenceIfNegative() throws Exception {
        subject.parse("--merge-time", "-100");
    }

    @Test
    public void shouldMaximumMergeTimeDifferenceDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getMaximumTimeDifference().isPresent());
    }

    @Test
    public void shouldAcceptValidMaximumMergeTimeDifferenceArg() throws Exception {
        subject.parse("--merge-time", "900");
        assertEquals(900, subject.getMaximumTimeDifference().get().intValue());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMaximumMergeTimeDifferenceArg() throws Exception {
        subject.parse("--merge-time");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMoveInAndMoveOutArgsAtTheSameTime() throws Exception {
        subject.parse("--move-in", "12,2", "--move-out", "13,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveInArg() throws Exception {
        subject.parse("--move-in", "foo,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveOutArg() throws Exception {
        subject.parse("--move-out", "foo,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveInOffsetArg() throws Exception {
        subject.parse("--move-in", "2,foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveOutOffsetArg() throws Exception {
        subject.parse("--move-out", "2,foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMoveInArg() throws Exception {
        subject.parse("--move-in");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMoveOutArg() throws Exception {
        subject.parse("--move-out");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMoveInArgIfMissingOffsetArg() throws Exception {
        subject.parse("--move-in", "20");
    }
    @Test(expected = ParseException.class)
    public void shouldRejectMoveOutArgIfMissingOffsetArg() throws Exception {
        subject.parse("--move-out", "20");
    }

    @Test
    public void shouldBeInMoveInModeIfMoveInArgIsDefined() throws Exception {
        subject.parse("--move-in", "2,12");
        assertEquals(CaptionMoveModeY.MOVE_INSIDE_BOUNDS, subject.getMoveModeY().get());
    }

    @Test
    public void shouldBeInMoveOutModeIfMoveOutArgIsDefined() throws Exception {
        subject.parse("--move-out", "2,12");
        assertEquals(CaptionMoveModeY.MOVE_OUTSIDE_BOUNDS, subject.getMoveModeY().get());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeMoveInArg() throws Exception {
        subject.parse("--move-in", "1,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeMoveOutArg() throws Exception {
        subject.parse("--move-out", "1,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveXArg() throws Exception {
        subject.parse("--move-x", "foo,2");
    }

    @Test
    public void shouldMoveYModeDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getMoveModeY().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveXOffsetArg() throws Exception {
        subject.parse("--move-x", "2,foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMoveXArg() throws Exception {
        subject.parse("--move-x");
    }

    @Test
    public void shouldParseMoveXModeWithValidArgs() throws Exception {
        subject.parse("--move-x", "left,12");
        assertEquals(CaptionMoveModeX.LEFT, subject.getMoveModeX().get());
        assertEquals(12, subject.getMoveXOffset().get().intValue());
    }

    @Test
    public void shouldParseMoveXModeWithoutOffset() throws Exception {
        subject.parse("--move-x", "right");
        assertEquals(CaptionMoveModeX.RIGHT, subject.getMoveModeX().get());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeMoveXOffset() throws Exception {
        subject.parse("--move-in", "right,-2");
    }

    @Test
    public void shouldMoveXModeDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getMoveModeX().isPresent());
        assertFalse(subject.getMoveXOffset().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidCropLinesArg() throws Exception {
        subject.parse("--crop-y", "-1");
    }

    @Test
    public void shouldCropLinesDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getCropLines().isPresent());
    }

    @Test
    public void shouldAcceptValidCropLinesArg() throws Exception {
        subject.parse("--crop-y", "75");
        assertEquals(75, subject.getCropLines().get().intValue());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeAlphaCropThresholdArg() throws Exception {
        subject.parse("--alpha-crop", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeAlphaCropThresholdArg() throws Exception {
        subject.parse("--alpha-crop", "256");
    }

    @Test
    public void shouldAlphaCropThresholdDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getAlphaCropThreshold().isPresent());
    }

    @Test
    public void shouldAcceptValidAlphaCropThresholdArg() throws Exception {
        subject.parse("--alpha-crop", "75");
        assertEquals(75, subject.getAlphaCropThreshold().get().intValue());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidScaleXArg() throws Exception {
        subject.parse("--scale", "foo,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidScaleYArg() throws Exception {
        subject.parse("--scale", "2,foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingScaleArg() throws Exception {
        subject.parse("--scale");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingSecondScaleArg() throws Exception {
        subject.parse("--scale", "2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeXScaleArg() throws Exception {
        subject.parse("--scale", String.valueOf(MIN_FREE_SCALE_FACTOR - 1) + ",1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeYScaleArg() throws Exception {
        subject.parse("--scale", "1," + String.valueOf(MAX_FREE_SCALE_FACTOR + 1));
    }

    @Test
    public void shouldScaleDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getScaleX().isPresent());
        assertFalse(subject.getScaleY().isPresent());
    }

    @Test
    public void shouldParseExportPalette() throws Exception {
        subject.parse("--export-palette");
        assertTrue(subject.isExportPalette().get());
    }

    @Test
    public void shouldExportPaletteDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isExportPalette().isPresent());
    }

    @Test
    public void shouldParseExportForcedSubtitlesOnly() throws Exception {
        subject.parse("--forced-only");
        assertTrue(subject.isExportForcedSubtitlesOnly().get());
    }

    @Test
    public void shouldExportForcedSubtitlesOnlyDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isExportForcedSubtitlesOnly().isPresent());
    }

    @Test
    public void shouldParseForceAllArg() throws Exception {
        subject.parse("--force-all", "set");
        assertEquals(ForcedFlagState.SET, subject.getForcedFlagState().get());

        subject.parse("--force-all", "clear");
        assertEquals(ForcedFlagState.CLEAR, subject.getForcedFlagState().get());
    }

    @Test
    public void shouldForceAllDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getForcedFlagState().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidForcedFlagArg() throws Exception {
        subject.parse("--force-all", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingForcedFlagArg() throws Exception {
        subject.parse("--force-all");
    }

    @Test
    public void shouldParseSwapCrCbArg() throws Exception {
        subject.parse("--swap");
        assertTrue(subject.isSwapCrCb().get());
    }

    @Test
    public void shouldSwapCrCbDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isSwapCrCb().isPresent());
    }

    @Test
    public void shouldParseFixInvisibleArg() throws Exception {
        subject.parse("--fix-invisible");
        assertTrue(subject.isFixInvisibleFrames().get());
    }

    @Test
    public void shouldFixInvisibleDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isFixInvisibleFrames().isPresent());
    }

    @Test
    public void shouldParseVerboseArg() throws Exception {
        subject.parse("--verbose");
        assertTrue(subject.isVerbose().get());
    }

    @Test
    public void shouldVerboseDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isVerbose().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingAlphaThresholdArg() throws Exception {
        subject.parse("--alpha-thr");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeAlphaThresholdArg() throws Exception {
        subject.parse("--alpha-thr", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeAlphaThresholdArg() throws Exception {
        subject.parse("--alpha-thr", "256");
    }

    @Test
    public void shouldAlphaThresholdDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getAlphaThreshold().isPresent());
    }

    @Test
    public void shouldAcceptValidAlphaThresholdArg() throws Exception {
        subject.parse("--alpha-thr", "75");
        assertEquals(75, subject.getAlphaThreshold().get().intValue());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeLuminanceLowMedThresholdArg() throws Exception {
        subject.parse("--lum-low-med-thr", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeLuminanceMedHighThresholdArg() throws Exception {
        subject.parse("--lum-med-hi-thr", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeLuminanceLowMedThresholdArg() throws Exception {
        subject.parse("--lum-low-med-thr", "256");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeLuminanceMedHighThresholdArg() throws Exception {
        subject.parse("--lum-med-hi-thr", "256");
    }

    @Test
    public void shouldLuminanceLowMedThresholdDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getLumLowMedThreshold().isPresent());
    }

    @Test
    public void shouldLuminanceMedHighThresholdDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getLumMedHighThreshold().isPresent());
    }

    @Test
    public void shouldAcceptValidLuminanceLowMedThresholdArg() throws Exception {
        subject.parse("--lum-low-med-thr", "75");
        assertEquals(75, subject.getLumLowMedThreshold().get().intValue());
    }

    @Test
    public void shouldAcceptValidLuminanceMedHighThresholdArg() throws Exception {
        subject.parse("--lum-med-hi-thr", "230");
        assertEquals(230, subject.getLumMedHighThreshold().get().intValue());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectLuminanceLowMedBeingGreaterThanMedHigh() throws Exception {
        subject.parse("--lum-low-med-thr", "75", "--lum-med-hi-thr", "50");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMissingLanguageCodeArg() throws Exception {
        subject.parse("--language");
    }

    @Test
    public void shouldParseLanguageCodeArg() throws Exception {
        subject.parse("--language", "de");
        assertTrue(subject.getLanguageIndex().get() > 0 && subject.getLanguageIndex().get() < Constants.LANGUAGES.length - 1);
    }

    @Test
    public void shouldLanguageCodeDefaultToAbsent() throws Exception {
        subject.parse("--version");
        assertFalse(subject.getLanguageIndex().isPresent());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNonExistentPaletteFileArg() throws Exception {
        subject.parse("--palette-file", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingPaletteFileArg() throws Exception {
        subject.parse("--palette-file");
    }

    @Test
    public void shouldAcceptValidPaletteFileArg() throws Exception {
        File paletteFile = File.createTempFile("paletteFile", null);
        paletteFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(paletteFile);
        fos.write("#COL".getBytes());
        fos.close();

        subject.parse("--palette-file", paletteFile.getAbsolutePath());

        assertEquals(paletteFile, subject.getPaletteFile());
    }
}
