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
package bdsup2sub.utils;

import bdsup2sub.core.Framerate;

public final class SubtitleUtils {

    private SubtitleUtils() {
    }

    public static double getFPS(String fps) {
        fps = fps.toLowerCase().trim();
        if (fps.equals("pal")  || fps.equals("25p") || fps.equals("25")) {
            return Framerate.PAL.getValue();
        }
        if (fps.equals("ntsc") || fps.equals("30p") || fps.equals("29.97") || fps.equals("29.970")) {
            return Framerate.NTSC.getValue();
        }
        if (fps.equals("24p")  || fps.equals("23.976")) {
            return Framerate.FPS_23_976.getValue();
        }
        if (fps.equals("23.975")) {
            return Framerate.FPS_23_975.getValue();
        }
        if (fps.equals("24")) {
            return Framerate.FPS_24.getValue();
        }
        if (fps.equals("50i")  || fps.equals("50")) {
            return Framerate.PAL_I.getValue();
        }
        if (fps.equals("60i")  || fps.equals("59.94")) {
            return Framerate.NTSC_I.getValue();
        }

        double d;
        try {
            d = Double.parseDouble(fps);
        } catch (NumberFormatException ex) {
            return -1.0;
        }
        if (Math.abs(d - Framerate.FPS_23_975.getValue()) < 0.001) {
            return Framerate.FPS_23_975.getValue();
        }
        if (Math.abs(d - Framerate.FPS_23_976.getValue()) < 0.001) {
            return Framerate.FPS_23_976.getValue();
        }
        if (Math.abs(d - Framerate.FPS_24.getValue()) < 0.001) {
            return Framerate.FPS_24.getValue();
        }
        if (Math.abs(d - Framerate.PAL.getValue()) < 0.001) {
            return Framerate.PAL.getValue();
        }
        if (Math.abs(d - Framerate.NTSC.getValue()) < 0.001) {
            return Framerate.NTSC.getValue();
        }
        if (Math.abs(d - Framerate.NTSC_I.getValue()) < 0.001) {
            return Framerate.NTSC_I.getValue();
        }
        if (Math.abs(d - Framerate.PAL_I.getValue()) < 0.001) {
            return Framerate.PAL_I.getValue();
        }
        return d;
    }

    /**
     * Synchronizes a time stamp in 90kHz resolution to the given frame rate.
     */
    public static long syncTimePTS(long timeStamp, double fps, double fpsTrg) {
        long retval;
        // correct time stamps to fit to frames
        if (fps == Framerate.NTSC.getValue() || fps == Framerate.PAL.getValue() || fps == Framerate.FPS_24.getValue()) {
            // NTSC: 90000/(30000/1001) = 3003
            // PAL:  90000/25 = 3600
            // 24Hz: 90000/24 = 3750
            int tpfi = (int)((90000+(fps/2))/fps); // target time per frame in 90kHz
            int tpfh = tpfi/2;
            retval = ((timeStamp + tpfh)/tpfi)*tpfi;
        } else if (fpsTrg == Framerate.FPS_23_976.getValue()) {
            // 90000/(24000/1001) = 3753.75 = 15015/4
            retval = ((((timeStamp + 1877)*4)/15015)*15015)/4;
        } else {
            double tpf = (90000/fpsTrg); // target time per frame in 90kHz
            retval = (long)((long)(timeStamp/tpf)*tpf+0.5);
        }
        return retval;
    }
}
