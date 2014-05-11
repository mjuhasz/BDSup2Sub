/*
 * Copyright 2009, Morten Nobel-Joergensen / Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bdsup2sub.bitmap;

import bdsup2sub.bitmap.Bitmap;
import bdsup2sub.bitmap.Palette;
import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;

/**
 * Subsampling scaling algorithm with various filters.
 *
 * <p>Based on the ResampleOp class from the <a href="http://code.google.com/p/java-image-scaling/">Java Image Scaling Library.</a>
 * by Morten Nobel-Joergensen which again is based on "Java Image Util".</p>
 *
 * @author Morten Nobel-Joergensen / 0xdeadbeef / mjuhasz
 */

class FilterOp {
    private int srcWidth;
    private int srcHeight;
    private final int dstWidth;
    private final int dstHeight;

    private byte r[];
    private byte g[];
    private byte b[];
    private byte a[];

    private SubSamplingData horizontalSubsamplingData;
    private SubSamplingData verticalSubsamplingData;
    private final ResampleFilter filter;

    public FilterOp(ResampleFilter filter, int dstWidth, int dstHeight) {
        this.filter = filter;
        this.dstWidth = dstWidth;
        this.dstHeight = dstHeight;
    }

    public int[] filter(Bitmap bitmap, Palette palette) {
        this.srcWidth  = bitmap.getWidth();
        this.srcHeight = bitmap.getHeight();

        r = palette.getR();
        g = palette.getG();
        b = palette.getB();
        a = palette.getAlpha();

        horizontalSubsamplingData = createSubSampling(srcWidth, dstWidth);
        verticalSubsamplingData = createSubSampling(srcHeight, dstHeight);

        int[] workPixels = new int[srcHeight * dstWidth];
        filterHorizontally(bitmap.getInternalBuffer(), workPixels);

        int[] outPixels = new int[dstHeight * dstWidth];
        filterVertically(workPixels, outPixels);

        return outPixels;
    }

    private SubSamplingData createSubSampling(int srcSize, int dstSize) {
        float scalingFactor = (float)(dstSize - 1) / (float)(srcSize - 1);
        int[] arrN = new int[dstSize];
        int numContributors;
        float[] arrWeight;
        int[] arrPixel;

        float fwidth = filter.getSamplingRadius();

        if (scalingFactor < 1.0f) {
            // scale down -> subsampling
            float width = fwidth / scalingFactor;
            numContributors= (int)(width * 2.0f + 2); // Heinz: added 1 to be safe with the ceilling
            arrWeight = new float[dstSize * numContributors];
            arrPixel = new int[dstSize * numContributors];

            float fNormFac = (float)(1f / (Math.ceil(width) / fwidth));
            for (int i = 0; i < dstSize; i++) {
                arrN[i]= 0;
                int subindex = i * numContributors;
                float center = i / scalingFactor;
                int left = (int)Math.floor(center - width);
                int right = (int)Math.ceil(center + width);
                for (int j=left; j <= right; j++) {
                    float weight = filter.apply((center - j) * fNormFac);
                    if (weight == 0.0f) {
                        continue;
                    }
                    int n;
                    if (j < 0) {
                        n = -j;
                    } else if (j >= srcSize) {
                        n = srcSize - j + srcSize - 1;
                    } else {
                        n = j;
                    }

                    int k = arrN[i];
                    arrN[i]+= 1;
                    if (n < 0 || n >= srcSize) {
                        weight = 0.0f; // Flag that cell should not be used
                    }

                    arrPixel[subindex + k] = n;
                    arrWeight[subindex + k] = weight;
                }

                // normalize the filter's weight's so the sum equals to 1.0, very important for avoiding box type of artifacts
                int max= arrN[i];
                float tot= 0;
                for (int k = 0; k < max; k++) {
                    tot+= arrWeight[subindex + k];
                }
                if (tot != 0f) { // 0 should never happen except bug in filter
                    for (int k = 0; k < max; k++) {
                        arrWeight[subindex + k] /= tot;
                    }
                }
            }
        } else {
            // scale up -> super-sampling
            numContributors = (int)(fwidth * 2.0f + 1);
            arrWeight = new float[dstSize * numContributors];
            arrPixel = new int[dstSize * numContributors];
            //
            for (int i = 0; i < dstSize; i++) {
                arrN[i] = 0;
                int subindex = i * numContributors;
                float center = i / scalingFactor;
                int left = (int)Math.floor(center - fwidth);
                int right = (int)Math.ceil(center + fwidth);
                for (int j = left; j <= right; j++) {
                    float weight = filter.apply(center - j);
                    if (weight == 0.0f) {
                        continue;
                    }
                    int n;
                    if (j < 0) {
                        n = -j;
                    } else if (j >= srcSize) {
                        n = srcSize - j + srcSize - 1;
                    } else {
                        n = j;
                    }

                    int k = arrN[i];
                    arrN[i] += 1;
                    if (n < 0 || n >= srcSize) {
                        weight = 0.0f; // Flag that cell should not be used
                    }

                    arrPixel[subindex + k] = n;
                    arrWeight[subindex + k] = weight;
                }

                // normalize the filter's weights so the sum equals to 1.0, very important for avoiding box type of artifacts
                int max = arrN[i];
                float tot = 0;
                for (int k = 0; k < max; k++) {
                    tot += arrWeight[subindex + k];
                }
                assert tot != 0:"should never happen except bug in filter";
                if (tot != 0f) {
                    for (int k = 0; k < max; k++) {
                        arrWeight[subindex + k] /= tot;
                    }
                }
            }
        }
        return new SubSamplingData(arrN, arrPixel, arrWeight, numContributors);
    }

    /**
     * Apply filter to sample vertically from temporary buffer to target buffer
     * @param src Integer array holding result from filtering horizontally
     * @param trg Integer array for target bitmap
     */
    private void filterVertically(int[] src, int[] trg) {
        for (int x = 0; x < dstWidth; x++) {
            for (int y = dstHeight-1; y >= 0 ; y--) {
                int yTimesNumContributors = y * verticalSubsamplingData.matrixWidth;
                int max = verticalSubsamplingData.sampleCount[y];
                int ofsY = dstWidth * y;
                float red   = 0;
                float green = 0;
                float blue  = 0;
                float alpha = 0;

                int index = yTimesNumContributors;
                for (int j = max-1; j >= 0 ; j--) {
                    int color = src[x + dstWidth * verticalSubsamplingData.pixelPositions[index]];
                    float w = verticalSubsamplingData.weightFactors[index];
                    alpha += ((color >> 24)&0xff) * w;
                    red   += ((color >> 16)&0xff) * w;
                    green += ((color >>  8)&0xff) * w;
                    blue  +=  (color       &0xff) * w;
                    index++;
                }
                int ri = (int)(red);
                if (ri < 0) {
                    ri = 0;
                } else if (ri > 255) {
                    ri = 255;
                }
                int gi = (int)(green);
                if (gi < 0) {
                    gi = 0;
                } else if (gi > 255) {
                    gi = 255;
                }
                int bi = (int)(blue);
                if (bi < 0) {
                    bi = 0;
                } else if (bi > 255) {
                    bi = 255;
                }
                int ai = (int)(alpha);
                if (ai < 0) {
                    ai = 0;
                } else if (ai > 255) {
                    ai = 255;
                }

                trg[x + ofsY] = ( (ai<<24) | (ri<<16) | (gi << 8) | bi);
            }
        }
    }

    /**
     * Apply filter to sample horizontally from src to Work
     * @param src Byte array holding source image data
     * @param trg Integer array to store temporary result from filtering horizontally
     */
    private void filterHorizontally(byte[] src, int[] trg) {
        for (int k = 0; k < srcHeight; k++) {
            int destOfsY = dstWidth * k;
            int srcOfsY = srcWidth * k;
            for (int i = dstWidth-1; i >= 0 ; i--) {
                float red   = 0;
                float green = 0;
                float blue  = 0;
                float alpha = 0;

                int max = horizontalSubsamplingData.sampleCount[i];
                int index = i * horizontalSubsamplingData.matrixWidth;

                for (int j = max-1; j >= 0; j--) {
                    int ofsX = horizontalSubsamplingData.pixelPositions[index];
                    int palIdx = src[srcOfsY+ofsX] & 0xff;
                    float w = horizontalSubsamplingData.weightFactors[index];
                    red   += (r[palIdx] & 0xff) * w;
                    green += (g[palIdx] & 0xff) * w;
                    blue  += (b[palIdx] & 0xff) * w;
                    alpha += (a[palIdx] & 0xff) * w;
                    index++;
                }
                int ri = (int)(red);
                if (ri < 0) {
                    ri = 0;
                } else if (ri > 255) {
                    ri = 255;
                }
                int gi = (int)(green);
                if (gi < 0) {
                    gi = 0;
                } else if (gi > 255) {
                    gi = 255;
                }
                int bi = (int)(blue);
                if (bi < 0) {
                    bi = 0;
                } else if (bi > 255) {
                    bi = 255;
                }
                int ai = (int)(alpha);
                if (ai < 0) {
                    ai = 0;
                } else if (ai > 255) {
                    ai = 255;
                }

                trg[i + destOfsY] = ( (ai<<24) | (ri<<16) | (gi << 8) | bi);
            }
        }
    }

    private class SubSamplingData {
        /** Number of samples */
        private final int[] sampleCount;
        /** 2D matrix of pixel positions */
        private final int[] pixelPositions;
        /** 2D matrix of weight factors */
        private final float[] weightFactors;
        /** Width of 2D matrices pixelPos and weight */
        private final int matrixWidth;

        /**
         * Private storage class to hold precalculated values for subsampling or supersampling
         * @param sampleCount   Number of samples contributing to the pixel
         * @param pixelPositions   2D matrix of pixel positions
         * @param weightFactors   2D matrix of weight factors
         * @param matrixWidth Width of 2D matrices pixelPos and weight
         */
        private SubSamplingData(int[] sampleCount, int[] pixelPositions, float[] weightFactors, int matrixWidth) {
            this.sampleCount = sampleCount;
            this.pixelPositions = pixelPositions;
            this.weightFactors = weightFactors;
            this.matrixWidth = matrixWidth;
        }
    }
}
