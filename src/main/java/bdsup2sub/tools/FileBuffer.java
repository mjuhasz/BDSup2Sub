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
package bdsup2sub.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Very simple version of a read only memory mapped file used for parsing large packet based files.
 */
public class FileBuffer {

    /** Size of the buffer in memory */
    private static final int BUFFERSIZE = 1024*1024; /* 1MB */
    /** Buffer in memory */
    private byte buf[];
    /** File name of the input file */
    private String filename;
    /** File input stream of the input file */
    private FileInputStream fi;
    /** File channel of the input file */
    private FileChannel fc;
    /** Current offset in file = start of memory buffer */
    private long offset;
    /** Last valid offset that is stored in internal buffer */
    private long offsetEnd;
    /** Length of file */
    private long length;

    public FileBuffer(final String filename) throws FileBufferException {
        this.filename = filename;
        length = new File(filename).length();
        if (length < BUFFERSIZE) {
            buf = new byte[(int)length];
        } else {
            buf = new byte[BUFFERSIZE];
        }
        try {
            fi = new FileInputStream(filename);
            fc = fi.getChannel();
            offset = 0;
            readBuffer(offset);
        } catch (FileNotFoundException ex) {
            throw new FileBufferException("File '" + filename + "' not found");
        }
    }

    /**
     * Move offset, read file to memory buffer.
     * @param offset New file offset
     * @throws FileBufferException
     */
    private void readBuffer(long offset) throws FileBufferException {
        try {
            this.offset = offset;
            fc.position(offset);
            long l = length - offset;
            int numRead;
            if (l < 0) {
                throw new FileBufferException("Offset " + offset + " out of bounds for file " + filename);
            }
            if (l < buf.length) {
                numRead = fi.read(buf, 0, (int)l);
            } else {
                numRead = fi.read(buf, 0, buf.length);
            }
            offsetEnd = offset + numRead - 1; // points to last valid position
        } catch (IOException ex) {
            throw new FileBufferException("IO error at offset +" + offset + " of file '" + filename + "'");
        } catch (IllegalArgumentException ex) {
            throw new FileBufferException("IO error at offset +" + offset + " of file '" + filename + "'");
        }
    }

    /**
     * Read one byte from the buffer.
     * @param offset File offset
     * @return Byte read from the buffer
     * @throws FileBufferException
     */
    public int getByte(long offset) throws FileBufferException {
        if ((offset < this.offset) || (offset > offsetEnd)) {
            readBuffer(offset);
        }
        return buf[(int)(offset - this.offset)] & 0xff;
    }

    /**
     * Read one (big endian) 16bit word from the buffer.
     * @param offset File offset
     * @return Word read from the buffer
     * @throws FileBufferException
     */
    public int getWord(long offset) throws FileBufferException {
        if ((offset < this.offset) || ((offset+1) > offsetEnd)) {
            readBuffer(offset);
        }
        int idx = (int)(offset - this.offset);
        return (buf[idx+1] & 0xff) | ((buf[idx] & 0xff) << 8);
    }

    /**
     * Read one (little endian) 16bit word from the buffer.
     * @param offset File offset
     * @return Word read from the buffer
     * @throws FileBufferException
     */
    public int getWordLE(long offset) throws FileBufferException {
        if ((offset < this.offset) || ((offset + 1) > offsetEnd)) {
            readBuffer(offset);
        }
        int idx = (int)(offset - this.offset);
        return (buf[idx] & 0xff) | ((buf[idx+1] & 0xff) << 8);
    }

    /**
     * Read one (big endian) 32bit dword from the buffer.
     * @param offset File offset
     * @return Dword read from the buffer
     * @throws FileBufferException
     */
    public int getDWord(long offset) throws FileBufferException {
        if ((offset < this.offset) || ((offset+3) > offsetEnd)) {
            readBuffer(offset);
        }
        int idx = (int)(offset - this.offset);
        return  (buf[idx+3] & 0xff)      | ((buf[idx + 2] & 0xff) << 8)
            |  ((buf[idx+1] & 0xff) << 16) | ((buf[idx]   & 0xff) << 24);
    }

    /**
     * Read one (little endian) 32bit dword from the buffer.
     * @param offset File offset
     * @return Dword read from the buffer
     * @throws FileBufferException
     */
    public int getDWordLE(long offset) throws FileBufferException {
        if ((offset < this.offset) || ((offset + 3) > offsetEnd)) {
            readBuffer(offset);
        }
        int idx = (int)(offset - this.offset);
        return (buf[idx] & 0xff) | ((buf[idx + 1] & 0xff) << 8)
            | ((buf[idx + 2] & 0xff) << 16) | ((buf[idx + 3] & 0xff) << 24);
    }

    /**
     * Read multiple bytes from the buffer.
     * @param ofs	File offset
     * @param b		Buffer to store bytes (has to be allocated and large enough)
     * @param len	Number of bytes to read
     * @throws FileBufferException
     */
    public void getBytes(long ofs, byte b[], int len) throws FileBufferException {
        if ((ofs < offset) || ( (ofs + len - 1) > offsetEnd)) {
            readBuffer(ofs);
        }
        for (int i = 0; i < len; i++) {
            b[i] = buf[(int)(ofs - offset + i)];
        }
    }

    /**
     * Get size of input file.
     * @return Size of input file in bytes
     */
    public long getSize() {
        return length;
    }

    /**
     * Close file buffer (closes input file).
     */
    public void close() {
        try {
            if (fc != null) {
                fc.close();
            }
            if (fi != null) {
                fi.close();
            }
        } catch (IOException ex) {
        }
    }

    @Override
    public void finalize() throws Throwable {
        if (fc != null) {
            fc.close();
        }
        if (fi != null) {
            fi.close();
        }
        super.finalize();
    }
}
