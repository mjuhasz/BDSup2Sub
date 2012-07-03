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
package bdsup2sub.supstream.bd;

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Configuration;
import bdsup2sub.core.Core;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.Framerate;
import bdsup2sub.supstream.*;
import bdsup2sub.tools.FileBuffer;
import bdsup2sub.tools.FileBufferException;
import bdsup2sub.tools.QuantizeFilter;
import bdsup2sub.utils.ToolBox;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import static bdsup2sub.utils.ByteUtils.*;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStr;

/**
 * Reading and writing of Blu-Ray captions demuxed from M2TS transport streams (BD-SUP).
 */
public class SupBD implements SubtitleStream {

    private static final Configuration configuration = Configuration.getInstance();

    private enum PGSCompositionState {
        /** normal: doesn't have to be complete */
        NORMAL,
        /** acquisition point */
        ACQU_POINT,
        /** epoch start - clears the screen */
        EPOCH_START,
        /** epoch continue */
        EPOCH_CONTINUE,
        /** unknown value */
        INVALID
    }

    /** ArrayList of captions contained in the current file  */
    private ArrayList<SubPictureBD> subPictures;
    /** color palette of the last decoded caption  */
    private Palette palette;
    /** bitmap of the last decoded caption  */
    private Bitmap bitmap;
    /** FileBuffer to read from the file  */
    private FileBuffer buffer;
    /** index of dominant color for the current caption  */
    private int primaryColorIndex;
    /** number of forced captions in the current file  */
    private int numForcedFrames;

    private static byte packetHeader[] = {
        0x50, 0x47,				// 0:  "PG"
        0x00, 0x00, 0x00, 0x00,	// 2:  PTS - presentation time stamp
        0x00, 0x00, 0x00, 0x00,	// 6:  DTS - decoding time stamp
        0x00,					// 10: segment_type
        0x00, 0x00,				// 11: segment_length (bytes following till next PG)
    };

    private static byte headerPCSStart[] = {
        0x00, 0x00, 0x00, 0x00,	// 0: video_width, video_height
        0x10, 					// 4: hi nibble: frame_rate (0x10=24p), lo nibble: reserved
        0x00, 0x00,				// 5: composition_number (increased by start and end header)
        (byte)0x80,				// 7: composition_state (0x80: epoch start)
        0x00,					// 8: palette_update_flag (0x80), 7bit reserved
        0x00,					// 9: palette_id_ref (0..7)
        0x01,					// 10: number_of_composition_objects (0..2)
        0x00, 0x00,				// 11: 16bit object_id_ref
        0x00,					// 13: window_id_ref (0..1)
        0x00,					// 14: object_cropped_flag: 0x80, forced_on_flag = 0x040, 6bit reserved
        0x00, 0x00, 0x00, 0x00	// 15: composition_object_horizontal_position, composition_object_vertical_position
    };

    private static byte headerPCSEnd[] = {
        0x00, 0x00, 0x00, 0x00,	// 0: video_width, video_height
        0x10,					// 4: hi nibble: frame_rate (0x10=24p), lo nibble: reserved
        0x00, 0x00,				// 5: composition_number (increased by start and end header)
        0x00,					// 7: composition_state (0x00: normal)
        0x00,					// 8: palette_update_flag (0x80), 7bit reserved
        0x00,					// 9: palette_id_ref (0..7)
        0x00,					// 10: number_of_composition_objects (0..2)
    };


    private static byte headerODSFirst[] = {
        0x00, 0x00,				// 0: object_id
        0x00,					// 2: object_version_number
        (byte)0xC0,				// 3: first_in_sequence (0x80), last_in_sequence (0x40), 6bits reserved
        0x00, 0x00, 0x00,		// 4: object_data_length - full RLE buffer length (including 4 bytes size info)
        0x00, 0x00, 0x00, 0x00,	// 7: object_width, object_height
    };

    private static byte headerODSNext[] = {
        0x00, 0x00,				// 0: object_id
        0x00,					// 2: object_version_number
        (byte)0x40,				// 3: first_in_sequence (0x80), last_in_sequence (0x40), 6bits reserved
    };

    private static byte headerWDS[] = {
        0x01,					// 0 : number of windows (currently assumed 1, 0..2 is legal)
        0x00,					// 1 : window id (0..1)
        0x00, 0x00, 0x00, 0x00,	// 2 : x-ofs, y-ofs
        0x00, 0x00, 0x00, 0x00	// 6 : width, height
    };


    public SupBD(String filename) throws CoreException {
        //int tFrame = (int)(90000/Core.getFPSSrc());
        int index = 0;
        try {
            buffer = new FileBuffer(filename);
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
        int bufsize = (int)buffer.getSize();
        SupSegment segment;
        SubPictureBD subPictureBD = null;
        SubPictureBD lastSubPicture = null;
        SubPictureBD picTmp = null;
        subPictures = new ArrayList<SubPictureBD>();
        int odsCounter = 0;
        int pdsCounter = 0;
        int odsCounterOld = 0;
        int pdsCounterOld = 0;
        int compositionNumber = -1;
        int compositionNumberOld = -1;
        int compositionCount = 0;
        long ptsPCS = 0;
        boolean paletteUpdate = false;
        PGSCompositionState compositionState = PGSCompositionState.INVALID;

        try {
            while (index < bufsize) {
                // for threaded version
                if (Core.isCancelled()) {
                    throw new CoreException("Cancelled by user!");
                }
                Core.setProgress(index);
                segment = readSegment(index);
                String msg;
                String so[] = new String[1]; // hack to return string
                switch (segment.segmentType) {
                    case 0x14: // palette
                        msg = "PDS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4);
                        if (compositionNumber != compositionNumberOld) {
                            if (subPictureBD != null) {
                                so[0] = null;
                                int paletteSize = parsePDS(segment, subPictureBD, so);
                                if (paletteSize >= 0) {
                                    Core.print(msg + ", " + so[0] + "\n");
                                    if (paletteSize > 0) {
                                        pdsCounter++;
                                    }
                                } else {
                                    Core.print(msg + "\n");
                                    Core.printWarn(so[0] + "\n");
                                }
                            } else {
                                Core.print(msg + "\n");
                                Core.printWarn("missing PTS start -> ignored\n");
                            }
                        } else {
                            Core.print(msg + ", composition number unchanged -> ignored\n");
                        }
                        break;
                    case 0x15: // image bitmap data
                        msg = "ODS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4);
                        if (compositionNumber != compositionNumberOld) {
                            if (!paletteUpdate) {
                                if (subPictureBD != null) {
                                    so[0] = null;
                                    if (parseODS(segment, subPictureBD, so)) {
                                        odsCounter++;
                                    }
                                    Core.print(msg + ", img size: " + subPictureBD.getImageWidth() + "*" + subPictureBD.getImageHeight() + (so[0] == null ? "\n" : ", " + so[0]) + "\n");
                                } else {
                                    Core.print(msg + "\n");
                                    Core.printWarn("missing PTS start -> ignored\n");
                                }
                            } else {
                                Core.print(msg + "\n");
                                Core.printWarn("palette update only -> ignored\n");
                            }
                        } else {
                            Core.print(msg + ", composition number unchanged -> ignored\n");
                        }
                        break;
                    case 0x16: // time codes
                        compositionNumber = getCompositionNumber(segment);
                        compositionState = getCompositionState(segment);
                        paletteUpdate = getPaletteUpdateFlag(segment);
                        ptsPCS = segment.segmentPTSTimestamp;
                        if (segment.segmentSize >= 0x13) {
                            compositionCount = 1; // could be also 2, but we'll ignore this for the moment
                        } else {
                            compositionCount = 0;
                        }
                        if (compositionState == PGSCompositionState.INVALID) {
                            Core.printWarn("Illegal composition state at offset " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                        } else if (compositionState == PGSCompositionState.EPOCH_START) {
                            // new frame
                            if (subPictures.size() > 0 && (odsCounter == 0 || pdsCounter == 0)) {
                                Core.printWarn("missing PDS/ODS: last epoch is discarded\n");
                                subPictures.remove(subPictures.size() - 1);
                                compositionNumberOld = compositionNumber - 1;
                                if (subPictures.size() > 0) {
                                    lastSubPicture = subPictures.get(subPictures.size() - 1);
                                } else {
                                    lastSubPicture = null;
                                }
                            } else {
                                lastSubPicture = subPictureBD;
                            }
                            subPictureBD = new SubPictureBD();
                            subPictures.add(subPictureBD);
                            subPictureBD.startTime = segment.segmentPTSTimestamp;
                            Core.printX("#> " + (subPictures.size()) + " (" + ptsToTimeStr(subPictureBD.startTime) + ")\n");

                            so[0] = null;
                            parsePCS(segment, subPictureBD, so);
                            // fix end time stamp of previous subPictureBD if still missing
                            if (lastSubPicture != null && lastSubPicture.endTime == 0) {
                                lastSubPicture.endTime = subPictureBD.startTime;
                            }

                            msg = "PCS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", START, size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4) + ", composition number: " + compositionNumber + ", forced: " + subPictureBD.isforced + (so[0] == null ? "\n" : ", " + so[0] + "\n");
                            msg += "PTS start: " + ptsToTimeStr(subPictureBD.startTime) + ", screen size: " + subPictureBD.width + "*" + subPictureBD.height + "\n";
                            Core.print(msg);

                            odsCounter = 0;
                            pdsCounter = 0;
                            odsCounterOld = 0;
                            pdsCounterOld = 0;
                            picTmp = null;
                        } else {
                            if (subPictureBD == null) {
                                Core.printWarn("missing start of epoch at offset " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                                break;
                            }
                            msg = "PCS offset:" + ToolBox.toHexLeftZeroPadded(index, 8) + ", ";
                            switch (compositionState) {
                                case EPOCH_CONTINUE:
                                    msg += "CONT, ";
                                    break;
                                case ACQU_POINT:
                                    msg += "ACQU, ";
                                    break;
                                case NORMAL:
                                    msg += "NORM, ";
                                    break;
                            }
                            msg += " size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4) + ", composition number: " + compositionNumber + ", forced: " + subPictureBD.isforced;
                            if (compositionNumber != compositionNumberOld) {
                                so[0] = null;
                                // store state to be able to revert to it
                                picTmp = subPictureBD.deepCopy();
                                // create new subPictureBD
                                parsePCS(segment, subPictureBD, so);
                            }
                            if (so[0] != null) {
                                msg += ", " + so[0];
                            }
                            msg += ", pal update: " + paletteUpdate + "\n";
                            msg += "PTS: " + ptsToTimeStr(segment.segmentPTSTimestamp) + "\n";
                            Core.print(msg);
                        }
                        break;
                    case 0x17: // window info
                        msg = "WDS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4);
                        if (subPictureBD != null) {
                            parseWDS(segment, subPictureBD);
                            Core.print(msg + ", dim: " + subPictureBD.winWidth + "*" + subPictureBD.winHeight + "\n");
                        } else {
                            Core.print(msg + "\n");
                            Core.printWarn("Missing PTS start -> ignored\n");
                        }
                        break;
                    case 0x80: // END
                        Core.print("END offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                        // decide whether to store this last composition section as caption or merge it
                        if (compositionState == PGSCompositionState.EPOCH_START) {
                            if (compositionCount>0 && odsCounter>odsCounterOld && compositionNumber!=compositionNumberOld && picMergable(lastSubPicture, subPictureBD)) {
                                // the last start epoch did not contain any (new) content
                                // and should be merged to the previous frame
                                subPictures.remove(subPictures.size()-1);
                                subPictureBD = lastSubPicture;
                                if (subPictures.size() > 0) {
                                    lastSubPicture = subPictures.get(subPictures.size()-1);
                                } else {
                                    lastSubPicture = null;
                                }
                                Core.printX("#< caption merged\n");
                            }
                        } else {
                            long startTime = 0;
                            if (subPictureBD != null) {
                                startTime = subPictureBD.startTime;  // store
                                subPictureBD.startTime = ptsPCS;    // set for testing merge
                            }

                            if (compositionCount>0 && odsCounter>odsCounterOld && compositionNumber!=compositionNumberOld && !picMergable(picTmp, subPictureBD)) {
                                // last PCS should be stored as separate caption
                                if (odsCounter-odsCounterOld>1 || pdsCounter-pdsCounterOld>1) {
                                    Core.printWarn("multiple PDS/ODS definitions: result may be erratic\n");
                                }
                                // replace subPictureBD with picTmp (deepCopy created before new PCS)
                                subPictures.set(subPictures.size()-1, picTmp); // replace in list
                                lastSubPicture = picTmp;
                                subPictures.add(subPictureBD); // add to list
                                Core.printX("#< " + (subPictures.size()) + " (" + ptsToTimeStr(subPictureBD.startTime) + ")\n");
                                odsCounterOld = odsCounter;

                            } else {
                                if (subPictureBD != null) {
                                    // merge with previous subPictureBD
                                    subPictureBD.startTime = startTime; // restore
                                    subPictureBD.endTime = ptsPCS;
                                    // for the unlikely case that forced flag changed during one captions
                                    if (picTmp != null && picTmp.isforced) {
                                        subPictureBD.isforced = true;
                                    }

                                    if (pdsCounter > pdsCounterOld || paletteUpdate) {
                                        Core.printWarn("palette animation: result may be erratic\n");
                                    }
                                } else {
                                    Core.printWarn("end without at least one epoch start\n");
                                }
                            }
                        }
                        pdsCounterOld = pdsCounter;
                        compositionNumberOld = compositionNumber;
                        break;
                    default:
                        Core.printWarn("<unknown> " + ToolBox.toHexLeftZeroPadded(segment.segmentType, 2) + " ofs:" + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                    break;
                }
                index += 13; // header size
                index += segment.segmentSize;
            }
        } catch (CoreException ex) {
            if (subPictures.size() == 0) {
                throw ex;
            }
            Core.printErr(ex.getMessage()+"\n");
            Core.print("Probably not all caption imported due to error.\n");
        }

        // check if last frame is valid
        if (subPictures.size() > 0 && (odsCounter==0 || pdsCounter==0)) {
            Core.printWarn("missing PDS/ODS: last epoch is discarded\n");
            subPictures.remove(subPictures.size()-1);
        }

        Core.setProgress(bufsize);
        // count forced frames
        numForcedFrames = 0;
        for (SubPictureBD p : subPictures) {
            if (p.isforced) {
                numForcedFrames++;
            }
        }
        Core.printX("\nDetected " + numForcedFrames + " forced captions.\n");
    }

    /**
     * Checks if two SubPicture object can be merged because the time gap between them is rather small
     * and the embedded objects seem to be identical
     * @param a first SubPicture object (earlier)
     * @param b 2nd SubPicture object (later)
     * @return true if the SubPictures can be merged
     */
    private static boolean picMergable(SubPictureBD a, SubPictureBD b) {
        boolean eq = false;
        if (a != null && b != null) {
            if (a.endTime == 0 || b.startTime - a.endTime < configuration.getMergePTSdiff()) {
            ImageObject ao = a.getImgObj();
            ImageObject bo = b.getImgObj();
            if (ao != null && bo != null)
                if (ao.getBufferSize() == bo.getBufferSize() && ao.getWidth() == bo.getWidth() && ao.getHeight() == bo.getHeight()) {
                    eq = true;
                }
            }
        }
        return eq;
    }

    /**
     * Get ID for given frame rate
     * @param fps frame rate
     * @return byte ID for the given frame rate
     */
    private static int getFpsId(double fps) {
        if (fps == Framerate.FPS_24.getValue())
            return 0x20;
        if (fps == Framerate.PAL.getValue())
            return 0x30;
        if (fps == Framerate.NTSC.getValue())
            return 0x40;
        if (fps == Framerate.PAL_I.getValue())
            return 0x60;
        if (fps == Framerate.NTSC_I.getValue())
            return 0x70;
        // assume FPS_23_976 (24p) (also for FPS_23_975)
        return 0x10;
    }

    /**
     * Get frame rate for given byte ID
     * @param id byte ID
     * @return frame rate
     */
    private static double getFpsFromID(final int id) {
        switch (id) {
            case 0x20:
                return Framerate.FPS_24.getValue();
            case 0x30:
                return Framerate.PAL.getValue();
            case 0x40:
                return Framerate.NTSC.getValue();
            case 0x60:
                return Framerate.PAL_I.getValue();
            case 0x70:
                return Framerate.NTSC_I.getValue();
            default:
                return Framerate.FPS_23_976.getValue(); // assume FPS_23_976 (24p) (also for FPS_23_975)
        }
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

    /**
     * Create the binary stream representation of one caption
     * @param pic SubPicture object containing caption info
     * @param bm bitmap
     * @param pal palette
     * @return byte buffer containing the binary stream representation of one caption
     */
    public static byte[] createSupFrame(final SubPicture pic, Bitmap bm, Palette pal) {
        // the last palette entry must be transparent
        if (pal.getSize() > 255 && pal.getAlpha(255) > 0) {
            // quantize image
            QuantizeFilter qf = new QuantizeFilter();
            Bitmap bmQ = new Bitmap(bm.getWidth(), bm.getHeight());
            int ct[] = qf.quantize(bm.toARGB(pal), bmQ.getInternalBuffer(), bm.getWidth(), bm.getHeight(), 255, false, false);
            int size = ct.length;
            if (size > 255) {
                size = 255;
                Core.print("Palette had to be reduced from " + pal.getSize() + " to " + size + " entries.\n");
                Core.printWarn("Quantizer failed.\n");
            } else {
                Core.print("Palette had to be reduced from " + pal.getSize() + " to " + size + " entries.\n");
            }
            // create palette
            pal = new Palette(size);
            for (int i=0; i < size; i++) {
                pal.setARGB(i,ct[i]);
            }
            // use new bitmap
            bm = bmQ;
        }

        byte rleBuf[] = encodeImage(bm);

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
        int size = packetHeader.length * (8 + numAddPackets);
        size += headerPCSStart.length + headerPCSEnd.length;
        size += 2*headerWDS.length + headerODSFirst.length;
        size += numAddPackets * headerODSNext.length;
        size += (2 + palSize * 5) /* PDS */;
        size += rleBuf.length;

        int yOfs = pic.getOfsY() - configuration.getCropOffsetY();
        if (yOfs < 0) {
            yOfs = 0;
        } else {
            int yMax = pic.height - pic.getImageHeight() - 2 * configuration.getCropOffsetY();
            if (yOfs > yMax) {
                yOfs = yMax;
            }
        }

        int h = pic.height-2 * configuration.getCropOffsetY();

        byte buf[] = new byte[size];
        int index = 0;

        int fpsId = getFpsId(configuration.getFpsTrg());

        /* time (in 90kHz resolution) needed to initialize (clear) the screen buffer
           based on the composition pixel rate of 256e6 bit/s - always rounded up */
        int frameInitTime = (pic.width * pic.height * 9 + 3199) / 3200; // better use default height here
        /* time (in 90kHz resolution) needed to initialize (clear) the window area
           based on the composition pixel rate of 256e6 bit/s - always rounded up
           Note: no cropping etc. -> window size == image size */
        int windowInitTime = (bm.getWidth() * bm.getHeight() * 9 + 3199) / 3200;
        /* time (in 90kHz resolution) needed to decode the image
           based on the decoding pixel rate of 128e6 bit/s - always rounded up  */
        int imageDecodeTime = (bm.getWidth() * bm.getHeight() * 9 + 1599) / 1600;
        // write PCS start
        packetHeader[10] = 0x16;											// ID
        int dts = (int)pic.startTime - (frameInitTime + windowInitTime);
        setDWord(packetHeader, 2, (int)pic.startTime);				// PTS
        setDWord(packetHeader, 6, dts);								// DTS
        setWord(packetHeader, 11, headerPCSStart.length);			// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }
        setWord(headerPCSStart,0, pic.width);
        setWord(headerPCSStart,2, h);								// cropped height
        setByte(headerPCSStart,4, fpsId);
        setWord(headerPCSStart,5, pic.compNum);
        headerPCSStart[14] = (pic.isforced ? (byte)0x40 : 0);
        setWord(headerPCSStart,15, pic.getOfsX());
        setWord(headerPCSStart,17, yOfs);
        for (byte b : headerPCSStart) {
            buf[index++] = b;
        }

        // write WDS
        packetHeader[10] = 0x17;											// ID
        int timeStamp = (int)pic.startTime - windowInitTime;
        setDWord(packetHeader, 2, timeStamp);						// PTS (keep DTS)
        setWord(packetHeader, 11, headerWDS.length);				// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }
        setWord(headerWDS, 2, pic.getOfsX());
        setWord(headerWDS, 4, yOfs);
        setWord(headerWDS, 6, bm.getWidth());
        setWord(headerWDS, 8, bm.getHeight());
        for (byte b : headerWDS) {
            buf[index++] = b;
        }

        // write PDS
        packetHeader[10] = 0x14;											// ID
        setDWord(packetHeader, 2, dts);								// PTS (=DTS of PCS/WDS)
        setDWord(packetHeader, 6, 0);								// DTS (0)
        setWord(packetHeader, 11, (2+palSize*5));					// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }
        buf[index++] = 0;
        buf[index++] = 0;
        for (int i=0; i < palSize; i++) {
            buf[index++] = (byte)i;											// index
            buf[index++] = pal.getY()[i];									// Y
            buf[index++] = pal.getCr()[i];									// Cr
            buf[index++] = pal.getCb()[i];									// Cb
            buf[index++] = pal.getAlpha()[i];								// Alpha
        }

        // write first OBJ
        int bufSize = rleBuf.length;
        int rleIndex = 0;
        if (bufSize > 0xffe4) {
            bufSize = 0xffe4;
        }
        packetHeader[10] = 0x15;											// ID
        timeStamp = dts + imageDecodeTime;
        setDWord(packetHeader, 2, timeStamp);						// PTS
        setDWord(packetHeader, 6, dts);								// DTS
        setWord(packetHeader, 11, headerODSFirst.length+bufSize);	// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }
        int marker = ((numAddPackets == 0) ? 0xC0000000 : 0x80000000);
        setDWord(headerODSFirst, 3, marker | (rleBuf.length+4));
        setWord(headerODSFirst, 7, bm.getWidth());
        setWord(headerODSFirst, 9, bm.getHeight());
        for (byte b : headerODSFirst) {
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
            packetHeader[10] = 0x15;										// ID (keep DTS & PTS)
            setWord(packetHeader, 11, headerODSNext.length + psize);	// size
            for (byte b : packetHeader) {
                buf[index++] = b;
            }
            for (byte b : headerODSNext) {
                buf[index++] = b;
            }
            for (int i=0; i < psize; i++) {
                buf[index++] = rleBuf[rleIndex++];
            }
            bufSize -= psize;
        }

        // write END
        packetHeader[10] = (byte)0x80;										// ID
        setDWord(packetHeader, 6, 0);								// DTS (0) (keep PTS of ODS)
        setWord(packetHeader, 11, 0);								// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }

        // write PCS end
        packetHeader[10] = 0x16;											// ID
        setDWord(packetHeader, 2, (int)pic.endTime);				// PTS
        dts = (int)pic.startTime - 1;
        setDWord(packetHeader, 6, dts);								// DTS
        setWord(packetHeader, 11, headerPCSEnd.length);				// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }
        setWord(headerPCSEnd,0, pic.width);
        setWord(headerPCSEnd,2, h);									// cropped height
        setByte(headerPCSEnd,4, fpsId);
        setWord(headerPCSEnd,5, pic.compNum+1);
        for (byte b : headerPCSEnd) {
            buf[index++] = b;
        }

        // write WDS
        packetHeader[10] = 0x17;											// ID
        timeStamp = (int)pic.endTime - windowInitTime;
        setDWord(packetHeader, 2, timeStamp);						// PTS (keep DTS of PCS)
        setWord(packetHeader, 11, headerWDS.length);				// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }
        setWord(headerWDS, 2, pic.getOfsX());
        setWord(headerWDS, 4, yOfs);
        setWord(headerWDS, 6, bm.getWidth());
        setWord(headerWDS, 8, bm.getHeight());
        for (byte b : headerWDS) {
            buf[index++] = b;
        }

        // write END
        packetHeader[10] = (byte)0x80;										// ID
        setDWord(packetHeader, 2, dts);								// PTS (DTS of end PCS)
        setDWord(packetHeader, 6, 0);								// DTS (0)
        setWord(packetHeader, 11, 0);								// size
        for (byte b : packetHeader) {
            buf[index++] = b;
        }

        return buf;
    }

    /**
     * read segment from the input stream
     * @param offset offset inside the input stream
     * @return SupSegment object containing info about the segment
     * @throws CoreException
     */
    private SupSegment readSegment(int offset) throws CoreException {
        try {
            SupSegment segment = new SupSegment();
            if (buffer.getWord(offset) != 0x5047) {
                throw new CoreException("PG missing at index " + ToolBox.toHexLeftZeroPadded(offset,8) + "\n");
            }
            segment.segmentPTSTimestamp = buffer.getDWord(offset+=2); // read PTS
            offset += 4; /* ignore DTS */
            segment.segmentType = buffer.getByte(offset+=4);
            segment.segmentSize = buffer.getWord(++offset);
            segment.offset = offset+2;
            return segment;
        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        }
    }

    /**
     * Retrieve composition state from PCS segment
     * @param segment the segment to analyze
     * @return CompositionState
     * @throws CoreException
     */
    private PGSCompositionState getCompositionState(SupSegment segment) throws CoreException {
        int type;
        try {
            type = buffer.getByte(segment.offset+7);
            switch (type) {
                case 0x00:
                    return PGSCompositionState.NORMAL;
                case 0x40:
                    return PGSCompositionState.ACQU_POINT;
                case 0x80:
                    return PGSCompositionState.EPOCH_START;
                case 0xC0:
                    return PGSCompositionState.EPOCH_CONTINUE;
                default:
                    return PGSCompositionState.INVALID;
            }
        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        }
    }

    /**
     * Retrieve composition number from PCS segment
     * @param segment the segment to analyze
     * @return composition number as integer
     * @throws CoreException
     */
    private int getCompositionNumber(SupSegment segment) throws CoreException {
        try {
            return buffer.getWord(segment.offset+5);
        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        }
    }

    /**
     * Retrieve palette (only) update flag from PCS segment
     * @param segment the segment to analyze
     * @return true: this is only a palette update - ignore ODS
     * @throws CoreException
     */
    private boolean getPaletteUpdateFlag(SupSegment segment) throws CoreException {
        try {
            return buffer.getByte(segment.offset+8) == 0x80;
        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        }
    }

    /**
     * parse an PCS packet which contains width/height info
     * @param segment object containing info about the current segment
     * @param pic SubPicture object containing info about the current caption
     * @param msg reference to message string
     * @throws CoreException
     */
    private void parsePCS(SupSegment segment, SubPictureBD pic, String msg[]) throws CoreException {
        int index = segment.offset;
        try {
            if (segment.segmentSize >= 4) {
                pic.width  = buffer.getWord(index);			// video_width
                pic.height = buffer.getWord(index+2);		// video_height
                int type = buffer.getByte(index+4);	// hi nibble: frame_rate, lo nibble: reserved
                int num  = buffer.getWord(index+5); 	// composition_number
                // skipped:
                // 8bit  composition_state: 0x00: normal, 		0x40: acquisition point
                //							0x80: epoch start,	0xC0: epoch continue, 6bit reserved
                // 8bit  palette_update_flag (0x80), 7bit reserved
                int palID = buffer.getByte(index+9);	// 8bit  palette_id_ref
                int coNum = buffer.getByte(index+10);	// 8bit  number_of_composition_objects (0..2)
                if (coNum > 0) {
                    // composition_object:
                    int objID = buffer.getWord(index+11);	// 16bit object_id_ref
                    msg[0] = "palID: "+palID+", objID: "+objID;
                    if (pic.imageObjectList == null) {
                        pic.imageObjectList = new ArrayList<ImageObject>();
                    }
                    ImageObject imgObj;
                    if (objID >= pic.imageObjectList.size()) {
                        imgObj = new ImageObject();
                        pic.imageObjectList.add(imgObj);
                    } else {
                        imgObj = pic.getImgObj(objID);
                    }
                    imgObj.setPaletteID(palID);
                    pic.objectID = objID;

                    // skipped:  8bit  window_id_ref
                    if (segment.segmentSize >= 0x13) {
                        pic.type = type;
                        // object_cropped_flag: 0x80, forced_on_flag = 0x040, 6bit reserved
                        int forcedCropped = buffer.getByte(index+14);
                        pic.compNum = num;
                        pic.isforced = ( (forcedCropped & 0x40) == 0x40);
                        imgObj.setxOfs(buffer.getWord(index+15));   // composition_object_horizontal_position
                        imgObj.setyOfs(buffer.getWord(index+17));   // composition_object_vertical_position
                        // if (object_cropped_flag==1)
                        // 		16bit object_cropping_horizontal_position
                        //		16bit object_cropping_vertical_position
                        //		16bit object_cropping_width
                        //		object_cropping_height
                    }
                }
            }

        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        }
    }

    /**
     * parse an PCS packet which contains window info
     * @param segment object containing info about the current segment
     * @param pic SubPicture object containing info about the current caption
     * @throws CoreException
     */
    private void parseWDS(SupSegment segment, SubPictureBD pic) throws CoreException {
        int index = segment.offset;
        try {
            if (segment.segmentSize >= 10) {
                // skipped:
                // 8bit: number of windows (currently assumed 1, 0..2 is legal)
                // 8bit: window id (0..1)
                pic.xWinOfs   = buffer.getWord(index+2);	// window_horizontal_position
                pic.yWinOfs   = buffer.getWord(index+4);	// window_vertical_position
                pic.winWidth  = buffer.getWord(index+6);	// window_width
                pic.winHeight = buffer.getWord(index+8);	// window_height
            }
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
    }

    /**
     * decode caption from the input stream
     * @param pic SubPicture object containing info about the caption
     * @param transIdx index of the transparent color
     * @return bitmap of the decoded caption
     * @throws CoreException
     */
    private Bitmap decodeImage(SubPictureBD pic, int transIdx) throws CoreException {
        int w = pic.getImageWidth();
        int h = pic.getImageHeight();
        // always decode image obj 0, start with first entry in fragment list
        ImageObjectFragment info = pic.getImgObj().getFragmentList().get(0);
        long startOfs = info.getImageBufferOfs();

        if (w > pic.width || h > pic.height) {
            throw new CoreException("Subpicture too large: " + w + "x" + h + " at offset " + ToolBox.toHexLeftZeroPadded(startOfs, 8));
        }

        Bitmap bm = new Bitmap(w, h, (byte)transIdx);

        int b;
        int index = 0;
        int ofs = 0;
        int size;
        int xpos = 0;

        try {
            // just for multi-packet support, copy all of the image data in one common buffer
            byte buf[] = new byte[pic.getImgObj().getBufferSize()];
            index = 0;
            for (int p = 0; p < pic.getImgObj().getFragmentList().size(); p++) {
                // copy data of all packet to one common buffer
                info = pic.getImgObj().getFragmentList().get(p);
                for (int i=0; i < info.getImagePacketSize(); i++) {
                    buf[index+i] = (byte)buffer.getByte(info.getImageBufferOfs()+i);
                }
                index += info.getImagePacketSize();
            }

            index = 0;

            do {
                b = buf[index++]&0xff;
                if (b == 0) {
                    b = buf[index++]&0xff;
                    if (b == 0) {
                        // next line
                        ofs = (ofs/w)*w;
                        if (xpos < w) {
                            ofs+=w;
                        }
                        xpos = 0;
                    } else {
                        if ( (b & 0xC0) == 0x40) {
                            // 00 4x xx -> xxx zeroes
                            size = ((b-0x40)<<8)+(buf[index++]&0xff);
                            for (int i=0; i < size; i++) {
                                bm.getInternalBuffer()[ofs++] = 0; /*(byte)b;*/
                            }
                            xpos += size;
                        } else if ((b & 0xC0) == 0x80) {
                            // 00 8x yy -> x times value y
                            size = (b-0x80);
                            b = buf[index++]&0xff;
                            for (int i=0; i<size; i++) {
                                bm.getInternalBuffer()[ofs++] = (byte)b;
                            }
                            xpos += size;
                        } else if  ((b & 0xC0) != 0) {
                            // 00 cx yy zz -> xyy times value z
                            size = ((b - 0xC0) << 8) + (buf[index++] & 0xff);
                            b = buf[index++] & 0xff;
                            for (int i=0; i < size; i++) {
                                bm.getInternalBuffer()[ofs++] = (byte)b;
                            }
                            xpos += size;
                        }  else {
                            // 00 xx -> xx times 0
                            for (int i=0; i < b; i++) {
                                bm.getInternalBuffer()[ofs++] = 0;
                            }
                            xpos += b;
                        }
                    }
                } else {
                    bm.getInternalBuffer()[ofs++] = (byte)b;
                    xpos++;
                }
            } while (index < buf.length);

            return bm;
        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            Core.printWarn("problems during RLE decoding of picture OBJ at offset " + ToolBox.toHexLeftZeroPadded(startOfs+index, 8)+"\n");
            return bm;
        }
    }

    /**
     * parse an ODS packet which contain the image data
     * @param segment object containing info about the current segment
     * @param pic SubPicture object containing info about the current caption
     * @param msg reference to message string
     * @return true if this is a valid new object (neither invalid nor a fragment)
     * @throws CoreException
     */
    private boolean parseODS(SupSegment segment, SubPictureBD pic, String msg[]) throws CoreException {
        int index = segment.offset;
        ImageObjectFragment info;
        try {
            int objID = buffer.getWord(index);		// 16bit object_id
            int objVer = buffer.getByte(index+1);		// 16bit object_id
            int objSeq = buffer.getByte(index+3);		// 8bit  first_in_sequence (0x80),
                                                            // last_in_sequence (0x40), 6bits reserved
            boolean first = (objSeq & 0x80) == 0x80;
            boolean last  = (objSeq & 0x40) == 0x40;

            if (pic.imageObjectList == null) {
                pic.imageObjectList = new ArrayList<ImageObject>();
            }
            ImageObject imgObj;
            if (objID >= pic.imageObjectList.size()) {
                imgObj = new ImageObject();
                pic.imageObjectList.add(imgObj);
            } else {
                imgObj = pic.getImgObj(objID);
            }

            if (imgObj.getFragmentList() == null || first) {			// 8bit  object_version_number
                // skipped:
                //  24bit object_data_length - full RLE buffer length (including 4 bytes size info)
                int width  = buffer.getWord(index+7);		// object_width
                int height = buffer.getWord(index+9);		// object_height

                if (width <= pic.width && height <= pic.height) {
                    imgObj.setFragmentList(new ArrayList<ImageObjectFragment>());
                    info = new ImageObjectFragment();
                    info.setImageBufferOfs(index+11);
                    info.setImagePacketSize(segment.segmentSize - (index+11-segment.offset));
                    imgObj.getFragmentList().add(info);
                    imgObj.setBufferSize(info.getImagePacketSize());
                    imgObj.setHeight(height);
                    imgObj.setWidth(width);
                    msg[0] = "ID: " + objID + ", update: " + objVer + ", seq: " + (first ? "first" : "")
                        + ((first && last) ? "/" : "") + (last ? "" + "last" : "");
                    return true;
                } else {
                    Core.printWarn("Invalid image size - ignored\n");
                    return false;
                }
            } else {
                // object_data_fragment
                // skipped:
                //  16bit object_id
                //  8bit  object_version_number
                //  8bit  first_in_sequence (0x80), last_in_sequence (0x40), 6bits reserved
                info = new ImageObjectFragment();
                info.setImageBufferOfs(index+4);
                info.setImagePacketSize(segment.segmentSize - (index+4-segment.offset));
                imgObj.getFragmentList().add(info);
                imgObj.setBufferSize(imgObj.getBufferSize() + info.getImagePacketSize());
                msg[0] = "ID: " + objID + ", update: " + objVer + ", seq: " + (first ? "first" : "")
                    + ((first && last) ? "/" : "") + (last ? "" + "last" : "");
                return false;
            }
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
    }

    /**
     * decode palette from the input stream
     * @param pic SubPicture object containing info about the current caption
     * @return
     * @throws CoreException
     */
    private Palette decodePalette(SubPictureBD pic) throws CoreException {
        boolean fadeOut = false;
        int palIndex;
        ArrayList<PaletteInfo> pl = pic.palettes.get(pic.getImgObj().getPaletteID());
        if (pl == null) {
            throw new CoreException("Palette ID out of bounds.");
        }

        Palette palette = new Palette(256, Core.usesBT601());
        // by definition, index 0xff is always completely transparent
        // also all entries must be fully transparent after initialization

        try {
            for (PaletteInfo p : pl) {
                int index = p.getPaletteOfs();
                for (int i = 0; i < p.getPaletteSize(); i++) {
                    // each palette entry consists of 5 bytes
                    palIndex = buffer.getByte(index);
                    int y = buffer.getByte(++index);
                    int cr, cb;
                    if (configuration.isSwapCrCb()) {
                        cb = buffer.getByte(++index);
                        cr = buffer.getByte(++index);
                    } else {
                        cr = buffer.getByte(++index);
                        cb = buffer.getByte(++index);
                    }
                    int alpha = buffer.getByte(++index);

                    int alphaOld = palette.getAlpha(palIndex);
                    // avoid fading out
                    if (alpha >= alphaOld) {
                        if (alpha < configuration.getAlphaCrop()) {// to not mess with scaling algorithms, make transparent color black
                            y = 16;
                            cr = 128;
                            cb = 128;
                        }
                        palette.setAlpha(palIndex, alpha);
                    } else {
                        fadeOut = true;
                    }

                    palette.setYCbCr(palIndex, y, cb, cr);
                    index++;
                }
            }
            if (fadeOut) {
                Core.printWarn("fade out detected -> patched palette\n");
            }
            return palette;
        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        }
    }

    /**
     * parse an PDS packet which contain palette info
     * @param segment object containing info about the current segment
     * @param pic SubPicture object containing info about the current caption
     * @param msg reference to message string
     * @throws CoreException
     * @throws FileBufferException
     * @throws CoreException
     * @returns number of valid palette entries (-1 for fault)
     */
    private int parsePDS(SupSegment segment, SubPictureBD pic, String msg[]) throws CoreException {
        int index = segment.offset;
        try {
            int paletteID = buffer.getByte(index);	// 8bit palette ID (0..7)
            // 8bit palette version number (incremented for each palette change)
            int paletteUpdate = buffer.getByte(index+1);
            if (pic.palettes == null) {
                pic.palettes = new ArrayList<ArrayList <PaletteInfo>>();
                for (int i=0; i<8; i++) {
                    pic.palettes.add(new ArrayList<PaletteInfo>());
                }
            }
            if (paletteID > 7) {
                msg[0] = "Illegal palette id at offset " + ToolBox.toHexLeftZeroPadded(index, 8);
                return -1;
            }

            ArrayList<PaletteInfo> al = pic.palettes.get(paletteID);
            if (al == null) {
                al = new ArrayList<PaletteInfo>();
            }
            PaletteInfo p = new PaletteInfo();
            p.setPaletteSize((segment.segmentSize-2)/5);
            p.setPaletteOfs(index+2);
            al.add(p);
            msg[0] = "ID: " + paletteID + ", update: " + paletteUpdate + ", " + p.getPaletteSize() + " entries";
            return p.getPaletteSize();
        } catch (FileBufferException ex) {
            throw new CoreException (ex.getMessage());
        }
    }

    /**
     * decode given picture
     * @param pic SubPicture object containing info about caption
     * @throws CoreException
     */
    private void decode(SubPictureBD pic)  throws CoreException {
        palette = decodePalette(pic);
        bitmap  = decodeImage(pic, palette.getIndexOfMostTransparentPaletteEntry());
        primaryColorIndex = bitmap.getPrimaryColorIndex(palette.getAlpha(), configuration.getAlphaThreshold(), palette.getY());
    }

    /* (non-Javadoc)
     * @see SubtitleStream#decode(int)
     */
    public void decode(int index) throws CoreException {
        if (index < subPictures.size()) {
            decode(subPictures.get(index));
        } else {
            throw new CoreException("Index "+index+" out of bounds\n");
        }
    }

    /* setters / getters */

    /* (non-Javadoc)
     * @see SubtitleStream#getPalette()
     */
    public Palette getPalette() {
        return palette;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getBitmap()
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getImage()
     */
    public BufferedImage getImage() {
        return bitmap.getImage(palette.getColorModel());
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getImage(Bitmap)
     */
    public BufferedImage getImage(Bitmap bm) {
        return bm.getImage(palette.getColorModel());
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getPrimaryColorIndex()
     */
    public int getPrimaryColorIndex() {
        return primaryColorIndex;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getSubPicture(int)
     */
    public SubPicture getSubPicture(int index) {
        return subPictures.get(index);
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getNumFrames()
     */
    public int getFrameCount() {
        return subPictures.size();
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getNumForcedFrames()
     */
    public int getForcedFrameCount() {
        return numForcedFrames;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#close()
     */
    public void close() {
        if (buffer != null) {
            buffer.close();
        }
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getEndTime(int)
     */
    public long getEndTime(int index) {
        return subPictures.get(index).endTime;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getStartTime(int)
     */
    public long getStartTime(int index) {
        return subPictures.get(index).startTime;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#isForced(int)
     */
    public boolean isForced(int index) {
        return subPictures.get(index).isforced;
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getStartOffset(int)
     */
    public long getStartOffset(int index) {
        SubPictureBD pic = subPictures.get(index);
        return pic.getImgObj().getFragmentList().get(0).getImageBufferOfs();
    }

    /**
     * Get frame rate for given caption
     * @param index index of caption
     * @return frame rate
     */
    public double getFps(int index) {
        return getFpsFromID(subPictures.get(index).type);
    }
}

class SupSegment {
    int segmentType;
    int segmentSize;
    long segmentPTSTimestamp;
    /** file offset of segment */
    int  offset;
}
