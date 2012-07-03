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
package bdsup2sub.supstream.bd;

import bdsup2sub.supstream.ImageObject;
import bdsup2sub.supstream.PaletteInfo;
import bdsup2sub.supstream.SubPicture;

import java.util.ArrayList;

/**
 * Extends SubPicture to store information read from BD SUP
 */
class SubPictureBD extends SubPicture implements Cloneable {

    /** objectID used in decoded object */
    int objectID;
    /** list of ODS packets containing image info */
    ArrayList<ImageObject> imageObjectList;
    /** width of subtitle window (might be larger than image) */
    int winWidth;
    /** height of subtitle window (might be larger than image) */
    int winHeight;
    /** upper left corner of subtitle window x */
    int xWinOfs;
    /** upper left corner of subtitle window y */
    int yWinOfs;
    /** FPS type (e.g. 0x10 = 24p) */
    int type;
    /** list of (list of) palette info - there are up to 8 palettes per epoch, each can be updated several times */
    ArrayList<ArrayList<PaletteInfo>> palettes;

    /* (non-Javadoc)
     * @see SubPicture#clone()
     */
    @Override
    public SubPictureBD clone() {
        return (SubPictureBD)super.clone();
    }

    /**
     * Create clone of this object, but featuring a deep copy of the palettes
     * and image object information.
     * Note that the ODS fragments are only a flat copy, since they are never
     * updated, only overwritten.
     * @return clone of this object
     */
    SubPictureBD deepCopy() {
        SubPictureBD c = this.clone();
        // deep copy palettes
        if (palettes != null) {
            c.palettes = new ArrayList<ArrayList<PaletteInfo>>();
            for (ArrayList<PaletteInfo> pi : palettes) {
                ArrayList<PaletteInfo> cpi = new ArrayList<PaletteInfo>();
                c.palettes.add(cpi);
                for (PaletteInfo p : pi) {
                    cpi.add(p.clone());
                }
            }
        }
        // (not so) deep copy of objects (cloning of the fragment lists is not needed)
        if (imageObjectList != null) {
            c.imageObjectList = new ArrayList<ImageObject>();
            for (ImageObject io : imageObjectList) {
                c.imageObjectList.add(io.clone());
            }
        }
        return c;
    }

    /* setters / getters */

    /**
     * get image width
     * @return image width in pixels
     */
    @Override
    public int getImageWidth() {
        return imageObjectList.get(objectID).getWidth();
    }

    /**
     * get image height
     * @return image height in pixels
     */
    @Override
    public int getImageHeight() {
        return imageObjectList.get(objectID).getHeight();
    }

    /**
     * get image x offset
     * @return image x offset in pixels
     */
    @Override
    public int getOfsX() {
        return imageObjectList.get(objectID).getxOfs();
    }

    /**
     * get image y offset
     * @return image y offset in pixels
     */
    @Override
    public int getOfsY() {
        return imageObjectList.get(objectID).getyOfs();
    }

    /**
     * Get image object containing RLE data
     * @param index index of subtitle
     * @return image object containing RLE data
     */
    ImageObject getImgObj(int index) {
        return imageObjectList.get(index);
    }

    /**
     * Get image object containing RLE data
     * @return image object containing RLE data
     */
    ImageObject getImgObj() {
        return imageObjectList.get(objectID);
    }
}
