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
package bdsup2sub.supstream.bdnxml;

import bdsup2sub.supstream.SubPicture;

/**
 * Extends SubPicture to store information read from Xml
 */
public class SubPictureXml extends SubPicture implements Cloneable {
    /** original x offset of uncropped bitmap */
    int originalX;
    /** original y offset of uncropped bitmap */
    int originalY;
    /** file name of Xml file */
    String fileName;

    /* (non-Javadoc)
     * @see SubPicture#clone()
     */
    @Override
    public SubPictureXml clone() {
        return (SubPictureXml)super.clone();
    }

    /**
     * store original offsets
     */
    void setOriginal() {
        originalX = getOfsX();
        originalY = getOfsY();
    }
}