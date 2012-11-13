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
import bdsup2sub.core.*;
import bdsup2sub.supstream.*;
import bdsup2sub.tools.FileBuffer;
import bdsup2sub.tools.FileBufferException;
import bdsup2sub.tools.QuantizeFilter;
import bdsup2sub.utils.ToolBox;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static bdsup2sub.utils.ByteUtils.*;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStr;

/**
 * Reading and writing of Blu-Ray captions demuxed from M2TS transport streams (BD-SUP).
 */
public class SupBD implements SubtitleStream {

    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = Logger.getInstance();

    /** ArrayList of captions contained in the current file  */
    private List<SubPictureBD> subPictures = new ArrayList<SubPictureBD>();
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
                                    logger.trace(msg + ", " + so[0] + "\n");
                                    if (paletteSize > 0) {
                                        pdsCounter++;
                                    }
                                } else {
                                    logger.trace(msg + "\n");
                                    logger.warn(so[0] + "\n");
                                }
                            } else {
                                logger.trace(msg + "\n");
                                logger.warn("missing PTS start -> ignored\n");
                            }
                        } else {
                            logger.trace(msg + ", composition number unchanged -> ignored\n");
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
                                    logger.trace(msg + ", img size: " + subPictureBD.getImageWidth() + "*" + subPictureBD.getImageHeight() + (so[0] == null ? "\n" : ", " + so[0]) + "\n");
                                } else {
                                    logger.trace(msg + "\n");
                                    logger.warn("missing PTS start -> ignored\n");
                                }
                            } else {
                                logger.trace(msg + "\n");
                                logger.warn("palette update only -> ignored\n");
                            }
                        } else {
                            logger.trace(msg + ", composition number unchanged -> ignored\n");
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
                            logger.warn("Illegal composition state at offset " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                        } else if (compositionState == PGSCompositionState.EPOCH_START) {
                            // new frame
                            if (subPictures.size() > 0 && (odsCounter == 0 || pdsCounter == 0)) {
                                logger.warn("missing PDS/ODS: last epoch is discarded\n");
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
                            subPictureBD.setStartTime(segment.segmentPTSTimestamp);
                            logger.info("#> " + (subPictures.size()) + " (" + ptsToTimeStr(subPictureBD.getStartTime()) + ")\n");

                            so[0] = null;
                            parsePCS(segment, subPictureBD, so);
                            // fix end time stamp of previous subPictureBD if still missing
                            if (lastSubPicture != null && lastSubPicture.getEndTime() == 0) {
                                lastSubPicture.setEndTime(subPictureBD.getStartTime());
                            }

                            msg = "PCS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", START, size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4) + ", composition number: " + compositionNumber + ", forced: " + subPictureBD.isForced() + (so[0] == null ? "\n" : ", " + so[0] + "\n");
                            msg += "PTS start: " + ptsToTimeStr(subPictureBD.getStartTime()) + ", screen size: " + subPictureBD.getWidth() + "*" + subPictureBD.getHeight() + "\n";
                            logger.trace(msg);

                            odsCounter = 0;
                            pdsCounter = 0;
                            odsCounterOld = 0;
                            pdsCounterOld = 0;
                            picTmp = null;
                        } else {
                            if (subPictureBD == null) {
                                logger.warn("missing start of epoch at offset " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
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
                            msg += " size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4) + ", composition number: " + compositionNumber + ", forced: " + subPictureBD.isForced();
                            if (compositionNumber != compositionNumberOld) {
                                so[0] = null;
                                // store state to be able to revert to it
                                picTmp = new SubPictureBD(subPictureBD);
                                // create new subPictureBD
                                parsePCS(segment, subPictureBD, so);
                            }
                            if (so[0] != null) {
                                msg += ", " + so[0];
                            }
                            msg += ", pal update: " + paletteUpdate + "\n";
                            msg += "PTS: " + ptsToTimeStr(segment.segmentPTSTimestamp) + "\n";
                            logger.trace(msg);
                        }
                        break;
                    case 0x17: // window info
                        msg = "WDS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", size: " + ToolBox.toHexLeftZeroPadded(segment.segmentSize, 4);
                        if (subPictureBD != null) {
                            parseWDS(segment, subPictureBD);
                            logger.trace(msg + ", dim: " + subPictureBD.getWindowWidth() + "*" + subPictureBD.getWindowHeight() + "\n");
                        } else {
                            logger.trace(msg + "\n");
                            logger.warn("Missing PTS start -> ignored\n");
                        }
                        break;
                    case 0x80: // END
                        logger.trace("END offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
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
                                logger.info("#< caption merged\n");
                            }
                        } else {
                            long startTime = 0;
                            if (subPictureBD != null) {
                                startTime = subPictureBD.getStartTime();  // store
                                subPictureBD.setStartTime(ptsPCS);    // set for testing merge
                            }

                            if (compositionCount>0 && odsCounter>odsCounterOld && compositionNumber!=compositionNumberOld && !picMergable(picTmp, subPictureBD)) {
                                // last PCS should be stored as separate caption
                                if (odsCounter-odsCounterOld>1 || pdsCounter-pdsCounterOld>1) {
                                    logger.warn("multiple PDS/ODS definitions: result may be erratic\n");
                                }
                                // replace subPictureBD with picTmp (deepCopy created before new PCS)
                                subPictures.set(subPictures.size()-1, picTmp); // replace in list
                                lastSubPicture = picTmp;
                                subPictures.add(subPictureBD); // add to list
                                logger.info("#< " + (subPictures.size()) + " (" + ptsToTimeStr(subPictureBD.getStartTime()) + ")\n");
                                odsCounterOld = odsCounter;

                            } else {
                                if (subPictureBD != null) {
                                    // merge with previous subPictureBD
                                    subPictureBD.setStartTime(startTime); // restore
                                    subPictureBD.setEndTime(ptsPCS);
                                    // for the unlikely case that forced flag changed during one captions
                                    if (picTmp != null && picTmp.isForced()) {
                                        subPictureBD.setForced(true);
                                    }

                                    if (pdsCounter > pdsCounterOld || paletteUpdate) {
                                        logger.warn("palette animation: result may be erratic\n");
                                    }
                                } else {
                                    logger.warn("end without at least one epoch start\n");
                                }
                            }
                        }
                        pdsCounterOld = pdsCounter;
                        compositionNumberOld = compositionNumber;
                        break;
                    default:
                        logger.warn("<unknown> " + ToolBox.toHexLeftZeroPadded(segment.segmentType, 2) + " ofs:" + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                    break;
                }
                index += 13; // header size
                index += segment.segmentSize;
            }
        } catch (CoreException ex) {
            if (subPictures.size() == 0) {
                throw ex;
            }
            logger.error(ex.getMessage() + "\n");
            logger.trace("Probably not all caption imported due to error.\n");
        }

        // check if last frame is valid
        if (subPictures.size() > 0 && (odsCounter==0 || pdsCounter==0)) {
            logger.warn("missing PDS/ODS: last epoch is discarded\n");
            subPictures.remove(subPictures.size()-1);
        }

        Core.setProgress(bufsize);
        // count forced frames
        numForcedFrames = 0;
        for (SubPictureBD p : subPictures) {
            if (p.isForced()) {
                numForcedFrames++;
            }
        }
        logger.info("\nDetected " + numForcedFrames + " forced captions.\n");
    }

    /**
     * Checks if two SubPicture object can be merged because the time gap between them is rather small
     * and the embedded objects seem to be identical
     * @param a first SubPicture object (earlier)
     * @param b 2nd SubPicture object (later)
     * @return true if the SubPictures can be merged
     */
    private boolean picMergable(SubPictureBD a, SubPictureBD b) {
        boolean eq = false;
        if (a != null && b != null) {
            if (a.getEndTime() == 0 || b.getStartTime() - a.getEndTime() < configuration.getMergePTSdiff()) {
            ImageObject ao = a.getImageObject();
            ImageObject bo = b.getImageObject();
            if (ao != null && bo != null)
                if (ao.getBufferSize() == bo.getBufferSize() && ao.getWidth() == bo.getWidth() && ao.getHeight() == bo.getHeight()) {
                    eq = true;
                }
            }
        }
        return eq;
    }

    /**
     * Read segment from the input stream.
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
            throw new CoreException(ex.getMessage());
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
            type = buffer.getByte(segment.offset + 7);
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
        for (PGSCompositionState state : PGSCompositionState.values()) {
            if (type == state.getType()) {
                return state;
            }
        }
        return PGSCompositionState.INVALID;
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
            throw new CoreException(ex.getMessage());
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
            throw new CoreException(ex.getMessage());
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
                pic.setWidth(buffer.getWord(index));    // video_width
                pic.setHeight(buffer.getWord(index+2)); // video_height
                int type = buffer.getByte(index+4);     // hi nibble: frame_rate, lo nibble: reserved
                int num  = buffer.getWord(index+5);     // composition_number
                // skipped:
                // 8bit  composition_state: 0x00: normal,       0x40: acquisition point
                //                          0x80: epoch start,  0xC0: epoch continue, 6bit reserved
                // 8bit  palette_update_flag (0x80), 7bit reserved
                int palID = buffer.getByte(index+9);        // 8bit  palette_id_ref
                int coNum = buffer.getByte(index+10);       // 8bit  number_of_composition_objects (0..2)
                if (coNum > 0) {
                    // composition_object:
                    int objID = buffer.getWord(index+11);   // 16bit object_id_ref
                    msg[0] = "palID: "+palID+", objID: "+objID;
                    if (pic.getImageObjectList() == null) {
                        pic.setImageObjectList(new ArrayList<ImageObject>());
                    }
                    ImageObject imgObj;
                    if (objID >= pic.getImageObjectList().size()) {
                        imgObj = new ImageObject();
                        pic.getImageObjectList().add(imgObj);
                    } else {
                        imgObj = pic.getImageObject(objID);
                    }
                    imgObj.setPaletteID(palID);
                    pic.setObjectID(objID);

                    // skipped:  8bit  window_id_ref
                    if (segment.segmentSize >= 0x13) {
                        pic.setType(type);
                        // object_cropped_flag: 0x80, forced_on_flag = 0x040, 6bit reserved
                        int forcedCropped = buffer.getByte(index+14);
                        pic.setCompNum(num);
                        pic.setForced(( (forcedCropped & 0x40) == 0x40));
                        imgObj.setXOffset(buffer.getWord(index + 15));   // composition_object_horizontal_position
                        imgObj.setYOffset(buffer.getWord(index + 17));   // composition_object_vertical_position
                        // if (object_cropped_flag==1)
                        //      16bit object_cropping_horizontal_position
                        //      16bit object_cropping_vertical_position
                        //      16bit object_cropping_width
                        //      object_cropping_height
                    }
                }
            }

        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
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
                pic.setXWindowOffset(buffer.getWord(index + 2));    // window_horizontal_position
                pic.setYWindowOffset(buffer.getWord(index + 4));    // window_vertical_position
                pic.setWindowWidth(buffer.getWord(index + 6));      // window_width
                pic.setWindowHeight(buffer.getWord(index + 8));     // window_height
            }
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
    }

    /**
     * Decode caption from the input stream.
     * @param pic SubPicture object containing info about the caption
     * @param transIdx index of the transparent color
     * @return bitmap of the decoded caption
     * @throws CoreException
     */
    private Bitmap decodeImage(SubPictureBD pic, int transIdx) throws CoreException {
        int w = pic.getImageWidth();
        int h = pic.getImageHeight();
        // always decode image obj 0, start with first entry in fragment list
        ImageObjectFragment info = pic.getImageObject().getFragmentList().get(0);
        long startOfs = info.getImageBufferOfs();

        if (w > pic.getWidth() || h > pic.getHeight()) {
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
            byte buf[] = new byte[pic.getImageObject().getBufferSize()];
            index = 0;
            for (int p = 0; p < pic.getImageObject().getFragmentList().size(); p++) {
                // copy data of all packet to one common buffer
                info = pic.getImageObject().getFragmentList().get(p);
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
            throw new CoreException(ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            logger.warn("problems during RLE decoding of picture OBJ at offset " + ToolBox.toHexLeftZeroPadded(startOfs + index, 8) + "\n");
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
            int objID = buffer.getWord(index);          // 16bit object_id
            int objVer = buffer.getByte(index+1);       // 16bit object_id
            int objSeq = buffer.getByte(index+3);       // 8bit  first_in_sequence (0x80),
                                                        // last_in_sequence (0x40), 6bits reserved
            boolean first = (objSeq & 0x80) == 0x80;
            boolean last  = (objSeq & 0x40) == 0x40;

            if (pic.getImageObjectList() == null) {
                pic.setImageObjectList(new ArrayList<ImageObject>());
            }
            ImageObject imgObj;
            if (objID >= pic.getImageObjectList().size()) {
                imgObj = new ImageObject();
                pic.getImageObjectList().add(imgObj);
            } else {
                imgObj = pic.getImageObject(objID);
            }

            if (imgObj.getFragmentList().isEmpty() || first) {  // 8bit  object_version_number
                // skipped:
                //  24bit object_data_length - full RLE buffer length (including 4 bytes size info)
                int width  = buffer.getWord(index+7);       // object_width
                int height = buffer.getWord(index+9);       // object_height

                if (width <= pic.getWidth() && height <= pic.getHeight()) {
                    info = new ImageObjectFragment(index+11, segment.segmentSize - (index+11-segment.offset));
                    imgObj.getFragmentList().add(info);
                    imgObj.setBufferSize(info.getImagePacketSize());
                    imgObj.setHeight(height);
                    imgObj.setWidth(width);
                    msg[0] = "ID: " + objID + ", update: " + objVer + ", seq: " + (first ? "first" : "")
                        + ((first && last) ? "/" : "") + (last ? "" + "last" : "");
                    return true;
                } else {
                    logger.warn("Invalid image size - ignored\n");
                    return false;
                }
            } else {
                // object_data_fragment
                // skipped:
                //  16bit object_id
                //  8bit  object_version_number
                //  8bit  first_in_sequence (0x80), last_in_sequence (0x40), 6bits reserved
                info = new ImageObjectFragment(index+4, segment.segmentSize - (index+4-segment.offset));
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
        List<PaletteInfo> pl = pic.getPalettes().get(pic.getImageObject().getPaletteID());
        if (pl == null) {
            throw new CoreException("Palette ID out of bounds.");
        }

        Palette palette = new Palette(256, Core.usesBT601());
        // by definition, index 0xff is always completely transparent
        // also all entries must be fully transparent after initialization

        try {
            for (PaletteInfo p : pl) {
                int index = p.getPaletteOffset();
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
                logger.warn("fade out detected -> patched palette\n");
            }
            return palette;
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
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
            int paletteID = buffer.getByte(index);  // 8bit palette ID (0..7)
            // 8bit palette version number (incremented for each palette change)
            int paletteUpdate = buffer.getByte(index+1);
            if (pic.getPalettes() == null) {
                pic.setPalettes(new ArrayList<List<PaletteInfo>>());
                for (int i=0; i<8; i++) {
                    pic.getPalettes().add(new ArrayList<PaletteInfo>());
                }
            }
            if (paletteID > 7) {
                msg[0] = "Illegal palette id at offset " + ToolBox.toHexLeftZeroPadded(index, 8);
                return -1;
            }

            List<PaletteInfo> al = pic.getPalettes().get(paletteID);
            if (al == null) {
                al = new ArrayList<PaletteInfo>();
            }
            PaletteInfo p = new PaletteInfo(index + 2, (segment.segmentSize-2)/5);
            al.add(p);
            msg[0] = "ID: " + paletteID + ", update: " + paletteUpdate + ", " + p.getPaletteSize() + " entries";
            return p.getPaletteSize();
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
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
        return subPictures.get(index).getEndTime();
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getStartTime(int)
     */
    public long getStartTime(int index) {
        return subPictures.get(index).getStartTime();
    }

    /* (non-Javadoc)
     * @see SubtitleStream#isForced(int)
     */
    public boolean isForced(int index) {
        return subPictures.get(index).isForced();
    }

    /* (non-Javadoc)
     * @see SubtitleStream#getStartOffset(int)
     */
    public long getStartOffset(int index) {
        SubPictureBD pic = subPictures.get(index);
        return pic.getImageObject().getFragmentList().get(0).getImageBufferOfs();
    }

    /**
     * Get frame rate for given caption
     * @param index index of caption
     * @return frame rate
     */
    public double getFps(int index) {
        return Framerate.valueForId(subPictures.get(index).getType());
    }


    private static class SupSegment {
        int segmentType;
        int segmentSize;
        long segmentPTSTimestamp;
        /** file offset of segment */
        int  offset;
    }
}

