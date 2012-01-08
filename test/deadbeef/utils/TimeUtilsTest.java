package deadbeef.utils;

import static deadbeef.utils.TimeUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class TimeUtilsTest {

	@Test
	public void shouldConvertPtsToTimeStr() {
		assertEquals("01:02:03.240", ptsToTimeStr((3600 + 120 + 3) * 90000 + 240 * 90));
	}
	
	@Test
	public void shouldConvertPtsToTimeStrIdx() {
		assertEquals("01:02:03:240", ptsToTimeStrIdx((3600 + 120 + 3) * 90000 + 240 * 90));
	}
	
	@Test
	public void shouldConvertPtsToTimeStrXml() {
		assertEquals("01:02:03:06", ptsToTimeStrXml((3600 + 120 + 3) * 90000 + 240 * 90, 25));
	}
	
	@Test
	public void shouldConvertTimeStrToPts() {
		assertEquals((3600 + 120 + 3) * 90000 + 240 * 90, timeStrToPTS("01:02:03.240"));
		assertEquals((3600 + 120 + 3) * 90000 + 240 * 90, timeStrToPTS("01:02:03:240"));
	}
	
	@Test
	public void shouldConvertTimeStrXmlToPTS() {
		assertEquals((3600 + 120 + 3) * 90000 + 240 * 90, timeStrXmlToPTS("01:02:03:06", 25));
	}	
}
