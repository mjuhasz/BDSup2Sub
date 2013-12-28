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
package bdsup2sub.supstream;

/**
 * Contains offset and size of one fragment containing (parts of the) RLE buffer
 */
public class ImageObjectFragment {

    /** offset to RLE buffer in SUP file */
    private final long imageBufferOfs;
    /** size of this part of the RLE buffer */
    private final int imagePacketSize;

    public ImageObjectFragment(long imageBufferOfs, int imagePacketSize) {
        this.imageBufferOfs = imageBufferOfs;
        this.imagePacketSize = imagePacketSize;
    }

    public long getImageBufferOfs() {
        return imageBufferOfs;
    }

    public int getImagePacketSize() {
        return imagePacketSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageObjectFragment that = (ImageObjectFragment) o;

        if (imageBufferOfs != that.imageBufferOfs) return false;
        if (imagePacketSize != that.imagePacketSize) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (imageBufferOfs ^ (imageBufferOfs >>> 32));
        result = 31 * result + imagePacketSize;
        return result;
    }
}