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

import java.util.ArrayList;
import java.util.List;

public class ImageObject  {

    /** list of ODS packets containing image info */
    private List<ImageObjectFragment> fragmentList;
    /** palette identifier */
    private int paletteID;
    /** overall size of RLE buffer (might be spread over several packages) */
    private int bufferSize;
    /** with of subtitle image */
    private int width;
    /** height of subtitle image */
    private int height;
    /** upper left corner of subtitle x */
    private int xOffset;
    /** upper left corner of subtitle y */
    private int yOffset;

    public ImageObject() {
    }

    public ImageObject(ImageObject other) {
        if (other.fragmentList != null) {
            this.fragmentList = new ArrayList<ImageObjectFragment>(other.fragmentList);
        }
        this.paletteID = other.paletteID;
        this.bufferSize = other.bufferSize;
        this.width = other.width;
        this.height = other.height;
        this.xOffset = other.xOffset;
        this.yOffset = other.yOffset;
    }

    public List<ImageObjectFragment> getFragmentList() {
        return fragmentList;
    }

    public void setFragmentList(List<ImageObjectFragment> fragmentList) {
        this.fragmentList = fragmentList;
    }

    public int getPaletteID() {
        return paletteID;
    }

    public void setPaletteID(int paletteID) {
        this.paletteID = paletteID;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
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

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }
}