package bdsup2sub.supstream.bd;

import bdsup2sub.core.Configuration;
import bdsup2sub.core.Core;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.Logger;
import bdsup2sub.supstream.ImageObject;
import bdsup2sub.supstream.ImageObjectFragment;
import bdsup2sub.supstream.PaletteInfo;
import bdsup2sub.tools.FileBuffer;
import bdsup2sub.tools.FileBufferException;
import bdsup2sub.utils.ToolBox;

import java.util.ArrayList;
import java.util.List;

import static bdsup2sub.utils.TimeUtils.ptsToTimeStr;

public class SupBDParser {

    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = Logger.getInstance();

    private static final int PGSSUP_FILE_MAGIC = 0x5047;
    private static final int PGSSUP_PALETTE_SEGMENT = 0x14;
    private static final int PGSSUP_PICTURE_SEGMENT = 0x15;
    private static final int PGSSUP_PRESENTATION_SEGMENT = 0x16;
    private static final int PGSSUP_WINDOW_SEGMENT = 0x17;
    private static final int PGSSUP_DISPLAY_SEGMENT = 0x80;

    private static class PCSSegment {
        int type;
        int size;
        long pts;
        int offset; // file offset of segment
    }

    private FileBuffer buffer;
    private List<SubPictureBD> subPictures = new ArrayList<SubPictureBD>();
    private int forcedFrameCount;

    public SupBDParser(String filename) throws CoreException {
        try {
            buffer = new FileBuffer(filename);
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
        parse();
    }

    private void parse() throws CoreException {
        int index = 0;
        long bufferSize = buffer.getSize();
        PCSSegment segment;
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
            while (index < bufferSize) {
                // for threaded version
                if (Core.isCanceled()) {
                    throw new CoreException("Canceled by user!");
                }
                Core.setProgress(index);
                segment = readPCSSegment(index);
                String msg;
                String so[] = new String[1]; // hack to return string
                switch (segment.type) {
                    case PGSSUP_PALETTE_SEGMENT:
                        msg = "PDS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", size: " + ToolBox.toHexLeftZeroPadded(segment.size, 4);
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
                                logger.warn("Missing PTS start -> ignored\n");
                            }
                        } else {
                            logger.trace(msg + ", composition number unchanged -> ignored\n");
                        }
                        break;
                    case PGSSUP_PICTURE_SEGMENT:
                        msg = "ODS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", size: " + ToolBox.toHexLeftZeroPadded(segment.size, 4);
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
                    case PGSSUP_PRESENTATION_SEGMENT:
                        compositionNumber = getCompositionNumber(segment);
                        compositionState = getCompositionState(segment);
                        paletteUpdate = getPaletteUpdateFlag(segment);
                        ptsPCS = segment.pts;
                        if (segment.size >= 0x13) {
                            compositionCount = 1; // could be also 2, but we'll ignore this for the moment
                        } else {
                            compositionCount = 0;
                        }
                        if (compositionState == PGSCompositionState.INVALID) {
                            logger.warn("Illegal composition state at offset " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                        } else if (compositionState == PGSCompositionState.EPOCH_START) {
                            // new frame
                            if (subPictures.size() > 0 && (odsCounter == 0 || pdsCounter == 0)) {
                                logger.warn("Missing PDS/ODS: last epoch is discarded\n");
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
                            subPictureBD.setStartTime(segment.pts);
                            logger.info("#> " + (subPictures.size()) + " (" + ptsToTimeStr(subPictureBD.getStartTime()) + ")\n");

                            so[0] = null;
                            parsePCS(segment, subPictureBD, so);
                            // fix end time stamp of previous subPictureBD if still missing
                            if (lastSubPicture != null && lastSubPicture.getEndTime() == 0) {
                                lastSubPicture.setEndTime(subPictureBD.getStartTime());
                            }

                            msg = "PCS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", START, size: " + ToolBox.toHexLeftZeroPadded(segment.size, 4) + ", composition number: " + compositionNumber + ", forced: " + subPictureBD.isForced() + (so[0] == null ? "\n" : ", " + so[0] + "\n");
                            msg += "PTS start: " + ptsToTimeStr(subPictureBD.getStartTime()) + ", screen size: " + subPictureBD.getWidth() + "*" + subPictureBD.getHeight() + "\n";
                            logger.trace(msg);

                            odsCounter = 0;
                            pdsCounter = 0;
                            odsCounterOld = 0;
                            pdsCounterOld = 0;
                            picTmp = null;
                        } else {
                            if (subPictureBD == null) {
                                logger.warn("Missing start of epoch at offset " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
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
                            msg += " size: " + ToolBox.toHexLeftZeroPadded(segment.size, 4) + ", composition number: " + compositionNumber + ", forced: " + subPictureBD.isForced();
                            if (compositionNumber != compositionNumberOld) {
                                so[0] = null;
                                // store the state so that we can revert to it
                                picTmp = new SubPictureBD(subPictureBD);
                                // create new subPictureBD
                                parsePCS(segment, subPictureBD, so);
                            }
                            if (so[0] != null) {
                                msg += ", " + so[0];
                            }
                            msg += ", pal update: " + paletteUpdate + "\n";
                            msg += "PTS: " + ptsToTimeStr(segment.pts) + "\n";
                            logger.trace(msg);
                        }
                        break;
                    case PGSSUP_WINDOW_SEGMENT:
                        msg = "WDS offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + ", size: " + ToolBox.toHexLeftZeroPadded(segment.size, 4);
                        if (subPictureBD != null) {
                            parseWDS(segment, subPictureBD);
                            logger.trace(msg + ", dim: " + subPictureBD.getWindowWidth() + "*" + subPictureBD.getWindowHeight() + "\n");
                        } else {
                            logger.trace(msg + "\n");
                            logger.warn("Missing PTS start -> ignored\n");
                        }
                        break;
                    case PGSSUP_DISPLAY_SEGMENT:
                        logger.trace("END offset: " + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                        // decide whether to store this last composition section as caption or merge it
                        if (compositionState == PGSCompositionState.EPOCH_START) {
                            if (compositionCount > 0 && odsCounter > odsCounterOld && compositionNumber != compositionNumberOld
                                    && subPictureBD != null && subPictureBD.isMergableWith(lastSubPicture)) {
                                // the last start epoch did not contain any (new) content
                                // and should be merged to the previous frame
                                subPictures.remove(subPictures.size()-1);
                                subPictureBD = lastSubPicture;
                                if (subPictures.size() > 0) {
                                    lastSubPicture = subPictures.get(subPictures.size() - 1);
                                } else {
                                    lastSubPicture = null;
                                }
                                logger.info("#< caption merged\n");
                            }
                        } else {
                            long startTime = 0;
                            if (subPictureBD != null) {
                                startTime = subPictureBD.getStartTime();  // store
                                subPictureBD.setStartTime(ptsPCS);        // set for testing merge
                            }

                            if (compositionCount > 0 && odsCounter > odsCounterOld && compositionNumber != compositionNumberOld
                                    && (subPictureBD == null || !subPictureBD.isMergableWith(picTmp))) {
                                // last PCS should be stored as separate caption
                                if (odsCounter-odsCounterOld>1 || pdsCounter-pdsCounterOld>1) {
                                    logger.warn("Multiple PDS/ODS definitions: result may be erratic\n");
                                }
                                // replace subPictureBD with picTmp (deepCopy created before new PCS)
                                subPictures.set(subPictures.size() - 1, picTmp); // replace in list
                                lastSubPicture = picTmp;
                                subPictures.add(subPictureBD);
                                logger.info("#< " + (subPictures.size()) + " (" + ptsToTimeStr(subPictureBD.getStartTime()) + ")\n");
                                odsCounterOld = odsCounter;

                            } else {
                                if (subPictureBD != null) {
                                    // merge with previous subPictureBD
                                    subPictureBD.setStartTime(startTime); // restore
                                    subPictureBD.setEndTime(ptsPCS);
                                    // for the unlikely case that forced flag changed during one caption
                                    if (picTmp != null && picTmp.isForced()) {
                                        subPictureBD.setForced(true);
                                    }
                                    if (pdsCounter > pdsCounterOld || paletteUpdate) {
                                        logger.warn("Palette animation: result may be erratic\n");
                                    }
                                } else {
                                    logger.warn("End without at least one epoch start\n");
                                }
                            }
                        }
                        pdsCounterOld = pdsCounter;
                        compositionNumberOld = compositionNumber;
                        break;
                    default:
                        logger.warn("<unknown> " + ToolBox.toHexLeftZeroPadded(segment.type, 2) + " ofs:" + ToolBox.toHexLeftZeroPadded(index, 8) + "\n");
                        break;
                }
                index += 13; // header size
                index += segment.size;
            }
        } catch (CoreException ex) {
            if (subPictures.size() == 0) {
                throw ex;
            }
            logger.error(ex.getMessage() + "\n");
            logger.trace("Probably not all caption imported due to error.\n");
        } catch (FileBufferException ex) {
            if (subPictures.size() == 0) {
                throw new CoreException(ex.getMessage());
            }
            logger.error(ex.getMessage() + "\n");
            logger.trace("Probably not all caption imported due to error.\n");
        }

        removeLastFrameIfInvalid(odsCounter, pdsCounter);
        Core.setProgress(bufferSize);
        countForcedFrames();
    }

    private void removeLastFrameIfInvalid(int odsCounter, int pdsCounter) {
        if (subPictures.size() > 0 && (odsCounter == 0 || pdsCounter == 0)) {
            logger.warn("Missing PDS/ODS: last epoch is discarded\n");
            subPictures.remove(subPictures.size() - 1);
        }
    }

    private void countForcedFrames() {
        forcedFrameCount = 0;
        for (SubPictureBD p : subPictures) {
            if (p.isForced()) {
                forcedFrameCount++;
            }
        }
        logger.info("\nDetected " + forcedFrameCount + " forced captions.\n");
    }

    private PCSSegment readPCSSegment(int offset) throws FileBufferException, CoreException {
        PCSSegment pcsSegment = new PCSSegment();
        if (buffer.getWord(offset) != PGSSUP_FILE_MAGIC) {
            throw new CoreException("PG missing at index " + ToolBox.toHexLeftZeroPadded(offset, 8) + "\n");
        }
        pcsSegment.pts = buffer.getDWord(offset += 2);
        offset += 4; /* ignore DTS */
        pcsSegment.type = buffer.getByte(offset += 4);
        pcsSegment.size = buffer.getWord(offset += 1);
        pcsSegment.offset = offset + 2;
        return pcsSegment;
    }

    private int getCompositionNumber(PCSSegment segment) throws FileBufferException {
        return buffer.getWord(segment.offset + 5);
    }

    private PGSCompositionState getCompositionState(PCSSegment segment) throws FileBufferException {
        int type = buffer.getByte(segment.offset + 7);
        for (PGSCompositionState state : PGSCompositionState.values()) {
            if (type == state.getType()) {
                return state;
            }
        }
        return PGSCompositionState.INVALID;
    }

    /**
     * Retrieve palette (only) update flag from PCS segment
     * @return true: this is only a palette update - ignore ODS
     */
    private boolean getPaletteUpdateFlag(PCSSegment segment) throws FileBufferException {
        return buffer.getByte(segment.offset + 8) == 0x80;
    }

    /**
     * parse an PCS packet which contains width/height info
     * @param segment object containing info about the current segment
     * @param subPictureBD SubPicture object containing info about the current caption
     * @param msg reference to message string
     * @throws FileBufferException
     */
    private void parsePCS(PCSSegment segment, SubPictureBD subPictureBD, String msg[]) throws FileBufferException {
        int index = segment.offset;
        if (segment.size >= 4) {
            subPictureBD.setWidth(buffer.getWord(index));               // video_width
            subPictureBD.setHeight(buffer.getWord(index + 2));          // video_height
            int type = buffer.getByte(index + 4);                       // hi nibble: frame_rate, lo nibble: reserved
            int compositionNumber = buffer.getWord(index + 5);         // composition_number
            // skipped:
            // 8bit  composition_state: 0x00: normal, 0x40: acquisition point, 0x80: epoch start,  0xC0: epoch continue, 6bit reserved
            // 8bit  palette_update_flag (0x80), 7bit reserved
            int paletteId = buffer.getByte(index + 9);                  // 8bit  palette_id_ref
            int compositionObjectCount = buffer.getByte(index + 10);    // 8bit  number_of_composition_objects (0..2)
            if (compositionObjectCount > 0) {
                // composition_object:
                int objectId = buffer.getWord(index + 11); // 16bit object_id_ref
                msg[0] = "paletteId: " + paletteId + ", objectId: " + objectId;
                ImageObject imageObject;
                if (objectId >= subPictureBD.getImageObjectList().size()) {
                    imageObject = new ImageObject();
                    subPictureBD.getImageObjectList().add(imageObject);
                } else {
                    imageObject = subPictureBD.getImageObject(objectId);
                }
                imageObject.setPaletteID(paletteId);
                subPictureBD.setObjectID(objectId);

                // skipped:  8bit  window_id_ref
                if (segment.size >= 0x13) {
                    subPictureBD.setType(type);
                    // object_cropped_flag: 0x80, forced_on_flag = 0x040, 6bit reserved
                    int forcedCropped = buffer.getByte(index + 14);
                    subPictureBD.setCompositionNumber(compositionNumber);
                    subPictureBD.setForced(((forcedCropped & 0x40) == 0x40));
                    imageObject.setXOffset(buffer.getWord(index + 15));   // composition_object_horizontal_position
                    imageObject.setYOffset(buffer.getWord(index + 17));   // composition_object_vertical_position
                    // if (object_cropped_flag==1)
                    //      16bit object_cropping_horizontal_position
                    //      16bit object_cropping_vertical_position
                    //      16bit object_cropping_width
                    //      object_cropping_height
                }
            }
        }
    }

    private void parseWDS(PCSSegment pcsSegment, SubPictureBD subPictureBD) throws FileBufferException {
        int index = pcsSegment.offset;
        if (pcsSegment.size >= 10) {
            // skipped:
            // 8bit: number of windows (currently assumed 1, 0..2 is legal)
            // 8bit: window id (0..1)
            subPictureBD.setXWindowOffset(buffer.getWord(index + 2));    // window_horizontal_position
            subPictureBD.setYWindowOffset(buffer.getWord(index + 4));    // window_vertical_position
            subPictureBD.setWindowWidth(buffer.getWord(index + 6));      // window_width
            subPictureBD.setWindowHeight(buffer.getWord(index + 8));     // window_height
        }
    }

    private boolean parseODS(PCSSegment pcsSegment, SubPictureBD subPictureBD, String msg[]) throws FileBufferException {
        int index = pcsSegment.offset;
        int objectID = buffer.getWord(index);                 // 16bit object_id
        int objectVersion = buffer.getByte(index+1);          // object_version_number
        int objectSequenceOrder = buffer.getByte(index+3);    // 8bit  first_in_sequence (0x80), last_in_sequence (0x40), 6bits reserved
        boolean first = (objectSequenceOrder & 0x80) == 0x80;
        boolean last = (objectSequenceOrder & 0x40) == 0x40;

        ImageObject imageObject;
        if (objectID >= subPictureBD.getImageObjectList().size()) {
            imageObject = new ImageObject();
            subPictureBD.getImageObjectList().add(imageObject);
        } else {
            imageObject = subPictureBD.getImageObject(objectID);
        }

        ImageObjectFragment imageObjectFragment;
        if (imageObject.getFragmentList().isEmpty() || first) {  // 8bit  object_version_number
            // skipped:
            // 24bit object_data_length - full RLE buffer length (including 4 bytes size info)
            int width  = buffer.getWord(index + 7);       // object_width
            int height = buffer.getWord(index + 9);       // object_height

            if (width <= subPictureBD.getWidth() && height <= subPictureBD.getHeight()) {
                imageObjectFragment = new ImageObjectFragment(index + 11, pcsSegment.size - (index + 11 - pcsSegment.offset));
                imageObject.getFragmentList().add(imageObjectFragment);
                imageObject.setBufferSize(imageObjectFragment.getImagePacketSize());
                imageObject.setHeight(height);
                imageObject.setWidth(width);
                msg[0] = "ID: " + objectID + ", update: " + objectVersion + ", seq: " + (first ? "first" : "")
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
            imageObjectFragment = new ImageObjectFragment(index + 4, pcsSegment.size - (index + 4 - pcsSegment.offset));
            imageObject.getFragmentList().add(imageObjectFragment);
            imageObject.setBufferSize(imageObject.getBufferSize() + imageObjectFragment.getImagePacketSize());
            msg[0] = "ID: " + objectID + ", update: " + objectVersion + ", seq: " + (first ? "first" : "")
                    + ((first && last) ? "/" : "") + (last ? "" + "last" : "");
            return false;
        }
    }

    private int parsePDS(PCSSegment pcsSegment, SubPictureBD subPictureBD, String msg[]) throws FileBufferException {
        int index = pcsSegment.offset;
        int paletteID = buffer.getByte(index);  // 8bit palette ID (0..7)
        // 8bit palette version number (incremented for each palette change)
        int paletteUpdate = buffer.getByte(index + 1);
        if (paletteID > 7) {
            msg[0] = "Illegal palette id at offset " + ToolBox.toHexLeftZeroPadded(index, 8);
            return -1;
        }

        PaletteInfo paletteInfo = new PaletteInfo(index + 2, (pcsSegment.size - 2) / 5);
        subPictureBD.getPalettes().get(paletteID).add(paletteInfo);
        msg[0] = "ID: " + paletteID + ", update: " + paletteUpdate + ", " + paletteInfo.getPaletteSize() + " entries";
        return paletteInfo.getPaletteSize();
    }

    public FileBuffer getBuffer() {
        return buffer;
    }

    public List<SubPictureBD> getSubPictures() {
        return subPictures;
    }

    public int getForcedFrameCount() {
        return forcedFrameCount;
    }
}
