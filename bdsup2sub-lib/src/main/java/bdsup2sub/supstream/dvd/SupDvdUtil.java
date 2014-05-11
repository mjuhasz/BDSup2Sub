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
import bdsup2sub.core.CoreException;
import bdsup2sub.core.LibLogger;
import bdsup2sub.supstream.ImageObjectFragment;
import bdsup2sub.tools.FileBuffer;
import bdsup2sub.tools.FileBufferException;
import bdsup2sub.utils.ToolBox;

import java.util.ArrayList;
import java.util.Iterator;

public final class SupDvdUtil {

    private static final Configuration configuration = Configuration.getInstance();
    private static final LibLogger logger = LibLogger.getInstance();


    private SupDvdUtil() {
    }

    /**
     * Compress bitmap to RLE buffer
     *
     * @param bm   bitmap to compress
     * @param even true: encode even lines, false: encode odd lines
     * @return RLE buffer
     */
    public static byte[] encodeLines(Bitmap bm, boolean even) {
        int ofs;
        byte color;
        int len;
        int y;
        ArrayList<Byte> nibbles = new ArrayList<Byte>();

        if (even) {
            y = 0;
        } else {
            y = 1;
        }

        for (; y < bm.getHeight(); y += 2) {
            ofs = y * bm.getWidth();
            for (int x = 0; x < bm.getWidth(); x += len, ofs += len) {
                color = bm.getInternalBuffer()[ofs];
                for (len = 1; x + len < bm.getWidth(); len++)
                    if (bm.getInternalBuffer()[ofs + len] != color) {
                        break;
                    }
                if (len < 4) {
                    nibbles.add((byte) ((len << 2) | (color & 3)));
                } else if (len < 0x10) {
                    nibbles.add((byte) (len >> 2));
                    nibbles.add((byte) ((len << 2) | (color & 3)));
                } else if (len < 0x40) {
                    nibbles.add((byte) 0);
                    nibbles.add((byte) (len >> 2));
                    nibbles.add((byte) ((len << 2) | (color & 3)));
                } else if (x + len == bm.getWidth()) {
                    nibbles.add((byte) (0));
                    nibbles.add((byte) (0));
                    nibbles.add((byte) (0));
                    nibbles.add(color);
                } else {
                    if (len > 0xff) {
                        len = 0xff;
                    }
                    nibbles.add((byte) (0));
                    nibbles.add((byte) (len >> 6));
                    nibbles.add((byte) (len >> 2));
                    nibbles.add((byte) ((len << 2) | (color & 3)));
                }
            }
            if ((nibbles.size() & 1) == 1) {
                nibbles.add((byte) (0));
            }
        }
        // end buffer with line feed
        nibbles.add((byte) (0));
        nibbles.add((byte) (0));
        nibbles.add((byte) (0));
        nibbles.add((byte) (0));

        int size = nibbles.size() / 2; // number of bytes
        byte[] retval = new byte[size];
        Iterator<Byte> it = nibbles.iterator();

        for (int i = 0; i < size; i++) {
            int hi = (it.next() & 0xf);
            int lo = (it.next() & 0xf);
            retval[i] = (byte) ((hi << 4) | lo);
        }
        return retval;
    }

    /**
     * create fitting four color palette for the given caption
     * @param pic SubPicture object containing info about the caption
     * @param pal base palette
     * @return decoded palette
     */
    public static Palette decodePalette(SubPictureDVD pic, Palette pal) {
        Palette miniPal = new Palette(4, true);
        for (int i=0; i < 4; i++) {
            int a = (pic.getAlpha()[i] * 0xff) / 0xf;
            if (a >= configuration.getAlphaCrop()) {
                miniPal.setRGB(i, pal.getR()[pic.getPal()[i]]&0xff, pal.getG()[pic.getPal()[i]]&0xff, pal.getB()[pic.getPal()[i]]&0xff);
                miniPal.setAlpha(i, a);
            } else {
                miniPal.setARGB(i, 0);
            }
        }
        return miniPal;
    }

    /**
     * decode caption from the input stream
     * @param pic SubPicture object containing info about the caption
     * @param transIdx index of the transparent color
     * @return bitmap of the decoded caption
     * @throws bdsup2sub.core.CoreException
     */
    public static Bitmap decodeImage(final SubPictureDVD pic, final FileBuffer fBuf, final int transIdx) throws CoreException {
        int w = pic.getOriginalWidth();
        int h = pic.getOriginalHeight();
        int warnings = 0;

        ImageObjectFragment info = pic.getRleFragments().get(0);
        long startOfs = info.getImageBufferOfs();

        if (w > pic.getWidth() || h > pic.getHeight()) {
            logger.warn("Subpicture too large: " + w + "x" + h
                    + " at offset " + ToolBox.toHexLeftZeroPadded(startOfs, 8) + "\n");
        }

        Bitmap bm = new Bitmap(w, h, (byte)transIdx);

        // copy buffer(s)
        byte buf[] = new byte[pic.getRleSize()];
        int index = 0;

        int sizeEven;
        int sizeOdd;

        if (pic.getOddOffset() > pic.getEvenOffset()) {
            sizeEven = pic.getOddOffset() - pic.getEvenOffset();
            sizeOdd = pic.getRleSize() - pic.getOddOffset();
        } else {
            sizeOdd = pic.getEvenOffset() - pic.getOddOffset();
            sizeEven = pic.getRleSize() - pic.getEvenOffset();
        }

        if (sizeEven <= 0 || sizeOdd <= 0)
            throw new CoreException("Corrupt buffer offset information");

        try {
            // copy buffers
            try {
                for (int p = 0; p < pic.getRleFragments().size(); p++) {
                    // copy data of all packet to one common buffer
                    info = pic.getRleFragments().get(p);
                    for (int i=0; i < info.getImagePacketSize(); i++) {
                        buf[index+i] = (byte)fBuf.getByte(info.getImageBufferOfs() + i);
                    }
                    index += info.getImagePacketSize();
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                warnings++;
            }
            // decode even lines
            try {
                decodeLine(buf, pic.getEvenOffset(), sizeEven, bm.getInternalBuffer(), 0, w,  w*(h/2+(h&1)));
            } catch (ArrayIndexOutOfBoundsException ex) {
                warnings++;
            }
            // decode odd lines
            try {
                decodeLine(buf, pic.getOddOffset(), sizeOdd, bm.getInternalBuffer(), w, w, (h/2)*w);
            } catch (ArrayIndexOutOfBoundsException ex) {
                warnings++;
            }

            if (warnings > 0) {
                logger.warn("problems during RLE decoding of picture at offset " + ToolBox.toHexLeftZeroPadded(startOfs, 8) + "\n");
            }
            return bm;
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
    }

    /**
     * decode one line from the RLE buffer
     * @param src source buffer
     * @param srcOfs offset in source buffer
     * @param srcLen length of bytes to decode in source buffer
     * @param trg target buffer for uncompressed data
     * @param trgOfs offset in target buffer
     * @param width image width of encoded caption
     * @param maxPixels maximum number of pixels in caption
     */
    private static void decodeLine(byte[] src, int srcOfs, int srcLen, byte[] trg, int trgOfs, int width, int maxPixels) {
        // to make access to nibbles easier, copy bytes to a nibble array
        byte nibbles[] = new byte[srcLen * 2];
        int b;

        for (int i=0; i < srcLen; i++) {
            b = src[srcOfs + i] & 0xff;
            nibbles[2 * i]   = (byte)(b >> 4);
            nibbles[2 * i + 1] = (byte)(b & 0x0f);
        }

        int index = 0;
        int sumPixels = 0;
        int x=0;

        while (index < nibbles.length && sumPixels < maxPixels) {
            int len;
            int col;
            b = nibbles[index++] & 0xff;
            if (b == 0) {
                // three or four nibble code
                b = nibbles[index++] & 0xff;
                if ((b & 0xc) != 0) {
                    // three byte code
                    len = b << 2;
                    b = nibbles[index++] & 0xff;
                    len |= (b >> 2);
                } else {
                    // line feed or four nibble code
                    len = b << 6;
                    b = nibbles[index++] & 0xff;
                    len  |= (b << 2);
                    b = nibbles[index++] & 0xff;
                    len |= (b >> 2);
                    if (len == 0) {
                        // line feed
                        len = width-x;
                        if (len <= 0 || sumPixels >= maxPixels) {
                            len = 0;
                            // handle line feed
                            trgOfs += 2 * width; // lines are interlaced!
                            sumPixels = ((trgOfs/width) / 2) * width;
                            x = 0;
                        }
                        if ((index & 1) == 1) {
                            index++;
                        }
                    }
                }
            } else {
                // one or two nibble code
                len = b >> 2;
                if (len == 0) {
                    // two nibble code
                    len = b << 2;
                    b = nibbles[index++] & 0xff;
                    len  |= (b >> 2);
                }
            }

            col = b & 0x3;
            sumPixels += len;

            for (int i=0; i < len; i++) {
                trg[trgOfs+x] = (byte)col;
                if (++x >= width) {
                    trgOfs += 2*width; // lines are interlaced!
                    x = 0;
                    if ((index & 1) == 1) {
                        index++;
                    }
                }
            }
        }
    }
}