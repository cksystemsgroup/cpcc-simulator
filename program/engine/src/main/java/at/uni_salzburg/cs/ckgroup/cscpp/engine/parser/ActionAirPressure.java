/*
 * @(#) ActionTemperature.java
 *
 * This code is part of the JNavigator project.
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

import java.io.Serializable;
import java.util.Locale;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class ActionAirPressure extends AbstractAction implements Serializable
{
	private static final long serialVersionUID = 2018138210673084098L;
	private double airPressure = 0;
	
	public double getAirPressure() {
		return airPressure;
	}

	@Override
	public boolean execute(ISensorProxy sprox) 
	{
		airPressure = sprox.getSensorValueAsDouble(ISensorProxy.SENSOR_NAME_AIR_PRESSURE);
		saveTimestamp();
		return true;
	}
	
	@Override
	public String toString() 
	{
		if(getTimestamp() != 0)
			return String.format(Locale.US, "AirPressure (%d %.1f)", getTimestamp(), airPressure);
		else
			return "AirPressure";
	}
}
