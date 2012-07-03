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

public class ImageObject implements Cloneable  {

    /** list of ODS packets containing image info */
    private ArrayList<ImageObjectFragment> fragmentList;
    /** palette identifier */
    private int paletteID;
    /** overall size of RLE buffer (might be spread over several packages) */
    private int bufferSize;
    /** with of subtitle image */
    private int width;
    /** height of subtitle image */
    private int height;
    /** upper left corner of subtitle x */
    private int xOfs;
    /** upper left corner of subtitle y */
    private int yOfs;

    @Override
    public ImageObject clone() {
        try {
            return (ImageObject)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public ArrayList<ImageObjectFragment> getFragmentList() {
        return fragmentList;
    }

    public void setFragmentList(ArrayList<ImageObjectFragment> fragmentList) {
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

    public int getxOfs() {
        return xOfs;
    }

    public void setxOfs(int xOfs) {
        this.xOfs = xOfs;
    }

    public int getyOfs() {
        return yOfs;
    }

    public void setyOfs(int yOfs) {
        this.yOfs = yOfs;
    }
}