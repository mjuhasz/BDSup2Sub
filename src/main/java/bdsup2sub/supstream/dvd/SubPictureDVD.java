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
package bdsup2sub.supstream.dvd;

import bdsup2sub.supstream.ImageObjectFragment;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.supstream.hd.SubPictureHD;

import java.util.ArrayList;

/**
 * Extends SubPicture to store information read from DVD (SUB/IDX or SUP/IFO)
 */
public class SubPictureDVD extends SubPicture implements Cloneable {

    /** offset to information in SUB file */
    public long offset;
    /** size of RLE buffer */
    public int rleSize;
    /** even line offset (inside RLE buffer) */
    public int evenOfs;
    /** odd line offset (inside RLE buffer) */
    public int oddOfs;
    /** list of RLE fragments */
    public ArrayList<ImageObjectFragment> rleFragments;
    /** uncropped bitmap width */
    public int originalWidth;
    /** uncropped bitmap height */
    public int originalHeight;
    /** original x offset of uncropped bitmap */
    int originalX;
    /** original y offset of uncropped bitmap */
    int originalY;
    /** 4 original alpha values */
    int originalAlpha[];
    /** 4 original palette values*/
    int originalPal[];
    /** 4 alpha values */
    public int alpha[];
    /** 4 palette values */
    public int pal[];

    /* (non-Javadoc)
     * @see SubPicture#clone()
     */
    @Override
    public SubPictureHD clone() {
        return (SubPictureHD)super.clone();
    }

    /**
     * store original sizes and offsets
     */
    void setOriginal() {
        originalWidth = getImageWidth();
        originalHeight = getImageHeight();
        originalX = getOfsX();
        originalY = getOfsY();

        originalAlpha = new int[4];
        originalPal = new int[4];
        for (int i=0; i < 4; i++) {
            originalAlpha[i] = alpha[i];
            originalPal[i] = pal[i];
        }
    }

    /**
     * Copy info of given generic subpicture into this DVD subpicture
     * Used to copy the edited info (position, forced flags etc. into a DVD SubPicture for writing
     * @param pic
     */
    public void copyInfo(final SubPicture pic) {
        width = pic.width;
        height = pic.height;
        startTime = pic.startTime;
        endTime = pic.endTime;
        isforced = pic.isforced;
        compNum = pic.compNum;
        setImageWidth(pic.getImageWidth());
        setImageHeight(pic.getImageHeight());
        setOfsX(pic.getOfsX());
        setOfsY(pic.getOfsY());
    }
}
