package deadbeef.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToolBoxTest {

    @Test
    public void shouldLeftPadWithZeros() {
        assertEquals("0023", ToolBox.leftZeroPad(23, 4));
        assertEquals("23", ToolBox.leftZeroPad(23, 2));
    }

    @Test
    public void shouldConvertToHexLeftZeroPadded() {
        assertEquals("0x00000012", ToolBox.toHexLeftZeroPadded(18, 8));
    }

    @Test
    public void shouldFormatDouble() {
        assertEquals("25", ToolBox.formatDouble(25));
        assertEquals("23.976", ToolBox.formatDouble(24000.0/1001));
    }
}
