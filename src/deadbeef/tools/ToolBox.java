package deadbeef.tools;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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
 * @author 0xdeadbeef
 */
public final class ToolBox {

	private static final DecimalFormat DECIMAL_FORMAT;
	static {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DECIMAL_FORMAT = new DecimalFormat("##.###", dfs);
	}
	private static final Pattern TIME_PATTERN = Pattern.compile( "(\\d+):(\\d+):(\\d+)[:\\.](\\d+)" );

	/**
	 * Convert an integer to a string with leading zeroes
	 * @param i Integer value to convert
	 * @param digits Number of digits to display (note: a 32bit number can have only 10 digits)
	 * @return String version of integer with trailing zeroes
	 */
	public static String zeroTrim(int i, int digits) {
		String s = String.valueOf(i);
		int l = s.length();
		if (l < digits) {
			s = "0000000000".substring(0, digits-l)+s;
		}
		return s;
	}

	/**
	 * Convert a long integer to a C-style hex string with leading zeroes
	 * @param val Integer value to convert
	 * @param digits Number of digits to display (note: a 32bit hex number can have only 8 digits)
	 * @return hex String version of integer with trailing zeroes (and starting with "0x")
	 */
	public static String hex(long val, int digits) {
		String s = Long.toString(val, 16);
		int l = s.length();
		if (l < digits) {
			s = "00000000".substring(0, digits-l) + s;
		}
		return "0x" + s;
	}

	/**
	 * Convert an integer to a C-style hex string with leading zeroes
	 * @param val Integer value to convert
	 * @param digits Number of digits to display (note: a 32bit hex number can have only 8 digits)
	 * @return hex String version of integer with trailing zeroes (and starting with "0x")
	 */
	public static String hex(int val, int digits) {
		String s = Integer.toString(val, 16);
		int l = s.length();
		if (l < digits) {
			s = "00000000".substring(0, digits-l) + s;
		}
		return "0x" + s;
	}

	/** Format double as string in the form "xx.yyy"
	 * @param d Double value
	 * @return Formatted string
	 */
	public static String formatDouble(final double d) {
		return DECIMAL_FORMAT.format(d);
	}

	/**
	 * Convert time in milliseconds to array containing hours, minutes, seconds and milliseconds
	 * @param ms Time in milliseconds
	 * @return Array containing hours, minutes, seconds and milliseconds (in this order)
	 */
	public static int[] msToTime(long ms) {
		int time[] = new int[4];
		// time[0] = hours
		time[0] = (int)(ms / (60*60*1000));
		ms -= time[0] * 60*60*1000;
		// time[1] = minutes
		time[1] = (int)(ms / (60*1000));
		ms -= time[1] * 60*1000;
		// time[2] = seconds
		time[2] = (int)(ms / 1000);
		ms -= time[2] *1000;
		time[3] = (int)ms;
		return time;
	}

	/**
	 * Convert time in 90kHz ticks to string hh:mm:ss.ms
	 * @param pts Time in 90kHz resolution
	 * @return String in format hh:mm:ss:ms
	 */
	public static String ptsToTimeStr(long pts) {
		int time[] = msToTime((pts + 45) / 90);
		return zeroTrim(time[0], 2) + ":" + zeroTrim(time[1], 2) + ":"
				+ zeroTrim(time[2], 2) + "." + zeroTrim(time[3], 3);
	}

	/**
	 * Convert time in 90kHz ticks to string hh:mm:ss:ms
	 * @param pts Time in 90kHz resolution
	 * @return String in format hh:mm:ss:ms
	 */
	public static String ptsToTimeStrIdx(long pts) {
		int time[] = msToTime((pts + 45) / 90);
		return zeroTrim(time[0], 2) + ":" + zeroTrim(time[1], 2) + ":"
				+ zeroTrim(time[2], 2) + ":" + zeroTrim(time[3], 3);
	}

	/**
	 * Convert time in 90kHz ticks to string hh:mm:ss:ff (where ff is number of frames)
	 * @param pts Time in 90kHz resolution
	 * @param fps Frames per second
	 * @return String in format hh:mm:ss:ff
	 */
	public static String ptsToTimeStrXml(long pts, double fps) {
		int time[] = msToTime((pts + 45) / 90);
		return zeroTrim(time[0], 2) + ":" + zeroTrim(time[1], 2) + ":"
				+ zeroTrim(time[2], 2) + ":" + zeroTrim((int)(fps*time[3] / 1000.0 + 0.5), 2);
	}

	/**
	 * Convert string in hh:mm:ss.ms or hh:mm:ss:ms format to time in 90kHz resolution
	 * @param s String in hh:mm:ss.ms or hh:mm:ss:ms format
	 * @return Time in 90kHz resolution
	 */
	public static long timeStrToPTS(String s) {
		Matcher m = TIME_PATTERN.matcher(s);
		if (m.matches()) {
			long hour = Integer.parseInt(m.group(1));
			long min = Integer.parseInt(m.group(2));
			long sec = Integer.parseInt(m.group(3));
			long ms  = Integer.parseInt(m.group(4));

			long temp = hour * 60;
			temp += min;
			temp *= 60;
			temp += sec;
			temp *= 1000;
			return (temp+ms) * 90;
		} else {
			return -1;
		}
	}

	/**
	 * Convert string in hh:mm:ss:ff format to time in 90kHz resolution
	 * @param s String in hh:mm:ss:ff format
	 * @param fps Frames per second
	 * @return Time in 90kHz resolution
	 */
	public static long timeStrXmlToPTS(String s, double fps) {
		Matcher m = TIME_PATTERN.matcher(s);
		if (m.matches()) {
			long hour    = Integer.parseInt(m.group(1));
			long min     = Integer.parseInt(m.group(2));
			long sec     = Integer.parseInt(m.group(3));
			long frames  = Integer.parseInt(m.group(4));

			long temp = hour * 60;
			temp += min;
			temp *= 60;
			temp += sec;
			temp *= 1000;
			return (temp + (int)(frames / fps * 1000.0 + 0.5)) * 90;
		} else {
			return -1;
		}
	}

	/**
	 * Read byte from a buffer from position index
	 * @param buffer Byte array
	 * @param index Index to read from
	 * @return Integer value of byte buffer[index]
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static int getByte(byte buffer[], int index) throws ArrayIndexOutOfBoundsException {
		return buffer[index] & 0xff;
	}

	/**
	 * Read (big endian) word from a buffer from position index
	 * @param buffer Byte array
	 * @param index Index to read from
	 * @return Integer value of word starting at buffer[index] (index points at most significant byte)
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static int getWord(byte buffer[], int index) throws ArrayIndexOutOfBoundsException {
		return (buffer[index+1] & 0xff) | ((buffer[index] & 0xff)<<8);
	}

	/**
	 * Write byte to buffer[index]
	 * @param buffer Byte array
	 * @param index Index to write to
	 * @param val Integer value of byte to write
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static void setByte(byte buffer[], int index, int val) throws ArrayIndexOutOfBoundsException {
		buffer[index] = (byte)(val);
	}

	/**
	 * Write (big endian) word to buffer[index] (index points at most significant byte)
	 * @param buffer Byte array
	 * @param index Index to write to
	 * @param val Integer value of word to write
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static void setWord(byte buffer[], int index, int val) throws ArrayIndexOutOfBoundsException {
		buffer[index]   = (byte)(val>>8);
		buffer[index+1] = (byte)(val);
	}

	/**
	 * Write (big endian) double word to buffer[index] (index points at most significant byte)
	 * @param buffer Byte array
	 * @param index Index to write to
	 * @param val Integer value of double word to write
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static void setDWord(byte buffer[], int index, int val) throws ArrayIndexOutOfBoundsException {
		buffer[index]   = (byte)(val>>24);
		buffer[index+1] = (byte)(val>>16);
		buffer[index+2] = (byte)(val>>8);
		buffer[index+3] = (byte)(val);
	}

	/**
	 * Write ASCII string to buffer[index] (no special handling for multi-byte characters)
	 * @param buffer Byte array
	 * @param index Index to write to
	 * @param s String containing ASCII characters
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static void setString(byte buffer[], int index, String s) throws ArrayIndexOutOfBoundsException {
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
