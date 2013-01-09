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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bdsup2sub.utils.ToolBox.leftZeroPad;

public class TimeUtils {

    private static final int PTS_FREQUENCY_IN_KHZ = 90;
    private static final Pattern TIME_PATTERN = Pattern.compile( "(\\d+):(\\d+):(\\d+)[:\\.](\\d+)" );

    public static String ptsToTimeStr(long pts) {
        int time[] = ptsToTime(pts);
        return leftZeroPad(time[0], 2) + ":" + leftZeroPad(time[1], 2) + ":" + leftZeroPad(time[2], 2) + "." + leftZeroPad(time[3], 3);
    }

    public static String ptsToTimeStrIdx(long pts) {
        int time[] = ptsToTime(pts);
        return leftZeroPad(time[0], 2) + ":" + leftZeroPad(time[1], 2) + ":" + leftZeroPad(time[2], 2) + ":" + leftZeroPad(time[3], 3);
    }

    public static String ptsToTimeStrXml(long pts, double fps) {
        int time[] = ptsToTime(pts);
        return leftZeroPad(time[0], 2) + ":" + leftZeroPad(time[1], 2) + ":" + leftZeroPad(time[2], 2) + ":" + leftZeroPad((int)(fps*time[3] / 1000.0 + 0.5), 2);
    }

    private static int[] ptsToTime(long pts) {
        return msToTime((pts + 45) / PTS_FREQUENCY_IN_KHZ);
    }

    private static int[] msToTime(long ms) {
        int time[] = new int[4];
        time[0] = (int)(ms / (60*60*1000)); // hours
        ms -= time[0] * 60*60*1000;
        time[1] = (int)(ms / (60*1000)); // minutes
        ms -= time[1] * 60*1000;
        time[2] = (int)(ms / 1000); // seconds
        ms -= time[2] *1000;
        time[3] = (int)ms; // milliseconds
        return time;
    }

    public static long timeStrToPTS(String s) {
        Matcher m = TIME_PATTERN.matcher(s);
        if (m.matches()) {
            long hour = Integer.parseInt(m.group(1));
            long min = Integer.parseInt(m.group(2));
            long sec = Integer.parseInt(m.group(3));
            long ms  = Integer.parseInt(m.group(4));

            long temp = hour * 60;
            temp += min;
            temp *= 60;
            temp += sec;
            temp *= 1000;
            return (temp+ms) * PTS_FREQUENCY_IN_KHZ;
        } else {
            return -1;
        }
    }

    public static long timeStrXmlToPTS(String s, double fps) {
        Matcher m = TIME_PATTERN.matcher(s);
        if (m.matches()) {
            long hour    = Integer.parseInt(m.group(1));
            long min     = Integer.parseInt(m.group(2));
            long sec     = Integer.parseInt(m.group(3));
            long frames  = Integer.parseInt(m.group(4));

            long temp = hour * 60;
            temp += min;
            temp *= 60;
            temp += sec;
            temp *= 1000;
            return (temp + (int)(frames / fps * 1000.0 + 0.5)) * PTS_FREQUENCY_IN_KHZ;
        } else {
            return -1;
        }
    }
}
