/*
 * Copyright 2013 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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

import bdsup2sub.tools.JFileFilter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public final class ToolBox {

    private static final DecimalFormat FPS_FORMATTER = new DecimalFormat("##.###", DecimalFormatSymbols.getInstance(Locale.US));

    public static String leftZeroPad(int value, int width) {
        return String.format("%0" + width + "d", value);
    }

    public static String toHexLeftZeroPadded(long value, int width) {
        return String.format("0x%0" + width + "x", value);
    }

    public static String formatDouble(double value) {
        return FPS_FORMATTER.format(value);
    }

    /**
     * Write ASCII string to buffer[index] (no special handling for multi-byte characters)
     * @param buffer Byte array
     * @param index Index to write to
     * @param s String containing ASCII characters
     * @throws ArrayIndexOutOfBoundsException
     */
    public static void setString(byte buffer[], int index, String s) {
        for (int i =0; i<s.length(); i++) {
            buffer[index+i] = (byte)s.charAt(i);
        }
    }

    /**
     * Show a dialog with details about an exception
     * @param ex Throwable/Exception to display
     */
    public static void showException(Throwable ex) {
        String m;
        m = "<html>";
        m += ex.getClass().getName() + "<p>";
        if (ex.getMessage() != null) {
            m += ex.getMessage() + "<p>";
        }
        StackTraceElement ste[] = ex.getStackTrace();
        for (StackTraceElement e : ste) {
            m += e.toString() + "<p>";
        }
        m += "</html>";
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Get file name via "file chooser" dialog
     * @param path   Default path (without file name).
     * @param filename     Default file name (without path).
     * @param extensions    Array of allowed extensions (without ".")
     * @param loadDialog   If true, this is a load dialog, else it's a save dialog
     * @param parent Parent component (Frame, Window)
     * @return       Selected filename or null if canceled
     */
    public static String getFilename(String path, String filename, List<String> extensions, boolean loadDialog, Component parent) {
        if (path == null || path.isEmpty()) {
            path = ".";
        }
        JFileChooser fileChooser = new JFileChooser(path);
        if (extensions != null) {
            JFileFilter fileFilter = new JFileFilter();
            for (String extension : extensions) {
                fileFilter.addExtension(extension);
            }
            fileChooser.setFileFilter(fileFilter);
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        File file;
        if (filename != null && !filename.isEmpty()) {
            file = new File(FilenameUtils.addSeparator(path) + filename);
            if (file.canRead()) {
                fileChooser.setSelectedFile(file);
            }
        }
        if (!loadDialog) {
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        }
        int returnVal = fileChooser.showDialog(parent, null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (file != null) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Returns the first few bytes of a file to check it's type
     * @param fname Filename of the file
     * @param num Number of bytes to return
     * @return Array of bytes (size num) from the beginning of the file
     */
    public static byte[] getFileID(String fname, int num) {
        byte buf[] = new byte[num];
        File f = new File(fname);
        if (f.length() < num) {
            return null;
        }
        try {
            FileInputStream fi = new FileInputStream(fname);
            fi.read(buf);
            fi.close();
        } catch (Exception ex) {
            return null;
        }
        return buf;
    }

    /**
     * Convert String to integer
     * @param s String containing integer (assumed: positive)
     * @return Integer value or -1.0 if no valid numerical value
     */
    public static int getInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Convert String to double
     * @param s String containing double
     * @return Double value or -1.0 if no valid numerical value
     */
    public static double getDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException ex) {
            return -1.0;
        }
    }
}
