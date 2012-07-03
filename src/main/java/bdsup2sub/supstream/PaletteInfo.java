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
public class PaletteInfo implements Cloneable {

    /** offset to palette info in SUP file */
    private int paletteOfs;
    /** number of palette entries */
    private int paletteSize;

    @Override
    public PaletteInfo clone() {
        try {
            return (PaletteInfo)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int getPaletteOfs() {
        return paletteOfs;
    }

    public void setPaletteOfs(int paletteOfs) {
        this.paletteOfs = paletteOfs;
    }

    public int getPaletteSize() {
        return paletteSize;
    }

    public void setPaletteSize(int paletteSize) {
        this.paletteSize = paletteSize;
    }
}