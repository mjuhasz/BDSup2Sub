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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static bdsup2sub.core.Configuration.*;
import static org.junit.Assert.*;

public class CommandLineParserTest {

    private CommandLineParser subject;

    @Before
    public void setUp() {
        subject = new CommandLineParser();
    }

    @Test
    public void shouldParseEmptyArgList() throws Exception {
        subject.parse("");
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
        assertFalse(subject.isVerbose());
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
        assertEquals(Resolution.NTSC, subject.getResolution());
    }

    @Test
    public void shouldParseNtscResolutionWithTextArg() throws Exception {
        subject.parse("--resolution", "ntsc");
        assertEquals(Resolution.NTSC, subject.getResolution());
    }

    @Test
    public void shouldParsePalResolutionWithNumericArg() throws Exception {
        subject.parse("--resolution", "576");
        assertEquals(Resolution.PAL, subject.getResolution());
    }

    @Test
    public void shouldParsePalResolutionWithTextArg() throws Exception {
        subject.parse("--resolution", "pal");
        assertEquals(Resolution.PAL, subject.getResolution());
    }

    @Test
    public void shouldParse720ResolutionArg() throws Exception {
        subject.parse("--resolution", "720");
        assertEquals(Resolution.HD_720, subject.getResolution());
    }

    @Test
    public void shouldParse720pResolutionArg() throws Exception {
        subject.parse("--resolution", "720p");
        assertEquals(Resolution.HD_720, subject.getResolution());
    }

    @Test
    public void shouldParse1080ResolutionArg() throws Exception {
        subject.parse("--resolution", "1080");
        assertEquals(Resolution.HD_1080, subject.getResolution());
    }

    @Test
    public void shouldParse1080pResolutionArg() throws Exception {
        subject.parse("--resolution", "1080p");
        assertEquals(Resolution.HD_1080, subject.getResolution());
    }

    @Test
    public void shouldParse1440x1080ResolutionArg() throws Exception {
        subject.parse("--resolution", "1440x1080");
        assertEquals(Resolution.HD_1440x1080, subject.getResolution());
    }

    @Test
    public void shouldParseKeepResolutionArg() throws Exception {
        subject.parse("--resolution", "keep");
        assertNull(subject.getResolution());
    }

    @Test
    public void shouldResolutionDefaultToKeep() throws Exception {
        subject.parse("--version");
        assertNull(subject.getResolution());
    }

    @Test
    public void shouldTargetFrameRateDefaultToKeep() throws Exception {
        subject.parse("--version");
        assertNull(subject.getTargetFrameRate());
    }

    @Test
    public void shouldSourceFrameRateDefaultToAuto() throws Exception {
        subject.parse("--version");
        assertNull(subject.getSourceFrameRate());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectConvertAndTargetFramerateAtTheSameTime() throws Exception {
        subject.parse("--convertfps", "15, 25", "--targetfps", "24");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectConvertFramerateIfTargetFramerateIsMissing() throws Exception {
        subject.parse("--convertfps", "15");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingConvertFramerateArgs() throws Exception {
        subject.parse("--convertfps");
    }

    @Test
    public void shouldAcceptValidConvertFramerateArgs() throws Exception {
        subject.parse("--convertfps", "15, 25");
        assertEquals(15, subject.getSourceFrameRate().doubleValue(), 0);
        assertEquals(25, subject.getTargetFrameRate().doubleValue(), 0);
    }

    @Test
    public void shouldAcceptAutoAsSourceForConvertFramerateArgs() throws Exception {
        subject.parse("--convertfps", "auto, 25");
        assertNull(subject.getSourceFrameRate());
        assertEquals(25, subject.getTargetFrameRate().doubleValue(), 0);
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidSourceFramerateForConvertFramerateArgs() throws Exception {
        subject.parse("--convertfps", "-1, 25");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidTargetFramerateForConvertFramerateArgs() throws Exception {
        subject.parse("--convertfps", "15, -1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidDelayArg() throws Exception {
        subject.parse("--delay", "foo");
    }

    @Test
    public void shouldDelayDefaultToZero() throws Exception {
        subject.parse("--version");
        assertEquals(0, subject.getDelay(), 0);
    }

    @Test
    public void shouldAcceptValidDelayArg() throws Exception {
        subject.parse("--delay", "-200");
        assertEquals(-200, subject.getDelay(), 0);
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
    public void shouldFilterDefaultToBilinear() throws Exception {
        subject.parse("--version");
        assertEquals(ScalingFilter.BILINEAR, subject.getScalingFilter());
    }

    @Test
    public void shouldParseFilterWithValidName() throws Exception {
        subject.parse("--filter", "Bicubic-Spline");
        assertEquals(ScalingFilter.BICUBIC_SPLINE, subject.getScalingFilter());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingPaletteModeArg() throws Exception {
        subject.parse("--palettemode");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidPaletteModeArg() throws Exception {
        subject.parse("--palettemode", "foo");
    }

    @Test
    public void shouldPaletteModeDefaultToCreateNew() throws Exception {
        subject.parse("--version");
        assertEquals(PaletteMode.CREATE_NEW, subject.getPaletteMode());
    }

    @Test
    public void shouldParsePaletteModeWithValidArg() throws Exception {
        subject.parse("--palettemode", "keep");
        assertEquals(PaletteMode.KEEP_EXISTING, subject.getPaletteMode());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMinimumDisplayTimeArg() throws Exception {
        subject.parse("--mindisptime", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMinimumDisplayTimeIfNotPositive() throws Exception {
        subject.parse("--mindisptime", "-100");
    }

    @Test
    public void shouldMinimumDisplayTimeDefaultToNull() throws Exception {
        subject.parse("--version");
        assertNull(subject.getMinimumDisplayTime());
    }

    @Test
    public void shouldAcceptValidMinimumDisplayTimeArg() throws Exception {
        subject.parse("--mindisptime", "900");
        assertEquals(900, subject.getMinimumDisplayTime().doubleValue(), 0);
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMinimumDisplayTimeArg() throws Exception {
        subject.parse("--mindisptime");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMaximumMergeTimeDifferenceArg() throws Exception {
        subject.parse("--maxtimediff", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMaximumMergeTimeDifferenceIfNegative() throws Exception {
        subject.parse("--maxtimediff", "-100");
    }

    @Test
    public void shouldMaximumMergeTimeDifferenceDefaultTo200ms() throws Exception {
        subject.parse("--version");
        assertEquals(200, subject.getMaximumTimeDifference(), 0);
    }

    @Test
    public void shouldAcceptValidMaximumMergeTimeDifferenceArg() throws Exception {
        subject.parse("--maxtimediff", "900");
        assertEquals(900, subject.getMaximumTimeDifference(), 0);
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMaximumMergeTimeDifferenceArg() throws Exception {
        subject.parse("--maxtimediff");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMoveInAndMoveOutArgsAtTheSameTime() throws Exception {
        subject.parse("--movein", "12,2", "--moveout", "13,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveInArg() throws Exception {
        subject.parse("--movein", "foo,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveOutArg() throws Exception {
        subject.parse("--moveout", "foo,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveInOffsetArg() throws Exception {
        subject.parse("--movein", "2,foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveOutOffsetArg() throws Exception {
        subject.parse("--moveout", "2,foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMoveInArg() throws Exception {
        subject.parse("--movein");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMoveOutArg() throws Exception {
        subject.parse("--moveout");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMoveInArgIfMissingOffsetArg() throws Exception {
        subject.parse("--movein", "20");
    }
    @Test(expected = ParseException.class)
    public void shouldRejectMoveOutArgIfMissingOffsetArg() throws Exception {
        subject.parse("--moveout", "20");
    }

    @Test
    public void shouldBeInMoveInModeIfMoveInArgIsDefined() throws Exception {
        subject.parse("--movein", "2,12");
        assertEquals(CaptionMoveModeY.MOVE_INSIDE_BOUNDS, subject.getMoveModeY());
    }

    @Test
    public void shouldBeInMoveOutModeIfMoveOutArgIsDefined() throws Exception {
        subject.parse("--moveout", "2,12");
        assertEquals(CaptionMoveModeY.MOVE_OUTSIDE_BOUNDS, subject.getMoveModeY());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeMoveInArg() throws Exception {
        subject.parse("--movein", "1,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeMoveOutArg() throws Exception {
        subject.parse("--moveout", "1,2");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveXArg() throws Exception {
        subject.parse("--movex", "foo,2");
    }

    @Test
    public void shouldMoveYModeDefaultToKeep() throws Exception {
        subject.parse("--version");
        assertEquals(CaptionMoveModeY.KEEP_POSITION, subject.getMoveModeY());
        assertEquals(10, subject.getMoveYOffset(), 0);
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidMoveXOffsetArg() throws Exception {
        subject.parse("--movex", "2,foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingMoveXArg() throws Exception {
        subject.parse("--movex");
    }

    @Test
    public void shouldParseMoveXModeWithValidArgs() throws Exception {
        subject.parse("--movex", "left,12");
        assertEquals(CaptionMoveModeX.LEFT, subject.getMoveModeX());
        assertEquals(12, subject.getMoveXOffset(), 0);
    }

    @Test
    public void shouldParseMoveXModeWithoutOffset() throws Exception {
        subject.parse("--movex", "right");
        assertEquals(CaptionMoveModeX.RIGHT, subject.getMoveModeX());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeMoveXOffset() throws Exception {
        subject.parse("--movein", "right,-2");
    }

    @Test
    public void shouldMoveXModeDefaultToKeep() throws Exception {
        subject.parse("--version");
        assertEquals(CaptionMoveModeX.KEEP_POSITION, subject.getMoveModeX());
        assertEquals(10, subject.getMoveXOffset(), 0);
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidCropLinesArg() throws Exception {
        subject.parse("--croplines", "-1");
    }

    @Test
    public void shouldCropLinesDefaultToZero() throws Exception {
        subject.parse("--version");
        assertEquals(0, subject.getCropLines());
    }

    @Test
    public void shouldAcceptValidCropLinesArg() throws Exception {
        subject.parse("--croplines", "75");
        assertEquals(75, subject.getCropLines());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeAlphaCropThresholdArg() throws Exception {
        subject.parse("--alphacropthr", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeAlphaCropThresholdArg() throws Exception {
        subject.parse("--alphacropthr", "256");
    }

    @Test
    public void shouldAlphaThresholdDefaultTo14() throws Exception {
        subject.parse("--version");
        assertEquals(DEFAULT_ALPHA_CROP_THRESHOLD, subject.getAlphaCropThreshold());
    }

    @Test
    public void shouldAcceptValidAlphaCropThresholdArg() throws Exception {
        subject.parse("--alphacropthr", "75");
        assertEquals(75, subject.getAlphaCropThreshold());
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
    public void shouldScaleHaveDefault() throws Exception {
        subject.parse("--version");
        assertEquals(DEFAULT_FREE_SCALE_FACTOR_X, subject.getScaleX(), 0);
        assertEquals(DEFAULT_FREE_SCALE_FACTOR_Y, subject.getScaleY(), 0);
    }

    @Test
    public void shouldParseExportPalette() throws Exception {
        subject.parse("--exppal");
        assertTrue(subject.isExportPalette());
    }

    @Test
    public void shouldExportPaletteDefaultToFalse() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isExportPalette());
    }

    @Test
    public void shouldParseExportForcedSubtitlesOnly() throws Exception {
        subject.parse("--forcedonly");
        assertTrue(subject.isExportForcedSubtitlesOnly());
    }

    @Test
    public void shouldExportForcedSubtitlesOnlyDefaultToFalse() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isExportForcedSubtitlesOnly());
    }

    @Test
    public void shouldParseForceAllArg() throws Exception {
        subject.parse("--forcedflag", "set");
        assertEquals(ForcedFlagState.SET, subject.getForcedFlagState());

        subject.parse("--forcedflag", "clear");
        assertEquals(ForcedFlagState.CLEAR, subject.getForcedFlagState());
    }

    @Test
    public void shouldForceAllDefaultToKeep() throws Exception {
        subject.parse("--version");
        assertEquals(ForcedFlagState.KEEP, subject.getForcedFlagState());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectInvalidForcedFlagArg() throws Exception {
        subject.parse("--forcedflag", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingForcedFlagArg() throws Exception {
        subject.parse("--forcedflag");
    }

    @Test
    public void shouldParseSwapCrCbArg() throws Exception {
        subject.parse("--swap");
        assertTrue(subject.isSwapCrCb());
    }

    @Test
    public void shouldSwapCrCbDefaultToFalse() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isSwapCrCb());
    }

    @Test
    public void shouldParseFixInvisibleArg() throws Exception {
        subject.parse("--fixinv");
        assertTrue(subject.isFixInvisibleFrames());
    }

    @Test
    public void shouldFixInvisibleDefaultToFalse() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isFixInvisibleFrames());
    }

    @Test
    public void shouldParseVerboseArg() throws Exception {
        subject.parse("--verbose");
        assertTrue(subject.isVerbose());
    }

    @Test
    public void shouldVerboseDefaultToFalse() throws Exception {
        subject.parse("--version");
        assertFalse(subject.isVerbose());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingAlphaThresholdArg() throws Exception {
        subject.parse("--alphathr");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeAlphaThresholdArg() throws Exception {
        subject.parse("--alphathr", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeAlphaThresholdArg() throws Exception {
        subject.parse("--alphathr", "256");
    }

    @Test
    public void shouldAlphaThresholdDefaultTo80() throws Exception {
        subject.parse("--version");
        assertEquals(DEFAULT_ALPHA_THRESHOLD, subject.getAlphaThreshold());
    }

    @Test
    public void shouldAcceptValidAlphaThresholdArg() throws Exception {
        subject.parse("--alphathr", "75");
        assertEquals(75, subject.getAlphaThreshold());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeLuminanceLowMidThresholdArg() throws Exception {
        subject.parse("--lumlowmidthr", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNegativeLuminanceMidHighThresholdArg() throws Exception {
        subject.parse("--lummidhighthr", "-1");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeLuminanceLowMidThresholdArg() throws Exception {
        subject.parse("--lumlowmidthr", "256");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectOutOfRangeLuminanceMidHighThresholdArg() throws Exception {
        subject.parse("--lummidhighthr", "256");
    }

    @Test
    public void shouldLuminanceLowMidThresholdDefaultTo160() throws Exception {
        subject.parse("--version");
        assertEquals(DEFAULT_LUMINANCE_LOW_MID_THRESHOLD, subject.getLumLowMidThreshold());
    }

    @Test
    public void shouldLuminanceMidHighThresholdDefaultTo210() throws Exception {
        subject.parse("--version");
        assertEquals(DEFAULT_LUMINANCE_MID_HIGH_THRESHOLD, subject.getLumMidHighThreshold());
    }

    @Test
    public void shouldAcceptValidLuminanceLowMidThresholdArg() throws Exception {
        subject.parse("--lumlowmidthr", "75");
        assertEquals(75, subject.getLumLowMidThreshold());
    }

    @Test
    public void shouldAcceptValidLuminanceMidHighThresholdArg() throws Exception {
        subject.parse("--lummidhighthr", "230");
        assertEquals(230, subject.getLumMidHighThreshold());
    }

    @Test(expected = ParseException.class)
    public void shouldRejectLuminanceLowMidBeingGreaterThanMidHigh() throws Exception {
        subject.parse("--lumlowmidthr", "75", "--lummidhighthr", "50");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectMissingLanguageCodeArg() throws Exception {
        subject.parse("--langcode");
    }

    @Test
    public void shouldParseLanguageCodeArg() throws Exception {
        subject.parse("--langcode", "de");
        assertTrue(subject.getLanguageIndex() > 0 && subject.getLanguageIndex() < Constants.LANGUAGES.length - 1);
    }

    @Test
    public void shouldLanguageCodeDefaultToEnglish() throws Exception {
        subject.parse("--version");
        assertEquals(0, subject.getLanguageIndex());
        assertEquals("en", Constants.LANGUAGES[0][1]);
    }

    @Test(expected = ParseException.class)
    public void shouldRejectNonExistentPaletteFileArg() throws Exception {
        subject.parse("--palettefile", "foo");
    }

    @Test(expected = ParseException.class)
    public void shouldRejectIfMissingPaletteFileArg() throws Exception {
        subject.parse("--palettefile");
    }

    @Test
    public void shouldAcceptValidPaletteFileArg() throws Exception {
        File paletteFile = File.createTempFile("paletteFile", null);
        paletteFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(paletteFile);
        fos.write(new byte[] { 0x23, 0x43, 0x4F, 0x4C });
        fos.close();

        subject.parse("--palettefile", paletteFile.getAbsolutePath());

        assertEquals(paletteFile, subject.getPaletteFile());
    }
}
