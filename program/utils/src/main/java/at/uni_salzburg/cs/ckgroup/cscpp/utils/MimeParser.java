/*
 * @(#) MimeParser.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2011  Clemens Krainer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package at.uni_salzburg.cs.ckgroup.cscpp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MimeParser {
	
	String contentType;
	String separator;
	String separatorCR;
	String terminator;
	String terminatorCR;
	
	public MimeParser(String contentType) {
		if (contentType == null)
			throw new NullPointerException("Content type is null.");
			
		String[] x = contentType.split(";\\s*boundary=");
		
		if (x.length == 0 || !"multipart/form-data".equals(x[0]))
			throw new IllegalArgumentException("Content type is not a valid 'multipart/form-data'.");
		
		separator = "--" + x[1];
		separatorCR = separator  + "\r";
		terminator = separator + "--";
		terminatorCR = terminator + "\r";
	}

	public List<MimeEntry> parse (InputStream inStream) throws IOException {
		
		List<MimeEntry> list = new ArrayList<MimeEntry>();
		MimeEntry currentEntry = null;
		
		boolean head = false;
		
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		
		int ch;
		while ( (ch = inStream.read()) >= 0) {
			
			if (ch == '\n') {
				if (tmp.toString().equals(terminator) || tmp.toString().equals(terminatorCR)) {
//					System.out.println("The end: " + tmp.toString());
					break;
				}
				
				if (tmp.toString().equals(separator) || tmp.toString().equals(separatorCR)) {
//					System.out.println("Delimiter: " + tmp.toString());
					head = true;
					currentEntry = new MimeEntry();
					list.add(currentEntry);
					tmp.reset();
					continue;
				}
				
				if (head && (tmp.size() == 0 || tmp.toString().equals("\r"))) {
					head = false;
					tmp.reset();
					continue;
				}
			
				if (head) {
					currentEntry.addHeader(tmp.toString());
				} else {
					tmp.write(ch);
					currentEntry.addBody(tmp.toByteArray());
				}
				tmp.reset();
			} else {
				tmp.write(ch);
			}
		}
		
		return list;
	}
	
}
