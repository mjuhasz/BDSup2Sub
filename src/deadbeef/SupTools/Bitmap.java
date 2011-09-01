package deadbeef.SupTools;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.HashMap;

import deadbeef.Filters.Filter;
import deadbeef.Filters.FilterOp;
import deadbeef.Tools.QuantizeFilter;

/*
 * Copyright 2009 Volker Oth (0xdeadbeef)
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

/**
 * Class to handle byte based bitmaps with a separate palette up to 256 colors.
 *
 * @author 0xdeadbeef
 *
 */
public class Bitmap {
	
	/** Bitmap width in pixels */
	private final int width;
	/** Bitmap height in pixels */
	private final int height;
	/** Byte array containing image data */
	private final byte buffer[];

	
	public Bitmap(int width, int height) {
		this.width = width;
		this.height = height;
		buffer = new byte[width * height];
	}

	public Bitmap(int width, int height, byte fillerColor) {
		this(width, height);
		fillWithColorValue(fillerColor);
	}

	public Bitmap(int width, int height, byte[] buffer) {
		this.width = width;
		this.height = height;
		this.buffer = buffer;
	}

	public Bitmap(Bitmap bitmap) {
		width = bitmap.width;
		height = bitmap.height;
		buffer = Arrays.copyOf(bitmap.buffer, bitmap.buffer.length);
	}

	private void fillWithColorValue(byte color) {
		for (int i=0; i<width*height; i++)
			buffer[i] = color;
	}

	public void fillRectangularWithColor(int rectX, int rectY, int rectWidth, int rectHeight, byte color) {
		int xMax = rectX + rectWidth;
		if (xMax > width) {
			xMax = width;
		}
		
		int yMax = rectY+rectHeight;
		if (yMax > height) {
			yMax = height;
		}

		for (int y = rectY; y < yMax; y++) {
			int yOfs = y * width;
			for (int x = rectX; x < xMax; x++) {
				buffer[yOfs + x] = color;
			}
		}
	}

	public BufferedImage getImage(Palette pal) {
		DataBuffer dataBuffer = new DataBufferByte(buffer, width * height);
		SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_BYTE, width, height, new int[]{0xff});
		WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
		return new BufferedImage(pal.getColorModel(), raster, false, null);
	}

	/**
	 * Find the most common color that is as light and opaque as possible<br>
	 * (the darker and more transparent a color is, the smaller is its influence).
	 * @param pal Palette
	 * @param alphaThr Alpha threshold (all colors with alpha < alphaThr will be ignored)
	 * @return Color index of the most common, lightest and most opaque color
	 */
	public int getPrimaryColorIndex(final Palette pal, final int alphaThr) {
		// create histogram for palette
		int hist[] = new int[pal.getSize()];
		for (int i=0; i<hist.length; i++)
			hist[i] = 0;
		for (int i=0; i<buffer.length; i++) {
			// for each pixel of the bitmap
			hist[(buffer[i]&0xff)]++;
		}
		// conditioning of histogramm
		// the primary color should be light and opaque
		for (int i=0; i<hist.length; i++) {
			int alpha = pal.getAlpha(i);
			if (alpha < alphaThr)
				alpha = 0;
			// use alpha as weight factor for histogram
			hist[i] = (hist[i]*alpha+128)/256;
			// use Y component as weight factor for histogram (partially)
			hist[i] = (hist[i]*((pal.getY()[i]&0xff))+128)/256;
		}
		// detect histogram index (=color) with highest value
		int max = 0; // only used as max value
		int col = 0;
		for (int i=0; i<hist.length; i++) {
			if (hist[i] > max) {
				max = hist[i];
				col = i;
			}
		}
		return col;
	}

	/**
	 * Return the highest used palette entry.
	 * @param p Palette
	 * @return Index of highest palette entry used in Bitmap
	 */
	public int getHighestColorIndex(final Palette p) {
		// create histogram for palette
		int maxIdx = 0;
		for (int i=0; i<buffer.length; i++) {
			// for each pixel of the bitmap
			int idx = buffer[i]&0xff;
			if (p.getAlpha(idx) > 0)
				if ( idx > maxIdx) {
					maxIdx = idx;
					if (maxIdx == 255)
						break;
				}
		}
		return maxIdx;
	}

	/**
	 * Convert a palletized Bitmap (where each palette entry has individual alpha)
	 * to a Bitmap with N color palette, where:<br>
	 * index0 = transparent, index1 = light color, ... , indexN-2 = dark color, indexN-1 = black.
	 * @param pal 		Palette of the source Bitmap
	 * @param alphaThr	Threshold for alpha (transparency), lower = more transparent
	 * @param lumThr    Threshold for luminances. For N-1 luminances, N-2 thresholds are needed
	 *                  lumThr[0] is the threshold for the lightest color (-> idx 1)
	 *                  lumThr[N-2] is the threshold for the darkest color (-> idx N-1)
	 * @return Bitmap which uses a fixed frame Palette.
	 */
	public Bitmap convertLm(final Palette pal, final int alphaThr, final int lumThr[]) {
		final byte cy[] = pal.getY();
		final byte a[] = pal.getAlpha();
		Bitmap bm = new Bitmap(width, height);

		// select nearest colors in existing palette
		HashMap<Integer,Integer> p = new HashMap<Integer,Integer>();

		for (int i=0; i<buffer.length; i++) {
			int colIdx;
			int idx = buffer[i] & 0xff;
			int alpha = a[idx] & 0xff;
			int cyp   = cy[idx] & 0xff;

			Integer idxEx = p.get((alpha<<8) | cyp);

			if (idxEx != null)
				colIdx = idxEx;
			else {
				colIdx = 0;

				// determine index in target
				if (alpha < alphaThr) {
					colIdx = 0; // transparent color
				} else {
					colIdx = 1; // default: lightest color
					for (int n=0; n<lumThr.length; n++) {
						if (cyp > lumThr[n])
							break;
						colIdx++; // try next darker color
					}
				}
				p.put((alpha<<8) | cyp, colIdx);
			}
			// write target pixel
			bm.buffer[i] = (byte)colIdx;
		}
		return bm;
	}

	/**
	 * Scales a palletized Bitmap (where each palette entry has individual alpha) using bilinear filtering
	 * to a Bitmap with N color palette, where:<br>
	 * index0 = transparent, index1 = light color, ... , indexN-2 = dark color, indexN-1 = black.
	 * @param sizeX		Target width
	 * @param sizeY		Target height
	 * @param pal 		Palette of the source Bitmap
	 * @param alphaThr	Threshold for alpha (transparency), lower = more transparent
	 * @param lumThr    Threshold for luminances. For N-1 luminances, N-2 thresholds are needed
	 *                  lumThr[0] is the threshold for the lightest color (-> idx 1)
	 *                  lumThr[N-2] is the threshold for the darkest color (-> idx N-1)
	 * @return Scaled Bitmap which uses a fixed frame Palette.
	 */
	public Bitmap scaleBilinearLm(final int sizeX, final int sizeY, final Palette pal, final int alphaThr, final int lumThr[]) {
		final byte cy[] = pal.getY();
		final byte a[] = pal.getAlpha();

		final double scaleX = (double)(width-1)/(sizeX-1);
		final double scaleY = (double)(height-1)/(sizeY-1);

		int lastCY = 0;
		int lastA  = 0;
		int lastColIdx = 0; // 0 is the transparent color

		Bitmap trg = new Bitmap(sizeX, sizeY);

		for (int yt = 0; yt < sizeY; yt++) {
			double ys = yt*scaleY;     // source coordinate
			int ysi = (int)ys;
			double wy = (ys-ysi);
			double wy1 = 1.0-wy;

			for (int xt = 0; xt < sizeX; xt++) {
				double xs = xt*scaleX;     // source coordinate
				int xsi = (int)xs;
				double wx = (xs-xsi);	// weight factor

				double wx1 = 1.0-wx;

				// interpolate pixel

				// top left
				double w = wx1*wy1;
				int idx = getPixel(xsi, ysi)&0xff;
				double at = (a[idx]&0xff)*w;
				double cyt = (cy[idx]&0xff)*w;

				// top right
				if (xsi < width-1) {
					w = wx*wy1;
					idx = getPixel(xsi+1, ysi)&0xff;
					at += (a[idx]&0xff)*w;
					cyt += (cy[idx]&0xff)*w;
				}  // else assume transparent black

				// bottom left
				if (ysi < height-1) {
					w = wx1*wy;
					idx = getPixel(xsi, ysi+1)&0xff;
					at += (a[idx]&0xff)*w;
					cyt += (cy[idx]&0xff)*w;
				} // else assume transparent black

				// bottom right
				if ((ysi < height-1) && (xsi < width-1)) {
					w = wx*wy;
					idx = getPixel(xsi+1, ysi+1)&0xff;
					at += (a[idx]&0xff)*w;
					cyt += (cy[idx]&0xff)*w;
				} // else assume transparent black

				int ati = (int)at;
				int cyti = (int)cyt;

				// find color index in palette

				// already known ?
				int colIdx = lastColIdx;
				if (ati != lastA || cyti != lastCY ) {
					// determine index in target
					if (ati < alphaThr) {
						colIdx = 0; // transparent color
					} else {
						colIdx = 1; // default: lightest color
						for (int n=0; n<lumThr.length; n++) {
							if (cyti > lumThr[n])
								break;
							colIdx++; // try next darker color
						}
					}
					// remember
					lastA = ati;
					lastCY = cyti;
					lastColIdx = colIdx;
				}
				// write target pixel
				trg.setPixel(xt, yt, (byte)colIdx);
			}
		}
		return trg;
	}

	/**
	 * Scales a palletized Bitmap (where each palette entry has individual alpha) using a given scaling filter
	 * to a Bitmap with N color palette, where:<br>
	 * index0 = transparent, index1 = light color, ... , indexN-2 = dark color, indexN-1 = black
	 * @param sizeX		Target width
	 * @param sizeY		Target height
	 * @param pal 		Palette of the source Bitmap
	 * @param alphaThr	Threshold for alpha (transparency), lower = more transparent
	 * @param lumThr    Threshold for luminances. For N-1 luminances, N-2 thresholds are needed
	 *                  lumThr[0] is the threshold for the lightest color (-> idx 1)
	 *                  lumThr[N-2] is the threshold for the darkest color (-> idx N-1)
	 * @param f Filter for scaling
	 * @return Scaled Bitmap which uses a fixed frame Palette.
	 */
	public Bitmap scaleFilterLm(final int sizeX, final int sizeY, final Palette pal, final int alphaThr, final int lumThr[], final Filter f) {
		FilterOp fOp = new FilterOp();
		fOp.setFilter(f);
		final int trg[] = fOp.filter(this, pal, sizeX, sizeY);

		Bitmap bm = new Bitmap(sizeX, sizeY);

		// select nearest colors in existing palette
		HashMap<Integer,Integer> p = new HashMap<Integer,Integer>();

		for (int i=0; i<trg.length; i++) {
			int color = trg[i];
			int colIdx;
			Integer idxEx = p.get(color);
			if (idxEx != null)
				colIdx = idxEx;
			else {
				colIdx = 0;
				int alpha = (color >> 24)& 0xff;
				int red   = (color >> 16)& 0xff;
				int green = (color >>  8)& 0xff;
				int blue  =  color       & 0xff;
				int cyp   = Palette.RGB2YCbCr(red, green, blue, false)[0];

				// determine index in target
				if (alpha < alphaThr) {
					colIdx = 0; // transparent color
				} else {
					colIdx = 1; // default: lightest color
					for (int n=0; n<lumThr.length; n++) {
						if (cyp > lumThr[n])
							break;
						colIdx++; // try next darker color
					}
				}
				p.put(color, colIdx);
			}
			// write target pixel
			bm.buffer[i] = (byte)colIdx;
		}
		return bm;
	}

	/** Scales a palletized Bitmap to a Bitmap with the same palette using bilinear filtering.
	 * @param sizeX Target width
	 * @param sizeY Target height
	 * @param pal   Palette of the source Bitmap
	 * @return Scaled Bitmap which uses the same Palette as the source Bitmap.
	 */
	public Bitmap scaleBilinear(final int sizeX, final int sizeY, final Palette pal) {
		final byte r[] = pal.getR();
		final byte g[] = pal.getG();
		final byte b[] = pal.getB();
		final byte a[] = pal.getAlpha();

		final double scaleX = (double)(width-1)/(sizeX-1);
		final double scaleY = (double)(height-1)/(sizeY-1);

		int lastR = 0;
		int lastG = 0;
		int lastB = 0;
		int lastA = 0;
		int lastColIdx = pal.getTransparentIndex();

		final Bitmap trg = new Bitmap(sizeX, sizeY);

		for (int yt = 0; yt < sizeY; yt++) {
			double ys = yt*scaleY;     // source coordinate
			int ysi = (int)ys;
			double wy = (ys-ysi);
			double wy1 = 1.0-wy;

			for (int xt = 0; xt < sizeX; xt++) {
				double xs = xt*scaleX;     // source coordinate
				int xsi = (int)xs;
				double wx = (xs-xsi);	// weight factor

				double wx1 = 1.0-wx;

				// interpolate pixel

				// top left
				double w = wx1*wy1;
				int idx = getPixel(xsi, ysi)&0xff;
				double at = (a[idx]&0xff)*w;
				double rt = (r[idx]&0xff)*w;
				double gt = (g[idx]&0xff)*w;
				double bt = (b[idx]&0xff)*w;

				// top right
				if (xsi < width-1) {
					w = wx*wy1;
					idx = getPixel(xsi+1, ysi)&0xff;
					at += (a[idx]&0xff)*w;
					rt += (r[idx]&0xff)*w;
					gt += (g[idx]&0xff)*w;
					bt += (b[idx]&0xff)*w;
				}  // else assume transparent black

				// bottom left
				if (ysi < height-1) {
					w = wx1*wy;
					idx = getPixel(xsi, ysi+1)&0xff;
					at += (a[idx]&0xff)*w;
					rt += (r[idx]&0xff)*w;
					gt += (g[idx]&0xff)*w;
					bt += (b[idx]&0xff)*w;
				} // else assume transparent black

				// bottom right
				if ((ysi < height-1) && (xsi < width-1)) {
					w = wx*wy;
					idx = getPixel(xsi+1, ysi+1)&0xff;
					at += (a[idx]&0xff)*w;
					rt += (r[idx]&0xff)*w;
					gt += (g[idx]&0xff)*w;
					bt += (b[idx]&0xff)*w;

				} // else assume transparent black

				int ati = (int)(at + 0.5);
				int rti = (int)(rt + 0.5);
				int gti = (int)(gt + 0.5);
				int bti = (int)(bt + 0.5);

				// find color index in palette

				// already known ?
				int colIdx = lastColIdx;
				if (ati != lastA || rti != lastR || gti != lastG || bti!=lastB) {
					int minDistance = 0xffffff; // init > 0xff*0xff*4 = 0x03f804
					for (idx=0; idx<pal.getSize(); idx++) {
						// distance vector (skip sqrt)
						int ad = ati-(a[idx]&0xff);
						int rd = rti-(r[idx]&0xff);
						int gd = gti-(g[idx]&0xff);
						int bd = bti-(b[idx]&0xff);
						int distance = rd*rd+gd*gd+bd*bd+ad*ad;
						// new minimum distance ?
						if ( distance < minDistance) {
							colIdx = idx;
							minDistance = distance;
							if (minDistance == 0)
								break;
						}
					}
					// remember values
					lastA = ati;
					lastR = rti;
					lastG = gti;
					lastB = bti;
					lastColIdx = colIdx;
				}
				// write target pixel
				trg.setPixel(xt, yt, (byte)colIdx);

			}
		}
		return trg;
	}

	/** Scales a palletized Bitmap to a Bitmap with a new quantized Palette using bilinear filtering.
	 * @param sizeX  Target width
	 * @param sizeY  Target height
	 * @param pal    Palette of the source Bitmap
	 * @param dither True: apply dithering
	 * @return Scaled Bitmap and new Palette
	 */
	public PaletteBitmap scaleBilinear(final int sizeX, final int sizeY, final Palette pal, final boolean dither) {
		final byte r[] = pal.getR();
		final byte g[] = pal.getG();
		final byte b[] = pal.getB();
		final byte a[] = pal.getAlpha();

		final double scaleX = (double)(width-1)/(sizeX-1);
		final double scaleY = (double)(height-1)/(sizeY-1);

		final int trg[] = new int[sizeX*sizeY];

		for (int yt = 0; yt < sizeY; yt++) {
			double ys = yt*scaleY;     // source coordinate
			int ysi = (int)ys;
			double wy = (ys-ysi);
			double wy1 = 1.0-wy;
			int ofsY = yt*sizeX;

			for (int xt = 0; xt < sizeX; xt++) {
				double xs = xt*scaleX;     // source coordinate
				int xsi = (int)xs;
				double wx = (xs-xsi);      // weight factor

				int idx;

				// interpolate pixel
				double wx1 = 1.0-wx;
				int x,y;

				// top left
				double w = wx1*wy1;
				idx = getPixel(xsi, ysi)&0xff;
				double at = (a[idx]&0xff)*w;
				double rt = (r[idx]&0xff)*w;
				double gt = (g[idx]&0xff)*w;
				double bt = (b[idx]&0xff)*w;

				// top right
				x = xsi+1;
				if (x < width) {
					w = wx*wy1;
					idx = getPixel(x, ysi)&0xff;
					at += (a[idx]&0xff)*w;
					rt += (r[idx]&0xff)*w;
					gt += (g[idx]&0xff)*w;
					bt += (b[idx]&0xff)*w;
				}  // else assume transparent black

				// bottom left
				y = ysi+1;
				if (y < height) {
					w = wx1*wy;
					idx = getPixel(xsi, y)&0xff;
					at += (a[idx]&0xff)*w;
					rt += (r[idx]&0xff)*w;
					gt += (g[idx]&0xff)*w;
					bt += (b[idx]&0xff)*w;
				} // else assume transparent black

				// bottom right
				x = xsi+1;
				y = ysi+1;
				if ((x < width) && (y < height)) {
					w = wx*wy;
					idx = getPixel(x, y)&0xff;
					at += (a[idx]&0xff)*w;
					rt += (r[idx]&0xff)*w;
					gt += (g[idx]&0xff)*w;
					bt += (b[idx]&0xff)*w;
				} // else assume transparent black

				int ati = (int)(at);
				int rti = (int)(rt);
				int gti = (int)(gt);
				int bti = (int)(bt);

				trg[xt + ofsY] = ((ati<<24) | (rti<<16) | (gti << 8) | bti);
			}
		}
		// quantize image
		QuantizeFilter qf = new QuantizeFilter();
		final Bitmap bm = new Bitmap(sizeX, sizeY);
		int ct[] = qf.quantize(trg, bm.buffer, sizeX, sizeY, 255, dither, dither);
		int size = ct.length;
		if (size > 255) {
			size = 255;
			Core.printWarn("Quantizer failed.\n");
		}
		// create palette
		Palette trgPal = new Palette(256);
		for (int i=0; i<size; i++)
			trgPal.setARGB(i,ct[i]);

		return new PaletteBitmap(bm, trgPal);
	}

	/** Scales a palletized Bitmap to a Bitmap with the same Palette using a given scaling filter.
	 * @param sizeX Target width
	 * @param sizeY Target height
	 * @param pal   Palette of the source Bitmap
	 * @param f     Filter for scaling
	 * @return Scaled Bitmap which uses the same Palette as the source Bitmap.
	 */
	public Bitmap scaleFilter(final int sizeX, final int sizeY, final Palette pal, final Filter f) {
		final byte r[] = pal.getR();
		final byte g[] = pal.getG();
		final byte b[] = pal.getB();
		final byte a[] = pal.getAlpha();

		FilterOp fOp = new FilterOp();
		fOp.setFilter(f);
		final int trg[] = fOp.filter(this, pal, sizeX, sizeY);

		final Bitmap bm = new Bitmap(sizeX, sizeY);

		// select nearest colors in existing palette
		HashMap<Integer,Integer> p = new HashMap<Integer,Integer>();

		for (int i=0; i<trg.length; i++) {
			int color = trg[i];
			int colIdx;
			Integer idxEx = p.get(color);
			if (idxEx != null)
				colIdx = idxEx;
			else {
				colIdx = 0;
				int minDistance = 0xffffff; // init > 0xff*0xff*4 = 0x03f804
				int alpha = (color >> 24)& 0xff;
				int red   = (color >> 16)& 0xff;
				int green = (color >>  8)& 0xff;
				int blue  =  color       & 0xff;
				for (int idx=0; idx<pal.getSize(); idx++) {
					// distance vector (skip sqrt)
					int ad = alpha - (a[idx]&0xff);
					int rd = red   - (r[idx]&0xff);
					int gd = green - (g[idx]&0xff);
					int bd = blue  - (b[idx]&0xff);
					int distance = rd*rd+gd*gd+bd*bd+ad*ad;
					// new minimum distance ?
					if ( distance < minDistance) {
						colIdx = idx;
						minDistance = distance;
						if (minDistance == 0)
							break;
					}
				}
				p.put(color, colIdx);
			}
			// write target pixel
			bm.buffer[i] = (byte)colIdx;
		}
		return bm;
	}

	/** Scales a palletized Bitmap to a Bitmap with a new quantized Palette using a given scaling filter.
	 * @param sizeX  Target width
	 * @param sizeY  Target height
	 * @param pal    Palette of the source Bitmap
	 * @param f      Filter for scaling
	 * @param dither True: apply dithering
	 * @return Scaled Bitmap and new Palette
	 */
	public PaletteBitmap scaleFilter(final int sizeX, final int sizeY, final Palette pal, final Filter f, final boolean dither) {

		FilterOp fOp = new FilterOp();
		fOp.setFilter(f);
		final int trg[] = fOp.filter(this, pal, sizeX, sizeY);

		// quantize image
		QuantizeFilter qf = new QuantizeFilter();
		final Bitmap bm = new Bitmap(sizeX, sizeY);
		int ct[] = qf.quantize(trg, bm.buffer, sizeX, sizeY, 255, dither, dither);
		int size = ct.length;
		if (size > 255) {
			size = 255;
			Core.printWarn("Quantizer failed.\n");
		}
		// create palette
		Palette trgPal = new Palette(256);
		for (int i=0; i<size; i++)
			trgPal.setARGB(i,ct[i]);

		return new PaletteBitmap(bm, trgPal);
	}

	/**
	 * Convert Bitmap to Integer array filled with ARGB values.
	 * @param pal Palette
	 * @return Integer array filled with ARGB values.
	 */
	public int[] toARGB(final Palette pal) {
		int t[] = new int[buffer.length];
		for (int i=0; i<t.length; i++)
			t[i] = pal.getARGB(buffer[i]&0xff);
		return t;
	}

	/**
	 * Create cropped Bitmap.
	 * @param x X offset in source bitmap
	 * @param y Y offset in source bitmap
	 * @param w Width of cropping window
	 * @param h Height of cropping window
	 * @return Cropped Bitmap sized w*h
	 */
	public Bitmap crop(final int x, final int y, final int w, final int h) {
		final Bitmap bm = new Bitmap(w,h);

		int yOfsSrc = y*width;
		int yOfsTrg = 0;
		for (int yt=0; yt < h; yt++, yOfsSrc+=width, yOfsTrg+=w) {
			for (int xt=0; xt < w; xt++)
				bm.buffer[xt+yOfsTrg] = buffer[x+xt+yOfsSrc];
		}
		return bm;
	}

	/**
	 * Get cropping bounds of Bitmap (first/last x/y coordinates that contain visible pixels).
	 * @param pal      Palette
	 * @param alphaThr Alpha threshold (only pixels with alpha >= alphaThr will be treated as visible)
	 * @return BitmapBounds containing bounds
	 */
	public BitmapBounds getBounds(final Palette pal, final int alphaThr) {
		final byte a[] = pal.getAlpha();
		int xMin, xMax, yMin, yMax;

		// search lower bound
		yMax = (height-1);
		int yOfs = yMax*width;
		loop1:
		for (int y=height-1; y > 0; y--, yOfs-=width) {
			yMax = y;
			for (int x=0; x< width; x++) {
				int idx = buffer[x+yOfs] & 0xff;
				if ( (a[idx]&0xff) >= alphaThr )
					break loop1;
			}
		}

		// search upper bound
		yMin = 0;
		yOfs = 0;
		loop2:
		for (int y=0; y < yMax; y++, yOfs+=width) {
			yMin = y;
			for (int x=0; x< width; x++) {
				int idx = buffer[x+yOfs] & 0xff;
				if ( (a[idx]&0xff) >= alphaThr )
					break loop2;
			}
		}

		// search right bound
		xMax = width-1;
		loop3:
		for (int x=width-1; x>0 ; x--) {
			xMax = x;
			yOfs = yMin*width;
			for (int y=yMin; y < yMax; y++, yOfs+=width) {
				int idx = buffer[x+yOfs] & 0xff;
				if ( (a[idx]&0xff) >= alphaThr )
					break loop3;
			}
		}


		// search left bound
		xMin = 0;
		loop4:
		for (int x=0; x< xMax; x++) {
			xMin = x;
			yOfs = yMin*width;
			for (int y=yMin; y < yMax; y++, yOfs+=width) {
				int idx = buffer[x+yOfs] & 0xff;
				if ( (a[idx]&0xff) >= alphaThr )
					break loop4;
			}
		}

		return new BitmapBounds(xMin, xMax, yMin, yMax);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public byte[] getInternalBuffer() {
		return buffer;
	}
	
	private byte getPixel(int x, int y) {
		return buffer[x + width * y];
	}

	private void setPixel(int x, int y, byte color) {
		buffer[x + width * y] = color;
	}
}
