/*
 * Copyright 2013 Miklos Juhasz (mjuhasz)
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
package bdsup2sub.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToolBoxTest {

    @Test
    public void shouldLeftPadWithZeros() {
        assertEquals("0023", ToolBox.leftZeroPad(23, 4));
        assertEquals("23", ToolBox.leftZeroPad(23, 2));
    }

    @Test
    public void shouldConvertToHexLeftZeroPadded() {
        assertEquals("0x00000012", ToolBox.toHexLeftZeroPadded(18, 8));
    }

    @Test
    public void shouldFormatDouble() {
        assertEquals("25", ToolBox.formatDouble(25));
        assertEquals("23.976", ToolBox.formatDouble(24000.0/1001));
    }
}
