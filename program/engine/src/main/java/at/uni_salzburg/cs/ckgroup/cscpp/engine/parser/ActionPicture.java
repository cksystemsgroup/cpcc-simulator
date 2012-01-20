/*
 * @(#) ActionPicture.java
 *
 * This code is part of the ESE CPCC project.
 * Copyright (c) 2012  Clemens Krainer, Michael Kleber, Andreas Schroecker, Bernhard Zechmeister
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class ActionPicture extends AbstractAction implements Serializable {
	
	private static final long serialVersionUID = -6396710753568266987L;
	private String filename = null;
	private File dataDir;

	@Override
	protected boolean retrieveValue(ISensorProxy sprox) 
	{
		InputStream instream = sprox.getSensorValueAsStream(ISensorProxy.SENSOR_NAME_PHOTO);
		if (instream == null) {
			return false;
		}
		
		try 
		{
			File f = File.createTempFile("img", ".png", dataDir);
			filename = f.getName();
			FileOutputStream o = new FileOutputStream(f);

			int l;
			byte[] tmp = new byte[2048];
			while ((l = instream.read(tmp)) != -1) 
			{
				o.write(tmp, 0, l);
			}
			o.close();
			return true;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		return false;
	}
	
	@Override
	public String toString() 
	{
		if(getTimestamp() != 0)
			return String.format(Locale.US, "Picture (%d \"%s\")", getTimestamp(), filename);
		else
			return "Picture";
	}

	public String getFilename() 
	{
		return filename;
	}

	public void setDataDir(File dataDir) 
	{
		this.dataDir = dataDir;
	}
}
