package deadbeef.supstream;

import deadbeef.bitmap.Palette;
import deadbeef.core.Core;

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
 * Interface to handle SUB/IDX and SUP/IFO the same way where possible.
 *
 * @author 0xdeadbeef
 */
public interface SubstreamDVD extends Substream {

	/**
	 * Get frame alpha values.
	 * @param index Index of caption
	 * @return Integer array with 4 entries representing the frame alpha values
	 */
	abstract int[] getFrameAlpha(int index);

	/**
	 * Get frame Palette.
	 * @param index Index of caption
	 * @return Integer array with 4 entries representing the frame palette
	 */
	abstract int[] getFramePal(int index);

	/**
	 * Get original frame alpha values (as they were before editing).
	 * @param index Index of caption
	 * @return Integer array with 4 entries representing the original frame alpha values.
	 */
	abstract int[] getOriginalFrameAlpha(int index);

	/**
	 * Get original frame Palette (as they were before editing).
	 * @param index index of caption
	 * @return Integer array with 4 entries representing the original frame palette.
	 */
	abstract int[] getOriginalFramePal(int index);

	/**
	 * Get imported 16 color DVD Palette.
	 * @return Imported 16 color DVD Palette.
	 */
	abstract Palette getSrcPalette();

	/**
	 * Replace imported 16 color DVD Palette with a new Palette.
	 * @param pal New Palette
	 */
	abstract void setSrcPalette(Palette pal);

	/**
	 * Get language index read from Idx.
	 * @return language index.
	 * @see Core#getLanguages()
	 */
	abstract int getLanguageIdx();

}
