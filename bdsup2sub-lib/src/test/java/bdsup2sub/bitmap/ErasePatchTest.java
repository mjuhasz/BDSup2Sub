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
package bdsup2sub.bitmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErasePatchTest {

    private ErasePatch subject;
    private int x = 1;
    private int y = 2;
    private int width = 3;
    private int height = 4;

    @Test
    public void shouldInitializeErasePatch() {
        subject = new ErasePatch(x, y, width, height);
        assertEquals(x, subject.x);
        assertEquals(y, subject.y);
        assertEquals(width, subject.width);
        assertEquals(height, subject.height);
    }
}
