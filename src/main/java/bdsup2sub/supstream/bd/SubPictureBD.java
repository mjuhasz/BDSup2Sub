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
import java.util.List;

public class SubPictureBD extends SubPicture {

    /** objectID used in decoded object */
    private int objectID;
    /** list of ODS packets containing image info */
    private List<ImageObject> imageObjectList;
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
    private List<List<PaletteInfo>> palettes;

    public SubPictureBD() {
    }

    public SubPictureBD(SubPictureBD other) {
        super(other);

        this.objectID = other.objectID;
        this.windowWidth = other.windowWidth;
        this.windowHeight = other.windowHeight;
        this.xWindowOffset = other.xWindowOffset;
        this.yWindowOffset = other.yWindowOffset;
        this.type = other.type;

        if (other.palettes != null) {
            this.palettes = new ArrayList<List<PaletteInfo>>();
            for (List<PaletteInfo> pi : other.palettes) {
                List<PaletteInfo> cpi = new ArrayList<PaletteInfo>(pi);
                this.palettes.add(cpi);
            }
        }
        if (other.imageObjectList != null) {
            this.imageObjectList = new ArrayList<ImageObject>();
            for (ImageObject io : other.imageObjectList) {
                this.imageObjectList.add(new ImageObject(io));
            }
        }
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

    int getObjectID() {
        return objectID;
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    public List<ImageObject> getImageObjectList() {
        return imageObjectList;
    }

    public void setImageObjectList(List<ImageObject> imageObjectList) {
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

    int getXWindowOffset() {
        return xWindowOffset;
    }

    public void setXWindowOffset(int xWindowOffset) {
        this.xWindowOffset = xWindowOffset;
    }

    int getYWindowOffset() {
        return yWindowOffset;
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

    public List<List<PaletteInfo>> getPalettes() {
        return palettes;
    }

    public void setPalettes(List<List<PaletteInfo>> palettes) {
        this.palettes = palettes;
    }
}
