package deadbeef.Filters;

import deadbeef.SupTools.Bitmap;
import deadbeef.SupTools.Palette;

/*
 * Copyright 2009 Morten Nobel-Joergensen / Volker Oth (0xdeadbeef)
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

/**
 * Subsampling scaling algorithm with various filters.
 *
 * <p>Based on the ResampleOp class from the <a href="http://code.google.com/p/java-image-scaling/">Java Image Scaling Library.</a>
 * by Morten Nobel-Joergensen which again is based on "Java Image Util".</p>
 *
 * @author Morten Nobel-Joergensen / 0xdeadbeef
 */

public class FilterOp {

	private class SubSamplingData{
		/** Number of samples */
		private final int[] numSamples;
		/** 2D matrix of pixel positions */
		private final int[] pixelPos;
		/** 2D matrix of weight factors */
		private final float[] weight;
		/** Width of 2D matrices pixelPos and weight */
		private final int matrixWidth;

		/**
		 * Private storage class to hold precalculated values for subsampling or supersampling
		 * @param s   Number of samples contributing to the pixel
		 * @param p   2D matrix of pixel positions
		 * @param w   2D matrix of weight factors
		 * @param num Width of 2D matrices pixelPos and weight
		 */
		private SubSamplingData(final int[] s, final int[] p, final float[] w, final int num) {
			numSamples = s;
			pixelPos = p;
			weight = w;
			matrixWidth = num;
		}
	}

	private int srcWidth;
	private int srcHeight;
	private int dstWidth;
	private int dstHeight;

	private byte r[];
	private byte g[];
	private byte b[];
	private byte a[];

	private SubSamplingData horizontalSubsamplingData;
	private SubSamplingData verticalSubsamplingData;
	private Filter filter = new MitchellFilter();

	/**
	 * Get current filter
	 * @return Current filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Set current filter
	 * @param filter Filter
	 */
	public void setFilter(final Filter filter) {
		this.filter = filter;
	}

	/**
	 * @param src Source bitmap
	 * @param pal Palette
	 * @param w Destination width
	 * @param h Destination height
	 * @return Destination integer array (filled with ARGB samples)
	 */
	public int[] filter(final Bitmap src, final Palette pal, final int w, final int h) {
		dstWidth = w;
		dstHeight = h;
		srcWidth  = src.getWidth();
		srcHeight = src.getHeight();

		r = pal.getR();
		g = pal.getG();
		b = pal.getB();
		a = pal.getAlpha();

		final float xscale = (float)(dstWidth - 1) / (float)(srcWidth - 1);
		final float yscale = (float)(dstHeight - 1) / (float)(srcHeight - 1);

		// Precalculate  subsampling/supersampling
		horizontalSubsamplingData = createSubSampling(srcWidth, dstWidth, xscale);
		verticalSubsamplingData = createSubSampling(srcHeight, dstHeight, yscale);

		final int[] workPixels = new int[srcHeight*dstWidth];
		filterHorizontally(src.getImg(), workPixels);

		final int[] outPixels = new int[dstHeight*dstWidth];
		filterVertically(workPixels, outPixels);

		return outPixels;
	}

	/**
	 * Create data structure holding precalculated values for subsampling or supersampling
	 * @param srcSize Number of pixels in source data line (might be image line or column)
	 * @param dstSize Number of pixels in destination data line (might be image line or column)
	 * @param scale   Scaling factor
	 * @return Data structure holding precalculated values for subsampling or supersampling
	 */
	private SubSamplingData createSubSampling(final int srcSize, final int dstSize, final float scale) {
		final int[] arrN = new int[dstSize];
		final int numContributors;
		final float[] arrWeight;
		final int[] arrPixel;

		final float fwidth = filter.getRadius();

		if (scale < 1.0f) {
			// scale down -> subsampling
			final float width = fwidth / scale;
			numContributors= (int)(width * 2.0f + 2); // Heinz: added 1 to be save with the ceilling
			arrWeight= new float[dstSize * numContributors];
			arrPixel= new int[dstSize * numContributors];

			final float fNormFac = (float)(1f / (Math.ceil(width) / fwidth));
			//
			for (int i= 0; i < dstSize; i++) {
				arrN[i]= 0;
				final int subindex = i * numContributors;
				final float center = i / scale;
				final int left = (int)Math.floor(center - width);
				final int right = (int)Math.ceil(center + width);
				for (int j=left; j <= right; j++) {
					float weight;
					weight= filter.value((center - j) * fNormFac);

					if (weight == 0.0f)
						continue;
					int n;
					if (j < 0)
						n = -j;
					else if (j >= srcSize)
						n = srcSize - j + srcSize - 1;
					else
						n = j;

					int k = arrN[i];
					arrN[i]+= 1;
					if (n < 0 || n >= srcSize)
						weight= 0.0f;// Flag that cell should not be used

					arrPixel[subindex +k]= n;
					arrWeight[subindex + k]= weight;
				}
				// normalize the filter's weight's so the sum equals to 1.0, very important for avoiding box type of artifacts
				final int max= arrN[i];
				float tot= 0;
				for (int k= 0; k < max; k++)
					tot+= arrWeight[subindex + k];
				if (tot != 0f) { // 0 should never happen except bug in filter
					for (int k= 0; k < max; k++)
						arrWeight[subindex + k]/= tot;
				}
			}
		} else {
			// scale up -> super-sampling
			numContributors = (int)(fwidth * 2.0f + 1);
			arrWeight = new float[dstSize * numContributors];
			arrPixel = new int[dstSize * numContributors];
			//
			for (int i= 0; i < dstSize; i++) {
				arrN[i]= 0;
				final int subindex = i * numContributors;
				final float center = i / scale;
				final int left = (int)Math.floor(center - fwidth);
				final int right = (int)Math.ceil(center + fwidth);
				for (int j= left; j <= right; j++) {
					float weight= filter.value(center - j);
					if (weight == 0.0f)
						continue;
					int n;
					if (j < 0)
						n = -j;
					else if (j >= srcSize)
						n = srcSize - j + srcSize - 1;
					else
						n = j;

					int k= arrN[i];
					arrN[i]+= 1;
					if (n < 0 || n >= srcSize)
						weight= 0.0f;// Flag that cell should not be used

					arrPixel[subindex +k] = n;
					arrWeight[subindex + k] = weight;
				}
				// normalize the filter's weights so the sum equals to 1.0, very important for avoiding box type of artifacts
				final int max= arrN[i];
				float tot= 0;
				for (int k= 0; k < max; k++)
					tot+= arrWeight[subindex + k];
				assert tot!=0:"should never happen except bug in filter";
				if (tot != 0f) {
					for (int k= 0; k < max; k++)
						arrWeight[subindex + k]/= tot;
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
	private void filterVertically(final int[] src, final int[] trg) {
		for (int x = 0; x < dstWidth; x++) {
			for (int y = dstHeight-1; y >=0 ; y--) {
				final int yTimesNumContributors = y * verticalSubsamplingData.matrixWidth;
				final int max = verticalSubsamplingData.numSamples[y];
				final int ofsY = dstWidth*y;
				float red   = 0;
				float green = 0;
				float blue  = 0;
				float alpha = 0;

				int index = yTimesNumContributors;
				for (int j= max-1; j >=0 ; j--) {
					final int color = src[x + dstWidth*verticalSubsamplingData.pixelPos[index]];
					final float w = verticalSubsamplingData.weight[index];
					alpha += ((color >> 24)&0xff) * w;
					red   += ((color >> 16)&0xff) * w;
					green += ((color >>  8)&0xff) * w;
					blue  +=  (color       &0xff) * w;
					index++;
				}
				int ri = (int)(red);
				if (ri < 0)
					ri = 0;
				else if (ri > 255)
					ri = 255;
				int gi = (int)(green);
				if (gi < 0)
					gi = 0;
				else if (gi > 255)
					gi = 255;
				int bi = (int)(blue);
				if (bi < 0)
					bi = 0;
				else if (bi > 255)
					bi = 255;
				int ai = (int)(alpha);
				if (ai < 0)
					ai = 0;
				else if (ai > 255)
					ai = 255;

				trg[x + ofsY] = ( (ai<<24) | (ri<<16) | (gi << 8) | bi);
			}
		}
	}

	/**
	 * Apply filter to sample horizontally from Src to Work
	 * @param src Byte array holding source image data
	 * @param trg Integer array to store temporary result from filtering horizontally
	 */
	private void filterHorizontally(final byte[] src, final int[] trg) {
		for (int k = 0; k < srcHeight; k++) {
			final int destOfsY = dstWidth*k;
			final int srcOfsY = srcWidth*k;
			for (int i = dstWidth-1; i>=0 ; i--) {
				float red   = 0;
				float green = 0;
				float blue  = 0;
				float alpha = 0;

				final int max = horizontalSubsamplingData.numSamples[i];
				int index = i * horizontalSubsamplingData.matrixWidth;

				for (int j = max-1; j >= 0; j--) {
					final int ofsX = horizontalSubsamplingData.pixelPos[index];
					final int palIdx = src[srcOfsY+ofsX] & 0xff;
					final float w = horizontalSubsamplingData.weight[index];
					red   += (r[palIdx] & 0xff) * w;
					green += (g[palIdx] & 0xff) * w;
					blue  += (b[palIdx] & 0xff) * w;
					alpha += (a[palIdx] & 0xff) * w;
					index++;
				}
				int ri = (int)(red);
				if (ri < 0)
					ri = 0;
				else if (ri > 255)
					ri = 255;
				int gi = (int)(green);
				if (gi < 0)
					gi = 0;
				else if (gi > 255)
					gi = 255;
				int bi = (int)(blue);
				if (bi < 0)
					bi = 0;
				else if (bi > 255)
					bi = 255;
				int ai = (int)(alpha);
				if (ai < 0)
					ai = 0;
				else if (ai > 255)
					ai = 255;

				trg[i + destOfsY] = ( (ai<<24) | (ri<<16) | (gi << 8) | bi);
			}
		}
	}

}
