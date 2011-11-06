package deadbeef.tools;

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
 * Simple helper class to read bitwise from a buffer.
 *
 * @author 0xdeadbeef
 */

public class BitStream {

	/** Data buffer */
	private final byte buffer[];
	/** Offset to current byte in buffer */
	private int byteOfs;
	/** Current byte from the buffer */
	private int b;
	/** Bits left in current byte b */
	private int bits;

	/**
	 * @param buffer Byte array to create bitstream for
	 */
	public BitStream(byte buffer[]) {
		this.buffer = buffer;
		byteOfs = 0;	// start with first byte
		b = buffer[0] & 0xff;
		bits = 8;		// 8 bits left in b
	}

	/**
	 * @return Number of bits left in buffer
	 */
	public int bitsLeft() {
		return 8 * (buffer.length - byteOfs) - (8 - bits);
	}

	/**
	 * Read n bits from buffer (n <= 32)
	 * @param n Number of bits to read from buffer (n<=32)
	 * @return Value containing the n bits (last bit read is LSB)
	 */
	public int readBits(int n) {
		int retval = 0;
		while (n>0) {
			// bit by bit
			retval <<= 1;
			if ((b & 0x80) == 0x80) {
				retval |= 1; // get next bit
			}
			b <<= 1;
			n--;
			if (--bits==0) {
				if (byteOfs<buffer.length-1) {
					b = buffer[++byteOfs] & 0xff;
					bits = 8;
				} else bits = 0;
			}
		}
		return retval;
	}

	/**
	 * Synchronize to next byte in data buffer (skip remaining 0-7 bits)
	 */
	public void syncToByte() {
		if (bits !=8) {
			if (byteOfs<buffer.length-1) {
				b = buffer[++byteOfs] & 0xff;
				bits = 8;
			} else {
				bits = 0;
			}
		}
	}
}
