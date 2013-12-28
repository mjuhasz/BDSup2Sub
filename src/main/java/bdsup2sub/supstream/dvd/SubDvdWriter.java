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
import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Configuration;
import bdsup2sub.core.Constants;
import bdsup2sub.core.CoreException;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.utils.ToolBox;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static bdsup2sub.core.Constants.LANGUAGES;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStrIdx;

public final class SubDvdWriter {

    private static final Configuration configuration = Configuration.getInstance();

    private static final byte[] PACK_HEADER = {
            0x00, 0x00, 0x01, (byte)0xba,							// 0:  0x000001ba - packet ID
            0x44, 0x02, (byte)0xc4, (byte)0x82, 0x04, (byte)0xa9,	// 4:  system clock reference
            0x01 , (byte)0x89, (byte)0xc3,							// 10: multiplexer rate
            (byte)0xf8,												// 13: stuffing info
    };

    private static final byte[] HEADER_FIRST = {						// header only in first packet
            0x00, 0x00, 0x01, (byte)0xbd,							// 0: 0x000001bd - sub ID
            0x00, 0x00,												// 4: packet length
            (byte)0x81, (byte)0x80, 								// 6:  packet type
            0x05,													// 8:  PTS length
            0x00, 0x0, 0x00, 0x00, 0x00,							// 9:  PTS
            0x20,													// 14: stream ID
            0x00, 0x00,												// 15: Subpicture size in bytes
            0x00, 0x00,												// 17: offset to control header
    };

    private static final byte[] HEADER_NEXT = {						// header in following packets
            0x00, 0x00, 0x01, (byte)0xbd,							// 0: 0x000001bd - sub ID
            0x00, 0x00,												// 4: packet length
            (byte)0x81, (byte)0x00, 								// 6: packet type
            0x00,													// 8: PTS length = 0
            0x20													// 9: Stream ID
    };

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

    private SubDvdWriter() {
    }

    /**
     * Create the binary stream representation of one caption
     *
     * @param pic SubPicture object containing caption info
     * @param bm  bitmap
     * @return byte buffer containing the binary stream representation of one caption
     */
    public static byte[] createSubFrame(SubPictureDVD pic, Bitmap bm) {
        /* create RLE buffers */
        byte even[] = SupDvdUtil.encodeLines(bm, true);
        byte odd[] = SupDvdUtil.encodeLines(bm, false);
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

        /* header - contains PTM */
        int ptm = (int) pic.getStartTime(); // should be end time, but STC writes start time?
        HEADER_FIRST[9] = (byte) (((ptm >> 29) & 0x0E) | 0x21);
        HEADER_FIRST[10] = (byte) (ptm >> 22);
        HEADER_FIRST[11] = (byte) ((ptm >> 14) | 1);
        HEADER_FIRST[12] = (byte) (ptm >> 7);
        HEADER_FIRST[13] = (byte) (ptm * 2 + 1);

        /* control header */
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
        tmp = even.length + odd.length + 22 + (pic.isForced() ? 1 : 0) + 4;
        CONTROL_HEADER[forcedOfs + 0] = (byte) ((tmp >> 8) & 0xff);
        CONTROL_HEADER[forcedOfs + 1] = (byte) (tmp & 0xff);
        CONTROL_HEADER[1 + 24] = (byte) ((tmp >> 8) & 0xff);
        CONTROL_HEADER[1 + 25] = (byte) (tmp & 0xff);

        // subpicture size
        tmp = even.length + odd.length + 4 + controlHeaderLen;
        HEADER_FIRST[15] = (byte) (tmp >> 8);
        HEADER_FIRST[16] = (byte) tmp;

        /* offset to control buffer - 2 is the size of the offset */
        tmp = even.length + odd.length + 2;
        HEADER_FIRST[17] = (byte) (tmp >> 8);
        HEADER_FIRST[18] = (byte) tmp;

        // in the SUB format only 0x800 bytes can be written per packet. If a packet
        // is larger, it has to be split into fragments <= 0x800 bytes
        // which follow one after the other.

        int sizeRLE = even.length + odd.length;
        int bufSize = PACK_HEADER.length + HEADER_FIRST.length + controlHeaderLen + sizeRLE;
        int numAdditionalPackets = 0;
        if (bufSize > 0x800) {
            // determine how many additional headers we will need
            // considering that each additional header also adds to the size
            // due to its own headers
            numAdditionalPackets = 1;
            int remainingRLEsize = sizeRLE - (0x800 - PACK_HEADER.length - HEADER_FIRST.length); // size - 0x7df
            while (remainingRLEsize > (0x800 - PACK_HEADER.length - HEADER_NEXT.length - controlHeaderLen)) {
                remainingRLEsize -= (0x800 - PACK_HEADER.length - HEADER_NEXT.length);
                bufSize += PACK_HEADER.length + HEADER_NEXT.length;
                numAdditionalPackets++;
            }
            // packet length of the 1st packet should be the maximum size
            tmp = 0x800 - PACK_HEADER.length - 6;
        } else {
            tmp = (bufSize - PACK_HEADER.length - 6);
        }

        // allocate and fill buffer
        byte buf[] = new byte[(1 + numAdditionalPackets) * 0x800];

        int stuffingBytes;
        int diff = buf.length - bufSize;
        if (diff > 0 && diff < 6) {
            stuffingBytes = diff;
        } else {
            stuffingBytes = 0;
        }

        int ofs = 0;
        for (byte packHeader : PACK_HEADER) {
            buf[ofs++] = packHeader;
        }

        // set packet length
        tmp += stuffingBytes;
        HEADER_FIRST[4] = (byte) (tmp >> 8);
        HEADER_FIRST[5] = (byte) tmp;

        // set pts length
        HEADER_FIRST[8] = (byte) (5 + stuffingBytes);

        // write header and use pts for stuffing bytes (if needed)
        for (int i = 0; i < 14; i++) {
            buf[ofs++] = HEADER_FIRST[i];
        }
        for (int i = 0; i < stuffingBytes; i++) {
            buf[ofs++] = (byte) 0xff;
        }
        for (int i = 14; i < HEADER_FIRST.length; i++) {
            buf[ofs++] = HEADER_FIRST[i];
        }

        // write (first part of) RLE buffer
        tmp = sizeRLE;
        if (numAdditionalPackets > 0) {
            tmp = (0x800 - PACK_HEADER.length - stuffingBytes - HEADER_FIRST.length);
            if (tmp > sizeRLE) { // can only happen in 1st buffer
                tmp = sizeRLE;
            }
        }
        for (int i = 0; i < tmp; i++) {
            if (i < even.length) {
                buf[ofs++] = even[i];
            } else {
                buf[ofs++] = odd[i - even.length];
            }
        }
        int ofsRLE = tmp;

        // fill gap in first packet with (parts of) control header
        // only if the control header is split over two packets
        int controlHeaderWritten = 0;
        if (numAdditionalPackets == 1 && ofs < 0x800) {
            for (; ofs < 0x800; ofs++) {
                buf[ofs] = CONTROL_HEADER[forcedOfs + (controlHeaderWritten++)];
            }
        }

        // write additional packets
        for (int p = 0; p < numAdditionalPackets; p++) {
            int rleSizeLeft;
            if (p == numAdditionalPackets - 1) {
                // last loop
                rleSizeLeft = sizeRLE - ofsRLE;
                tmp = HEADER_NEXT.length + (controlHeaderLen - controlHeaderWritten) + (sizeRLE - ofsRLE) - 6;
            } else {
                tmp = 0x800 - PACK_HEADER.length - 6;
                rleSizeLeft = (0x800 - PACK_HEADER.length - HEADER_NEXT.length);
                // now, again, it could happen that the RLE buffer runs out before the last package
                if (rleSizeLeft > (sizeRLE - ofsRLE)) {
                    rleSizeLeft = sizeRLE - ofsRLE;
                }
            }
            // copy packet headers
            PACK_HEADER[13] = (byte) (0xf8);
            for (byte packHeader : PACK_HEADER) {
                buf[ofs++] = packHeader;
            }

            // set packet length
            HEADER_NEXT[4] = (byte) (tmp >> 8);
            HEADER_NEXT[5] = (byte) tmp;
            for (byte b : HEADER_NEXT) {
                buf[ofs++] = b;
            }

            // copy RLE buffer
            for (int i = ofsRLE; i < ofsRLE + rleSizeLeft; i++) {
                if (i < even.length) {
                    buf[ofs++] = even[i];
                } else {
                    buf[ofs++] = odd[i - even.length];
                }
            }
            ofsRLE += rleSizeLeft;
            // fill possible gap in all but last package with (parts of) control header
            // only if the control header is split over two packets
            // this can only happen in the package before the last one though
            if (p != numAdditionalPackets - 1) {
                for (; ofs < (p + 2) * 0x800; ofs++) {
                    buf[ofs] = CONTROL_HEADER[forcedOfs + (controlHeaderWritten++)];
                }
            }
        }

        // write (rest of) control header
        for (int i = controlHeaderWritten; i < controlHeaderLen; i++) {
            buf[ofs++] = CONTROL_HEADER[forcedOfs + i];
        }

        // fill rest of last packet with padding bytes
        diff = buf.length - ofs;
        if (diff >= 6) {
            diff -= 6;
            buf[ofs++] = 0x00;
            buf[ofs++] = 0x00;
            buf[ofs++] = 0x01;
            buf[ofs++] = (byte) 0xbe;
            buf[ofs++] = (byte) (diff >> 8);
            buf[ofs++] = (byte) diff;
            for (; ofs < buf.length; ofs++) {
                buf[ofs] = (byte) 0xff;
            }
        } // else should never happen due to stuffing bytes

        return buf;
    }

    /**
     * Create VobSub IDX file
     * @param fname file name
     * @param pic a SubPicture object used to read screen width and height
     * @param offsets array of offsets (one for each caption)
     * @param timestamps array of PTS time stamps (one for each caption)
     * @param palette 16 color main Palette
     * @throws bdsup2sub.core.CoreException
     */
    public static void writeIdx(String fname, SubPicture pic, int[] offsets, int[] timestamps, Palette palette) throws CoreException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(fname));

            out.write("# VobSub index file, v7 (do not modify this line!)"); out.newLine();
            out.write("# Created by " + Constants.APP_NAME + " " + Constants.APP_VERSION); out.newLine();
            out.newLine();
            out.write("# Frame size"); out.newLine();
            out.write("size: " + pic.getWidth() + "x" + (pic.getHeight() -2 * configuration.getCropOffsetY())); out.newLine();
            out.newLine();
            out.write("# Origin - upper-left corner"); out.newLine();
            out.write("org: 0, 0"); out.newLine();
            out.newLine();
            out.write("# Scaling"); out.newLine();
            out.write("scale: 100%, 100%"); out.newLine();
            out.newLine();
            out.write("# Alpha blending"); out.newLine();
            out.write("alpha: 100%"); out.newLine();
            out.newLine();
            out.write("# Smoothing"); out.newLine();
            out.write("smooth: OFF"); out.newLine();
            out.newLine();
            out.write("# Fade in/out in milliseconds"); out.newLine();
            out.write("fadein/out: 0, 0"); out.newLine();
            out.newLine();
            out.write("# Force subtitle placement relative to (org.x, org.y)"); out.newLine();
            out.write("align: OFF at LEFT TOP"); out.newLine();
            out.newLine();
            out.write("# For correcting non-progressive desync. (in millisecs or hh:mm:ss:ms)"); out.newLine();
            out.write("time offset: 0"); out.newLine();
            out.newLine();
            out.write("# ON: displays only forced subtitles, OFF: shows everything"); out.newLine();
            out.write("forced subs: OFF"); out.newLine();
            out.newLine();
            out.write("# The palette of the generated file"); out.newLine();
            out.write("palette: ");
            //Palette pal = Core.getCurrentDVDPalette();
            for (int i=0; i < palette.getSize(); i++) {
                int rbg[] = palette.getRGB(i);
                int val = (rbg[0]<<16) | (rbg[1]<<8) | rbg[2];
                out.write(ToolBox.toHexLeftZeroPadded(val, 6).substring(2));
                if (i != palette.getSize()-1) {
                    out.write(", ");
                }
            }
            out.newLine();out.newLine();
            out.write("# Custom colors (transp idxs and the four colors)"); out.newLine();
            out.write("custom colors: OFF, tridx: 1000, colors: 000000, 444444, 888888, cccccc"); out.newLine();
            out.newLine();
            out.write("# Language index in use"); out.newLine();
            out.write("langidx: 0"); out.newLine();
            out.newLine();
            out.write("# " + LANGUAGES[configuration.getLanguageIdx()][0]); out.newLine();
            out.write("id: " + LANGUAGES[configuration.getLanguageIdx()][1] + ", index: 0"); out.newLine();
            out.write("# Decomment next line to activate alternative name in DirectVobSub / Windows Media Player 6.x"); out.newLine();
            out.write("# alt: " + LANGUAGES[configuration.getLanguageIdx()][0]); out.newLine();
            out.write("# Vob/Cell ID: 1, 1 (PTS: 0)"); out.newLine();
            for (int i=0; i < timestamps.length; i++) {
                out.write("timestamp: "+ptsToTimeStrIdx(timestamps[i]));
                out.write(", filepos: "+ToolBox.toHexLeftZeroPadded(offsets[i], 9).substring(2));
                out.newLine();
            }
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