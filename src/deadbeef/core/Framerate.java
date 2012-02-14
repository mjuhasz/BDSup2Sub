package deadbeef.core;

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
