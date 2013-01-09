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
package bdsup2sub.bitmap;

import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BitmapTest {

    private static final int WIDTH = 10;
    private static final int HEIGHT = 20;

    private Bitmap subject;

    @Before
    public void setUp() {
        subject = new Bitmap(WIDTH, HEIGHT);
    }

    @Test
    public void shouldCreateBitmapOfCorrectDimensions() {
        assertEquals(WIDTH, subject.getWidth());
        assertEquals(HEIGHT, subject.getHeight());
    }

    @Test
    public void shouldCreateBitmapInitializedWithZeros() {
        byte[] buffer = subject.getInternalBuffer();
        for (byte b : buffer) {
            assertEquals(0, b);
        }
    }

    @Test
    public void shouldCreateBitmapFilledWithSpecifiedColorIndex() {
        byte fillerColorIndex = 12;
        Bitmap bitmap = new Bitmap(WIDTH, HEIGHT, fillerColorIndex);
        byte[] buffer = bitmap.getInternalBuffer();
        for (byte b : buffer) {
            assertEquals(fillerColorIndex, b);
        }
    }

    @Test
    public void shouldCreateBitmapReusingExistingBuffer() {
        byte[] buffer = new byte[WIDTH * HEIGHT];
        Bitmap bitmap = new Bitmap(WIDTH, HEIGHT, buffer);
        assertSame(buffer, bitmap.getInternalBuffer());
    }

    @Test
    public void shouldCreateBitmapByCloningExistingOne() {
        Bitmap bitmap = new Bitmap(subject);
        assertNotSame(subject.getInternalBuffer(), bitmap.getInternalBuffer());
        assertTrue(Arrays.equals(subject.getInternalBuffer(), bitmap.getInternalBuffer()));
    }

    @Test
    public void shouldFillRectangularWithColorIndex() {
        int rectX = 2, rectY = 3;
        int rectWidth = 7, rectHeight = 16;
        byte colorIndex = 1;

        subject.fillRectangularWithColorIndex(rectX, rectY, rectWidth, rectHeight, colorIndex);

        byte[] buffer = subject.getInternalBuffer();
        int topLeftCorner = rectX + rectY * WIDTH;
        int bottomRightCorner = (rectX + rectWidth - 1) + (rectY + rectHeight - 1) * WIDTH;

        assertEquals(0, buffer[topLeftCorner - 1]);
        assertEquals(1, buffer[topLeftCorner]);
        assertEquals(1, buffer[bottomRightCorner]);
        assertEquals(0, buffer[bottomRightCorner + 1]);
    }

    @Test
    public void shouldCropRectangularIfBiggerThanImage() {
        int rectX = 2, rectY = 3;
        int rectWidth = 9, rectHeight = 18;
        byte colorIndex = 1;

        subject.fillRectangularWithColorIndex(rectX, rectY, rectWidth, rectHeight, colorIndex);

        byte[] buffer = subject.getInternalBuffer();
        int topLeftCorner = rectX + rectY * WIDTH;
        int bottomRightCorner = WIDTH * HEIGHT - 1;

        assertEquals(0, buffer[topLeftCorner - 1]);
        assertEquals(1, buffer[topLeftCorner]);
        assertEquals(1, buffer[bottomRightCorner]);
    }

    @Test
    public void shouldReturnBufferedImage() {
        int size = 16;
        byte[] r  = new byte[size];
        byte[] g  = new byte[size];
        byte[] b  = new byte[size];
        byte[] a  = new byte[size];
        Arrays.fill(r, (byte)0);
        Arrays.fill(g, (byte)0);
        Arrays.fill(b, (byte)0);
        Arrays.fill(a, (byte)0);

        ColorModel colorModel = new IndexColorModel(8, size, r, g, b, a);
        BufferedImage bufferedImage = subject.getImage(colorModel);

        assertNotNull(bufferedImage);
        assertEquals(WIDTH, bufferedImage.getWidth());
        assertEquals(HEIGHT, bufferedImage.getHeight());
    }

    @Test
    public void shouldReturnCroppingBounds() {
        int rectX = 2, rectY = 3;
        int rectWidth = 7, rectHeight = 16;
        byte colorIndex = 1;

        subject.fillRectangularWithColorIndex(rectX, rectY, rectWidth, rectHeight, colorIndex);

        byte[] alpha = new byte[] { 0, (byte)255, (byte)255, (byte)255 }; // 0: transparent, 255: opaque
        byte alphaThreshold = 14;

        BitmapBounds bounds = subject.getCroppingBounds(alpha, alphaThreshold);

        assertEquals(rectX, bounds.xMin);
        assertEquals(rectX + rectWidth - 1, bounds.xMax);
        assertEquals(rectY, bounds.yMin);
        assertEquals(rectY + rectHeight - 1, bounds.yMax);
    }

    @Test
    public void shouldReturnCroppedImage() {
        int rectX = 2, rectY = 3;
        int rectWidth = 7, rectHeight = 16;
        byte colorIndex = 1;

        subject.fillRectangularWithColorIndex(rectX, rectY, rectWidth, rectHeight, colorIndex);

        Bitmap croppedBitmap = subject.crop(rectX, rectY, rectWidth, rectHeight);

        assertNotSame(subject, croppedBitmap);
        assertEquals(rectHeight, croppedBitmap.getHeight());
        assertEquals(rectWidth, croppedBitmap.getWidth());

        byte[] buffer = croppedBitmap.getInternalBuffer();
        for (byte b : buffer) {
            assertEquals(colorIndex, b);
        }
    }

    @Test
    public void shouldReturnHighestVisibleColorIndex() {
        subject.fillRectangularWithColorIndex(4, 4, 2, 2, (byte)2);
        subject.fillRectangularWithColorIndex(0, 0, 2, 2, (byte)3);

        assertEquals(2, subject.getHighestVisibleColorIndex(new byte[] {0, 0, (byte)255, 0}));
    }

    @Test
    public void shouldReturnPrimaryColorIndex() {
        subject.fillRectangularWithColorIndex(4, 4, 2, 2, (byte)2); //purple: RGB(127,32,200), Y=79
        subject.fillRectangularWithColorIndex(0, 0, 2, 2, (byte)3); // yellow: RGB(247,232,10), Y=211

        assertEquals(3, subject.getPrimaryColorIndex(new byte[] {0, 0, (byte)255, (byte)200}, 80, new byte[] {0, 0, 79, (byte)211}));
    }

    @Test
    public void shouldReturnBitmapWithNormalizedPalette() {
        subject.fillRectangularWithColorIndex(0, 0, WIDTH, HEIGHT, (byte)3); // background: 3
        subject.fillRectangularWithColorIndex(0, 0, 2, 2, (byte)0); // pattern: 0
        subject.fillRectangularWithColorIndex(4, 4, 2, 2, (byte)1); // e1: 1
        subject.fillRectangularWithColorIndex(7, 7, 2, 2, (byte)2); // e2: 2

        Bitmap bitmap = subject.getBitmapWithNormalizedPalette(new byte[] {(byte)255, (byte)158, (byte)187, 0}, 80, new byte[] {(byte)230, (byte)200, (byte)150, 0}, new int[] { 210, 160 });

        assertNotSame(subject, bitmap);

        byte[] buffer = bitmap.getInternalBuffer();
        assertEquals(0, buffer[WIDTH * HEIGHT - 1]); // background color index remapped from 3 to 0
        assertEquals(1, buffer[0]); // pattern color index remapped 0 from 1
        assertEquals(2, buffer[4 * WIDTH + 4]); // e1 color index remapped 1 from 2
        assertEquals(3, buffer[7 * WIDTH + 7]); // e2 color index remapped 2 from 3
    }
}
