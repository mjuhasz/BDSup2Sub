package deadbeef.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

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
 * Very simple version of a read only memory mapped file used for parsing large packet based files.
 *
 * @author 0xdeadbeef
 */

public class FileBuffer {

	/** Size of the buffer in memory */
	private final static int BUFFERSIZE = 1024*1024; /* 1MB */
	/** Buffer in memory */
	private final byte buf[];
	/** File name of the input file */
	private final String fileName;
	/** File input stream of the input file */
	private final FileInputStream fi;
	/** File channel of the input file */
	private final FileChannel fc;
	/** Current offset in file = start of memory buffer */
	private long offset;
	/** Last valid offset that is stored in internal buffer */
	private long offset_end;
	/** Length of file */
	private final long len;

	/**
	 * Constructor.
	 * @param fname File name of input file
	 * @throws FileBufferException
	 */
	public FileBuffer(final String fname) throws FileBufferException {
		try {
			fileName = fname;
			len = new File(fname).length();
			if (len<BUFFERSIZE)
				buf = new byte[(int)len];
			else
				buf = new byte[BUFFERSIZE];
			fi = new FileInputStream(fname);
			fc = fi.getChannel();
			offset = 0;
			readBuffer(offset);
		} catch (FileNotFoundException ex) {
			throw new FileBufferException("File '"+fname+"' not found");
		}
	}

	/**
	 * Move offset, read file to memory buffer.
	 * @param ofs New file offset
	 * @throws FileBufferException
	 */
	private void readBuffer(final long ofs) throws FileBufferException {
		try {
			offset = ofs;
			fc.position(offset);
			long l = len-offset;
			int numRead;
			if (l < 0)
				throw new FileBufferException("Offset "+ofs+" out of bounds for file "+fileName);
			if (l < buf.length)
				numRead = fi.read(buf, 0, (int)l);
			else
				numRead = fi.read(buf, 0, buf.length);
			offset_end = offset+numRead-1; // points to last valid position
		} catch (IOException ex) {
			throw new FileBufferException("IO error at offset +"+ofs+" of file '"+fileName+"'");
		} catch (IllegalArgumentException ex) {
			throw new FileBufferException("IO error at offset +"+ofs+" of file '"+fileName+"'");
		}
	}

	/**
	 * Read one byte from the buffer.
	 * @param ofs File offset
	 * @return Byte read from the buffer
	 * @throws FileBufferException
	 */
	public int getByte(final long ofs) throws FileBufferException {
		if ( (ofs < offset) || (ofs > offset_end) )
			readBuffer(ofs);
		return buf[(int)(ofs-offset)]&0xff;
	}

	/**
	 * Read one (big endian) 16bit word from the buffer.
	 * @param ofs File offset
	 * @return Word read from the buffer
	 * @throws FileBufferException
	 */
	public int getWord(final long ofs) throws FileBufferException {
		if ( (ofs < offset) || ( (ofs+1) > offset_end) )
			readBuffer(ofs);
		int idx = (int)(ofs-offset);
		return (buf[idx+1] & 0xff) | ((buf[idx] & 0xff)<<8);
	}

	/**
	 * Read one (little endian) 16bit word from the buffer.
	 * @param ofs File offset
	 * @return Word read from the buffer
	 * @throws FileBufferException
	 */
	public int getWordLE(final long ofs) throws FileBufferException {
		if ( (ofs < offset) || ( (ofs+1) > offset_end) )
			readBuffer(ofs);
		int idx = (int)(ofs-offset);
		return (buf[idx] & 0xff) | ((buf[idx+1] & 0xff)<<8);
	}

	/**
	 * Read one (big endian) 32bit dword from the buffer.
	 * @param ofs File offset
	 * @return Dword read from the buffer
	 * @throws FileBufferException
	 */
	public int getDWord(final long ofs) throws FileBufferException {
		if ( (ofs < offset) || ( (ofs+3) > offset_end) )
			readBuffer(ofs);
		int idx = (int)(ofs-offset);
		return  (buf[idx+3] & 0xff)      | ((buf[idx+2] & 0xff)<<8)
			|  ((buf[idx+1] & 0xff)<<16) | ((buf[idx]   & 0xff)<<24);
	}

	/**
	 * Read one (little endian) 32bit dword from the buffer.
	 * @param ofs File offset
	 * @return Dword read from the buffer
	 * @throws FileBufferException
	 */
	public int getDWordLE(final long ofs) throws FileBufferException {
		if ( (ofs < offset) || ( (ofs+3) > offset_end) )
			readBuffer(ofs);
		int idx = (int)(ofs-offset);
		return  (buf[idx] & 0xff)        | ((buf[idx+1] & 0xff)<<8)
			|  ((buf[idx+2] & 0xff)<<16) | ((buf[idx+3] & 0xff)<<24);
	}

	/**
	 * Read multiple bytes from the buffer.
	 * @param ofs	File offset
	 * @param b		Buffer to store bytes (has to be allocated and large enough)
	 * @param len	Number of bytes to read
	 * @throws FileBufferException
	 */
	public void getBytes(final long ofs, final byte b[], final int len) throws FileBufferException {
		if ( (ofs < offset) || ( (ofs+len-1) > offset_end) )
			readBuffer(ofs);
		for (int i= 0; i<len; i++)
			b[i] = buf[(int)(ofs-offset+i)];
	}

	/**
	 * Get size of input file.
	 * @return Size of input file in bytes
	 */
	public long getSize() {
		return len;
	}

	/**
	 * Close file buffer (closes input file).
	 */
	public void close() {
		try {
			if (fc != null)
				fc.close();
			if (fi != null)
				fi.close();
		} catch (IOException ex) {}
	}

	@Override
	public void finalize() throws Throwable {
		if (fc != null)
			fc.close();
		if (fi != null)
			fi.close();
		super.finalize();
	}
}
