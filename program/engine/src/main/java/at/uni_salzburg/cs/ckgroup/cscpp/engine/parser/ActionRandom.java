/*
 * @(#) ActionTemperature.java
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

import java.io.Serializable;
import java.util.Locale;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class ActionRandom extends AbstractAction implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2187229388004412298L;
	private int random = 0;
	
	public int getRandom() {
		return random;
	}

	@Override
	public boolean execute(ISensorProxy sprox) 
	{
		random = sprox.getSensorValueAsInteger(ISensorProxy.SENSOR_NAME_RANDOM);
		saveTimestamp();
		return false;
	}

	public String toString() 
	{
		String s =  super.toString();
		if(getTimestamp() != 0)
			s += String.format(Locale.US, "Random: %d", random);
		return s;
	}
}
