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
package bdsup2sub.bitmap;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

import static bdsup2sub.bitmap.ColorSpaceUtils.RGB2YCbCr;

/**
 * Palette class for mixed representation of RGB/YCbCr palettes with alpha information.
 */
public class Palette {
    /** Number of palette entries */
    private final int size;
    /** Byte buffer for RED info */
    private final byte[] r;
    /** Byte buffer for GREEN info */
    private final byte[] g;
    /** Byte buffer for BLUE info */
    private final byte[] b;
    /** Byte buffer for alpha info */
    private final byte[] a;
    /** Byte buffer for Y (luminance) info */
    private final byte[] y;
    /** Byte buffer for Cb (chrominance blue) info */
    private final byte[] cb;
    /** Byte buffer for Cr (chrominance red) info */
    private final byte[] cr;
    /** Use BT.601 color model instead of BT.709 */
    private final boolean useBT601;

    /**
     * Initializes palette with transparent black (RGBA: 0x00000000)
     * @param size Number of palette entries
     */
    public Palette(int size, boolean useBT601) {
        this.size = size;
        this.useBT601 = useBT601;
        r  = new byte[size];
        g  = new byte[size];
        b  = new byte[size];
        a  = new byte[size];
        y  = new byte[size];
        cb = new byte[size];
        cr = new byte[size];

        // set at least all alpha values to invisible
        int[] yCbCr = RGB2YCbCr(0, 0, 0, useBT601);
        for (int i=0; i < size; i++) {
            a[i]  = 0;
            r[i]  = 0;
            g[i]  = 0;
            b[i]  = 0;
            y[i]  = (byte)yCbCr[0];
            cb[i] = (byte)yCbCr[1];
            cr[i] = (byte)yCbCr[2];
        }
    }

    /**
     * Initializes a palette with transparent black (RGBA: 0x00000000)
     */
    public Palette(int size) {
        this(size, false);
    }

    public Palette(byte[] red, byte[] green, byte[] blue, byte[] alpha, boolean useBT601) {
        size = red.length;
        this.useBT601 = useBT601;
        r  = new byte[size];
        g  = new byte[size];
        b  = new byte[size];
        a  = new byte[size];
        y  = new byte[size];
        cb = new byte[size];
        cr = new byte[size];

        int yCbCr[];
        for (int i=0; i<size; i++) {
            a[i]  = alpha[i];
            r[i]  = red[i];
            g[i]  = green[i];
            b[i]  = blue[i];
            yCbCr = RGB2YCbCr(r[i]&0xff, g[i]&0xff, b[i]&0xff, useBT601);
            y[i]  = (byte)yCbCr[0];
            cb[i] = (byte)yCbCr[1];
            cr[i] = (byte)yCbCr[2];
        }
    }

    /**
     * Constructs a palette from red, green blue and alpha buffers using BT.709
     */
    public Palette(byte[] red, byte[] green, byte[] blue, byte[] alpha) {
        this(red, green, blue, alpha, false);
    }

    public Palette(Palette palette) {
        size = palette.getSize();
        useBT601 = palette.usesBT601();
        r  = new byte[size];
        g  = new byte[size];
        b  = new byte[size];
        a  = new byte[size];
        y  = new byte[size];
        cb = new byte[size];
        cr = new byte[size];

        for (int i = 0; i < size; i++) {
            a[i]  = palette.a[i];
            r[i]  = palette.r[i];
            g[i]  = palette.g[i];
            b[i]  = palette.b[i];
            y[i]  = palette.y[i];
            cb[i] = palette.cb[i];
            cr[i] = palette.cr[i];
        }
    }

    public ColorModel getColorModel() {
        return new IndexColorModel(8, size, r, g, b, a);
    }

    public void setColor(int index, Color c) {
        setRGB(index, c.getRed(), c.getGreen(), c.getBlue());
        setAlpha(index, c.getAlpha());
    }

    public void setARGB(int index, int c) {
        setColor(index, new Color(c, true));
    }

    public Color getColor(int index) {
        return new Color(r[index] & 0xff, g[index] & 0xff, b[index] & 0xff, a[index] & 0xff);
    }

    public int getARGB(int index) {
        return ((a[index] & 0xff) << 24) | ((r[index] & 0xff) << 16) | (( g[index] & 0xff) << 8) | (b[index] & 0xff) ;
    }

    public void setRGB(int index, int red, int green, int blue) {
        r[index] = (byte)red;
        g[index] = (byte)green;
        b[index] = (byte)blue;

        int yCbCr[] = RGB2YCbCr(red, green, blue, useBT601);
        y[index]  = (byte)yCbCr[0];
        cb[index] = (byte)yCbCr[1];
        cr[index] = (byte)yCbCr[2];
    }

    public void setYCbCr(int index, int yn, int cbn, int crn) {
        y[index]  = (byte)yn;
        cb[index] = (byte)cbn;
        cr[index] = (byte)crn;

        int rgb[] = ColorSpaceUtils.YCbCr2RGB(yn, cbn, crn, useBT601);
        r[index] = (byte)rgb[0];
        g[index] = (byte)rgb[1];
        b[index] = (byte)rgb[2];
    }

    public void setAlpha(int index, int alpha) {
        a[index] = (byte)alpha;
    }

    public int getAlpha(int index) {
        return a[index] & 0xff;
    }

    public byte[] getAlpha() {
        return Arrays.copyOf(a, a.length);
    }

    public int[] getRGB(int index) {
        int rgb[] = new int[3];
        rgb[0] = r[index] & 0xff;
        rgb[1] = g[index] & 0xff;
        rgb[2] = b[index] & 0xff;
        return rgb;
    }

    public int[] getYCbCr(final int index) {
        int yCbCr[] = new int[3];
        yCbCr[0] = y[index]  & 0xff;
        yCbCr[1] = cb[index] & 0xff;
        yCbCr[2] = cr[index] & 0xff;
        return yCbCr;
    }

    public byte[] getR() {
        return Arrays.copyOf(r, r.length);
    }

    public byte[] getG() {
        return Arrays.copyOf(g, g.length);
    }

    public byte[] getB() {
        return Arrays.copyOf(b, b.length);
    }

    public byte[] getY() {
        return Arrays.copyOf(y, y.length);
    }

    public byte[] getCb() {
        return Arrays.copyOf(cb, cb.length);
    }

    public byte[] getCr() {
        return Arrays.copyOf(cr, cr.length);
    }

    public int getSize() {
        return size;
    }

    public int getIndexOfMostTransparentPaletteEntry() {
        int transpIdx = 0;
        int minAlpha = 0x100;
        for (int i = 0; i < size; i++ ) {
            if ((a[i] & 0xff) < minAlpha) {
                minAlpha = a[i] & 0xff;
                transpIdx = i;
                if (minAlpha == 0) {
                    break;
                }
            }
        }
        return transpIdx;
    }

    public boolean usesBT601() {
        return useBT601;
    }
}
