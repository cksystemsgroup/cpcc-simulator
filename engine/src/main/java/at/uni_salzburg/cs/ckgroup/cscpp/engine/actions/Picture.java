/*
 * @(#) Picture.java
 *
 * This code is part of the CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ISensorProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public class Picture extends AbstractAction {
	
	private static final Logger LOG = Logger.getLogger(Picture.class);

	private String filename = null;
	
	private File dataDir;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public File getDataDir() {
		return dataDir;
	}
	
	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}
	
	@Override
	protected boolean retrieveValue(ISensorProxy sprox) 
	{
		InputStream inStream = sprox.getSensorValueAsStream(ISensorProxy.SENSOR_NAME_PHOTO);
		if (inStream == null) {
			return false;
		}
		
		try {
			File f = File.createTempFile("img", ".png", dataDir);
			setFilename(f.getName());
			FileOutputStream outStream = new FileOutputStream(f);			
			FileUtils.copyStream(inStream, outStream, true);
			return true;
		} catch (IOException e) {
			LOG.error("Reading sensor " + ISensorProxy.SENSOR_NAME_PHOTO, e);
		}

		return false;
	}
	
	@Override
	public String toString() 
	{
		if (isComplete()) {
			return String.format(Locale.US, "Picture (%d, \"%s\")", getTimestamp(), getFilename());
		}
		return "Picture";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject o = new JSONObject();
		o.put("type", ISensorProxy.SENSOR_NAME_PHOTO);
		if (getTimestamp() != 0) {
			o.put("time", getTimestamp());
			o.put("value", getFilename());
		}
		return o.toJSONString();
	}
}
