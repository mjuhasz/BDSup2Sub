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

/**
 * Contains offset and size of one fragment containing (parts of the) RLE buffer
 */
public class ImageObjectFragment implements Cloneable  {

    /** offset to RLE buffer in SUP file */
    private long imageBufferOfs;
    /** size of this part of the RLE buffer */
    private int imagePacketSize;

    @Override
    public ImageObjectFragment clone() {
        try {
            return (ImageObjectFragment)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public long getImageBufferOfs() {
        return imageBufferOfs;
    }

    public void setImageBufferOfs(long imageBufferOfs) {
        this.imageBufferOfs = imageBufferOfs;
    }

    public int getImagePacketSize() {
        return imagePacketSize;
    }

    public void setImagePacketSize(int imagePacketSize) {
        this.imagePacketSize = imagePacketSize;
    }
}