/*
 * @(#) RegistryPersistence.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2012  Clemens Krainer
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.registry;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public class RegistryPersistence {
	
	private static Object lock = new Object();
	
	@SuppressWarnings("unchecked")
	public static void loadRegistry (File registryFile, Map<String, RegData> registrationData) throws IOException {
		synchronized (lock) {
			String reg = FileUtils.loadFileAsString(registryFile);
			JSONParser parser = new JSONParser();
			List<Object> list;
			try {
				list = (List<Object>)parser.parse(reg);
			} catch (ParseException e) {
				throw new IOException("Parsing " + registryFile.getAbsolutePath() + " failed", e);
			}
			
			for (Object entry : list) {
				RegData rd = new RegData((JSONObject)entry);
				registrationData.put(rd.getEngineUri(), rd);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void storeRegistry (File registryFile, Map<String, RegData> registrationData) throws IOException {
		synchronized (lock) {
			JSONArray obj = new JSONArray();
			for (Entry<String, RegData> entry : registrationData.entrySet()) {
				RegData rd = entry.getValue();
				obj.add(rd);
			}
			
			PrintWriter pw = new PrintWriter(registryFile);
			pw.print(obj.toString());
			pw.close();
		}
	}

}
