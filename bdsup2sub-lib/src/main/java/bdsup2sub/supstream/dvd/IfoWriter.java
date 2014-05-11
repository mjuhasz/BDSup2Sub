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
package bdsup2sub.supstream.dvd;

import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Configuration;
import bdsup2sub.core.Constants;
import bdsup2sub.core.CoreException;
import bdsup2sub.utils.ByteUtils;
import bdsup2sub.utils.ToolBox;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class IfoWriter {

    private static final Configuration configuration = Configuration.getInstance();

    private IfoWriter() {
    }

    public static void writeIFO(String fname, int pictureHeight, Palette palette) throws CoreException {
        byte[] buf = new byte[0x1800];
        int index = 0;

        // video attributes
        int vidAttr = pictureHeight == 480 ? 0x4f01 : 0x5f01;

        // VTSI_MAT
        ToolBox.setString(buf, index, "DVDVIDEO-VTS");
        ByteUtils.setDWord(buf, index + 0x12, 0x00000004);    // last sector of title set
        ByteUtils.setDWord(buf, index + 0x1C, 0x00000004);    // last sector of IFO
        ByteUtils.setDWord(buf, index + 0x80, 0x000007ff);    // end byte address of VTS_MAT
        ByteUtils.setDWord(buf, index + 0xC8, 0x00000001);    // start sector of Title Vob (*2048 -> 0x0800) -> PTT_SRPTI
        ByteUtils.setDWord(buf, index + 0xCC, 0x00000002);    // start sector of Titles&Chapters table (*2048 -> 0x1000) -> VTS_PGCITI

        ByteUtils.setWord(buf, index + 0x100, vidAttr);    // video attributes
        ByteUtils.setWord(buf, index + 0x200, vidAttr);    // video attributes

        String l = Constants.LANGUAGES[configuration.getLanguageIdx()][1];
        ByteUtils.setWord(buf, index + 0x254, 1);            // number of subtitle streams
        ByteUtils.setByte(buf, index + 0x256, 1);            // subtitle attributes
        ByteUtils.setByte(buf, index + 0x258, (byte) l.charAt(0));
        ByteUtils.setByte(buf, index + 0x259, (byte) l.charAt(1));

        // PTT_SRPTI
        index = 0x0800;
        ByteUtils.setWord(buf, index, 0x0001);               // Number of TTUs
        ByteUtils.setWord(buf, index + 0x04, 0x000f);        // End byte of PTT_SRPT table
        ByteUtils.setDWord(buf, index + 0x04, 0x0000000C);   // TTU_1: starting byte
        ByteUtils.setWord(buf, index + 0x0C, 0x0001);        // PTT_1: program chain number PGCN
        ByteUtils.setWord(buf, index + 0x0e, 0x0001);        // PTT_1: program number PG

        // VTS_PGCITI/VTS_PTT_SRPT
        index = 0x1000;
        ByteUtils.setWord(buf, index, 0x0001);                // Number of VTS_PGCI_SRP (2 bytes, 2 bytes reserved)
        ByteUtils.setDWord(buf, index + 0x04, 0x00000119);    // end byte of VTS_PGCI_SRP table (281)
        ByteUtils.setDWord(buf, index + 0x08, 0x81000000);    // VTS_PGC_1_ category mask. entry PGC (0x80), title number 1 (0x01), Category 0,...
        ByteUtils.setDWord(buf, index + 0x0C, 0x00000010);    // VTS_PGCI start byte (16)

        // VTS_PGC_1
        index = 0x1010;
        ByteUtils.setByte(buf, index + 0x02, 0x01);        // Number of Programs
        ByteUtils.setByte(buf, index + 0x03, 0x01);        // Number of Cells
        for (int i = 0; i < 16; i++) {
            int ycbcr[] = palette.getYCbCr(i);
            ByteUtils.setByte(buf, index + 0xA4 + 4 * i + 1, ycbcr[0]);
            ByteUtils.setByte(buf, index + 0xA4 + 4 * i + 2, ycbcr[1]);
            ByteUtils.setByte(buf, index + 0xA4 + 4 * i + 3, ycbcr[2]);
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(fname));
            out.write(buf);
        } catch (IOException ex) {
            throw new CoreException(ex.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
    }
}