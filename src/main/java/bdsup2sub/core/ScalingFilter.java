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

public enum ScalingFilter {
    BILINEAR {
        @Override
        public String toString() {
            return "Bilinear";
        }
    },
    TRIANGLE {
        @Override
        public String toString() {
            return "Triangle";
        }
    },
    BICUBIC {
        @Override
        public String toString() {
            return "Bicubic";
        }
    },
    BELL {
        @Override
        public String toString() {
            return "Bell";
        }
    },
    BICUBIC_SPLINE {
        @Override
        public String toString() {
            return "Bicubic-Spline";
        }
    },
    HERMITE {
        @Override
        public String toString() {
            return "Hermite";
        }
    },
    LANCZOS3 {
        @Override
        public String toString() {
            return "Lanczos3";
        }
    },
    MITCHELL {
        @Override
        public String toString() {
            return "Mitchell";
        }
    },
}
