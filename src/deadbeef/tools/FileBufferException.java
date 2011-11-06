package deadbeef.tools;

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
 * Generic exception class for FileBuffer.
 *
 * @author 0xdeadbeef
 */
public class FileBufferException extends Exception {
	final static long serialVersionUID = 0x000000001;

	/**
	 * Constructor
	 */
	public FileBufferException() {
		super();
	}

	/**
	 * Constructor.
	 * @param s Exception string
	 */
	public FileBufferException(final String s) {
		super(s);
	}
}