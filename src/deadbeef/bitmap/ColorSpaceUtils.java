package deadbeef.bitmap;

public final class ColorSpaceUtils {

    private ColorSpaceUtils() {
    }

    /**
     * Convert YCBCr color info to RGB
     * @param y  8 bit luminance
     * @param cb 8 bit chrominance blue
     * @param cr 8 bit chrominance red
     * @return Integer array with red, blue, green component (in this order)
     */
    public static int[] YCbCr2RGB(int y, int cb, int cr, boolean useBT601) {
        int[] rgb = new int[3];
        double y1, r, g, b;

        y  -= 16;
        cb -= 128;
        cr -= 128;

        y1 = y * 1.164383562;
        if (useBT601) {
            /* BT.601 for YCbCr 16..235 -> RGB 0..255 (PC) */
            r  = y1 + cr * 1.596026317;
            g  = y1 - cr * 0.8129674985 - cb * 0.3917615979;
            b  = y1 + cb * 2.017232218;
        } else {
            /* BT.709 for YCbCr 16..235 -> RGB 0..255 (PC) */
            r  = y1 + cr * 1.792741071;
            g  = y1 - cr * 0.5329093286 - cb * 0.2132486143;
            b  = y1 + cb * 2.112401786;
        }
        rgb[0] = (int)(r  + 0.5);
        rgb[1] = (int)(g  + 0.5);
        rgb[2] = (int)(b  + 0.5);
        for (int i = 0; i < 3; i++) {
            if (rgb[i] < 0) {
                rgb[i] = 0;
            } else if (rgb[i] > 255) {
                rgb[i] = 255;
            }
        }
        return rgb;
    }

    /**
     * Convert RGB color info to YCBCr
     * @param r 8 bit red component
     * @param g 8 bit green component
     * @param b 8 bit blue component
     * @return Integer array with luminance (Y), chrominance blue (Cb), chrominance red (Cr) (in this order)
     */
    public static int[] RGB2YCbCr(int r, int g, int b, boolean useBT601) {
        int[] yCbCr = new int[3];
        double y,cb,cr;

        if (useBT601) {
            /* BT.601 for RGB 0..255 (PC) -> YCbCr 16..235 */
            y  =  r * 0.299 * 219 / 255    + g * 0.587 * 219 / 255    + b * 0.114 * 219 / 255;
            cb = -r * 0.168736 * 224 / 255 - g * 0.331264 * 224 / 255 + b * 0.5 * 224 / 255;
            cr =  r * 0.5 * 224 / 255      - g * 0.418688 * 224 / 255 - b * 0.081312 * 224 / 255;
        } else {
            /* BT.709 for RGB 0..255 (PC) -> YCbCr 16..235 */
            y  =  r * 0.2126 * 219 / 255          + g * 0.7152 * 219 / 255          + b * 0.0722 * 219 / 255;
            cb = -r * 0.2126 / 1.8556 * 224 / 255 - g * 0.7152 / 1.8556 * 224 / 255 + b * 0.5 * 224 / 255;
            cr =  r * 0.5 * 224 / 255		      - g * 0.7152 / 1.5748 * 224 / 255 - b * 0.0722 / 1.5748 * 224 / 255;
        }
        yCbCr[0] =  16 + (int)(y  + 0.5);
        yCbCr[1] = 128 + (int)(cb + 0.5);
        yCbCr[2] = 128 + (int)(cr + 0.5);
        for (int i = 0; i < 3; i++) {
            if (yCbCr[i] < 16) {
                yCbCr[i] = 16;
            } else {
                if (i == 0) {
                    if (yCbCr[i] > 235)
                        yCbCr[i] = 235;
                } else {
                    if (yCbCr[i] > 240)
                        yCbCr[i] = 240;
                }
            }
        }
        return yCbCr;
    }
}
