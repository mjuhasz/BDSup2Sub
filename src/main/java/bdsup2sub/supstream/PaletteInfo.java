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
 * Contains offset and size of one update of a palette
 */
public class PaletteInfo {

    /** offset to palette info in SUP file */
    private final int paletteOffset;
    /** number of palette entries */
    private final int paletteSize;

    public PaletteInfo(int paletteOffset, int paletteSize) {
        this.paletteOffset = paletteOffset;
        this.paletteSize = paletteSize;
    }

    public int getPaletteOffset() {
        return paletteOffset;
    }

    public int getPaletteSize() {
        return paletteSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaletteInfo that = (PaletteInfo) o;

        if (paletteOffset != that.paletteOffset) return false;
        if (paletteSize != that.paletteSize) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = paletteOffset;
        result = 31 * result + paletteSize;
        return result;
    }
}