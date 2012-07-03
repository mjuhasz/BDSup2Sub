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

/**
 * Stores information about one subpicture frame.
 *
 * Note: image related members are private and need getters to allow more complex access functions
 * for BD-SUPs. Indeed the class SubPictureBD doesn't access the image width/height and offsets of the
 * parent class at all. Only when the copy function is used to create a SubPicture copy from a
 * SubPictureBD instance, these members are filled correctly.<br>
 * This also means that the setter functions for these members are pretty much useless as they
 * only change the members of the parent class, but don't influence the values returned by the getters.
 * This is a little unclean but by design to not allow write access to the internal structures.
 */
public class SubPicture implements Cloneable {

    /** with of subtitle image */
    private int imageWidth;
    /** height of subtitle image */
    private int imageHeight;
    /** upper left corner of subtitle x */
    private int xOfs;
    /** upper left corner of subtitle y */
    private int yOfs;

    /* public */

    /** screen width */
    public int width;
    /** screen height */
    public int height;
    /** start time in milliseconds */
    public long startTime;
    /** end time in milliseconds */
    public long endTime;
    /** if true, this is a forced subtitle */
    public boolean isforced;
    /** composition number - increased at start and end PCS */
    public int compNum;
    /** frame was already decoded */
    public boolean wasDecoded;

    /* the following fields are really only needed for editing */

    /** exclude from export? */
    public boolean exclude;

    /** list of erase patches */
    public ArrayList<ErasePatch> erasePatch;

    /**
     * Allows to get a clone of the parent object even for SubPictureBD objects.
     * @return clone of the parent object
     */
    public SubPicture copy() {
        SubPicture sp = new SubPicture();
        sp.width = width;
        sp.height = height;
        sp.startTime = startTime;
        sp.endTime = endTime;
        sp.isforced = isforced;
        sp.compNum = compNum;

        /* Note that by using the getter functions
         * the internal values of a SubPictureBD are
         * copied into the plain members of the
         * SubPicture object.
         */
        sp.setImageWidth(getImageWidth());
        sp.setImageHeight(getImageHeight());
        sp.setOfsX(getOfsX());
        sp.setOfsY(getOfsY());

        sp.exclude = exclude;
        sp.wasDecoded = wasDecoded;
        if (erasePatch != null && erasePatch.size()>0) {
            ArrayList<ErasePatch> epl = new ArrayList<ErasePatch>();
            for (ErasePatch ep : erasePatch) {
                epl.add(ep);
            }
            sp.erasePatch = epl;
        }
        return sp;
    }

    /**
     * get image width
     * @return image width in pixels
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * get image height
     * @return image height in pixels
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * get image x offset
     * @return image x offset in pixels
     */
    public int getOfsX() {
        return xOfs;
    }

    /**
     * get image y offset
     * @return image y offset in pixels
     */
    public int getOfsY() {
        return yOfs;
    }

    /**
     * Set image width
     * @param w width in pixels
     */
    public void setImageWidth(int w) {
        imageWidth = w;
    }

    /**
     * Set image height
     * @param h height in pixels
     */
    public void setImageHeight(int h) {
        imageHeight = h;
    }

    /**
     * Set image x offset
     * @param ofs offset in pixels
     */
    public void setOfsX(int ofs) {
        xOfs = ofs;
    }

    /**
     * Set image y offset
     * @param ofs offset in pixels
     */
    public void setOfsY(int ofs) {
        yOfs = ofs;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public SubPicture clone() {
        try {
            return (SubPicture)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
