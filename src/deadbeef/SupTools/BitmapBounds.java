package deadbeef.SupTools;

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

	/**
	 * Constructor.
	 * @param minX x coordinate of first column that contains visible pixels
	 * @param maxX x coordinate of last column that contains visible pixels
	 * @param minY y coordinate of first line that contains visible pixels
	 * @param maxY y coordinate of last line that contains visible pixels
	 */
	public BitmapBounds(int minX, int maxX, int minY, int maxY) {
		xMin = minX;
		xMax = maxX;
		yMin = minY;
		yMax = maxY;
	}

	/** X coordinate of first column that contains visible pixels. */
	public int xMin;
	/** X coordinate of last column that contains visible pixels. */
	public int xMax;
	/** Y coordinate of first line that contains visible pixels. */
	public int yMin;
	/** Y coordinate of last line that contains visible pixels. */
	public int yMax;

}
