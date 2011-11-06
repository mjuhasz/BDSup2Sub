package deadbeef.SupTools;

import static org.junit.Assert.*;

import org.junit.Test;

import deadbeef.bitmap.ErasePatch;

public class ErasePatchTest {

	private ErasePatch subject;
	private int x = 1;
	private int y = 2;
	private int width = 3;
	private int height = 4;
	
	@Test
	public void shouldInitializeErasePatch() {
		subject = new ErasePatch(x, y, width, height);
		assertEquals(x, subject.x);
		assertEquals(y, subject.y);
		assertEquals(width, subject.width);
		assertEquals(height, subject.height);
	}
}
