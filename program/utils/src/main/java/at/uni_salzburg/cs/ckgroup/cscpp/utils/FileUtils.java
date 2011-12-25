/*
 * @(#) FileUtils.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
	
	public static void ensureDirectory (File dir) throws IOException {
		if (!dir.exists())
			dir.mkdirs();
			
		if (!dir.exists())
			throw new IOException("Can not create directory " + dir);
	}

	public static void zipToStream (File file, String baseDir, ZipOutputStream out) throws IOException {
		String name = (".".equals(baseDir) ? "" : baseDir + File.separator) + file.getName();

		ZipEntry e = new ZipEntry(name + (file.isFile() ? "" : File.separator));
		e.setTime(file.lastModified());
		e.setCompressedSize(file.length());
		e.setSize(file.length());
		out.putNextEntry(e);
		
		if (file.isFile()) {
			InputStream inStream = new FileInputStream(file);
			int len;
			byte[] buf = new byte[8192];
			while ((len = inStream.read(buf)) >= 0) {
				out.write(buf, 0, len);
			}
			out.closeEntry();
		} else {
			out.closeEntry();
			for (String n : file.list()) {
				File f = new File (file, n);
				zipToStream(f, name, out);
			}
		}
	}
	
}
