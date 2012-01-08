package deadbeef.utils;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import deadbeef.tools.JFileFilter;

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
 * Selection of utility functions.
 *
 * @author 0xdeadbeef, mjuhasz
 */
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
		for (int i=0; i<ste.length; i++) {
			m += ste[i].toString() + "<p>";
		}
		m += "</html>";
		ex.printStackTrace();
		JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Get file name via "file chooser" dialog
	 * @param path   Default path (without file name). Might be "", but not null.
	 * @param fn     Default file name (without path). Can be null.
	 * @param ext    Array of allowed extensions (without ".")
	 * @param load   If true, this is a load dialog, else it's a save dialog
	 * @param parent Parent component (Frame, Window)
	 * @return       Selected filename or null if canceled
	 */
	public static String getFileName(String path, String fn, String ext[], boolean load, Component parent) {
		String p = path;
		File f;
		if (p.length() == 0) {
			p = ".";
		}
		JFileChooser fc = new JFileChooser(p);
		if (ext != null) {
			JFileFilter filter = new JFileFilter();
			for (int i=0; i < ext.length; i++) {
				filter.addExtension(ext[i]);
			}
			fc.setFileFilter(filter);
		}
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (fn != null) {
			f = new File(addSeparator(path) + fn);
			fc.setSelectedFile(f);
		}
		if (!load) {
			fc.setDialogType(JFileChooser.SAVE_DIALOG);
		}
		int returnVal = fc.showDialog(parent, null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			if (f != null) {
				return f.getAbsolutePath();
			}
		}
		return null;
	}

	/**
	 * Add (system default) path separator to string (if there isn't one already)
	 * @param fName String containing path name
	 * @return String that ends with the (system default) path separator for sure
	 */
	public static String addSeparator(String fName) {
		int pos = fName.lastIndexOf(File.separator);
		if (pos != fName.length()-1) {
			pos = fName.lastIndexOf("/");
		}
		if (pos != fName.length()-1) {
			return fName + File.separator;
		} else {
			return fName;
		}
	}

	/**
	 * Exchange any DOS style path separator ("\") with a Unix style separator ("/")
	 * @param fName String containing file/path name
	 * @return String with only Unix style path separators
	 */
	public static String exchangeSeparators(String fName) {
		int pos;
		StringBuffer sb = new StringBuffer(fName);
		while ((pos = sb.indexOf("\\")) != -1) {
			sb.setCharAt(pos,'/');
		}
		return sb.toString();
	}

	/**
	 * Return file name from path
	 * @param path String of a path with a file name
	 * @return String containing only the file name
	 */
	public static String getFileName(String path) {
		int p1 = path.lastIndexOf("/");
		int p2 = path.lastIndexOf("\\");
		if (p2 > p1) {
			p1 = p2;
		}
		if (p1 < 0) {
			p1 = 0;
		} else {
			p1++;
		}
		return path.substring(p1);
	}

	/**
	 * Return path name from a file name
	 * @param path String of file name with a path
	 * @return String containing only the path (excluding path separator)
	 */
	public static String getPathName(String path) {
		int p1 = path.lastIndexOf("/");
		int p2 = path.lastIndexOf("\\");
		if (p2 > p1) {
			p1 = p2;
		}
		if (p1 < 0) {
			p1 = 0;
		}
		return path.substring(0, p1);
	}


	/**
	 * Returns the extension (".XXX") of a filename without the dot
	 * @param path String containing file name
	 * @return String containing only the extension (without the dot) or null (if no extension found)
	 */
	public static String getExtension(String path) {
		int p1 = path.lastIndexOf("/");
		int p2 = path.lastIndexOf("\\");
		int p = path.lastIndexOf(".");
		if (p==-1 || p<p1 || p<p2) {
			return null;
		}
		return path.substring(p + 1);
	}

	/**
	 * Strips the extension (".XXX") from a file name (including the dot)
	 * If no extension is found, the unchanged string is returned.
	 * @param path String containing a file name
	 * @return String to a filename without the extension
	 */
	public static String stripExtension(String path) {
		int p1 = path.lastIndexOf("/");
		int p2 = path.lastIndexOf("\\");
		int p = path.lastIndexOf(".");
		if (p == -1 || p < p1 || p < p2) {
			return path;
		}
		return path.substring(0, p);
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
