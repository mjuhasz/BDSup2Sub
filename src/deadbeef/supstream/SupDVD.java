package deadbeef.supstream;


import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import deadbeef.bitmap.Bitmap;
import deadbeef.bitmap.BitmapBounds;
import deadbeef.bitmap.Palette;
import deadbeef.core.Core;
import deadbeef.core.CoreException;
import deadbeef.tools.FileBuffer;
import deadbeef.tools.FileBufferException;
import deadbeef.tools.ToolBox;

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
 * Handling of DVD SUP/IFO streams.
 *
 * @author 0xdeadbeef, mjuhasz
 */
public class SupDVD implements Substream, SubstreamDVD {

	/** ArrayList of captions contained in the current file  */
	private final ArrayList<SubPictureDVD> subPictures;
	/** color palette read from ifo file  */
	private Palette srcPalette;
	/** color palette created for last decoded caption  */
	private Palette palette;
	/** bitmap of the last decoded caption  */
	private Bitmap bitmap;
	/** screen width of imported VobSub  */
	private int screenWidth;
	/** screen height of imported VobSub  */
	private int screenHeight;
	/** global x offset  */
	private int ofsXglob;
	/** global y offset  */
	private int ofsYglob;
	/** global delay  */
	private int delayGlob;
	/** index of language read from IFO */
	private int languageIdx;
	/** FileBuffer for reading SUP */
	private final FileBuffer buffer;
	/** index of dominant color for the current caption  */
	private int primaryColorIndex;
	/** number of forced captions in the current file  */
	private int numForcedFrames;
	/** store last alpha values for invisible workaround */
	private static int lastAlpha[] = {0,0xf,0xf,0xf};
	/* the string "DVDVIDEO-VTS" as byte representation */
	private static byte IFOheader[] = { 0x44, 0x56, 0x44, 0x56, 0x49, 0x44, 0x45, 0x4F, 0x2D, 0x56, 0x54, 0x53 };

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


	/**
	 * Constructor
	 * @param fnSup file name of SUP file
	 * @param fnIfo file name of IFO file
	 * @throws CoreException
	 */
	public SupDVD(final String fnSup, final String fnIfo) throws CoreException {
		screenWidth = 720;
		screenHeight = 576;
		ofsXglob = 0;
		ofsYglob = 0;
		delayGlob = 0;
		languageIdx = 0;

		srcPalette = new Palette (Core.getDefaultDVDPalette());
		subPictures = new ArrayList<SubPictureDVD>();

		readIFO(fnIfo);

		try {
			long ofs = 0;
			buffer = new FileBuffer(fnSup);
			long size = buffer.getSize();
			Core.setProgressMax((int)size);
			int i = 0;
			do {
				Core.printX("# "+(++i)+"\n");
				Core.setProgress((int)ofs);
				Core.print("Ofs: "+ToolBox.hex(ofs,8)+"\n");
				ofs = readSupFrame(ofs, buffer);
			} while (ofs < size);

		} catch (FileBufferException e) {
			throw new CoreException(e.getMessage());
		}

		Core.printX("\nDetected "+numForcedFrames+" forced captions.\n");
	}

	/**
	 * Create the binary stream representation of one caption
	 * @param pic SubPicture object containing caption info
	 * @param bm bitmap
	 * @return byte buffer containing the binary stream representation of one caption
	 */
	public static byte[] createSupFrame(final SubPictureDVD pic, final Bitmap bm) {
		/* create RLE buffers */
		final byte even[] = SubDVD.encodeLines(bm, true);
		final byte odd[]  = SubDVD.encodeLines(bm, false);
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
		final int sizeRLE = even.length+ odd.length;
		final int bufSize = 10 + 4 + controlHeaderLen + sizeRLE;
		final byte buf[] = new byte[bufSize];

		// write header
		buf[0] = 0x53;
		buf[1] = 0x50;
		// write PTS (4 bytes of 8 bytes used) - little endian!
		final int pts = (int)pic.startTime;
		buf[5] = (byte)(pts >> 24);
		buf[4] = (byte)(pts >> 16);
		buf[3] = (byte)(pts >> 8);
		buf[2] = (byte)pts;

		// write packet size
		tmp = controlHeaderLen + sizeRLE + 4; // 4 for the size and the offset
		buf[10] = (byte)(tmp >> 8);
		buf[11] = (byte)(tmp);

		// write offset to control header +
		tmp = sizeRLE + 2; // 2 for the offset
		buf[12] = (byte)(tmp >> 8);
		buf[13] = (byte)(tmp);

		// copy rle buffers
		int ofs = 14;
		for (int i=0; i<even.length; i++)
			buf[ofs++] = even[i];
		for (int i=0; i<odd.length; i++)
			buf[ofs++] = odd[i];

		/* create control header */
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
		tmp = sizeRLE + 22 + (pic.isforced?1:0) + 4;
		controlHeader[forcedOfs+0] = (byte)((tmp >> 8)&0xff);
		controlHeader[forcedOfs+1] = (byte)(tmp&0xff);
		controlHeader[1+24] = (byte)((tmp >> 8)&0xff);
		controlHeader[1+25] = (byte)(tmp&0xff);

		// write control header
		for (int i=0; i<controlHeaderLen; i++)
			buf[ofs++] = controlHeader[forcedOfs+i];

		return buf;
	}

	/**
	 * Read one frame from SUP file
	 * @param ofs offset of current frame
	 * @param buffer File Buffer to read from
	 * @return offset to next frame
	 * @throws CoreException
	 */
	long readSupFrame(long ofs, final FileBuffer buffer) throws CoreException  {
		long ctrlOfs = -1;
		int  ctrlOfsRel = 0;
		int  rleSize = 0;
		int  ctrlSize = -1;
		ImageObjectFragment rleFrag;
		int  length;
		byte ctrlHeader[] = null;

		try {
			// 2 bytes:  packet identifier 0x5350
			long startOfs = ofs;
			if (buffer.getWord(ofs) != 0x5350)
				throw new CoreException("Missing packet identifier at ofs "+ToolBox.hex(ofs,8));
			// 8 bytes PTS:  system clock reference, but use only the first 4
			SubPictureDVD pic = new SubPictureDVD();
			pic.offset = ofs;
			pic.width = screenWidth;
			pic.height = screenHeight;

			int pts = buffer.getDWordLE(ofs+=2);
			pic.startTime = pts + delayGlob;
			// 2 bytes:  packet length (number of bytes after this entry)
			length = buffer.getWord(ofs+=8);
			// 2 bytes: offset to control buffer
			ctrlOfsRel = buffer.getWord(ofs+=2);
			rleSize = ctrlOfsRel-2;				// calculate size of RLE buffer
			ctrlSize = length-ctrlOfsRel-2;		// calculate size of control header
			if (ctrlSize < 0)
				throw new CoreException("Invalid control buffer size");
			ctrlOfs = ctrlOfsRel + ofs;			// absolute offset of control header
			ofs += 2;
			pic.rleFragments = new ArrayList<ImageObjectFragment>(1);
			rleFrag = new ImageObjectFragment();
			rleFrag.imageBufferOfs = ofs;
			rleFrag.imagePacketSize = rleSize;
			pic.rleFragments.add(rleFrag);
			pic.rleSize = rleSize;

			pic.pal = new int[4];
			pic.alpha = new int[4];
			int alphaSum = 0;
			int alphaUpdate[] = new int[4];
			int alphaUpdateSum;
			int delay = -1;
			boolean ColAlphaUpdate = false;

			Core.print("SP_DCSQT at ofs: "+ToolBox.hex(ctrlOfs,8)+"\n");

			// copy control header in buffer (to be more compatible with VobSub)
			ctrlHeader = new byte[ctrlSize];
			for (int i=0; i < ctrlSize; i++)
				ctrlHeader[i] = (byte)buffer.getByte(ctrlOfs+i);

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

				pic.setOriginal();

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

			} catch (IndexOutOfBoundsException ex) {
				throw new CoreException("Index "+ex.getMessage()+" out of bounds in control header.");
			}

			subPictures.add(pic);
			return startOfs+length+0x0a;

		} catch (FileBufferException ex) {
			throw new CoreException(ex.getMessage());
		}

	}

	/**
	 * Create IFO file
	 * @param fname file name
	 * @param pic a SubPicture object used to read screen width and height
	 * @param pal 16 color main Palette
	 * @throws CoreException
	 */
	public static void writeIFO(final String fname, final SubPicture pic, final Palette pal) throws CoreException {
		final byte buf[] = new byte[0x1800];
		int index = 0;

		// video attributes
		int vidAttr;
		if (pic.height == 480)
			vidAttr = 0x4f01; // NTSC
		else
			vidAttr = 0x5f01; // PAL

		// VTSI_MAT
		ToolBox.setString(buf, index, "DVDVIDEO-VTS");
		ToolBox.setDWord(buf, index+0x12, 0x00000004);	// last sector of title set
		ToolBox.setDWord(buf, index+0x1C, 0x00000004);	// last sector of IFO
		ToolBox.setDWord(buf, index+0x80, 0x000007ff);	// end byte address of VTS_MAT
		ToolBox.setDWord(buf, index+0xC8, 0x00000001);	// start sector of Title Vob (*2048 -> 0x0800) -> PTT_SRPTI
		ToolBox.setDWord(buf, index+0xCC, 0x00000002);	// start sector of Titles&Chapters table (*2048 -> 0x1000) -> VTS_PGCITI

		ToolBox.setWord(buf,  index+0x100, vidAttr);	// video attributes
		ToolBox.setWord(buf,  index+0x200, vidAttr);	// video attributes

		String l = Core.getLanguages()[Core.getLanguageIdx()][1];
		ToolBox.setWord(buf,  index+0x254, 1);			// number of subtitle streams
		ToolBox.setByte(buf,  index+0x256, 1);			// subtitle attributes
		ToolBox.setByte(buf,  index+0x258, (byte)l.charAt(0));
		ToolBox.setByte(buf,  index+0x259, (byte)l.charAt(1));

		// PTT_SRPTI
		index = 0x0800;
		ToolBox.setWord(buf,  index,      0x0001);		// Number of TTUs
		ToolBox.setWord(buf,  index+0x04, 0x000f);		// End byte of PTT_SRPT table
		ToolBox.setDWord(buf, index+0x04, 0x0000000C);	// TTU_1: starting byte
		ToolBox.setWord(buf,  index+0x0C, 0x0001);		// PTT_1: program chain number PGCN
		ToolBox.setWord(buf,  index+0x0e, 0x0001);		// PTT_1: program number PG

		// VTS_PGCITI/VTS_PTT_SRPT
		index = 0x1000;
		ToolBox.setWord(buf,  index,      0x0001);		// Number of VTS_PGCI_SRP (2 bytes, 2 bytes reserved)
		ToolBox.setDWord(buf, index+0x04, 0x00000119);	// end byte of VTS_PGCI_SRP table (281)
		ToolBox.setDWord(buf, index+0x08, 0x81000000);	// VTS_PGC_1_ category mask. entry PGC (0x80), title number 1 (0x01), Category 0,...
		ToolBox.setDWord(buf, index+0x0C, 0x00000010);	// VTS_PGCI start byte (16)

		// VTS_PGC_1
		index = 0x1010;
		ToolBox.setByte(buf,  index+0x02,  0x01);		// Number of Programs
		ToolBox.setByte(buf,  index+0x03,  0x01);		// Number of Cells
		for (int i=0; i<16; i++) {
			int ycbcr[] = pal.getYCbCr(i);
			ToolBox.setByte(buf, index+0xA4+4*i+1,  ycbcr[0]);
			ToolBox.setByte(buf, index+0xA4+4*i+2,  ycbcr[1]);
			ToolBox.setByte(buf, index+0xA4+4*i+3,  ycbcr[2]);
		}

		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fname));
			out.write(buf);
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
	 * Read palette and size/frame info from IFO file
	 * @param fname file name
	 * @throws CoreException
	 */
	void readIFO(final String fname) throws CoreException {
		final FileBuffer buf;
		try {
			buf = new FileBuffer(fname);
			byte header[] = new byte[IFOheader.length];
			buf.getBytes(0, header, IFOheader.length);
			for (int i = 0; i<IFOheader.length; i++)
				if (header[i]!=IFOheader[i])
					throw new CoreException("Not a valid IFO file.");

			// video attributes
			int vidAttr = buf.getWord(0x200);
			if ( (vidAttr & 0x3000) != 0) {
				// PAL
				switch ((vidAttr>>3) & 3) {
					case 0:
						screenWidth = 720;
						screenHeight = 576;
						break;
					case 1:
						screenWidth = 704;
						screenHeight = 576;
						break;
					case 2:
						screenWidth = 352;
						screenHeight = 576;
						break;
					case 3:
						screenWidth = 352;
						screenHeight = 288;
						break;
				}
			} else {
				// NTSC
				switch ((vidAttr>>3) & 3) {
					case 0:
						screenWidth = 720;
						screenHeight = 480;
						break;
					case 1:
						screenWidth = 704;
						screenHeight = 480;
						break;
					case 2:
						screenWidth = 352;
						screenHeight = 480;
						break;
					case 3:
						screenWidth = 352;
						screenHeight = 240;
						break;
				}
			}
			Core.print("Resolution: "+screenWidth+"x"+screenHeight+"\n");

			// get start offset of Titles&Chapters table
			long VTS_PGCITI_ofs = buf.getDWord(0xCC) * 2048;

			// get language index of subtitle streams (ignore all but first language)
			if (buf.getWord(0x254) > 0 && buf.getByte(0x256) == 1) {
				StringBuffer langSB = new StringBuffer(2);
				boolean found = false;
				langSB.append((char)buf.getByte(0x258));
				langSB.append((char)buf.getByte(0x259));
				String lang = langSB.toString();
				for (int i=0; i<Core.getLanguages().length; i++)
					if (lang.equalsIgnoreCase(Core.getLanguages()[i][1])) {
						languageIdx = i;
						found = true;
						break;
					}
				if (!found)
					Core.printWarn("Illegal language id: "+lang+"\n");
				else
					Core.print("Set language to: "+lang+"\n");
			} else
				Core.printWarn("Missing language id.\n");

			// PTT_SRPTI
			VTS_PGCITI_ofs += buf.getDWord(VTS_PGCITI_ofs+0x0C);
			Core.print("Reading palette from offset: "+ToolBox.hex(VTS_PGCITI_ofs, 8)+"\n");

			// assume palette in VTS_PGC_1
			long index = VTS_PGCITI_ofs;
			for (int i=0; i<16; i++) {
				int y  = buf.getByte(index+0xA4+4*i+1) & 0xff;
				int cb = buf.getByte(index+0xA4+4*i+2) & 0xff;
				int cr = buf.getByte(index+0xA4+4*i+3) & 0xff;
				srcPalette.setYCbCr(i, y, cb, cr);
			}

		} catch (FileBufferException e) {
			throw new CoreException(e.getMessage());
		}

	}

	/**
	 * decode given picture
	 * @param pic SubPicture object containing info about caption
	 * @throws CoreException
	 */
	private void decode(final SubPictureDVD pic) throws CoreException {
		palette = SubDVD.decodePalette(pic, srcPalette);
		bitmap  = SubDVD.decodeImage(pic, buffer, palette.getIndexOfMostTransparentPaletteEntry());

		// crop
		final BitmapBounds bounds = bitmap.getCroppingBounds(palette.getAlpha(), Core.getAlphaCrop());
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

		primaryColorIndex = bitmap.getPrimaryColorIndex(palette.getAlpha(), Core.getAlphaThr(), palette.getY());
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
		return bm.getImage(palette.getColorModel());
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
		return bitmap.getImage(palette.getColorModel());
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
