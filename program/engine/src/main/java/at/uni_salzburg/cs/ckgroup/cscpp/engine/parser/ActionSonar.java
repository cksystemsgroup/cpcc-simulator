/*
 * @(#) ActionPicture.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2011  Clemens Krainer, Michael Kleber, Andreas Schroecker, Bernhard Zechmeister
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class ActionSonar extends AbstractAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7510139159604214396L;
	private String filename = null;
	private File dataDir;

	@Override
	public boolean execute(ISensorProxy sprox) 
	{
		String sonar = sprox.getSensorValue(ISensorProxy.SENSOR_NAME_SONAR);
		if (sonar == null)
			return false;
		
		try 
		{
			File f = File.createTempFile("sonar", ".str", dataDir);
			filename = f.getName();
			FileWriter fw = new FileWriter(f);
			fw.write(sonar);
			fw.close();
			
			saveTimestamp();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public String toString() 
	{
		String s =  super.toString();
		if(getTimestamp() != 0)
			s += String.format(Locale.US, "Sonar (\"%s\")", filename);
		return s;
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
