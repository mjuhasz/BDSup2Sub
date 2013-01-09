/*
 * Copyright 2013 Miklos Juhasz (mjuhasz)
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
package bdsup2sub.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteUtilsTest {

    @Test
    public void shouldReturnUnsignedByte() {
        byte[] arr = new byte[] {2, (byte)245};
        assertEquals(2, ByteUtils.getByte(arr, 0));
        assertEquals(245, ByteUtils.getByte(arr, 1));
    }

    @Test
    public void shouldSetUnsignedByte() {
        byte[] arr = new byte[2];
        ByteUtils.setByte(arr, 0, 2);
        assertEquals(arr[0], 2);

        ByteUtils.setByte(arr, 1, 245);
        assertEquals(arr[1], (byte)245);
    }

    @Test
    public void shouldReturnBigEndianWord() {
        byte[] arr = new byte[] {(byte)(256 >> 8), (byte)256};
        assertEquals(256, ByteUtils.getWord(arr, 0));
    }

    @Test
    public void shouldSetBigEndianWord() {
        byte[] arr = new byte[2];
        ByteUtils.setWord(arr, 0, 256);

        assertEquals(arr[0], 256 >> 8);
        assertEquals(arr[1], (byte)256);
    }

    @Test
    public void shouldSetBigEndianDWord() {
        byte[] arr = new byte[4];
        ByteUtils.setDWord(arr, 0, 131071);

        assertEquals(arr[0], (byte)(131071 >> 24));
        assertEquals(arr[1], (byte)(131071 >> 16));
        assertEquals(arr[2], (byte)(131071 >> 8));
        assertEquals(arr[3], (byte)(131071));
    }
}
