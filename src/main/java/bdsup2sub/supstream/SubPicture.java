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
package bdsup2sub.supstream;

import bdsup2sub.bitmap.ErasePatch;

import java.util.ArrayList;
import java.util.List;

public class SubPicture {

    /** width of subtitle image */
    private int imageWidth;
    /** height of subtitle image */
    private int imageHeight;
    /** upper left corner of subtitle x */
    private int xOffset;
    /** upper left corner of subtitle y */
    private int yOffset;

    /** screen width */
    private int width;
    /** screen height */
    private int height;
    /** start time in milliseconds */
    private long startTime;
    /** end time in milliseconds */
    private long endTime;
    /** if true, this is a forced subtitle */
    private boolean forced;
    /** composition number - increased at start and end PCS */
    private int compositionNumber;
    /** frame was already decoded */
    private boolean wasDecoded;

    /* the following fields are really only needed for editing */

    /** exclude from export? */
    private boolean excluded;
    /** list of erase patches */
    private List<ErasePatch> erasePatch = new ArrayList<ErasePatch>();

    public SubPicture() {
    }

    public SubPicture(SubPicture other) {
        /* Note that by using the getter functions
         * the internal values of a SubPictureBD are
         * copied into the plain members of the
         * SubPicture object.
         */
        this.imageWidth = other.getImageWidth();
        this.imageHeight = other.getImageHeight();
        this.xOffset = other.getXOffset();
        this.yOffset = other.getYOffset();
        this.imageWidth = other.imageWidth;
        this.imageHeight = other.imageHeight;
        this.xOffset = other.xOffset;
        this.yOffset = other.yOffset;

        this.width = other.width;
        this.height = other.height;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.forced = other.forced;
        this.compositionNumber = other.compositionNumber;
        this.wasDecoded = other.wasDecoded;
        this.excluded = other.excluded;
        if (other.erasePatch != null) {
            this.erasePatch = new ArrayList<ErasePatch>(other.erasePatch);
        }
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setImageWidth(int w) {
        imageWidth = w;
    }

    public void setImageHeight(int h) {
        imageHeight = h;
    }

    public void setOfsX(int ofs) {
        xOffset = ofs;
    }

    public void setOfsY(int ofs) {
        yOffset = ofs;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public int getCompositionNumber() {
        return compositionNumber;
    }

    public void setCompositionNumber(int compositionNumber) {
        this.compositionNumber = compositionNumber;
    }

    public boolean isWasDecoded() {
        return wasDecoded;
    }

    public void setWasDecoded(boolean wasDecoded) {
        this.wasDecoded = wasDecoded;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public List<ErasePatch> getErasePatch() {
        return erasePatch;
    }

    public void setErasePatch(List<ErasePatch> erasePatch) {
        this.erasePatch = erasePatch;
    }
}
