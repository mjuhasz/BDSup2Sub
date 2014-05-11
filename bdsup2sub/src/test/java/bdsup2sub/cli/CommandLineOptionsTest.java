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

import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommandLineOptionsTest {

    private CommandLineOptions subject;
    private Options options;

    @Before
    public void setUp() throws Exception {
        subject = new CommandLineOptions();
        options = subject.getOptions();
    }

    @Test
    public void shouldHaveHelpOption() {
        String option = "h";
        assertTrue(options.hasOption(option));
        assertEquals("help", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveVersionOption() {
        String option = "V";
        assertTrue(options.hasOption(option));
        assertEquals("version", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveOutputOption() {
        String option = "o";
        assertTrue(options.hasOption(option));
        assertEquals("output", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveVerboseOption() {
        String option = "v";
        assertTrue(options.hasOption(option));
        assertEquals("verbose", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveLoadSettingsOption() {
        String option = "L";
        assertTrue(options.hasOption(option));
        assertEquals("load-settings", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveResolutionOption() {
        String option = "r";
        assertTrue(options.hasOption(option));
        assertEquals("resolution", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("resolution", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveTargetFramerateOption() {
        String option = "T";
        assertTrue(options.hasOption(option));
        assertEquals("fps-target", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("fps", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveConvertFramerateOption() {
        String option = "C";
        assertTrue(options.hasOption(option));
        assertEquals("convert-fps", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertEquals(2, options.getOption(option).getArgs());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals(',', options.getOption(option).getValueSeparator());
        assertEquals("src>, <trg", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveFramerateOptionGroup() {
        OptionGroup optionGroup = options.getOptionGroup(options.getOption("T"));
        assertNotNull(optionGroup);
        assertTrue(optionGroup.getOptions().contains(options.getOption("T")));
        assertTrue(optionGroup.getOptions().contains(options.getOption("C")));
    }

    @Test
    public void shouldHaveDelayOption() {
        String option = "d";
        assertTrue(options.hasOption(option));
        assertEquals("delay", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("delay", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveFilterOption() {
        String option = "f";
        assertTrue(options.hasOption(option));
        assertEquals("filter", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("filter", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHavePaletteModeOption() {
        String option = "p";
        assertTrue(options.hasOption(option));
        assertEquals("palette-mode", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("mode", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMinimumDisplayTimeOption() {
        String option = "m";
        assertTrue(options.hasOption(option));
        assertEquals("minimum-time", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("time", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMaximumTimeDifferenceOption() {
        String option = "x";
        assertTrue(options.hasOption(option));
        assertEquals("merge-time", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("time", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMoveInOption() {
        String option = "I";
        assertTrue(options.hasOption(option));
        assertEquals("move-in", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertEquals(2, options.getOption(option).getArgs());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals(',', options.getOption(option).getValueSeparator());
        assertEquals("ratio, offset", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMoveOutOption() {
        String option = "O";
        assertTrue(options.hasOption(option));
        assertEquals("move-out", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertEquals(2, options.getOption(option).getArgs());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals(',', options.getOption(option).getValueSeparator());
        assertEquals("ratio, offset", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMoveOptionGroup() {
        OptionGroup optionGroup = options.getOptionGroup(options.getOption("I"));
        assertNotNull(optionGroup);
        assertTrue(optionGroup.getOptions().contains(options.getOption("I")));
        assertTrue(optionGroup.getOptions().contains(options.getOption("O")));
    }

    @Test
    public void shouldHaveMoveHorizontallyOption() {
        String option = "X";
        assertTrue(options.hasOption(option));
        assertEquals("move-x", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertEquals(2, options.getOption(option).getArgs());
        assertTrue(options.getOption(option).hasOptionalArg());
        assertEquals(',', options.getOption(option).getValueSeparator());
        assertEquals("pos[, offset]", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveCropLinesOption() {
        String option = "c";
        assertTrue(options.hasOption(option));
        assertEquals("crop-y", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveAlphaCropThresholdOption() {
        String option = "a";
        assertTrue(options.hasOption(option));
        assertEquals("alpha-crop", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveScaleOption() {
        String option = "S";
        assertTrue(options.hasOption(option));
        assertEquals("scale", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertEquals(2, options.getOption(option).getArgs());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals(',', options.getOption(option).getValueSeparator());
        assertEquals("x, y", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveExportPaletteOption() {
        String option = "P";
        assertTrue(options.hasOption(option));
        assertEquals("export-palette", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveExportForcedSubtitlesOnlyOption() {
        String option = "D";
        assertTrue(options.hasOption(option));
        assertEquals("forced-only", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveForcedFlagStateOption() {
        String option = "F";
        assertTrue(options.hasOption(option));
        assertEquals("force-all", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("state", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveSwapCrCbOption() {
        String option = "w";
        assertTrue(options.hasOption(option));
        assertEquals("swap", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveFixInvisibleFramesOption() {
        String option = "i";
        assertTrue(options.hasOption(option));
        assertEquals("fix-invisible", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveAlphaThresholdOption() {
        String option = "A";
        assertTrue(options.hasOption(option));
        assertEquals("alpha-thr", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveLuminanceLowMedThresholdOption() {
        String option = "M";
        assertTrue(options.hasOption(option));
        assertEquals("lum-low-med-thr", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveLuminanceMedHighThresholdOption() {
        String option = "H";
        assertTrue(options.hasOption(option));
        assertEquals("lum-med-hi-thr", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveLanguageCodeOption() {
        String option = "l";
        assertTrue(options.hasOption(option));
        assertEquals("language", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("language", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHavePaletteFileOption() {
        String option = "t";
        assertTrue(options.hasOption(option));
        assertEquals("palette-file", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("file", options.getOption(option).getArgName());
    }
}
