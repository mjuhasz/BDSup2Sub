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

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.BitmapBounds;
import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.*;
import bdsup2sub.supstream.ImageObjectFragment;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.tools.FileBuffer;
import bdsup2sub.tools.FileBufferException;
import bdsup2sub.utils.ToolBox;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static bdsup2sub.core.Constants.*;
import static bdsup2sub.utils.ByteUtils.getByte;
import static bdsup2sub.utils.ByteUtils.getWord;
import static bdsup2sub.utils.TimeUtils.timeStrToPTS;

/**
 * Handling of SUB/IDX (VobSub) streams.
 */
public class SubDvd implements DvdSubtitleStream {

    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = Logger.getInstance();

    /** ArrayList of captions contained in the current file */
    private final ArrayList<SubPictureDVD> subPictures = new ArrayList<SubPictureDVD>();
    /** color palette read from idx file  */
    private Palette srcPalette = new Palette(DEFAULT_DVD_PALETTE);
    /** color palette created for last decoded caption  */
    private Palette palette;
    /** bitmap of the last decoded caption */
    private Bitmap bitmap;
    /** screen width of imported VobSub */
    private int screenWidth = 720;
    /** screen height of imported VobSub  */
    private int screenHeight = 576;
    /** global x offset  */
    private int globalXOffset;
    /** global y offset */
    private int globalYOffset;
    /** global delay  */
    private int globalDelay;
    /** index of language read from IDX */
    private int languageIndex;
    /** stream ID */
    private int streamID;
    /** FileBuffer for reading SUB */
    private final FileBuffer buffer;
    /** index of dominant color for the current caption */
    private int primaryColorIndex;
    /** number of forced captions in the current file  */
    private int forcedFrameCount;
    /** store last alpha values for invisible workaround */
    private static int[] lastAlpha = {0, 0xf, 0xf, 0xf};


    public SubDvd(String subFile, String idxFile) throws CoreException {
        readIdx(idxFile);
        Core.setProgressMax(subPictures.size());
        try {
            buffer = new FileBuffer(subFile);
        } catch (FileBufferException e) {
            throw new CoreException(e.getMessage());
        }
        for (int i=0; i < subPictures.size(); i++) {
            Core.setProgress(i);
            logger.info("# " + (i + 1) + "\n");
            logger.trace("Offset: " + ToolBox.toHexLeftZeroPadded(subPictures.get(i).getOffset(), 8) + "\n");
            long nextOfs;
            if (i < subPictures.size() - 1) {
                nextOfs = subPictures.get(i+1).getOffset();
            } else {
                nextOfs = buffer.getSize();
            }
            readSubFrame(subPictures.get(i), nextOfs, buffer);
        }
        logger.info("\nDetected " + forcedFrameCount + " forced captions.\n");
    }

    private void readIdx(String idxFile) throws CoreException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(idxFile));
            String s;
            int v;
            int langIdx = 0;
            boolean ignore = false;
            while ((s = in.readLine()) != null) {
                s = s.trim();
                if (s.length() < 1 || s.charAt(0) == '#') {
                    continue;
                }
                int pos = s.indexOf(':');
                if (pos == -1 || s.length()-pos <= 1) {
                    logger.error("Illegal key: " + s + "\n");
                    continue;
                }
                String key = s.substring(0, pos).trim();
                String val = s.substring(pos+1).trim();

                // size (e.g. "size: 720x576")
                if (key.equalsIgnoreCase("size")) {
                    pos = val.indexOf('x');
                    if (pos == -1 || val.length()-pos <= 1) {
                        throw new CoreException("Illegal size: " + val);
                    }
                    v = ToolBox.getInt(val.substring(0,pos));
                    if (v < 2) {
                        throw new CoreException("Illegal screen width: " + v);
                    }
                    screenWidth = v;
                    v = ToolBox.getInt(val.substring(pos+1));
                    if (v < 2) {
                        throw new CoreException("Illegal screen height: " + v);
                    }
                    screenHeight = v;
                    continue;
                }

                // origin (e.g. "org: 0, 0")
                if (key.equalsIgnoreCase("org")) {
                    pos = val.indexOf(',');
                    if (pos == -1 || val.length()-pos <= 1) {
                        throw new CoreException("Illegal origin: " + val);
                    }
                    v = ToolBox.getInt(val.substring(0,pos));
                    if (v < 0) {
                        throw new CoreException("Illegal x origin: " + v);
                    }
                    globalXOffset = v;
                    v = ToolBox.getInt(val.substring(pos+1));
                    if (v < 0) {
                        throw new CoreException("Illegal y origin: " + v);
                    }
                    globalYOffset = v;
                    continue;
                }

                // scale (e.g. "scale: 100%, 100%")
                if (key.equalsIgnoreCase("scale")) {
                    // ignored for the moment
                    continue;
                }

                // alpha (e.g. "alpha: 100%")
                if (key.equalsIgnoreCase("alpha")) {
                    // ignored for the moment
                    continue;
                }

                // smoothing (e.g. "smooth: OFF")
                if (key.equalsIgnoreCase("smooth")) {
                    // ignored for the moment
                    continue;
                }

                // fading (e.g. "fadein/out: 0, 0");
                if (key.equalsIgnoreCase("fadein/out")) {
                    // ignored for the moment
                    continue;
                }

                // alignment (e.g. "align: OFF at LEFT TOP")
                if (key.equalsIgnoreCase("align")) {
                    // ignored for the moment
                    continue;
                }

                // time offset (e.g. "time offset: 0")
                if (key.equalsIgnoreCase("time offset")) {
                    v = ToolBox.getInt(val);
                    if (v < 0) {
                        v = (int)timeStrToPTS(val);
                    }
                    if (v < 0) {
                        throw new CoreException("Illegal time offset: " + v);
                    }
                    globalDelay = v * 90; // ms -> 90kHz
                    continue;
                }

                // forced subs (e.g. "forced subs: OFF")
                if (key.equalsIgnoreCase("align")) {
                    // ignored for the moment
                    continue;
                }

                // palette
                if (key.equalsIgnoreCase("palette")) {
                    String vals[] = val.split(",");
                    if (vals == null || vals.length < 1 || vals.length > 16) {
                        throw new CoreException("Illegal palette definition: " + val);
                    }
                    for (int i=0; i<vals.length; i++) {
                        int color = -1;
                        try {
                            color = Integer.parseInt(vals[i].trim(), 16);
                        } catch (NumberFormatException ex) {
                        }
                        if (color == -1) {
                            throw new CoreException("Illegal palette entry: " + vals[i]);
                        }
                        srcPalette.setARGB(i, color);
                    }
                    continue;
                }

                // custom colors (e.g. "custom colors: OFF, tridx: 1000, colors: 000000, 444444, 888888, cccccc")
                if (key.equalsIgnoreCase("custom colors")) {
                    // ignored for the moment
                    continue;
                }

                // language index (e.g. "langidx: 0")
                if (key.equalsIgnoreCase("langidx")) {
                    v = ToolBox.getInt(val);
                    if (v < 0) {
                        throw new CoreException("Illegal language idx: " + v);
                    }
                    langIdx = v; // ms -> 90kHz
                    // ignored for the moment
                    continue;
                }

                // language id (e.g. "id: de, index: 0")
                if (key.equalsIgnoreCase("id")) {
                    String id;
                    pos = val.indexOf(',');
                    if (pos > 0 ) {
                        id = val.substring(0, pos).trim();
                    } else {
                        id = val;
                    }
                    if (id.length() != 2) {
                        logger.warn("Illegal language id: " + id + "\n");
                        continue;
                    }
                    boolean found = false;
                    for (int i=0; i < LANGUAGES.length; i++) {
                        if (id.equalsIgnoreCase(LANGUAGES[i][1])) {
                            languageIndex = i;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        logger.warn("Illegal language id: " + id + "\n");
                    }

                    pos = val.indexOf(':');
                    if (pos == -1 || s.length() - pos <= 1) {
                        logger.error("Missing index key: " + val + "\n");
                        continue;
                    }
                    key = val.substring(0, pos).trim();
                    val = val.substring(pos+1).trim();
                    if (key.equalsIgnoreCase("index"))  {
                        logger.error("Missing index key: " + s + "\n");
                        continue;
                    }
                    v = ToolBox.getInt(val);
                    if (v < 0) {
                        throw new CoreException("Illegal language index: " + v);
                    }

                    if (v != langIdx) {
                        ignore = true;
                        logger.warn("Language id " + id + "(index:" + v + ") inactive -> ignored\n");
                    } else {
                        streamID = v;
                        ignore = false;
                    }
                    continue;
                }

                if (!ignore) {
                    // timestamp: 00:00:14:160, filepos: 000000000
                    if (key.equalsIgnoreCase("timestamp")) {
                        String vs;
                        pos = val.indexOf(',');
                        if (pos == -1 || val.length()-pos <= 1) {
                            throw new CoreException("Illegal timestamp entry: " + val);
                        }
                        vs = val.substring(0,pos);
                        long t = timeStrToPTS(vs);
                        if (t < 0) {
                            throw new CoreException("Illegal timestamp: " + vs);
                        }
                        vs = val.substring(pos+1).toLowerCase();
                        pos = vs.indexOf("filepos:");
                        if (pos == -1 || vs.length()-pos <= 1) {
                            throw new CoreException("Missing filepos: " + val);
                        }
                        long l = Long.parseLong(vs.substring(pos+8).trim(), 16);
                        if (l == -1) {
                            throw new CoreException("Illegal filepos: " + vs.substring(pos+8));
                        }
                        SubPictureDVD pic = new SubPictureDVD();
                        pic.setOffset(l);
                        pic.setWidth(screenWidth);
                        pic.setHeight(screenHeight);
                        pic.setStartTime(t + globalDelay);
                        subPictures.add(pic);
                    }
                }
            }
        } catch (IOException ex) {
            throw new CoreException(ex.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Read one frame from SUB file
     * @param pic SubPicture object for this frame
     * @param endOfs end offset
     * @param buffer File Buffer to read from
     * @throws CoreException
     */
    private void readSubFrame(SubPictureDVD pic, long endOfs, FileBuffer buffer) throws CoreException  {
        long ofs = pic.getOffset();
        long ctrlOfs = -1;
        long nextOfs;
        int  ctrlOfsRel = 0;
        int rleSize = 0;
        int rleBufferFound = 0;
        int ctrlSize = -1;
        int ctrlHeaderCopied = 0;
        byte ctrlHeader[] = null;
        ImageObjectFragment rleFrag;
        int length;
        int packHeaderSize;
        boolean firstPackFound = false;

        try {
            do {
                // 4 bytes:  packet identifier 0x000001ba
                long startOfs = ofs;
                if (buffer.getDWord(ofs) != 0x000001ba) {
                    throw new CoreException("Missing packet identifier at ofs " + ToolBox.toHexLeftZeroPadded(ofs,8));
                }
                // 6 bytes:  system clock reference
                // 3 bytes:  multiplexer rate
                // 1 byte:   stuffing info
                int stuffOfs = buffer.getByte(ofs+=13) & 7;
                // 4 bytes:  sub packet ID 0x000001bd
                if (buffer.getDWord(ofs += (1+stuffOfs)) != 0x000001bd) {
                    throw new CoreException("Missing packet identifier at ofs " + ToolBox.toHexLeftZeroPadded(ofs,8));
                }
                // 2 bytes:  packet length (number of bytes after this entry)
                length = buffer.getWord(ofs+=4);
                nextOfs = ofs+2+length;
                // 2 bytes:  packet type
                ofs += 2;
                packHeaderSize = (int)(ofs-startOfs);
                boolean firstPack = ((buffer.getByte(++ofs) & 0x80) == 0x80);
                // 1 byte    pts length
                int ptsLength = buffer.getByte(ofs+=1);
                ofs += 1 + ptsLength; // skip PTS and stream ID
                int packetStreamID = buffer.getByte(ofs++) - 0x20;
                if (packetStreamID != streamID) {
                    // packet doesn't belong to stream -> skip
                    if (nextOfs % 0x800 != 0) {
                        ofs = (nextOfs/0x800 + 1)*0x800;
                        logger.warn("Offset to next fragment is invalid. Fixed to:" + ToolBox.toHexLeftZeroPadded(ofs, 8) + "\n");
                    } else {
                        ofs = nextOfs;
                    }
                    ctrlOfs += 0x800;
                    continue;
                }
                int headerSize = (int)(ofs-startOfs); // only valid for additional packets
                if (firstPack && ptsLength >= 5) {
                    int size = buffer.getWord(ofs);
                    ofs += 2;
                    ctrlOfsRel = buffer.getWord(ofs);
                    rleSize = ctrlOfsRel-2;             // calculate size of RLE buffer
                    ctrlSize = size-ctrlOfsRel-2;       // calculate size of control header
                    if (ctrlSize < 0) {
                        throw new CoreException("Invalid control buffer size");
                    }
                    ctrlHeader = new byte[ctrlSize];
                    ctrlOfs = ctrlOfsRel + ofs; // might have to be corrected for multiple packets
                    ofs += 2;
                    headerSize = (int)(ofs-startOfs);
                    pic.setRleFragments(new ArrayList<ImageObjectFragment>());
                    firstPackFound = true;
                } else {
                    if (firstPackFound) {
                        ctrlOfs += headerSize; // fix absolute offset by adding header bytes
                    } else {
                        logger.warn("Invalid fragment skipped at ofs " + ToolBox.toHexLeftZeroPadded(startOfs, 8) + "\n");
                    }
                }

                // check if control header is (partly) in this packet
                int diff = (int)(nextOfs - ctrlOfs - ctrlHeaderCopied);
                if (diff<0) {
                    diff = 0;
                }
                int copied = ctrlHeaderCopied;
                try {
                    for (int i=0; (i < diff) && (ctrlHeaderCopied<ctrlSize); i++) {
                        ctrlHeader[ctrlHeaderCopied] = (byte)buffer.getByte(ctrlOfs + i + copied);
                        ctrlHeaderCopied++;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new CoreException("Inconsistent control buffer access (" + ex.getMessage() + ")");
                }
                rleFrag = new ImageObjectFragment(ofs, length - headerSize - diff + packHeaderSize);
                pic.getRleFragments().add(rleFrag);

                rleBufferFound += rleFrag.getImagePacketSize();

                if (ctrlHeaderCopied != ctrlSize && (nextOfs % 0x800 != 0)) {
                    ofs = (nextOfs/0x800 + 1) * 0x800;
                    logger.warn("Offset to next fragment is invalid. Fixed to:" + ToolBox.toHexLeftZeroPadded(ofs, 8) + "\n");
                    rleBufferFound += ofs-nextOfs;
                } else {
                    ofs = nextOfs;
                }
            } while (ofs < endOfs && ctrlHeaderCopied < ctrlSize);

            if (ctrlHeaderCopied != ctrlSize) {
                logger.warn("Control buffer size inconsistent.\n");
                // fill rest of buffer with break command to avoid wrong detection of forced caption (0x00)
                for (int i=ctrlHeaderCopied; i<ctrlSize; i++) {
                    ctrlHeader[i] = (byte)0xff;
                }
            }

            if (rleBufferFound != rleSize) {
                logger.warn("RLE buffer size inconsistent.\n");
            }

            pic.setRleSize(rleBufferFound);
        } catch (FileBufferException ex) {
            throw new CoreException(ex.getMessage());
        }

        pic.setPal(new int[4]);
        pic.setAlpha(new int[4]);
        int alphaSum = 0;
        int alphaUpdate[] = new int[4];
        int alphaUpdateSum;
        int delay = -1;
        boolean ColAlphaUpdate = false;

        logger.trace("SP_DCSQT at ofs: " + ToolBox.toHexLeftZeroPadded(ctrlOfs, 8) + "\n");

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
                        forcedFrameCount++;
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
                        for (int i = 0; i<4; i++) {
                            alphaSum += pic.getAlpha()[i] & 0xff;
                        }
                        logger.trace("Alpha:     " + pic.getAlpha()[0] + ", " + pic.getAlpha()[1] + ", " + pic.getAlpha()[2] + ", " + pic.getAlpha()[3] + "\n");
                        break;
                    case 5: // coordinates
                        int xOfs = (getByte(ctrlHeader, index)<<4) | (getByte(ctrlHeader, index+1)>>4);
                        pic.setOfsX(globalXOffset +xOfs);
                        pic.setImageWidth((((getByte(ctrlHeader, index+1)&0xf)<<8) | (getByte(ctrlHeader, index+2))) - xOfs + 1);
                        int yOfs = (getByte(ctrlHeader, index+3)<<4) | (getByte(ctrlHeader, index+4)>>4);
                        pic.setOfsY(globalYOffset +yOfs);
                        pic.setImageHeight((((getByte(ctrlHeader, index+4)&0xf)<<8) | (getByte(ctrlHeader, index+5))) - yOfs + 1);
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
                        ColAlphaUpdate = true;
                        //int len = ToolBox.getWord(ctrlHeader, index);
                        // ignore the details for now, but just get alpha and palette info
                        alphaUpdateSum = 0;
                        b = getByte(ctrlHeader, index+10);
                        alphaUpdate[3] = (b >> 4);
                        alphaUpdate[2] = b & 0x0f;
                        b = getByte(ctrlHeader, index+11);
                        alphaUpdate[1] = (b >> 4);
                        alphaUpdate[0] = b & 0x0f;
                        for (int i = 0; i<4; i++) {
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
                        delay = getWord(ctrlHeader, index)*1024;
                        endSeqOfs = getWord(ctrlHeader, index+2)-ctrlOfsRel-2;
                        if (endSeqOfs < 0 || endSeqOfs > ctrlSize) {
                            logger.warn("Invalid end sequence offset -> no end time\n");
                            endSeqOfs = ctrlSize;
                        }
                        index += 4;
                        break;
                    case 0xff: // end sequence
                        break parse_ctrl;
                    default:
                        logger.warn("Unknown control sequence " + ToolBox.toHexLeftZeroPadded(cmd, 2) + " skipped\n");
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

            if (ColAlphaUpdate) {
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
            pic.storeOriginal();
        } catch (IndexOutOfBoundsException ex) {
            throw new CoreException("Index "+ex.getMessage() + " out of bounds in control header.");
        }
    }

    public void decode(int index) throws CoreException {
        if (index < subPictures.size()) {
            decode(subPictures.get(index));
        } else {
            throw new CoreException("Index " + index + " out of bounds\n");
        }
    }

    private void decode(SubPictureDVD pic)  throws CoreException {
        palette = SupDvdUtil.decodePalette(pic, srcPalette);
        bitmap  = SupDvdUtil.decodeImage(pic, buffer, palette.getIndexOfMostTransparentPaletteEntry());

        // crop
        BitmapBounds bounds = bitmap.getCroppingBounds(palette.getAlpha(), configuration.getAlphaCrop());
        if (bounds.yMin>0 || bounds.xMin > 0 || bounds.xMax<bitmap.getWidth()-1 || bounds.yMax<bitmap.getHeight()-1) {
            int w = bounds.xMax - bounds.xMin + 1;
            int h = bounds.yMax - bounds.yMin + 1;
            if (w<2) {
                w = 2;
            }
            if (h<2) {
                h = 2;
            }
            bitmap = bitmap.crop(bounds.xMin, bounds.yMin, w, h);
            // update picture
            pic.setImageWidth(w);
            pic.setImageHeight(h);
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
        return forcedFrameCount;
    }

    public boolean isForced(int index) {
        return subPictures.get(index).isForced();
    }

    public void close() {
        if (buffer!=null)
            buffer.close();
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
        return languageIndex;
    }

    public Palette getSrcPalette() {
        return srcPalette;
    }

    public void setSrcPalette(Palette pal) {
        srcPalette = pal;
    }
}
