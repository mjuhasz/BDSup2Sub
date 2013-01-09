/*
 * Copyright 2013 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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

public final class ByteUtils {

    private ByteUtils() {
    }

    public static int getByte(byte[] buffer, int index) {
        return buffer[index] & 0xff;
    }

    public static void setByte(byte[] buffer, int index, int val) {
        buffer[index] = (byte)(val);
    }

    public static int getWord(byte[] buffer, int index) {
        return (buffer[index+1] & 0xff) | ((buffer[index] & 0xff) << 8);
    }

    public static void setWord(byte[] buffer, int index, int val) {
        buffer[index]     = (byte)(val >> 8);
        buffer[index + 1] = (byte)(val);
    }

    public static void setDWord(byte[] buffer, int index, int val) {
        buffer[index]     = (byte)(val >> 24);
        buffer[index + 1] = (byte)(val >> 16);
        buffer[index + 2] = (byte)(val >> 8);
        buffer[index + 3] = (byte)(val);
    }
}
