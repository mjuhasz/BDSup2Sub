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

public enum OutputMode {
    /** DVD SUB/IDX (VobSub) stream */
    VOBSUB {
        @Override
        public String toString() {
            return "SUB/IDX";
        }
    },
    /** DVD SUP/IFO stream */
    SUPIFO {
        @Override
        public String toString() {
            return "SUP/IFO";
        }
    },
    /** Blu-Ray SUP stream */
    BDSUP {
        @Override
        public String toString() {
            return "SUP(BD)";
        }
    },
    /** Sony BDN XML (+PNGs) */
    XML {
        @Override
        public String toString() {
            return "XML/PNG";
        }
    },
}
