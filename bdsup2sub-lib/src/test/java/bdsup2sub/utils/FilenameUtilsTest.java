/*
 * Copyright 2014 Miklos Juhasz (mjuhasz)
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
package bdsup2sub.utils;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class FilenameUtilsTest {

    private static final char SEPARATOR_CHAR = File.separatorChar;
    private static final String PARENT_DIR_PATH = SEPARATOR_CHAR + "home" + SEPARATOR_CHAR + "user";
    private static final String BASENAME = "someFile";
    private static final String EXTENSION = "txt";
    private static final String FILENAME = BASENAME + "." + EXTENSION;
    private static final String FILENAME_FULL_PATH = PARENT_DIR_PATH + SEPARATOR_CHAR + FILENAME;

    @Test
    public void shouldGetExtension() throws Exception {
        assertEquals(EXTENSION, FilenameUtils.getExtension(FILENAME_FULL_PATH));
    }

    @Test
    public void shouldReturnEmptyStringIfNoExtensionFound() throws Exception {
        assertEquals("", FilenameUtils.getExtension(BASENAME));
        assertEquals("", FilenameUtils.getExtension(".." + SEPARATOR_CHAR + BASENAME));
    }

    @Test
    public void shouldRemoveExtension() throws Exception {
        assertEquals(PARENT_DIR_PATH + SEPARATOR_CHAR + BASENAME, FilenameUtils.removeExtension(FILENAME_FULL_PATH));
    }

    @Test
    public void shouldReturnTheOriginalStringIfNoExtensionFound() throws Exception {
        assertEquals("/home/user/someFile", FilenameUtils.removeExtension("/home/user/someFile"));
        assertEquals("C:\\Windows\\someFile", FilenameUtils.removeExtension("C:\\Windows\\someFile"));
    }

    @Test
    public void shouldGetName() throws Exception {
        assertEquals(FILENAME, FilenameUtils.getName(FILENAME_FULL_PATH));
    }

    @Test
    public void shouldGetPath() throws Exception {
        assertEquals(PARENT_DIR_PATH, FilenameUtils.getParent(FILENAME_FULL_PATH));
    }

    @Test
    public void shouldChangeWindowsSeparatorsToUnix() throws Exception {
        assertEquals("/foo/bar.jar", FilenameUtils.separatorsToUnix("\\foo\\bar.jar"));
    }

    @Test
    public void shouldMakeSurePathEndsWithASeparator() throws Exception {
        assertEquals(PARENT_DIR_PATH + SEPARATOR_CHAR, FilenameUtils.addSeparator(PARENT_DIR_PATH));
        assertEquals(PARENT_DIR_PATH + SEPARATOR_CHAR, FilenameUtils.addSeparator(PARENT_DIR_PATH + SEPARATOR_CHAR));
    }
}
