package deadbeef.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilenameUtilsTest {

    private final String unixFilename = "/home/user/someFile.txt";
    private final String winFilename = "C:\\Windows\\someFile.txt";

    @Test
    public void shouldGetExtension() throws Exception {
        assertEquals("txt", FilenameUtils.getExtension(unixFilename));
        assertEquals("txt", FilenameUtils.getExtension(winFilename));
    }

    @Test
    public void shouldReturnEmptyStringIfNoExtensionFound() throws Exception {
        assertEquals("", FilenameUtils.getExtension("someFile"));
        assertEquals("", FilenameUtils.getExtension("../someFile"));
    }

    @Test
    public void shouldRemoveExtension() throws Exception {
        assertEquals("/home/user/someFile", FilenameUtils.removeExtension(unixFilename));
        assertEquals("C:\\Windows\\someFile", FilenameUtils.removeExtension(winFilename));
    }

    @Test
    public void shouldReturnTheOriginalStringIfNoExtensionFound() throws Exception {
        assertEquals("/home/user/someFile", FilenameUtils.removeExtension("/home/user/someFile"));
        assertEquals("C:\\Windows\\someFile", FilenameUtils.removeExtension("C:\\Windows\\someFile"));
    }

    @Test
    public void shouldGetName() throws Exception {
        assertEquals("someFile.txt", FilenameUtils.getName(unixFilename));
    }

    @Test
    public void shouldGetPath() throws Exception {
        assertEquals("/home/user", FilenameUtils.getParent(unixFilename));
    }

    @Test
    public void shouldChangeWindowsSeparatorsToUnix() throws Exception {
        assertEquals("/foo/bar.jar", FilenameUtils.separatorsToUnix("\\foo\\bar.jar"));
    }

    @Test
    public void shouldMakeSurePathEndsWithASeparator() throws Exception {
        assertEquals("/home/", FilenameUtils.addSeparator("/home"));
        assertEquals("/home/", FilenameUtils.addSeparator("/home/"));
    }
}
