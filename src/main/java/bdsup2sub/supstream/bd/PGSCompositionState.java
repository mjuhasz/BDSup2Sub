package bdsup2sub.supstream.bd;

public enum PGSCompositionState {
    /** normal: doesn't have to be complete */
    NORMAL(0x00),
    /** acquisition point */
    ACQU_POINT(0x40),
    /** epoch start - clears the screen */
    EPOCH_START(0x80),
    /** epoch continue */
    EPOCH_CONTINUE(0xC0),
    /** unknown value */
    INVALID(-1);

    private final int type;

    private PGSCompositionState(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
