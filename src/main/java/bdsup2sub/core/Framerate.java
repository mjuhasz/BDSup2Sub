/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
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
    FPS_23_976(24000.0/1001), //24p
    FPS_23_975(23.975),
    FPS_24(24.0),
    PAL(25.0),
    NTSC(30000.0/1001),
    PAL_I(50.0),
    NTSC_I(60000.0/1001);

    private double value;

    private Framerate(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
