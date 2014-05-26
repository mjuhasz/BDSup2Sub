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

import bdsup2sub.BDSup2SubManager;
import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.BitmapBounds;
import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Configuration;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.LibLogger;
import bdsup2sub.supstream.ImageObjectFragment;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.tools.FileBuffer;
import bdsup2sub.tools.FileBufferException;
import bdsup2sub.utils.ToolBox;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static bdsup2sub.utils.ByteUtils.*;
import static bdsup2sub.utils.ToolBox.toHexLeftZeroPadded;

/**
 * Handling of DVD SUP/IFO streams.
 */
public class SupDvd implements DvdSubtitleStream {

    private static final Configuration configuration = Configuration.getInstance();
    private static final LibLogger logger = LibLogger.getInstance();

    private List<SubPictureDVD> subPictures = new ArrayList<SubPictureDVD>();
    private int screenWidth = 720;
    private int screenHeight = 576;
    private int languageIdx;

    private Palette srcPalette;
    /** color palette created for last decoded caption  */
    private Palette palette;
    /** bitmap of the last decoded caption  */
    private Bitmap bitmap;
    /** FileBuffer for reading SUP */
    private FileBuffer fileBuffer;
    /** index of dominant color for the current caption  */
    private int primaryColorIndex;
    /** number of forced captions in the current file  */
    private int numForcedFrames;

    /** store last alpha values for invisible workaround */
    private static int[] lastAlpha = { 0, 0xf, 0xf, 0xf };
    private final BDSup2SubManager manager;


    public SupDvd(String supFile, String ifoFile, BDSup2SubManager manager) throws CoreException {
        this.manager = manager;
        IfoParser ifoParser = new IfoParser(ifoFile);
        this.screenHeight = ifoParser.getScreenHeight();
        this.screenWidth = ifoParser.getScreenWidth();
        this.languageIdx = ifoParser.getLanguageIdx();
        this.srcPalette = ifoParser.getSrcPalette();

        readSupFile(supFile);
    }

    private void readSupFile(String supFile) throws CoreException {
        try {
            long offset = 0;
            fileBuffer = new FileBuffer(supFile);
            long size = fileBuffer.getSize();
            manager.setProgressMax((int) size);
            int i = 0;
            do {
                logger.info("# " + (++i) + "\n");
                manager.setProgress(offset);
                logger.trace("Offset: " + ToolBox.toHexLeftZeroPadded(offset, 8) + "\n");
                offset = readSupFrame(offset, fileBuffer);
            } while (offset < size);
        } catch (FileBufferException e) {
            throw new CoreException(e.getMessage());
        }
        logger.info("\nDetected " + numForcedFrames + " forced captions.\n");
    }

    private long readSupFrame(long offset, FileBuffer buffer) throws CoreException  {
        long ctrlOffset;
        int  ctrlOfsRel;
        int  rleSize;
        int  ctrlSize;
        ImageObjectFragment rleFrag;
        int  length;
        byte[] ctrlHeader;

        try {
            // 2 bytes:  packet identifier 0x5350
            long startOffset = offset;
            if (buffer.getWord(offset) != 0x5350) {
                throw new CoreException("Missing packet identifier at offset " + ToolBox.toHexLeftZeroPadded(offset, 8));
            }
            // 8 bytes PTS:  system clock reference, but use only the first 4
            SubPictureDVD pic = new SubPictureDVD();
            pic.setOffset(offset);
            pic.setWidth(screenWidth);
            pic.setHeight(screenHeight);

            int pts = buffer.getDWordLE(offset += 2);
            pic.setStartTime(pts);
            // 2 bytes:  packet length (number of bytes after this entry)
            length = buffer.getWord(offset += 8);
            // 2 bytes: offset to control buffer
            ctrlOfsRel = buffer.getWord(offset += 2);
            rleSize = ctrlOfsRel - 2;           // calculate size of RLE buffer
            ctrlSize = length - ctrlOfsRel - 2; // calculate size of control header
            if (ctrlSize < 0) {
                throw new CoreException("Invalid control buffer size");
            }
            ctrlOffset = ctrlOfsRel + offset;   // absolute offset of control header
            offset += 2;
            pic.setRleFragments(new ArrayList<ImageObjectFragment>(1));
            rleFrag = new ImageObjectFragment(offset, rleSize);
            pic.getRleFragments().add(rleFrag);
            pic.setRleSize(rleSize);

            pic.setPal(new int[4]);
            pic.setAlpha(new int[4]);
            int alphaSum = 0;
            int[] alphaUpdate = new int[4];
            int alphaUpdateSum;
            int delay = -1;
            boolean colorAlphaUpdate = false;

            logger.trace("SP_DCSQT at ofs: " + ToolBox.toHexLeftZeroPadded(ctrlOffset, 8) + "\n");

            // copy control header in buffer (to be more compatible with VobSub)
            ctrlHeader = new byte[ctrlSize];
            for (int i=0; i < ctrlSize; i++) {
                ctrlHeader[i] = (byte)buffer.getByte(ctrlOffset + i);
            }

            try {
                // parse control header
                int b;
                int index = 0;
                int endSeqOfs = getWord(ctrlHeader, index) - ctrlOfsRel - 2;
                if (endSeqOfs < 0 || endSeqOfs > ctrlSize) {
                    logger.warn("Invalid end sequence offset -> no end time\n");
                    endSeqOfs = ctrlSize;
                }
                index += 2;
                parse_ctrl:
                while (index < endSeqOfs) {
                    int cmd = getByte(ctrlHeader, index++);
                    switch (cmd) {
                        case 0: // forced (?)
                            pic.setForced(true);
                            numForcedFrames++;
                            break;
                        case 1: // start display
                            break;
                        case 3: // palette info
                            b = getByte(ctrlHeader, index++);
                            pic.getPal()[3] = (b >> 4);
                            pic.getPal()[2] = b & 0x0f;
                            b = getByte(ctrlHeader, index++);
                            pic.getPal()[1] = (b >> 4);
                            pic.getPal()[0] = b & 0x0f;
                            logger.trace("Palette:   " + pic.getPal()[0] + ", " + pic.getPal()[1] + ", " + pic.getPal()[2] + ", " + pic.getPal()[3] + "\n");
                            break;
                        case 4: // alpha info
                            b = getByte(ctrlHeader, index++);
                            pic.getAlpha()[3] = (b >> 4);
                            pic.getAlpha()[2] = b & 0x0f;
                            b = getByte(ctrlHeader, index++);
                            pic.getAlpha()[1] = (b >> 4);
                            pic.getAlpha()[0] = b & 0x0f;
                            for (int i = 0; i < 4; i++) {
                                alphaSum += pic.getAlpha()[i] & 0xff;
                            }
                            logger.trace("Alpha:     " + pic.getAlpha()[0] + ", " + pic.getAlpha()[1] + ", " + pic.getAlpha()[2] + ", " + pic.getAlpha()[3] + "\n");
                            break;
                        case 5: // coordinates
                            int xOfs = (getByte(ctrlHeader, index) << 4) | (getByte(ctrlHeader, index+1) >> 4);
                            pic.setOfsX(xOfs);
                            pic.setImageWidth((((getByte(ctrlHeader, index + 1) & 0xf) << 8) | (getByte(ctrlHeader, index + 2))) - xOfs + 1);
                            int yOfs = (getByte(ctrlHeader, index + 3) << 4) | (getByte(ctrlHeader, index + 4) >> 4);
                            pic.setOfsY(yOfs);
                            pic.setImageHeight((((getByte(ctrlHeader, index + 4) & 0xf) << 8) | (getByte(ctrlHeader, index + 5))) - yOfs + 1);
                            logger.trace("Area info:" + " ("
                                    + pic.getXOffset() + ", " + pic.getYOffset() + ") - (" + (pic.getXOffset() + pic.getImageWidth() - 1) + ", "
                                    + (pic.getYOffset() + pic.getImageHeight() - 1) + ")\n");
                            index += 6;
                            break;
                        case 6: // offset to RLE buffer
                            pic.setEvenOffset(getWord(ctrlHeader, index) - 4);
                            pic.setOddOffset(getWord(ctrlHeader, index + 2) - 4);
                            index += 4;
                            logger.trace("RLE ofs:   " + ToolBox.toHexLeftZeroPadded(pic.getEvenOffset(), 4) + ", " + ToolBox.toHexLeftZeroPadded(pic.getOddOffset(), 4) + "\n");
                            break;
                        case 7: // color/alpha update
                            colorAlphaUpdate = true;
                            //int len = ToolBox.getWord(ctrlHeader, index);
                            // ignore the details for now, but just get alpha and palette info
                            alphaUpdateSum = 0;
                            b = getByte(ctrlHeader, index + 10);
                            alphaUpdate[3] = (b >> 4);
                            alphaUpdate[2] = b & 0x0f;
                            b = getByte(ctrlHeader, index + 11);
                            alphaUpdate[1] = (b >> 4);
                            alphaUpdate[0] = b & 0x0f;
                            for (int i = 0; i < 4; i++) {
                                alphaUpdateSum += alphaUpdate[i] & 0xff;
                            }
                            // only use more opaque colors
                            if (alphaUpdateSum > alphaSum) {
                                alphaSum = alphaUpdateSum;
                                System.arraycopy(alphaUpdate, 0, pic.getAlpha(), 0, 4);
                                // take over frame palette
                                b = getByte(ctrlHeader, index+8);
                                pic.getPal()[3] = (b >> 4);
                                pic.getPal()[2] = b & 0x0f;
                                b = getByte(ctrlHeader, index+9);
                                pic.getPal()[1] = (b >> 4);
                                pic.getPal()[0] = b & 0x0f;
                            }
                            // search end sequence
                            index = endSeqOfs;
                            delay = getWord(ctrlHeader, index) * 1024;
                            endSeqOfs = getWord(ctrlHeader, index + 2)-ctrlOfsRel - 2;
                            if (endSeqOfs < 0 || endSeqOfs > ctrlSize) {
                                logger.warn("Invalid end sequence offset -> no end time\n");
                                endSeqOfs = ctrlSize;
                            }
                            index += 4;
                            break;
                        case 0xff: // end sequence
                            break parse_ctrl;
                        default:
                            logger.warn("Unknown control sequence " + toHexLeftZeroPadded(cmd, 2) + " skipped\n");
                            break;
                    }
                }

                if (endSeqOfs != ctrlSize) {
                    int ctrlSeqCount = 1;
                    index = -1;
                    int nextIndex = endSeqOfs;
                    while (nextIndex != index) {
                        index = nextIndex;
                        delay = getWord(ctrlHeader, index) * 1024;
                        nextIndex = getWord(ctrlHeader, index + 2) - ctrlOfsRel - 2;
                        ctrlSeqCount++;
                    }
                    if (ctrlSeqCount > 2) {
                        logger.warn("Control sequence(s) ignored - result may be erratic.");
                    }
                    pic.setEndTime(pic.getStartTime() + delay);
                } else {
                    pic.setEndTime(pic.getStartTime());
                }

                pic.storeOriginal();

                if (colorAlphaUpdate) {
                    logger.warn("Palette update/alpha fading detected - result may be erratic.\n");
                }

                if (alphaSum == 0) {
                    if (configuration.getFixZeroAlpha()) {
                        System.arraycopy(lastAlpha, 0, pic.getAlpha(), 0, 4);
                        logger.warn("Invisible caption due to zero alpha - used alpha info of last caption.\n");
                    } else {
                        logger.warn("Invisible caption due to zero alpha (not fixed due to user setting).\n");
                    }
                }
                lastAlpha = pic.getAlpha();
            } catch (IndexOutOfBoundsException ex) {
                throw new CoreException("Index " + ex.getMessage() + " out of bounds in control header.");
            }

            subPictures.add(pic);
            return startOffset + length + 0x0a;
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }
    }

    public void decode(int index) throws CoreException {
        if (index < subPictures.size()) {
            decode(subPictures.get(index));
        } else {
            throw new CoreException("Index " + index + " out of bounds\n");
        }
    }

    private void decode(SubPictureDVD pic) throws CoreException {
        palette = SupDvdUtil.decodePalette(pic, srcPalette);
        bitmap  = SupDvdUtil.decodeImage(pic, fileBuffer, palette.getIndexOfMostTransparentPaletteEntry());

        // crop
        BitmapBounds bounds = bitmap.getCroppingBounds(palette.getAlpha(), configuration.getAlphaCrop());
        if (bounds.yMin > 0 || bounds.xMin > 0 || bounds.xMax < bitmap.getWidth() - 1 || bounds.yMax < bitmap.getHeight() - 1) {
            int width = bounds.xMax - bounds.xMin + 1;
            int height = bounds.yMax - bounds.yMin + 1;
            if (width < 2) {
                width = 2;
            }
            if (height < 2) {
                height = 2;
            }
            bitmap = bitmap.crop(bounds.xMin, bounds.yMin, width, height);
            // update picture
            pic.setImageWidth(width);
            pic.setImageHeight(height);
            pic.setOfsX(pic.getOriginalX() + bounds.xMin);
            pic.setOfsY(pic.getOriginalY() + bounds.yMin);
        }
        primaryColorIndex = bitmap.getPrimaryColorIndex(palette.getAlpha(), configuration.getAlphaThreshold(), palette.getY());
    }

    public int[] getFramePalette(int index) {
        return subPictures.get(index).getPal();
    }

    public int[] getOriginalFramePalette(int index) {
        return subPictures.get(index).getOriginalPal();
    }

    public int[] getFrameAlpha(int index) {
        return subPictures.get(index).getAlpha();
    }

    public int[] getOriginalFrameAlpha(int index) {
        return subPictures.get(index).getOriginalAlpha();
    }

    public BufferedImage getImage(Bitmap bm) {
        return bm.getImage(palette.getColorModel());
    }

    public Palette getPalette() {
        return palette;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public BufferedImage getImage() {
        return bitmap.getImage(palette.getColorModel());
    }

    public int getPrimaryColorIndex() {
        return primaryColorIndex;
    }

    public SubPicture getSubPicture(int index) {
        return subPictures.get(index);
    }

    public int getFrameCount() {
        return subPictures.size();
    }

    public int getForcedFrameCount() {
        return numForcedFrames;
    }

    public boolean isForced(int index) {
        return subPictures.get(index).isForced();
    }

    public void close() {
        if (fileBuffer != null) {
            fileBuffer.close();
        }
    }

    public long getEndTime(int index) {
        return subPictures.get(index).getEndTime();
    }

    public long getStartTime(int index) {
        return subPictures.get(index).getStartTime();
    }

    public long getStartOffset(int index) {
        return subPictures.get(index).getOffset();
    }

    public int getLanguageIndex() {
        return languageIdx;
    }

    public Palette getSrcPalette() {
        return srcPalette;
    }

    public void setSrcPalette(Palette pal) {
        srcPalette = pal;
    }
}
