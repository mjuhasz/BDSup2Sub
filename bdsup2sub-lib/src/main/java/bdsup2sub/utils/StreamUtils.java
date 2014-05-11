/*
 * Copyright 2014 Miklos Juhasz (mjuhasz)
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

import bdsup2sub.core.StreamID;

public class StreamUtils {

    /**
     * Identifies a stream by examining the first two bytes.
     * @param id Byte array holding four bytes at minimum
     * @return StreamID
     */
    public static StreamID getStreamID(byte id[]) {
        StreamID sid;

        if (id[0]==0x50 && id[1]==0x47) {
            sid = StreamID.BDSUP;
        } else if (id[0]==0x53 && id[1]==0x50) {
            sid = StreamID.SUP;
        } else if (id[0]==0x00 && id[1]==0x00 && id[2]==0x01 && id[3]==(byte)0xba) {
            sid = StreamID.DVDSUB;
        } else if (id[0]==0x23 && id[1]==0x20 && id[2]==0x56 && id[3]==0x6f) {
            sid = StreamID.IDX;
        } else if (id[0]==0x3c && id[1]==0x3f && id[2]==0x78 && id[3]==0x6d) {
            sid = StreamID.XML;
        } else if (id[0]==0x44 && id[1]==0x56 && id[2]==0x44 && id[3]==0x56) {
            sid = StreamID.IFO;
        } else {
            sid = StreamID.UNKNOWN;
        }
        return sid;
    }
}
