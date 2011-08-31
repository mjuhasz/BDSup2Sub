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
 * Storage class holding information to erase a rectangular part of the caption.
 *
 * @author 0xdeadbeef
 */
public class ErasePatch {

	/**
	 * Constructor.
	 * @param xp X coordinate of patch
	 * @param yp Y coordinate of patch
	 * @param wp Width of patch
	 * @param hp Height of patch
	 */
	public ErasePatch(int xp, int yp, int wp, int hp) {
		x = xp;
		y = yp;
		w = wp;
		h = hp;
	}

	/** X coordinate of patch */
	public int x;
	/** Y coordinate of patch */
	public int y;
	/** Width of patch */
	public int w;
	/** Height of patch */
	public int h;
}
