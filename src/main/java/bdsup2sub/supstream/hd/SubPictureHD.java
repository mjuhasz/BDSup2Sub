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
    private int paletteOffset;
    /** offset to alpha info for this subpicture in SUP file */
    private int alphaOffset;
    /** size of RLE buffer (odd and even part)*/
    private int imageBufferSize;
    /** offset to even part of RLE buffer in SUP file*/
    private int imageBufferOffsetEven;
    /** offset to odd part of RLE buffer in SUP file*/
    private int imageBufferOffsetOdd;

    @Override
    public SubPictureHD clone() {
        return (SubPictureHD)super.clone();
    }

    public int getPaletteOffset() {
        return paletteOffset;
    }

    public void setPaletteOffset(int paletteOffset) {
        this.paletteOffset = paletteOffset;
    }

    public int getAlphaOffset() {
        return alphaOffset;
    }

    public void setAlphaOffset(int alphaOffset) {
        this.alphaOffset = alphaOffset;
    }

    public int getImageBufferSize() {
        return imageBufferSize;
    }

    public void setImageBufferSize(int imageBufferSize) {
        this.imageBufferSize = imageBufferSize;
    }

    public int getImageBufferOffsetEven() {
        return imageBufferOffsetEven;
    }

    public void setImageBufferOffsetEven(int imageBufferOffsetEven) {
        this.imageBufferOffsetEven = imageBufferOffsetEven;
    }

    public int getImageBufferOffsetOdd() {
        return imageBufferOffsetOdd;
    }

    public void setImageBufferOffsetOdd(int imageBufferOffsetOdd) {
        this.imageBufferOffsetOdd = imageBufferOffsetOdd;
    }
}