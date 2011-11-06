package deadbeef.SupTools;

import static org.junit.Assert.*;

import org.junit.Test;

public class PaletteBitmapTest {

	private BitmapWithPalette subject;
	
	@Test
	public void shouldInitializeBitmapWithPalette() {
		Bitmap bitmap = new Bitmap(1, 2);
		Palette palette = new Palette(1);
		
		subject = new BitmapWithPalette(bitmap, palette);
		
		assertSame(bitmap, subject.bitmap);
		assertSame(palette, subject.palette);
	}
}
