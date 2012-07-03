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

public class SubPictureBD extends SubPicture implements Cloneable {

    /** objectID used in decoded object */
    private int objectID;
    /** list of ODS packets containing image info */
    private ArrayList<ImageObject> imageObjectList;
    /** width of subtitle window (might be larger than image) */
    private int windowWidth;
    /** height of subtitle window (might be larger than image) */
    private int windowHeight;
    /** upper left corner of subtitle window x */
    private int xWindowOffset;
    /** upper left corner of subtitle window y */
    private int yWindowOffset;
    /** FPS type (e.g. 0x10 = 24p) */
    private int type;
    /** list of (list of) palette info - there are up to 8 palettes per epoch, each can be updated several times */
    private ArrayList<ArrayList<PaletteInfo>> palettes;


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

    @Override
    public int getImageWidth() {
        return imageObjectList.get(objectID).getWidth();
    }

    @Override
    public int getImageHeight() {
        return imageObjectList.get(objectID).getHeight();
    }

    @Override
    public int getXOffset() {
        return imageObjectList.get(objectID).getXOffset();
    }

    @Override
    public int getYOffset() {
        return imageObjectList.get(objectID).getYOffset();
    }

    /**
     * Get image object containing RLE data
     * @param index index of subtitle
     * @return image object containing RLE data
     */
    ImageObject getImageObject(int index) {
        return imageObjectList.get(index);
    }

    /**
     * Get image object containing RLE data
     * @return image object containing RLE data
     */
    ImageObject getImageObject() {
        return imageObjectList.get(objectID);
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    public ArrayList<ImageObject> getImageObjectList() {
        return imageObjectList;
    }

    public void setImageObjectList(ArrayList<ImageObject> imageObjectList) {
        this.imageObjectList = imageObjectList;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public void setXWindowOffset(int xWindowOffset) {
        this.xWindowOffset = xWindowOffset;
    }

    public void setYWindowOffset(int yWindowOffset) {
        this.yWindowOffset = yWindowOffset;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<ArrayList<PaletteInfo>> getPalettes() {
        return palettes;
    }

    public void setPalettes(ArrayList<ArrayList<PaletteInfo>> palettes) {
        this.palettes = palettes;
    }
}
