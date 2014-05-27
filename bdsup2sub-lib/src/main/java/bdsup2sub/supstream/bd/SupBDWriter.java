package bdsup2sub.supstream.bd;

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Configuration;
import bdsup2sub.core.Framerate;
import bdsup2sub.core.LibLogger;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.tools.QuantizeFilter;

import java.util.ArrayList;
import java.util.Iterator;

import static bdsup2sub.utils.ByteUtils.setByte;
import static bdsup2sub.utils.ByteUtils.setDWord;
import static bdsup2sub.utils.ByteUtils.setWord;

public final class SupBDWriter {

    private static final Configuration configuration = Configuration.getInstance();
    private static final LibLogger logger = LibLogger.getInstance();

    private static final byte PACKET_HEADER[] = {
            0x50, 0x47,             // 0:  "PG"
            0x00, 0x00, 0x00, 0x00, // 2:  PTS - presentation time stamp
            0x00, 0x00, 0x00, 0x00, // 6:  DTS - decoding time stamp
            0x00,                   // 10: segment_type
            0x00, 0x00,             // 11: segment_length (bytes following till next PG)
    };

    private static final byte HEADER_PCS_START[] = {
            0x00, 0x00, 0x00, 0x00, // 0: video_width, video_height
            0x10,                   // 4: hi nibble: frame_rate (0x10=24p), lo nibble: reserved
            0x00, 0x00,             // 5: composition_number (increased by start and end header)
            (byte)0x80,             // 7: composition_state (0x80: epoch start)
            0x00,                   // 8: palette_update_flag (0x80), 7bit reserved
            0x00,                   // 9: palette_id_ref (0..7)
            0x01,                   // 10: number_of_composition_objects (0..2)
            0x00, 0x00,             // 11: 16bit object_id_ref
            0x00,                   // 13: window_id_ref (0..1)
            0x00,                   // 14: object_cropped_flag: 0x80, forced_on_flag = 0x040, 6bit reserved
            0x00, 0x00, 0x00, 0x00  // 15: composition_object_horizontal_position, composition_object_vertical_position
    };

    private static final byte HEADER_PCS_END[] = {
            0x00, 0x00, 0x00, 0x00, // 0: video_width, video_height
            0x10,                   // 4: hi nibble: frame_rate (0x10=24p), lo nibble: reserved
            0x00, 0x00,             // 5: composition_number (increased by start and end header)
            0x00,                   // 7: composition_state (0x00: normal)
            0x00,                   // 8: palette_update_flag (0x80), 7bit reserved
            0x00,                   // 9: palette_id_ref (0..7)
            0x00,                   // 10: number_of_composition_objects (0..2)
    };


    private static final byte HEADER_ODS_FIRST[] = {
            0x00, 0x00,             // 0: object_id
            0x00,                   // 2: object_version_number
            (byte)0xC0,             // 3: first_in_sequence (0x80), last_in_sequence (0x40), 6bits reserved
            0x00, 0x00, 0x00,       // 4: object_data_length - full RLE buffer length (including 4 bytes size info)
            0x00, 0x00, 0x00, 0x00, // 7: object_width, object_height
    };

    private static final byte HEADER_ODS_NEXT[] = {
            0x00, 0x00,             // 0: object_id
            0x00,                   // 2: object_version_number
            (byte)0x40,             // 3: first_in_sequence (0x80), last_in_sequence (0x40), 6bits reserved
    };

    private static final byte HEADER_WDS[] = {
            0x01,                   // 0 : number of windows (currently assumed 1, 0..2 is legal)
            0x00,                   // 1 : window id (0..1)
            0x00, 0x00, 0x00, 0x00, // 2 : x-ofs, y-ofs
            0x00, 0x00, 0x00, 0x00  // 6 : width, height
    };

    private SupBDWriter() {
    }

    /**
     * Create the binary stream representation of one caption
     * @param pic SubPicture object containing caption info
     * @param bm bitmap
     * @param pal palette
     * @return byte buffer containing the binary stream representation of one caption
     */
    public static byte[] createSupFrame(SubPicture pic, Bitmap bm, Palette pal) {
        // the last palette entry must be transparent
        if (pal.getSize() > 255 && pal.getAlpha(255) > 0) {
            // quantize image
            QuantizeFilter qf = new QuantizeFilter();
            Bitmap bmQ = new Bitmap(bm.getWidth(), bm.getHeight());
            int[] ct = qf.quantize(bm.toARGB(pal), bmQ.getInternalBuffer(), bm.getWidth(), bm.getHeight(), 255, false, false);
            int size = ct.length;
            if (size > 255) {
                size = 255;
                logger.trace("Palette had to be reduced from " + pal.getSize() + " to " + size + " entries.\n");
                logger.warn("Quantizer failed.\n");
            } else {
                logger.trace("Palette had to be reduced from " + pal.getSize() + " to " + size + " entries.\n");
            }
            // create palette
            pal = new Palette(size);
            for (int i=0; i < size; i++) {
                pal.setARGB(i,ct[i]);
            }
            // use new bitmap
            bm = bmQ;
        }

        byte[] rleBuf = encodeImage(bm);

        // for some obscure reason, a packet can be a maximum 0xfffc bytes
        // since 13 bytes are needed for the header("PG", PTS, DTS, ID, SIZE)
        // there are only 0xffef bytes available for the packet
        // since the first ODS packet needs an additional 11 bytes for info
        // and the following ODS packets need 4 additional bytes, the
        // first package can store only 0xffe4 RLE buffer bytes and the
        // following packets can store 0xffeb RLE buffer bytes
        int numAddPackets;
        if (rleBuf.length <= 0xffe4) {
            numAddPackets = 0; // no additional packets needed;
        } else {
            numAddPackets = 1 + (rleBuf.length - 0xffe4) / 0xffeb;
        }

        // a typical frame consists of 8 packets. It can be enlonged by additional
        // object frames
        int palSize = bm.getHighestVisibleColorIndex(pal.getAlpha()) + 1;
        int size = PACKET_HEADER.length * (8 + numAddPackets);
        size += HEADER_PCS_START.length + HEADER_PCS_END.length;
        size += 2* HEADER_WDS.length + HEADER_ODS_FIRST.length;
        size += numAddPackets * HEADER_ODS_NEXT.length;
        size += (2 + palSize * 5) /* PDS */;
        size += rleBuf.length;

        int yOfs = pic.getYOffset() - configuration.getCropOffsetY();
        if (yOfs < 0) {
            yOfs = 0;
        } else {
            int yMax = pic.getHeight() - pic.getImageHeight() - 2 * configuration.getCropOffsetY();
            if (yOfs > yMax) {
                yOfs = yMax;
            }
        }

        int h = pic.getHeight() -2 * configuration.getCropOffsetY();

        byte[] buf = new byte[size];
        int index = 0;

        int fpsId = Framerate.idForFramerate(configuration.getFpsTrg());

        /* time (in 90kHz resolution) needed to initialize (clear) the screen buffer
           based on the composition pixel rate of 256e6 bit/s - always rounded up */
        int frameInitTime = (pic.getWidth() * pic.getHeight() * 9 + 3199) / 3200; // better use default height here
        /* time (in 90kHz resolution) needed to initialize (clear) the window area
           based on the composition pixel rate of 256e6 bit/s - always rounded up
           Note: no cropping etc. -> window size == image size */
        int windowInitTime = (bm.getWidth() * bm.getHeight() * 9 + 3199) / 3200;
        /* time (in 90kHz resolution) needed to decode the image
           based on the decoding pixel rate of 128e6 bit/s - always rounded up  */
        int imageDecodeTime = (bm.getWidth() * bm.getHeight() * 9 + 1599) / 1600;
        // write PCS start
        PACKET_HEADER[10] = 0x16;                               // ID
        setDWord(PACKET_HEADER, 2, (int) pic.getStartTime());   // PTS
        setDWord(PACKET_HEADER, 6, 0);                          // DTS (0)
        setWord(PACKET_HEADER, 11, HEADER_PCS_START.length);    // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }
        setWord(HEADER_PCS_START,0, pic.getWidth());
        setWord(HEADER_PCS_START, 2, h);                        // cropped height
        setByte(HEADER_PCS_START, 4, fpsId);
        setWord(HEADER_PCS_START, 5, pic.getCompositionNumber());
        HEADER_PCS_START[14] = (pic.isForced() ? (byte)0x40 : 0);
        setWord(HEADER_PCS_START,15, pic.getXOffset());
        setWord(HEADER_PCS_START,17, yOfs);
        for (byte b : HEADER_PCS_START) {
            buf[index++] = b;
        }

        // write WDS
        PACKET_HEADER[10] = 0x17;                                   // ID
        int timeStamp = (int) pic.getStartTime() - windowInitTime;
        setDWord(PACKET_HEADER, 2, timeStamp);                      // PTS
        setDWord(PACKET_HEADER, 6, 0);                              // DTS (0)
        setWord(PACKET_HEADER, 11, HEADER_WDS.length);              // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }
        setWord(HEADER_WDS, 2, pic.getXOffset());
        setWord(HEADER_WDS, 4, yOfs);
        setWord(HEADER_WDS, 6, bm.getWidth());
        setWord(HEADER_WDS, 8, bm.getHeight());
        for (byte b : HEADER_WDS) {
            buf[index++] = b;
        }

        // write PDS
        PACKET_HEADER[10] = 0x14;                       // ID
        int dts = (int) pic.getStartTime() - (frameInitTime + windowInitTime);
        setDWord(PACKET_HEADER, 2, dts);                // PTS
        setDWord(PACKET_HEADER, 6, 0);                  // DTS (0)
        setWord(PACKET_HEADER, 11, (2 + palSize * 5));  // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }
        buf[index++] = 0;
        buf[index++] = 0;
        for (int i=0; i < palSize; i++) {
            buf[index++] = (byte)i;             // index
            buf[index++] = pal.getY()[i];       // Y
            buf[index++] = pal.getCr()[i];      // Cr
            buf[index++] = pal.getCb()[i];      // Cb
            buf[index++] = pal.getAlpha()[i];   // Alpha
        }

        // write first OBJ
        int bufSize = rleBuf.length;
        int rleIndex = 0;
        if (bufSize > 0xffe4) {
            bufSize = 0xffe4;
        }
        PACKET_HEADER[10] = 0x15;                                       // ID
        timeStamp = dts + imageDecodeTime;
        setDWord(PACKET_HEADER, 2, timeStamp);                          // PTS
        setDWord(PACKET_HEADER, 6, 0);                                  // DTS (0)
        setWord(PACKET_HEADER, 11, HEADER_ODS_FIRST.length + bufSize);  // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }
        int marker = ((numAddPackets == 0) ? 0xC0000000 : 0x80000000);
        setDWord(HEADER_ODS_FIRST, 3, marker | (rleBuf.length + 4));
        setWord(HEADER_ODS_FIRST, 7, bm.getWidth());
        setWord(HEADER_ODS_FIRST, 9, bm.getHeight());
        for (byte b : HEADER_ODS_FIRST) {
            buf[index++] = b;
        }
        for (int i=0; i < bufSize; i++) {
            buf[index++] = rleBuf[rleIndex++];
        }

        // write additional OBJ packets
        bufSize = rleBuf.length-bufSize; // remaining bytes to write
        for (int p=0; p < numAddPackets; p++) {
            int psize = bufSize;
            if (psize > 0xffeb) {
                psize = 0xffeb;
            }
            PACKET_HEADER[10] = 0x15;                                   // ID (keep DTS & PTS)
            setWord(PACKET_HEADER, 11, HEADER_ODS_NEXT.length + psize); // size
            for (byte b : PACKET_HEADER) {
                buf[index++] = b;
            }
            for (byte b : HEADER_ODS_NEXT) {
                buf[index++] = b;
            }
            for (int i=0; i < psize; i++) {
                buf[index++] = rleBuf[rleIndex++];
            }
            bufSize -= psize;
        }

        // write END
        PACKET_HEADER[10] = (byte)0x80;                 // ID
        setDWord(PACKET_HEADER, 2, timeStamp);          // PTS
        setDWord(PACKET_HEADER, 6, 0);                  // DTS (0)
        setWord(PACKET_HEADER, 11, 0);                  // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }

        // write PCS end
        PACKET_HEADER[10] = 0x16;                               // ID
        setDWord(PACKET_HEADER, 2, (int) pic.getEndTime());     // PTS
        setDWord(PACKET_HEADER, 6, 0);                          // DTS (0)
        setWord(PACKET_HEADER, 11, HEADER_PCS_END.length);      // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }
        setWord(HEADER_PCS_END,0, pic.getWidth());
        setWord(HEADER_PCS_END, 2, h);                          // cropped height
        setByte(HEADER_PCS_END, 4, fpsId);
        setWord(HEADER_PCS_END, 5, pic.getCompositionNumber() + 1);
        for (byte b : HEADER_PCS_END) {
            buf[index++] = b;
        }

        // write WDS
        PACKET_HEADER[10] = 0x17;                               // ID
        timeStamp = (int) pic.getEndTime() - windowInitTime;
        setDWord(PACKET_HEADER, 2, timeStamp);                  // PTS
        setDWord(PACKET_HEADER, 6, 0);
        setWord(PACKET_HEADER, 11, HEADER_WDS.length);          // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }
        setWord(HEADER_WDS, 2, pic.getXOffset());
        setWord(HEADER_WDS, 4, yOfs);
        setWord(HEADER_WDS, 6, bm.getWidth());
        setWord(HEADER_WDS, 8, bm.getHeight());
        for (byte b : HEADER_WDS) {
            buf[index++] = b;
        }

        // write END
        PACKET_HEADER[10] = (byte)0x80;             // ID
        setDWord(PACKET_HEADER, 2, timeStamp);      // PTS (PTS of end PCS)
        setDWord(PACKET_HEADER, 6, 0);              // DTS (0)
        setWord(PACKET_HEADER, 11, 0);              // size
        for (byte b : PACKET_HEADER) {
            buf[index++] = b;
        }

        return buf;
    }

    /**
     * Create RLE buffer from bitmap
     * @param bm bitmap to compress
     * @return RLE buffer
     */
    private static byte[] encodeImage(Bitmap bm) {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        byte color;
        int ofs;
        int len;
        //boolean eol;

        for (int y=0; y < bm.getHeight(); y++) {
            ofs = y * bm.getWidth();
            //eol = false;
            int x;
            for (x=0; x < bm.getWidth(); x+=len, ofs+=len) {
                color = bm.getInternalBuffer()[ofs];
                for (len=1; x+len < bm.getWidth(); len++) {
                    if (bm.getInternalBuffer()[ofs+len] != color) {
                        break;
                    }
                }
                if (len<=2 && color != 0) {
                    // only a single occurrence -> add color
                    bytes.add(color);
                    if (len==2) {
                        bytes.add(color);
                    }
                } else {
                    if (len > 0x3fff) {
                        len = 0x3fff;
                    }
                    bytes.add((byte)0); // rle id
                    // commented out due to bug in SupRip
                    /*if (color == 0 && x+len == bm.getWidth()) {
                        bytes.add((byte)0);
                        eol = true;
                    } else*/
                    if (color == 0 && len < 0x40){
                        // 00 xx -> xx times 0
                        bytes.add((byte)len);
                    } else if (color == 0){
                        // 00 4x xx -> xxx zeroes
                        bytes.add((byte)(0x40|(len>>8)) );
                        bytes.add((byte)len);
                    } else if(len < 0x40) {
                        // 00 8x cc -> x times value cc
                        bytes.add((byte)(0x80|len) );
                        bytes.add(color);
                    } else {
                        // 00 cx yy cc -> xyy times value cc
                        bytes.add((byte)(0xc0|(len>>8)) );
                        bytes.add((byte)len);
                        bytes.add(color);
                    }
                }
            }
            if (/*!eol &&*/ x == bm.getWidth()) {
                bytes.add((byte)0); // rle id
                bytes.add((byte)0);
            }
        }
        int size =  bytes.size();
        byte[] retval = new byte[size];
        Iterator<Byte> it = bytes.iterator();
        for (int i=0; i < size; i++) {
            retval[i] = it.next();
        }
        return retval;
    }
}
