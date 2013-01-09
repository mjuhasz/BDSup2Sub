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
package bdsup2sub.core;

public enum Framerate {
    FPS_23_976(24000.0/1001, 0x10), //24p
    FPS_23_975(23.975, 0x10),
    FPS_24(24.0, 0x20),
    PAL(25.0, 0x30),
    NTSC(30000.0/1001, 0x40),
    PAL_I(50.0,0x60),
    NTSC_I(60000.0/1001, 0x70);

    private final double value;
    private final int id;

    private Framerate(double value, int id) {
        this.value = value;
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public int getId() {
        return id;
    }

    public static double valueForId(int id) {
        for (Framerate framerate : Framerate.values()) {
            if (id == framerate.getId()) {
                return framerate.getValue();
            }
        }
        throw new IllegalArgumentException("Unsupported id: " + id);
    }

    public static int idForFramerate(double fps) {
        for (Framerate framerate : Framerate.values()) {
            if (fps == framerate.getValue()) {
                return framerate.getId();
            }
        }
        throw new IllegalArgumentException("Unsupported framerate: " + fps);
    }
}
