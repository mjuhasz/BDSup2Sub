package bdsup2sub.core;

public enum PaletteMode {
    KEEP_EXISTING {
        @Override
        public String toString() {
            return "keep existing";
        }
    },
    CREATE_NEW {
        @Override
        public String toString() {
            return "create new";
        }
    },
    CREATE_DITHERED {
        @Override
        public String toString() {
            return "dithered";
        }
    },
}