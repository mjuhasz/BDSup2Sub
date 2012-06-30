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
        String option = "load-settings";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
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
    public void shouldHaveConvertFramerateOption() {
        String option = "convertfps";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertEquals(2, options.getOption(option).getArgs());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals(',', options.getOption(option).getValueSeparator());
        assertEquals("<src>, <trg>", options.getOption(option).getArgName());
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
        assertEquals("palettemode", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("mode", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMinimumDisplayTimeOption() {
        String option = "m";
        assertTrue(options.hasOption(option));
        assertEquals("mindisptime", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("time", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMaximumTimeDifferenceOption() {
        String option = "x";
        assertTrue(options.hasOption(option));
        assertEquals("maxtimediff", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("time", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveMoveInOption() {
        String option = "I";
        assertTrue(options.hasOption(option));
        assertEquals("movein", options.getOption(option).getLongOpt());
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
        assertEquals("moveout", options.getOption(option).getLongOpt());
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
        assertEquals("movex", options.getOption(option).getLongOpt());
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
        assertEquals("croplines", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveAlphaCropThresholdOption() {
        String option = "a";
        assertTrue(options.hasOption(option));
        assertEquals("alphacropthr", options.getOption(option).getLongOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveScaleOption() {
        String option = "scale";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertEquals(2, options.getOption(option).getArgs());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals(',', options.getOption(option).getValueSeparator());
        assertEquals("x, y", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveExportPaletteOption() {
        String option = "exppal";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveExportForcedSubtitlesOnlyOption() {
        String option = "forcedonly";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveForcedFlagStateOption() {
        String option = "forcedflag";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("state", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveSwapCrCbOption() {
        String option = "swap";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveFixInvisibleFramesOption() {
        String option = "fixinv";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertFalse(options.getOption(option).hasArg());
    }

    @Test
    public void shouldHaveAlphaThresholdOption() {
        String option = "alphathr";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveLuminanceLowMidThresholdOption() {
        String option = "lumlowmidthr";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveLuminanceMidHighThresholdOption() {
        String option = "lummidhighthr";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("n", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHaveLanguageCodeOption() {
        String option = "langcode";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("langcode", options.getOption(option).getArgName());
    }

    @Test
    public void shouldHavePaletteFileOption() {
        String option = "palettefile";
        assertTrue(options.hasOption(option));
        assertNull(options.getOption(option).getOpt());
        assertFalse(options.getOption(option).isRequired());
        assertTrue(options.getOption(option).hasArg());
        assertFalse(options.getOption(option).hasOptionalArg());
        assertEquals("file", options.getOption(option).getArgName());
    }
}
