/*
 * Copyright 2014 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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

import java.util.Arrays;
import java.util.List;

/**
 * Extends SubPicture to store information read from DVD (SUB/IDX or SUP/IFO)
 */
public class SubPictureDVD extends SubPicture {

    /** offset to information in SUB file */
    private long offset;
    /** size of RLE buffer */
    private int rleSize;
    /** even line offset (inside RLE buffer) */
    private int evenOffset;
    /** odd line offset (inside RLE buffer) */
    private int oddOffset;
    /** list of RLE fragments */
    private List<ImageObjectFragment> rleFragments;
    /** uncropped bitmap width */
    private int originalWidth;
    /** uncropped bitmap height */
    private int originalHeight;
    /** original x offset of uncropped bitmap */
    private int originalX;
    /** original y offset of uncropped bitmap */
    private int originalY;
    /** 4 original alpha values */
    private int[] originalAlpha;
    /** 4 original palette values*/
    private int[] originalPal;
    /** 4 alpha values */
    private int[] alpha;
    /** 4 palette values */
    private int[] pal;

    public void storeOriginal() {
        originalWidth = getImageWidth();
        originalHeight = getImageHeight();
        originalX = getXOffset();
        originalY = getYOffset();

        originalAlpha = Arrays.copyOf(alpha, alpha.length);
        originalPal = Arrays.copyOf(pal, pal.length);
    }

    /**
     * Copy info of given generic subpicture into this DVD subpicture
     * Used to copy the edited info (position, forced flags etc. into a DVD SubPicture for writing
     * @param pic
     */
    public void copyInfo(SubPicture pic) {
        setWidth(pic.getWidth());
        setHeight(pic.getHeight());
        setStartTime(pic.getStartTime());
        setEndTime(pic.getEndTime());
        setForced(pic.isForced());
        setCompositionNumber(pic.getCompositionNumber());
        setImageWidth(pic.getImageWidth());
        setImageHeight(pic.getImageHeight());
        setOfsX(pic.getXOffset());
        setOfsY(pic.getYOffset());
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getRleSize() {
        return rleSize;
    }

    public void setRleSize(int rleSize) {
        this.rleSize = rleSize;
    }

    public int getEvenOffset() {
        return evenOffset;
    }

    public void setEvenOffset(int evenOffset) {
        this.evenOffset = evenOffset;
    }

    public int getOddOffset() {
        return oddOffset;
    }

    public void setOddOffset(int oddOffset) {
        this.oddOffset = oddOffset;
    }

    public List<ImageObjectFragment> getRleFragments() {
        return rleFragments;
    }

    public void setRleFragments(List<ImageObjectFragment> rleFragments) {
        this.rleFragments = rleFragments;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public int getOriginalX() {
        return originalX;
    }

    public int getOriginalY() {
        return originalY;
    }

    public int[] getOriginalAlpha() {
        return originalAlpha;
    }

    public int[] getOriginalPal() {
        return originalPal;
    }

    public int[] getAlpha() {
        return alpha;
    }

    public void setAlpha(int[] alpha) {
        this.alpha = alpha;
    }

    public int[] getPal() {
        return pal;
    }

    public void setPal(int[] pal) {
        this.pal = pal;
    }
}
