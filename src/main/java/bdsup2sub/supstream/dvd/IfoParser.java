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
package bdsup2sub.supstream.dvd;

import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.Logger;
import bdsup2sub.tools.FileBuffer;
import bdsup2sub.tools.FileBufferException;
import bdsup2sub.utils.ToolBox;

import java.util.Arrays;

import static bdsup2sub.core.Constants.DEFAULT_DVD_PALETTE;
import static bdsup2sub.core.Constants.LANGUAGES;

public class IfoParser {

    private static final Logger logger = Logger.getInstance();

    private static final byte[] IFO_HEADER = "DVDVIDEO-VTS".getBytes();

    private final FileBuffer fileBuffer;

    private int screenWidth;
    private int screenHeight;
    private int languageIdx;
    private Palette srcPalette = new Palette(DEFAULT_DVD_PALETTE);


    public IfoParser(String filename) throws CoreException {
        try {
            this.fileBuffer = new FileBuffer(filename);
            processIFO();
        } catch (FileBufferException e) {
            throw new CoreException(e.getMessage());
        }
    }

    private void processIFO() throws CoreException {
        try {
            validateIfoHeader();
            readVideoAttributes();
            readFirstLanguageIndex();
            readFirstPalette();
        } catch (FileBufferException e) {
            throw new CoreException(e.getMessage());
        }
    }

    private void validateIfoHeader() throws FileBufferException, CoreException {
        byte header[] = new byte[IFO_HEADER.length];
        fileBuffer.getBytes(0, header, IFO_HEADER.length);
        if (!Arrays.equals(header, IFO_HEADER)) {
            throw new CoreException("Not a valid IFO file.");
        }
    }

    private void readVideoAttributes() throws FileBufferException {
        int vidAttr = fileBuffer.getWord(0x200);
        if ((vidAttr & 0x3000) != 0) {
            // PAL
            switch ((vidAttr>>3) & 3) {
                case 0:
                    screenWidth = 720;
                    screenHeight = 576;
                    break;
                case 1:
                    screenWidth = 704;
                    screenHeight = 576;
                    break;
                case 2:
                    screenWidth = 352;
                    screenHeight = 576;
                    break;
                case 3:
                    screenWidth = 352;
                    screenHeight = 288;
                    break;
            }
        } else {
            // NTSC
            switch ((vidAttr>>3) & 3) {
                case 0:
                    screenWidth = 720;
                    screenHeight = 480;
                    break;
                case 1:
                    screenWidth = 704;
                    screenHeight = 480;
                    break;
                case 2:
                    screenWidth = 352;
                    screenHeight = 480;
                    break;
                case 3:
                    screenWidth = 352;
                    screenHeight = 240;
                    break;
            }
        }
        logger.trace("Resolution: " + screenWidth + "x" + screenHeight + "\n");
    }

    private void readFirstLanguageIndex() throws FileBufferException {
        if (fileBuffer.getWord(0x254) > 0 && fileBuffer.getByte(0x256) == 1) {
            StringBuilder langSB = new StringBuilder(2);
            boolean found = false;
            langSB.append((char) fileBuffer.getByte(0x258));
            langSB.append((char) fileBuffer.getByte(0x259));
            String lang = langSB.toString();
            for (int i=0; i < LANGUAGES.length; i++) {
                if (lang.equalsIgnoreCase(LANGUAGES[i][1])) {
                    languageIdx = i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                logger.warn("Illegal language id: " + lang + "\n");
            } else {
                logger.trace("Set language to: " + lang + "\n");
            }
        } else {
            logger.warn("Missing language id.\n");
        }
    }

    private void readFirstPalette() throws FileBufferException {
        // get start offset of Titles&Chapters table
        long VTS_PGCITI_ofs = fileBuffer.getDWord(0xCC) * 2048;
        // PTT_SRPTI
        VTS_PGCITI_ofs += fileBuffer.getDWord(VTS_PGCITI_ofs+0x0C);
        logger.trace("Reading palette from offset: " + ToolBox.toHexLeftZeroPadded(VTS_PGCITI_ofs, 8) + "\n");

        // assume palette in VTS_PGC_1
        long index = VTS_PGCITI_ofs;
        for (int i=0; i < 16; i++) {
            int y  = fileBuffer.getByte(index+0xA4+4*i+1) & 0xff;
            int cb = fileBuffer.getByte(index+0xA4+4*i+2) & 0xff;
            int cr = fileBuffer.getByte(index+0xA4+4*i+3) & 0xff;
            srcPalette.setYCbCr(i, y, cb, cr);
        }
    }

    public Palette getSrcPalette() {
        return srcPalette;
    }

    public int getLanguageIdx() {
        return languageIdx;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }
}
