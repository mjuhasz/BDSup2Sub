package deadbeef.SupTools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import deadbeef.Tools.FileBuffer;
import deadbeef.Tools.FileBufferException;
import deadbeef.Tools.ToolBox;

/*
 * Copyright 2009 Volker Oth (0xdeadbeef)
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

/**
 * Handling of SUB/IDX (VobSub) streams.
 *
 * @author 0xdeadbeef, mjuhasz
 */
class SubDVD implements Substream, SubstreamDVD {

	final private static byte packHeader[] = {
		0x00, 0x00, 0x01, (byte)0xba,							// 0:  0x000001ba - packet ID
		0x44, 0x02, (byte)0xc4, (byte)0x82, 0x04, (byte)0xa9,	// 4:  system clock reference
		0x01 , (byte)0x89, (byte)0xc3,							// 10: multiplexer rate
		(byte)0xf8,												// 13: stuffing info
	};

	private static byte headerFirst[] = {						// header only in first packet
		0x00, 0x00, 0x01, (byte)0xbd,							// 0: 0x000001bd - sub ID
		0x00, 0x00,												// 4: packet length
		(byte)0x81, (byte)0x80, 								// 6:  packet type
		0x05,													// 8:  PTS length
		0x00, 0x0, 0x00, 0x00, 0x00,							// 9:  PTS
		0x20,													// 14: stream ID
		0x00, 0x00,												// 15: Subpicture size in bytes
		0x00, 0x00,												// 17: offset to control header
	};

	private static byte headerNext[] = {						// header in following packets
		0x00, 0x00, 0x01, (byte)0xbd,							// 0: 0x000001bd - sub ID
		0x00, 0x00,												// 4: packet length
		(byte)0x81, (byte)0x00, 								// 6: packet type
		0x00,													// 8: PTS length = 0
		0x20													// 9: Stream ID
	};

	private static byte controlHeader[] = {
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

	/** ArrayList of captions contained in the current file */
	private final ArrayList<SubPictureDVD> subPictures;
	/** color palette read from idx file  */
	private Palette srcPalette;
	/** color palette created for last decoded caption  */
	private Palette palette;
	/** bitmap of the last decoded caption */
	private Bitmap bitmap;
	/** screen width of imported VobSub */
	private int screenWidth;
	/** screen height of imported VobSub  */
	private int screenHeight;
	/** global x offset  */
	private int ofsXglob;
	/** global y offset */
	private int ofsYglob;
	/** global delay  */
	private int delayGlob;
	/** index of language read from IDX */
	private int languageIdx;
	/** stream ID */
	private int streamID;
	/** FileBuffer for reading SUB */
	private final FileBuffer buffer;
	/** index of dominant color for the current caption */
	private int primaryColorIndex;
	/** number of forced captions in the current file  */
	private int numForcedFrames;
	/** store last alpha values for invisible workaround */
	private static int lastAlpha[] = {0,0xf,0xf,0xf};


	/**
	 * Constructor
	 * @param fnSub file name of SUB file
	 * @param fnIdx file name of IDX file
	 * @throws CoreException
	 */
	SubDVD(final String fnSub, final String fnIdx) throws CoreException {
		screenWidth = 720;
		screenHeight = 576;
		ofsXglob = 0;
		ofsYglob = 0;
		delayGlob = 0;
		languageIdx = 0;

		srcPalette = new Palette (Core.getDefaultDVDPalette());
		subPictures = new ArrayList<SubPictureDVD>();

		readIdx(fnIdx);
		Core.setProgressMax(subPictures.size());
		try {
			buffer = new FileBuffer(fnSub);
		} catch (FileBufferException e) {
			throw new CoreException(e.getMessage());
		}
		for (int i=0; i<subPictures.size(); i++) {
			Core.setProgress(i);
			Core.printX("# "+(i+1)+"\n");
			Core.print("Ofs: "+ToolBox.hex(subPictures.get(i).offset,8)+"\n");
			long nextOfs;
			if (i < subPictures.size()-1)
				nextOfs =  subPictures.get(i+1).offset;
			else nextOfs = buffer.getSize();
			readSubFrame(subPictures.get(i), nextOfs, buffer);
		}

		Core.printX("\nDetected "+numForcedFrames+" forced captions.\n");
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
	static void decodeLine(final byte src[], final int srcOfs, final int srcLen, final byte trg[], int trgOfs, final int width,  final int maxPixels) {
		// to make access to nibbles easier, copy bytes to a nibble array
		final byte nibbles[] = new byte[srcLen*2];
		int b;

		for (int i=0; i<srcLen; i++) {
			b = src[srcOfs+i] & 0xff;
			nibbles[2*i]   = (byte)(b >> 4);
			nibbles[2*i+1] = (byte)(b & 0x0f);
		}

		int index = 0;
		int sumPixels = 0;
		int x=0;

		while (index < nibbles.length && sumPixels < maxPixels) {
			int len;
			int col;
			b = nibbles[index++]&0xff;
			if (b == 0) {
				// three or four nibble code
				b = nibbles[index++]&0xff;
				if ( (b & 0xc) != 0) {
					// three byte code
					len = b << 2;
					b = nibbles[index++]&0xff;
					len  |= (b >> 2);
				} else {
					// line feed or four nibble code
					len = b << 6;
					b = nibbles[index++]&0xff;
					len  |= (b << 2);
					b = nibbles[index++]&0xff;
					len  |= (b >> 2);
					if (len == 0) {
						// line feed
						len = width-x;
						if (len <= 0 || sumPixels >= maxPixels) {
							len = 0;
							// handle line feed
							trgOfs += 2*width; // lines are interlaced!
							sumPixels = ((trgOfs/width)/2)*width;
							x = 0;
						}
						if ( (index & 1) == 1)
							index++;
					}
				}
			} else {
				// one or two nibble code
				len = b >> 2;
				if (len == 0) {
					// two nibble code
					len = b << 2;
					b = nibbles[index++]&0xff;
					len  |= (b >> 2);
				}
			}

			col = b & 0x3;
			sumPixels += len;

			for (int i=0; i<len; i++) {
				trg[trgOfs+x] = (byte)col;
				if (++x >= width) {
					trgOfs += 2*width; // lines are interlaced!
					x = 0;
					if ( (index & 1) == 1)
						index++;
				}
			}
		}
	}

	/**
	 * compress bitmap to RLE buffer
	 * @param bm bitmap to compress
	 * @param even true: encode even lines, false: encode odd lines
	 * @return RLE buffer
	 */
	static byte[] encodeLines(final Bitmap bm, final boolean even) {
		int ofs = 0;
		byte color;
		int len;
		int y;
		final ArrayList<Byte> nibbles = new ArrayList<Byte>();

		if (even)
			y = 0;
		else
			y = 1;

		for (; y < bm.getHeight(); y+=2) {
			ofs = y*bm.getWidth();
			for (int x=0; x < bm.getWidth(); x+=len, ofs+=len) {
				color = bm.getImg()[ofs];
				for (len=1; x+len < bm.getWidth(); len++)
					if (bm.getImg()[ofs+len] != color)
						break;
				if (len < 4) {
					nibbles.add((byte)((len << 2)|(color&3)));
				} else if (len < 0x10) {
					nibbles.add((byte)(len >> 2));
					nibbles.add((byte)((len << 2)|(color&3)));
				} else if (len < 0x40) {
					nibbles.add((byte)0);
					nibbles.add((byte)(len >> 2));
					nibbles.add((byte)((len << 2)|(color&3)));
				} else if (x+len == bm.getWidth()) {
					nibbles.add((byte)(0));
					nibbles.add((byte)(0));
					nibbles.add((byte)(0));
					nibbles.add(color);
				} else {
					if (len > 0xff)
						len = 0xff;
					nibbles.add((byte)(0));
					nibbles.add((byte)(len >> 6));
					nibbles.add((byte)(len >> 2));
					nibbles.add((byte)((len << 2)|(color&3)));
				}
			}
			if ((nibbles.size() & 1) == 1)
				nibbles.add((byte)(0));
		}
		// end buffer with line feed
		nibbles.add((byte)(0));
		nibbles.add((byte)(0));
		nibbles.add((byte)(0));
		nibbles.add((byte)(0));

		final int size =  nibbles.size() / 2; // number of bytes
		final byte[] retval = new byte[size];
		final Iterator<Byte> it = nibbles.iterator();

		for (int i=0; i<size; i++) {
			int hi = (it.next()&0xf);
			int lo = (it.next()&0xf);
			retval[i] = (byte)((hi<<4) | lo);
		}
		return retval;
	}

	/**
	 * Create the binary stream representation of one caption
	 * @param pic SubPicture object containing caption info
	 * @param bm bitmap
	 * @param wdtvFixEnabled
	 * @return byte buffer containing the binary stream representation of one caption
	 */
	static byte[] createSubFrame(final SubPictureDVD pic, final Bitmap bm, boolean wdtvFixEnabled) {
	    if (wdtvFixEnabled) {
	        swapATransparentColorWithTheLastOne(pic, bm);
	    }

		/* create RLE buffers */
		final byte even[] = encodeLines(bm, true);
		final byte odd[]  = encodeLines(bm, false);
		int tmp;

		int forcedOfs;
		int controlHeaderLen;
		if (pic.isforced) {
			forcedOfs = 0;
			controlHeader[2] = 0x01; // display
			controlHeader[3] = 0x00; // forced
			controlHeaderLen = controlHeader.length;

		} else {
			forcedOfs = 1;
			controlHeader[2] = 0x00; // part of offset
			controlHeader[3] = 0x01; // display
			controlHeaderLen = controlHeader.length-1;
		}

		// fill out all info but the offets (determined later)

		/* header - contains PTM */
		final int ptm = (int)pic.startTime; // should be end time, but STC writes start time?
		headerFirst[9]  = (byte)(((ptm >> 29) & 0x0E) | 0x21);
		headerFirst[10] = (byte)(ptm >> 22);
		headerFirst[11] = (byte)((ptm >> 14) | 1);
		headerFirst[12] = (byte)(ptm >> 7);
		headerFirst[13] = (byte)(ptm * 2 + 1);

		/* control header */
		/* palette (store reversed) */
		controlHeader[1+4] = (byte)(((pic.pal[3]&0xf)<<4) | (pic.pal[2]&0x0f));
		controlHeader[1+5] = (byte)(((pic.pal[1]&0xf)<<4) | (pic.pal[0]&0x0f));
		/* alpha (store reversed) */
		controlHeader[1+7] = (byte)(((pic.alpha[3]&0xf)<<4) | (pic.alpha[2]&0x0f));
		controlHeader[1+8] = (byte)(((pic.alpha[1]&0xf)<<4) | (pic.alpha[0]&0x0f));

		/* coordinates of subtitle */
		controlHeader[1+10] = (byte)((pic.getOfsX() >> 4) & 0xff);
		tmp = pic.getOfsX()+bm.getWidth()-1;
		controlHeader[1+11] = (byte)(((pic.getOfsX() & 0xf)<<4) | ((tmp>>8)&0xf) );
		controlHeader[1+12] = (byte)(tmp&0xff);

		int yOfs = pic.getOfsY() - Core.getCropOfsY();
		if (yOfs < 0)
			yOfs = 0;
		else {
			int yMax = pic.height - pic.getImageHeight() - 2*Core.getCropOfsY();
			if (yOfs > yMax)
				yOfs = yMax;
		}

		controlHeader[1+13] = (byte)((yOfs >> 4) & 0xff);
		tmp = yOfs+bm.getHeight()-1;
		controlHeader[1+14] = (byte)(((yOfs & 0xf)<<4) | ((tmp>>8)&0xf) );
		controlHeader[1+15] = (byte)(tmp&0xff);

		/* offset to even lines in rle buffer */
		controlHeader[1+17] = 0x00; /* 2 bytes subpicture size and 2 bytes control header ofs */
		controlHeader[1+18] = 0x04; /* note: SubtitleCreator uses 6 and adds 0x0000 in between */

		/* offset to odd lines in rle buffer */
		tmp = even.length + controlHeader[1+18];
		controlHeader[1+19] = (byte)((tmp >> 8)&0xff);
		controlHeader[1+20] = (byte)(tmp&0xff);

		/* display duration in frames */
		tmp = (int)((pic.endTime-pic.startTime)/1024); // 11.378ms resolution????
		controlHeader[1+22] = (byte)((tmp >> 8)&0xff);
		controlHeader[1+23] = (byte)(tmp&0xff);

		/* offset to end sequence - 22 is the offset of the end sequence */
		tmp = even.length + odd.length + 22 + (pic.isforced?1:0) + 4;
		controlHeader[forcedOfs+0] = (byte)((tmp >> 8)&0xff);
		controlHeader[forcedOfs+1] = (byte)(tmp&0xff);
		controlHeader[1+24] = (byte)((tmp >> 8)&0xff);
		controlHeader[1+25] = (byte)(tmp&0xff);

		// subpicture size
		tmp = even.length + odd.length + 4 + controlHeaderLen;
		headerFirst[15] = (byte)(tmp >> 8);
		headerFirst[16] = (byte)tmp;

		/* offset to control buffer - 2 is the size of the offset */
		tmp = even.length + odd.length + 2;
		headerFirst[17] = (byte)(tmp >> 8);
		headerFirst[18] = (byte)tmp;

		// unfortunately, the SUB format is so completely fucked that
		// only 0x800 bytes can be written per packet. If a packet
		// is larger, it has to be split into fragments <= 0x800 bytes
		// which follow one after the other.

		final int sizeRLE = even.length+ odd.length;
		int bufSize = packHeader.length + headerFirst.length + controlHeaderLen + sizeRLE;
		int numAdditionalPackets = 0;
		if (bufSize > 0x800) {
			// determine how many additional headers we will need
			// considering that each additional header also adds to the size
			// due to its own headers
			numAdditionalPackets = 1;
			int remainingRLEsize = sizeRLE  - (0x800-packHeader.length-headerFirst.length); // size - 0x7df
			while (remainingRLEsize > (0x800-packHeader.length-headerNext.length-controlHeaderLen)) {
				remainingRLEsize -= (0x800-packHeader.length-headerNext.length);
				bufSize += packHeader.length+headerNext.length;
				numAdditionalPackets++;
			}
			// packet length of the 1st packet should be the maximum size
			tmp = 0x800-packHeader.length-6;
		} else
			tmp = (bufSize-packHeader.length-6);

		// allocate and fill buffer
		final byte buf[] = new byte[(1+numAdditionalPackets)*0x800];

		int stuffingBytes;
		int diff = buf.length-bufSize;
		if (diff > 0 && diff < 6)
			stuffingBytes = diff;
		else
			stuffingBytes = 0;

		int ofs = 0;
		for (int i=0; i<packHeader.length; i++)
			buf[ofs++] = packHeader[i];

		// set packet length
		tmp += stuffingBytes;
		headerFirst[4] = (byte)(tmp >> 8);
		headerFirst[5] = (byte)tmp;

		// set pts length
		headerFirst[8] = (byte)(5 + stuffingBytes);

		// write header and use pts for stuffing bytes (if needed)
		for (int i=0; i<14; i++)
			buf[ofs++] = headerFirst[i];
		for (int i=0; i<stuffingBytes; i++)
			buf[ofs++] = (byte)0xff;
		for (int i=14; i<headerFirst.length; i++)
			buf[ofs++] = headerFirst[i];

		// write (first part of) RLE buffer
		tmp = sizeRLE;
		if (numAdditionalPackets > 0) {
			tmp = (0x800-packHeader.length-stuffingBytes-headerFirst.length);
			if (tmp > sizeRLE) // can only happen in 1st buffer
				tmp = sizeRLE;
		}
		for (int i=0; i<tmp; i++) {
			if (i<even.length)
				buf[ofs++] = even[i];
			else
				buf[ofs++] = odd[i-even.length];
		}
		int ofsRLE=tmp;

		// fill gap in first packet with (parts of) control header
		// only if the control header is split over two packets
		int controlHeaderWritten = 0;
		if (numAdditionalPackets == 1 && ofs<0x800) {
			for (; ofs<0x800; ofs++)
				buf[ofs] = controlHeader[forcedOfs+(controlHeaderWritten++)];
		}

		// write additional packets
		for (int p=0; p<numAdditionalPackets; p++) {
			int rleSizeLeft;
			if (p==numAdditionalPackets-1) {
				// last loop
				rleSizeLeft = sizeRLE-ofsRLE;
				tmp = headerNext.length + (controlHeaderLen-controlHeaderWritten) + (sizeRLE-ofsRLE) - 6;
			} else {
				tmp = 0x800-packHeader.length-6;
				rleSizeLeft = (0x800-packHeader.length-headerNext.length);
				// now, again, it could happen that the RLE buffer runs out before the last package
				if (rleSizeLeft > (sizeRLE-ofsRLE))
					rleSizeLeft = sizeRLE-ofsRLE;
			}
			// copy packet headers
			packHeader[13] = (byte)(0xf8);
			for (int i=0; i<packHeader.length; i++)
				buf[ofs++] = packHeader[i];

			// set packet length
			headerNext[4] = (byte)(tmp >> 8);
			headerNext[5] = (byte)tmp;
			for (int i=0; i<headerNext.length; i++)
				buf[ofs++] = headerNext[i];

			// copy RLE buffer
			for (int i=ofsRLE; i<ofsRLE+rleSizeLeft; i++) {
				if (i<even.length)
					buf[ofs++] = even[i];
				else
					buf[ofs++] = odd[i-even.length];
			}
			ofsRLE += rleSizeLeft;
			// fill possible gap in all but last package with (parts of) control header
			// only if the control header is split over two packets
			// this can only happen in the package before the last one though
			if (p != numAdditionalPackets-1)
				for (; ofs<(p+2)*0x800; ofs++)
					buf[ofs] = controlHeader[forcedOfs+(controlHeaderWritten++)];
		}

		// write (rest of) control header
		for (int i=controlHeaderWritten; i<controlHeaderLen; i++)
			buf[ofs++] = controlHeader[forcedOfs+i];

		// fill rest of last packet with padding bytes
		diff = buf.length - ofs;
		if (diff >= 6) {
			diff -= 6;
			buf[ofs++] = 0x00;
			buf[ofs++] = 0x00;
			buf[ofs++] = 0x01;
			buf[ofs++] = (byte)0xbe;
			buf[ofs++] = (byte)(diff >> 8);
			buf[ofs++] = (byte)diff;
			for (; ofs<buf.length; ofs++)
				buf[ofs] = (byte)0xff;
		} // else should never happen due to stuffing bytes

		return buf;
	}

    private static void swapATransparentColorWithTheLastOne(final SubPictureDVD pic, final Bitmap bm) {
        if (pic.alpha[3] != 0) { // wdtv live sets the opacity for color 3 to 0
            Core.print("WDTV fix is needed to workaround color 3 opacity bug\n");

            //find a transparent color
            byte tranparentIndex = -1;
            for (int i = 0; i < pic.alpha.length; i++) {
                if (pic.alpha[i] == 0) {
                    tranparentIndex = (byte)i;
                    break;
                }
            }

            if (tranparentIndex != -1) {
                //swap image indexes
                byte[] img = bm.getImg();
                for (int i=0; i<img.length; i++) {
                    if (img[i] == tranparentIndex) {
                        img[i] = 3;
                    } else if (img[i] == 3) {
                        img[i] = tranparentIndex;
                    }
                }

                //swap alpha values
                int alpha3 = pic.alpha[3];
                pic.alpha[3] = pic.alpha[tranparentIndex];
                pic.alpha[tranparentIndex] = alpha3;

                //swap palette references
                int pal3 = pic.pal[3];
                pic.pal[3] = pic.pal[tranparentIndex];
                pic.pal[tranparentIndex] = pal3;
            } else {
                Core.printErr("Fix for WDTV is not applicable - no transparent color on sub frame \n");
            }
        }
    }

	/**
	 * Read and parse IDX file
	 * @param fname file name
	 * @throws CoreException
	 */
	void readIdx(final String fname) throws CoreException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fname));
			String s;
			int v;
			int langIdx = 0;
			boolean ignore = false;
			while ( (s = in.readLine()) != null ) {
				s = ToolBox.trim(s);
				if (s.length() < 1 || s.charAt(0) == '#')
					continue;
				int pos = s.indexOf(':');
				if (pos == -1 || s.length()-pos <= 1) {
					Core.printErr("Illegal key: "+s+"\n");
					continue;
				}
				String key = ToolBox.trim(s.substring(0, pos));
				String val = ToolBox.trim(s.substring(pos+1));

				// size (e.g. "size: 720x576")
				if (key.equalsIgnoreCase("size")) {
					pos = val.indexOf('x');
					if (pos == -1 || val.length()-pos <= 1)
						throw new CoreException("Illegal size: "+val);
					v = ToolBox.getInt(val.substring(0,pos));
					if (v < 2)
						throw new CoreException("Illegal screen width: "+v);
					screenWidth = v;
					v = ToolBox.getInt(val.substring(pos+1));
					if (v < 2)
						throw new CoreException("Illegal screen height: "+v);
					screenHeight = v;
					continue;
				}

				// origin (e.g. "org: 0, 0")
				if (key.equalsIgnoreCase("org")) {
					pos = val.indexOf(',');
					if (pos == -1 || val.length()-pos <= 1)
						throw new CoreException("Illegal origin: "+val);
					v = ToolBox.getInt(val.substring(0,pos));
					if (v < 0)
						throw new CoreException("Illegal x origin: "+v);
					ofsXglob = v;
					v = ToolBox.getInt(val.substring(pos+1));
					if (v < 0)
						throw new CoreException("Illegal y origin: "+v);
					ofsYglob = v;
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
					if (v < 0)
						v = (int)ToolBox.timeStrToPTS(val);
					if (v < 0)
						throw new CoreException("Illegal time offset: "+v);
					delayGlob = v*90; // ms -> 90kHz
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
					if (vals == null || vals.length < 1 || vals.length > 16)
						throw new CoreException("Illegal palette definition: "+val);
					for (int i=0; i<vals.length; i++) {
						int color = -1;
						try {
							color = Integer.parseInt(ToolBox.trim(vals[i]), 16);
						} catch (NumberFormatException ex) {}
						if (color == -1)
							throw new CoreException("Illegal palette entry: "+vals[i]);
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
					if (v < 0)
						throw new CoreException("Illegal language idx: "+v);
					langIdx = v; // ms -> 90kHz
					// ignored for the moment
					continue;
				}

				// language id (e.g. "id: de, index: 0")
				if (key.equalsIgnoreCase("id")) {
					String id;
					pos = val.indexOf(',');
					if (pos > 0 )
						id = ToolBox.trim(val.substring(0,pos));
					else
						id = val;
					if (id.length() != 2) {
						Core.printWarn("Illegal language id: "+id+"\n");
						continue;
					}
					boolean found = false;
					for (int i=0; i<Core.getLanguages().length; i++)
						if (id.equalsIgnoreCase(Core.getLanguages()[i][1])) {
							languageIdx = i;
							found = true;
							break;
						}
					if (!found)
						Core.printWarn("Illegal language id: "+id+"\n");

					pos = val.indexOf(':');
					if (pos == -1 || s.length()-pos <= 1) {
						Core.printErr("Missing index key: "+val+"\n");
						continue;
					}
					key = ToolBox.trim(val.substring(0, pos));
					val = ToolBox.trim(val.substring(pos+1));
					if (key.equalsIgnoreCase("index"))  {
						Core.printErr("Missing index key: "+s+"\n");
						continue;
					}
					v = ToolBox.getInt(val);
					if (v < 0)
						throw new CoreException("Illegal language index: "+v);

					if (v != langIdx) {
						ignore = true;
						Core.printWarn("Language id "+id+"(index:"+v+") inactive -> ignored\n");
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
						if (pos == -1 || val.length()-pos <= 1)
							throw new CoreException("Illegal timestamp entry: "+val);
						vs = val.substring(0,pos);
						long t = ToolBox.timeStrToPTS(vs);
						if (t < 0)
							throw new CoreException("Illegal timestamp: "+vs);
						vs = val.substring(pos+1).toLowerCase();
						pos = vs.indexOf("filepos:");
						if (pos == -1 || vs.length()-pos <= 1)
							throw new CoreException("Missing filepos: "+val);
						long l = Long.parseLong(ToolBox.trim(vs.substring(pos+8)), 16);
						if (l==-1)
							throw new CoreException("Illegal filepos: "+vs.substring(pos+8));
						SubPictureDVD pic = new SubPictureDVD();
						pic.offset = l;
						pic.width = screenWidth;
						pic.height = screenHeight;
						pic.startTime = t + delayGlob;
						subPictures.add(pic);
					}
				}
			}

		} catch (IOException ex) {
			throw new CoreException(ex.getMessage());
		}
		finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ex) {};
		}
	}

	/**
	 * create fitting four color palette for the given caption
	 * @param pic SubPicture object containing info about the caption
	 * @param pal base palette
	 * @return decoded palette
	 */
	static Palette decodePalette(final SubPictureDVD pic, final Palette pal) {
		final Palette miniPal = new Palette(4, true);
		for (int i=0; i<4; i++) {
			int a = (pic.alpha[i]*0xff)/0xf;
			if (a >= Core.getAlphaCrop()) {
				miniPal.setRGB(i, pal.getR()[pic.pal[i]]&0xff, pal.getG()[pic.pal[i]]&0xff, pal.getB()[pic.pal[i]]&0xff);
				miniPal.setAlpha(i, a);
			} else
				miniPal.setARGB(i, 0);
		}
		return miniPal;
	}

	/**
	 * decode caption from the input stream
	 * @param pic SubPicture object containing info about the caption
	 * @param transIdx index of the transparent color
	 * @return bitmap of the decoded caption
	 * @throws CoreException
	 */
	static Bitmap decodeImage(final SubPictureDVD pic, final FileBuffer fBuf, final int transIdx) throws CoreException {
		final int w = pic.originalWidth;
		final int h = pic.originalHeight;
		int warnings = 0;

		ImageObjectFragment info = pic.rleFragments.get(0);
		final long startOfs = info.imageBufferOfs;

		if (w > pic.width || h > pic.height)
			Core.printWarn("Subpicture too large: "+w+"x"+h+
					" at offset "+ToolBox.hex(startOfs, 8)+"\n");

		final Bitmap bm = new Bitmap(w, h, transIdx);

		// copy buffer(s)
		final byte buf[] = new byte[pic.rleSize];
		int index = 0;

		final int sizeEven;
		final int sizeOdd;

		if (pic.oddOfs > pic.evenOfs) {
			sizeEven = pic.oddOfs-pic.evenOfs;
			sizeOdd = pic.rleSize-pic.oddOfs;
		} else {
			sizeOdd = pic.evenOfs-pic.oddOfs;
			sizeEven = pic.rleSize-pic.evenOfs;
		}

		if (sizeEven <= 0 || sizeOdd <= 0)
			throw new CoreException("Corrupt buffer offset information");

		try {
			// copy buffers
			try {
				for (int p = 0; p < pic.rleFragments.size(); p++) {
					// copy data of all packet to one common buffer
					info = pic.rleFragments.get(p);
					for (int i=0; i < info.imagePacketSize; i++)
						buf[index+i] = (byte)fBuf.getByte(info.imageBufferOfs+i);
					index += info.imagePacketSize;
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				warnings++;
			}
			// decode even lines
			try {
				decodeLine(buf, pic.evenOfs, sizeEven, bm.getImg(), 0, w,  w*(h/2+(h&1)));
			} catch (ArrayIndexOutOfBoundsException ex) {
				warnings++;
			}
			// decode odd lines
			try {
				decodeLine(buf, pic.oddOfs, sizeOdd, bm.getImg(), w, w, (h/2)*w);
			} catch (ArrayIndexOutOfBoundsException ex) {
				warnings++;
			}

			if (warnings > 0)
				Core.printWarn("problems during RLE decoding of picture at offset "+
						ToolBox.hex(startOfs,8)+"\n");

			return bm;

		} catch (FileBufferException ex) {
			throw new CoreException (ex.getMessage());
		}
	}

	/**
	 * Read one frame from SUB file
	 * @param pic SubPicture object for this frame
	 * @param endOfs end offset
	 * @param buffer File Buffer to read from
	 * @throws CoreException
	 */
	void readSubFrame(final SubPictureDVD pic, final long endOfs, final FileBuffer buffer) throws CoreException  {
		long ofs = pic.offset;
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
				final long startOfs = ofs;
				if (buffer.getDWord(ofs) != 0x000001ba)
					throw new CoreException("Missing packet identifier at ofs "+ToolBox.hex(ofs,8));
				// 6 bytes:  system clock reference
				// 3 bytes:  multiplexer rate
				// 1 byte:   stuffing info
				int stuffOfs = buffer.getByte(ofs+=13) & 7;
				// 4 bytes:  sub packet ID 0x000001bd
				if (buffer.getDWord(ofs += (1+stuffOfs)) != 0x000001bd)
					throw new CoreException("Missing packet identifier at ofs "+ToolBox.hex(ofs,8));
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
				int packetStreamID = buffer.getByte(ofs++) & 0xf;
				if (packetStreamID != streamID) {
					// packet doesn't belong to stream -> skip
					if (nextOfs % 0x800 != 0) {
						ofs = (nextOfs/0x800 + 1)*0x800;
						Core.printWarn("Offset to next fragment is invalid. Fixed to:"+ToolBox.hex(ofs, 8)+"\n");
					} else
						ofs = nextOfs;
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
					if (ctrlSize < 0)
						throw new CoreException("Invalid control buffer size");
					ctrlHeader = new byte[ctrlSize];
					ctrlOfs = ctrlOfsRel + ofs; // might have to be corrected for multiple packets
					ofs += 2;
					headerSize = (int)(ofs-startOfs);
					pic.rleFragments = new ArrayList<ImageObjectFragment>();
					firstPackFound = true;
				} else {
					if (firstPackFound)
						ctrlOfs += headerSize; // fix absolute offset by adding header bytes
					else
						Core.printWarn("Invalid fragment skipped at ofs "+ToolBox.hex(startOfs, 8)+"\n");
				}

				// check if control header is (partly) in this packet
				int diff = (int)(nextOfs - ctrlOfs - ctrlHeaderCopied);
				if (diff<0)
					diff = 0;
				int copied = ctrlHeaderCopied;
				try {
					for (int i=0; (i < diff) && (ctrlHeaderCopied<ctrlSize); i++) {
						ctrlHeader[ctrlHeaderCopied] = (byte)buffer.getByte(ctrlOfs+i+copied);
						ctrlHeaderCopied++;
					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					throw new CoreException("Inconsistent control buffer access ("+ex.getMessage()+")");
				}
				rleFrag = new ImageObjectFragment();
				rleFrag.imageBufferOfs = ofs;
				rleFrag.imagePacketSize = (length - headerSize - diff + packHeaderSize);
				pic.rleFragments.add(rleFrag);

				rleBufferFound += rleFrag.imagePacketSize;

				if (ctrlHeaderCopied != ctrlSize && (nextOfs % 0x800 != 0)) {
					ofs = (nextOfs/0x800 + 1)*0x800;
					Core.printWarn("Offset to next fragment is invalid. Fixed to:"+ToolBox.hex(ofs, 8)+"\n");
					rleBufferFound += ofs-nextOfs;
				} else
					ofs = nextOfs;
			} while ( ofs < endOfs && ctrlHeaderCopied < ctrlSize);

			if (ctrlHeaderCopied != ctrlSize) {
				Core.printWarn("Control buffer size inconsistent.\n");
				// fill rest of buffer with break command to avoid wrong detection of forced caption (0x00)
				for (int i=ctrlHeaderCopied; i<ctrlSize; i++)
					ctrlHeader[i] = (byte)0xff;
			}

			if (rleBufferFound != rleSize)
				Core.printWarn("RLE buffer size inconsistent.\n");

			pic.rleSize = rleBufferFound;

		} catch (FileBufferException ex) {
			throw new CoreException(ex.getMessage());
		}

		pic.pal = new int[4];
		pic.alpha = new int[4];
		int alphaSum = 0;
		int alphaUpdate[] = new int[4];
		int alphaUpdateSum;
		int delay = -1;
		boolean ColAlphaUpdate = false;

		Core.print("SP_DCSQT at ofs: "+ToolBox.hex(ctrlOfs,8)+"\n");

		try {
			// parse control header
			int b;
			int index = 0;
			int endSeqOfs = ToolBox.getWord(ctrlHeader, index)-ctrlOfsRel-2;
			if (endSeqOfs < 0 || endSeqOfs > ctrlSize) {
				Core.printWarn("Invalid end sequence offset -> no end time\n");
				endSeqOfs = ctrlSize;
			}
			index += 2;
			parse_ctrl:
			while (index < endSeqOfs) {
				int cmd = ToolBox.getByte(ctrlHeader, index++);
				switch (cmd) {
					case 0: // forced (?)
						pic.isforced = true;
						numForcedFrames++;
						break;
					case 1: // start display
						break;
					case 3: // palette info
						b = ToolBox.getByte(ctrlHeader, index++);
						pic.pal[3] = (b >> 4);
						pic.pal[2] = b & 0x0f;
						b = ToolBox.getByte(ctrlHeader, index++);
						pic.pal[1] = (b >> 4);
						pic.pal[0] = b & 0x0f;
						Core.print("Palette:   "+pic.pal[0]+", "+pic.pal[1]+", "+pic.pal[2]+", "+pic.pal[3]+"\n");
						break;
					case 4: // alpha info
						b = ToolBox.getByte(ctrlHeader, index++);
						pic.alpha[3] = (b >> 4);
						pic.alpha[2] = b & 0x0f;
						b = ToolBox.getByte(ctrlHeader, index++);
						pic.alpha[1] = (b >> 4);
						pic.alpha[0] = b & 0x0f;
						for (int i = 0; i<4; i++)
							alphaSum += pic.alpha[i] & 0xff;
						Core.print("Alpha:     "+pic.alpha[0]+", "+pic.alpha[1]+", "+pic.alpha[2]+", "+pic.alpha[3]+"\n");
						break;
					case 5: // coordinates
						int xOfs = (ToolBox.getByte(ctrlHeader, index)<<4) | (ToolBox.getByte(ctrlHeader, index+1)>>4);
						pic.setOfsX(ofsXglob+xOfs);
						pic.setImageWidth((((ToolBox.getByte(ctrlHeader, index+1)&0xf)<<8) | (ToolBox.getByte(ctrlHeader, index+2))) - xOfs + 1);
						int yOfs = (ToolBox.getByte(ctrlHeader, index+3)<<4) | (ToolBox.getByte(ctrlHeader, index+4)>>4);
						pic.setOfsY(ofsYglob+yOfs);
						pic.setImageHeight((((ToolBox.getByte(ctrlHeader, index+4)&0xf)<<8) | (ToolBox.getByte(ctrlHeader, index+5))) - yOfs + 1);
						Core.print("Area info:"+" ("
								+pic.getOfsX()+", "+pic.getOfsY()+") - ("+(pic.getOfsX()+pic.getImageWidth()-1)+", "
								+(pic.getOfsY()+pic.getImageHeight()-1)+")\n");
						index += 6;
						break;
					case 6: // offset to RLE buffer
						pic.evenOfs = ToolBox.getWord(ctrlHeader, index) - 4;
						pic.oddOfs  = ToolBox.getWord(ctrlHeader, index+2) - 4;
						index += 4;
						Core.print("RLE ofs:   "+ToolBox.hex(pic.evenOfs, 4)+", "+ToolBox.hex(pic.oddOfs, 4)+"\n");
						break;
					case 7: // color/alpha update
						ColAlphaUpdate = true;
						//int len = ToolBox.getWord(ctrlHeader, index);
						// ignore the details for now, but just get alpha and palette info
						alphaUpdateSum = 0;
						b = ToolBox.getByte(ctrlHeader, index+10);
						alphaUpdate[3] = (b >> 4);
						alphaUpdate[2] = b & 0x0f;
						b = ToolBox.getByte(ctrlHeader, index+11);
						alphaUpdate[1] = (b >> 4);
						alphaUpdate[0] = b & 0x0f;
						for (int i = 0; i<4; i++)
							alphaUpdateSum += alphaUpdate[i] & 0xff;
						// only use more opaque colors
						if (alphaUpdateSum > alphaSum) {
							alphaSum = alphaUpdateSum;
							for (int i = 0; i<4; i++)
								pic.alpha[i] = alphaUpdate[i];
							// take over frame palette
							b = ToolBox.getByte(ctrlHeader, index+8);
							pic.pal[3] = (b >> 4);
							pic.pal[2] = b & 0x0f;
							b = ToolBox.getByte(ctrlHeader, index+9);
							pic.pal[1] = (b >> 4);
							pic.pal[0] = b & 0x0f;
						}
						// search end sequence
						index = endSeqOfs;
						delay = ToolBox.getWord(ctrlHeader, index)*1024;
						endSeqOfs = ToolBox.getWord(ctrlHeader, index+2)-ctrlOfsRel-2;
						if (endSeqOfs < 0 || endSeqOfs > ctrlSize) {
							Core.printWarn("Invalid end sequence offset -> no end time\n");
							endSeqOfs = ctrlSize;
						}
						index += 4;
						break;
					case 0xff: // end sequence
						break parse_ctrl;
					default:
						Core.printWarn("Unknown control sequence "+ToolBox.hex(cmd,2)+" skipped\n");
						break;
				}
			}

			if (endSeqOfs != ctrlSize) {
				int ctrlSeqCount = 1;
				index = -1;
				int nextIndex = endSeqOfs;
				while (nextIndex != index) {
					index = nextIndex;
					delay = ToolBox.getWord(ctrlHeader, index) * 1024;
					nextIndex = ToolBox.getWord(ctrlHeader, index + 2) - ctrlOfsRel - 2;
					ctrlSeqCount++;
				}
				if (ctrlSeqCount > 2) {
					Core.printWarn("Control sequence(s) ignored - result may be erratic.");
				}
				pic.endTime = pic.startTime + delay;
			} else {
				pic.endTime = pic.startTime;
			}

			if (ColAlphaUpdate)
				Core.printWarn("Palette update/alpha fading detected - result may be erratic.\n");

			if (alphaSum == 0) {
				if (Core.getFixZeroAlpha()) {
					for (int i=0; i<4; i++)
						pic.alpha[i] = lastAlpha[i];
					Core.printWarn("Invisible caption due to zero alpha - used alpha info of last caption.\n");
				} else
					Core.printWarn("Invisible caption due to zero alpha (not fixed due to user setting).\n");
			}

			lastAlpha = pic.alpha;

			pic.setOriginal();

		} catch (IndexOutOfBoundsException ex) {
			throw new CoreException("Index "+ex.getMessage()+" out of bounds in control header.");
		}
	}

	/**
	 * Create VobSub IDX file
	 * @param fname file name
	 * @param pic a SubPicture object used to read screen width and height
	 * @param offsets array of offsets (one for each caption)
	 * @param timestamps array of PTS time stamps (one for each caption)
	 * @param pal 16 color main Palette
	 * @throws CoreException
	 */
	static void writeIdx(final String fname, final SubPicture pic, final int offsets[], final int timestamps[], final Palette pal) throws CoreException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(fname));

			out.write("# VobSub index file, v7 (do not modify this line!)"); out.newLine();
			out.write("# Created by "+Core.progNameVer); out.newLine();
			out.newLine();
			out.write("# Frame size"); out.newLine();
			out.write("size: "+pic.width+"x"+(pic.height-2*Core.getCropOfsY())); out.newLine();
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
			for (int i=0; i<pal.getSize(); i++) {
				int rbg[] = pal.getRGB(i);
				int val = (rbg[0]<<16) | (rbg[1]<<8) | rbg[2];
				out.write(ToolBox.hex(val, 6).substring(2));
				if (i != pal.getSize()-1)
					out.write(", ");
			}
			out.newLine();out.newLine();
			out.write("# Custom colors (transp idxs and the four colors)"); out.newLine();
			out.write("custom colors: OFF, tridx: 1000, colors: 000000, 444444, 888888, cccccc"); out.newLine();
			out.newLine();
			out.write("# Language index in use"); out.newLine();
			out.write("langidx: 0"); out.newLine();
			out.newLine();
			out.write("# "+Core.getLanguages()[Core.getLanguageIdx()][0]); out.newLine();
			out.write("id: "+Core.getLanguages()[Core.getLanguageIdx()][1]+", index: 0"); out.newLine();
			out.write("# Decomment next line to activate alternative name in DirectVobSub / Windows Media Player 6.x"); out.newLine();
			out.write("# alt: "+Core.getLanguages()[Core.getLanguageIdx()][0]); out.newLine();
			out.write("# Vob/Cell ID: 1, 1 (PTS: 0)"); out.newLine();
			for (int i=0; i<timestamps.length; i++) {
				out.write("timestamp: "+ToolBox.ptsToTimeStrIdx(timestamps[i]));
				out.write(", filepos: "+ToolBox.hex(offsets[i], 9).substring(2));
				out.newLine();
			}
		} catch (IOException ex) {
			throw new CoreException(ex.getMessage());
		}
		finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException ex) {};
		}
	}

	/**
	 * decode given picture
	 * @param pic SubPicture object containing info about caption
	 * @throws CoreException
	 */
	private void decode(final SubPictureDVD pic)  throws CoreException {
		palette = decodePalette(pic, srcPalette);
		bitmap  = decodeImage(pic, buffer, palette.getTransparentIndex());

		// crop
		BitmapBounds bounds = bitmap.getBounds(palette, Core.getAlphaCrop());
		if (bounds.yMin>0 || bounds.xMin > 0 || bounds.xMax<bitmap.getWidth()-1 || bounds.yMax<bitmap.getHeight()-1) {
			int w = bounds.xMax - bounds.xMin + 1;
			int h = bounds.yMax - bounds.yMin + 1;
			if (w<2)
				w = 2;
			if (h<2)
				h = 2;
			bitmap = bitmap.crop(bounds.xMin, bounds.yMin, w, h);
			// update picture
			pic.setImageWidth(w);
			pic.setImageHeight(h);
			pic.setOfsX(pic.originalX + bounds.xMin);
			pic.setOfsY(pic.originalY + bounds.yMin);
		}

		primaryColorIndex = bitmap.getPrimaryColorIndex(palette, Core.getAlphaThr());
	}

	/* (non-Javadoc)
	 * @see Substream#decode(int)
	 */
	public void decode(final int index) throws CoreException {
		if (index < subPictures.size())
			decode(subPictures.get(index));
		else
			throw new CoreException("Index "+index+" out of bounds\n");
	}

	/* setters / getters */

	/**
	 * Return frame palette
	 * @param index index of caption
	 * @return int array with 4 entries representing frame palette
	 */
	public int[] getFramePal(final int index) {
		return subPictures.get(index).pal;
	}

	/**
	 * Return original frame palette
	 * @param index index of caption
	 * @return int array with 4 entries representing frame palette
	 */
	public int[] getOriginalFramePal(final int index) {
		return subPictures.get(index).originalPal;
	}

	/**
	 * Return frame alpha
	 * @param index index of caption
	 * @return int array with 4 entries representing frame alphas
	 */
	public int[] getFrameAlpha(final int index) {
		return subPictures.get(index).alpha;
	}

	/**
	 * Return original frame alpha
	 * @param index index of caption
	 * @return int array with 4 entries representing frame alphas
	 */
	public int[] getOriginalFrameAlpha(final int index) {
		return subPictures.get(index).originalAlpha;
	}

	/* (non-Javadoc)
	 * @see Substream#getImage(Bitmap)
	 */
	public BufferedImage getImage(final Bitmap bm) {
		return bm.getImage(palette);
	}

	/* (non-Javadoc)
	 * @see Substream#getPalette()
	 */
	public Palette getPalette() {
		return palette;
	}

	/* (non-Javadoc)
	 * @see Substream#getBitmap()
	 */
	public Bitmap getBitmap() {
		return bitmap;
	}

	/* (non-Javadoc)
	 * @see Substream#getImage()
	 */
	public BufferedImage getImage() {
		return bitmap.getImage(palette);
	}

	/* (non-Javadoc)
	 * @see Substream#getPrimaryColorIndex()
	 */
	public int getPrimaryColorIndex() {
		return primaryColorIndex;
	}

	/* (non-Javadoc)
	 * @see Substream#getSubPicture(int)
	 */
	public SubPicture getSubPicture(final int index) {
		return subPictures.get(index);
	}

	/* (non-Javadoc)
	 * @see Substream#getNumFrames()
	 */
	public int getNumFrames() {
		return subPictures.size();
	}

	/* (non-Javadoc)
	 * @see Substream#getNumForcedFrames()
	 */
	public int getNumForcedFrames() {
		return numForcedFrames;
	}

	/* (non-Javadoc)
	 * @see Substream#isForced(int)
	 */
	public boolean isForced(final int index) {
		return subPictures.get(index).isforced;
	}

	/* (non-Javadoc)
	 * @see Substream#close()
	 */
	public void close() {
		if (buffer!=null)
			buffer.close();
	}

	/* (non-Javadoc)
	 * @see Substream#getEndTime(int)
	 */
	public long getEndTime(final int index) {
		return subPictures.get(index).endTime;
	}

	/* (non-Javadoc)
	 * @see Substream#getStartTime(int)
	 */
	public long getStartTime(final int index) {
		return subPictures.get(index).startTime;
	}

	/* (non-Javadoc)
	 * @see Substream#getStartOffset(int)
	 */
	public long getStartOffset(final int index) {
		return subPictures.get(index).offset;
	}

	/**
	 * get language index read from Ids
	 * @return language as String
	 */
	public int getLanguageIdx() {
		return languageIdx;
	}

	/**
	 * get imported palette
	 * @return imported palette
	 */
	public Palette getSrcPalette() {
		return srcPalette;
	}

	/**
	 * set imported palette
	 * @param pal new palette
	 */
	public void setSrcPalette(Palette pal) {
		srcPalette = pal;
	}

}
