package deadbeef.SupTools;

import static org.junit.Assert.*;

import org.junit.Test;

public class BitmapBoundsTest {

	private BitmapBounds subject;
	private int minX = 1;
	private int maxX = 2;
	private int minY = 3;
	private int maxY = 4;
	
	@Test
	public void shouldInitializeBitmapBounds() {
		subject = new BitmapBounds(minX, maxX, minY, maxY);
		assertEquals(minX, subject.xMin);
		assertEquals(maxX, subject.xMax);
		assertEquals(minY, subject.yMin);
		assertEquals(maxY, subject.yMax);
	}
}
