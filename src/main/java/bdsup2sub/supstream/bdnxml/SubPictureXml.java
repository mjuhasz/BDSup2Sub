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
package bdsup2sub.supstream.bdnxml;

import bdsup2sub.supstream.SubPicture;

public class SubPictureXml extends SubPicture {
    /** original x offset of uncropped bitmap */
    private int originalXOffset;
    /** original y offset of uncropped bitmap */
    private int originalYOffset;
    /** file name of Xml file */
    private String fileName;

    public SubPictureXml() {
    }

    public SubPictureXml(SubPictureXml other) {
        super(other);
        this.originalXOffset = other.originalXOffset;
        this.originalYOffset = other.originalYOffset;
        this.fileName = other.fileName;
    }

    public void storeOriginalOffsets() {
        originalXOffset = getXOffset();
        originalYOffset = getYOffset();
    }

    public int getOriginalXOffset() {
        return originalXOffset;
    }

    public int getOriginalYOffset() {
        return originalYOffset;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}