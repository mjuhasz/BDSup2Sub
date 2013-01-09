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
package bdsup2sub.bitmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitmapBoundsTest {

    private BitmapBounds subject;
    private int minX = 1;
    private int maxX = 2;
    private int minY = 3;
    private int maxY = 4;

    @Test
    public void shouldInitializeBitmapBounds() {
        subject = new BitmapBounds(minX, maxX, minY, maxY);
        assertEquals(minX, subject.xMin);
        assertEquals(maxX, subject.xMax);
        assertEquals(minY, subject.yMin);
        assertEquals(maxY, subject.yMax);
    }
}
