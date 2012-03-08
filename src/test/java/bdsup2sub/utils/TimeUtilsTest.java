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
package bdsup2sub.utils;

import org.junit.Test;

import static bdsup2sub.utils.TimeUtils.*;
import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {

    @Test
    public void shouldConvertPtsToTimeStr() {
        assertEquals("01:02:03.240", ptsToTimeStr((3600 + 120 + 3) * 90000 + 240 * 90));
    }

    @Test
    public void shouldConvertPtsToTimeStrIdx() {
        assertEquals("01:02:03:240", ptsToTimeStrIdx((3600 + 120 + 3) * 90000 + 240 * 90));
    }

    @Test
    public void shouldConvertPtsToTimeStrXml() {
        assertEquals("01:02:03:06", ptsToTimeStrXml((3600 + 120 + 3) * 90000 + 240 * 90, 25));
    }

    @Test
    public void shouldConvertTimeStrToPts() {
        assertEquals((3600 + 120 + 3) * 90000 + 240 * 90, timeStrToPTS("01:02:03.240"));
        assertEquals((3600 + 120 + 3) * 90000 + 240 * 90, timeStrToPTS("01:02:03:240"));
    }

    @Test
    public void shouldConvertTimeStrXmlToPTS() {
        assertEquals((3600 + 120 + 3) * 90000 + 240 * 90, timeStrXmlToPTS("01:02:03:06", 25));
    }
}
