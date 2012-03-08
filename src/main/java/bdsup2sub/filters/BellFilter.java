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
package bdsup2sub.filters;

public class BellFilter implements Filter {

    public float getRadius() {
        return 1.5f;
    }

    public float value(float value) {
        if (value < 0.0f)
            value = -value;

        if (value < 0.5f)
            return 0.75f - (value * value);
        if (value < 1.5f) {
            value = value - 1.5f;
            return 0.5f * (value * value);
        }
        return 0.0f;
    }
}
