/*
 * @(#) Utils.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2012  Clemens Krainer, Michael Lippautz
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
package org.cpcc.interpreter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

public class Utils {

	
	public static byte[] serialize(Object c, ScriptableObject scope) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ScriptableOutputStream sos = new ScriptableOutputStream(baos, scope);
		sos.addExcludedName("org.cpcc.interpreter.runtime.TransientState");
		sos.excludeStandardObjectNames();
		sos.writeObject(c);
		sos.close();
		return baos.toByteArray();
	}
	
	public static Object deserialize(byte[] blob, ScriptableObject scope) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(blob);
		ScriptableInputStream sis = new ScriptableInputStream(bais, scope);
		Object c = (Object)sis.readObject();
		sis.close();
		return c;
	}
	
	public static String compactPath(String path) throws FileNotFoundException {
		
		StringTokenizer tokenizer = new StringTokenizer(path.trim(), "/\\");
		
		List<String> tokens = new ArrayList<String>();
		
		while (tokenizer.hasMoreTokens()) {
			String currentToken = tokenizer.nextToken();
			
			if (".".equals(currentToken)) {
				continue;
			}

			if ("..".equals(currentToken)) {
				if (tokens.size() == 0) {
					throw new FileNotFoundException("Invalid path name: " + path);
				}				
				tokens.remove(tokens.size()-1);
			} else {
				tokens.add(currentToken);
			}
		}
		
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String e : tokens) {
			if (first) {
				first = false;
			} else {
				b.append(File.separator);
			}
			b.append(e);
		}
		
		return b.toString();
	}
}
