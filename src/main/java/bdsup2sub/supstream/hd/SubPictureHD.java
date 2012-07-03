/*
 * Copyright 2012 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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
package bdsup2sub.supstream.hd;

import bdsup2sub.supstream.SubPicture;

/**
 * Extends SubPicture to store information read from HD-DVD SUP
 */
public class SubPictureHD extends SubPicture implements Cloneable {

    /** offset to palette info for this subpicture in SUP file */
    int paletteOfs;
    /** offset to alpha info for this subpicture in SUP file */
    int alphaOfs;
    /** size of RLE buffer (odd and even part)*/
    int imageBufferSize;
    /** offset to even part of RLE buffer in SUP file*/
    int imageBufferOfsEven;
    /** offset to odd part of RLE buffer in SUP file*/
    int imageBufferOfsOdd;

    /* member functions */

    /* (non-Javadoc)
     * @see SubPicture#clone()
     */
    @Override
    public SubPictureHD clone() {
        return (SubPictureHD)super.clone();
    }
}