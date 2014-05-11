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

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.core.Configuration;

public final class SupDvdWriter {

    private static final Configuration configuration = Configuration.getInstance();

    private static final byte[] CONTROL_HEADER = {
            0x00,													//  dummy byte (for shifting when forced)
            0x00, 0x00,												//  0: offset to end sequence
            0x01,													//  2: CMD 1: start displaying
            0x03, 0x32, 0x10,										//  3: CMD 3: Palette Info
            0x04, (byte)0xff, (byte)0xff,							//  6: CMD 4: Alpha Info
            0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,				//  9: CMD 5: sub position
            0x06, 0x00, 0x00, 0x00, 0x00,							// 16: CMD 6: rle offsets
            (byte)0xff,												// 21: End of control header
            0x00, 0x00,												// 22: display duration in 90kHz/1024
            0x00, 0x00,												// 24: offset to end sequence (again)
            0x02, (byte)0xff,										// 26: CMD 2: stop displaying
    };

    private SupDvdWriter() {
    }

    /**
     * Create the binary stream representation of one caption
     *
     * @param pic SubPicture object containing caption info
     * @param bm  bitmap
     * @return byte buffer containing the binary stream representation of one caption
     */
    public static byte[] createSupFrame(SubPictureDVD pic, Bitmap bm) {
        /* create RLE buffers */
        byte[] even = SupDvdUtil.encodeLines(bm, true);
        byte[] odd = SupDvdUtil.encodeLines(bm, false);
        int tmp;

        int forcedOfs;
        int controlHeaderLen;
        if (pic.isForced()) {
            forcedOfs = 0;
            CONTROL_HEADER[2] = 0x01; // display
            CONTROL_HEADER[3] = 0x00; // forced
            controlHeaderLen = CONTROL_HEADER.length;
        } else {
            forcedOfs = 1;
            CONTROL_HEADER[2] = 0x00; // part of offset
            CONTROL_HEADER[3] = 0x01; // display
            controlHeaderLen = CONTROL_HEADER.length - 1;
        }

        // fill out all info but the offets (determined later)
        int sizeRLE = even.length + odd.length;
        int bufSize = 10 + 4 + controlHeaderLen + sizeRLE;
        byte[] buf = new byte[bufSize];

        // write header
        buf[0] = 0x53;
        buf[1] = 0x50;
        // write PTS (4 bytes of 8 bytes used) - little endian!
        int pts = (int) pic.getStartTime();
        buf[5] = (byte) (pts >> 24);
        buf[4] = (byte) (pts >> 16);
        buf[3] = (byte) (pts >> 8);
        buf[2] = (byte) pts;

        // write packet size
        tmp = controlHeaderLen + sizeRLE + 4; // 4 for the size and the offset
        buf[10] = (byte) (tmp >> 8);
        buf[11] = (byte) (tmp);

        // write offset to control header +
        tmp = sizeRLE + 2; // 2 for the offset
        buf[12] = (byte) (tmp >> 8);
        buf[13] = (byte) (tmp);

        // copy rle buffers
        int ofs = 14;
        for (byte b : even) {
            buf[ofs++] = b;
        }
        for (byte b : odd) {
            buf[ofs++] = b;
        }

        /* create control header */
        /* palette (store reversed) */
        CONTROL_HEADER[1 + 4] = (byte) (((pic.getPal()[3] & 0xf) << 4) | (pic.getPal()[2] & 0x0f));
        CONTROL_HEADER[1 + 5] = (byte) (((pic.getPal()[1] & 0xf) << 4) | (pic.getPal()[0] & 0x0f));
        /* alpha (store reversed) */
        CONTROL_HEADER[1 + 7] = (byte) (((pic.getAlpha()[3] & 0xf) << 4) | (pic.getAlpha()[2] & 0x0f));
        CONTROL_HEADER[1 + 8] = (byte) (((pic.getAlpha()[1] & 0xf) << 4) | (pic.getAlpha()[0] & 0x0f));

        /* coordinates of subtitle */
        CONTROL_HEADER[1 + 10] = (byte) ((pic.getXOffset() >> 4) & 0xff);
        tmp = pic.getXOffset() + bm.getWidth() - 1;
        CONTROL_HEADER[1 + 11] = (byte) (((pic.getXOffset() & 0xf) << 4) | ((tmp >> 8) & 0xf));
        CONTROL_HEADER[1 + 12] = (byte) (tmp & 0xff);

        int yOfs = pic.getYOffset() - configuration.getCropOffsetY();
        if (yOfs < 0) {
            yOfs = 0;
        } else {
            int yMax = pic.getHeight() - pic.getImageHeight() - 2 * configuration.getCropOffsetY();
            if (yOfs > yMax) {
                yOfs = yMax;
            }
        }

        CONTROL_HEADER[1 + 13] = (byte) ((yOfs >> 4) & 0xff);
        tmp = yOfs + bm.getHeight() - 1;
        CONTROL_HEADER[1 + 14] = (byte) (((yOfs & 0xf) << 4) | ((tmp >> 8) & 0xf));
        CONTROL_HEADER[1 + 15] = (byte) (tmp & 0xff);

        /* offset to even lines in rle buffer */
        CONTROL_HEADER[1 + 17] = 0x00; /* 2 bytes subpicture size and 2 bytes control header ofs */
        CONTROL_HEADER[1 + 18] = 0x04; /* note: SubtitleCreator uses 6 and adds 0x0000 in between */

        /* offset to odd lines in rle buffer */
        tmp = even.length + CONTROL_HEADER[1 + 18];
        CONTROL_HEADER[1 + 19] = (byte) ((tmp >> 8) & 0xff);
        CONTROL_HEADER[1 + 20] = (byte) (tmp & 0xff);

        /* display duration in frames */
        tmp = (int) ((pic.getEndTime() - pic.getStartTime()) / 1024); // 11.378ms resolution????
        CONTROL_HEADER[1 + 22] = (byte) ((tmp >> 8) & 0xff);
        CONTROL_HEADER[1 + 23] = (byte) (tmp & 0xff);

        /* offset to end sequence - 22 is the offset of the end sequence */
        tmp = sizeRLE + 22 + (pic.isForced() ? 1 : 0) + 4;
        CONTROL_HEADER[forcedOfs + 0] = (byte) ((tmp >> 8) & 0xff);
        CONTROL_HEADER[forcedOfs + 1] = (byte) (tmp & 0xff);
        CONTROL_HEADER[1 + 24] = (byte) ((tmp >> 8) & 0xff);
        CONTROL_HEADER[1 + 25] = (byte) (tmp & 0xff);

        // write control header
        for (int i = 0; i < controlHeaderLen; i++) {
            buf[ofs++] = CONTROL_HEADER[forcedOfs + i];
        }

        return buf;
    }
}