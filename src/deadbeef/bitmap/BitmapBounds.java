package deadbeef.bitmap;

/*
 * Copyright 2009 Volker Oth (0xdeadbeef)
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

/**
 * Storage class used by {@link Bitmap} to return image bounds.
 *
 * @author 0xdeadbeef
 */

public class BitmapBounds {

    /** X coordinate of first column that contains visible pixels. */
    public final int xMin;
    /** X coordinate of last column that contains visible pixels. */
    public final int xMax;
    /** Y coordinate of first line that contains visible pixels. */
    public final int yMin;
    /** Y coordinate of last line that contains visible pixels. */
    public final int yMax;

    /**
     * @param xMin x coordinate of first column that contains visible pixels
     * @param xMax x coordinate of last column that contains visible pixels
     * @param yMin y coordinate of first line that contains visible pixels
     * @param yMax y coordinate of last line that contains visible pixels
     */
    public BitmapBounds(int xMin, int xMax, int yMin, int yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
}
