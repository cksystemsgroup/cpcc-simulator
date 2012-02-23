/*
 * @(#) ActionTemperature.java
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

import java.io.Serializable;
import java.util.Locale;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class ActionSpeed extends AbstractAction implements Serializable
{
	private static final long serialVersionUID = 3336998124182011297L;
	private double speedOverGround = 0;
	
	public double getSpeedOverGround() {
		return speedOverGround;
	}

	@Override
	protected boolean retrieveValue(ISensorProxy sprox) {
		
		Double sensorValue = sprox.getSpeedOverGround();
		if (sensorValue == null) {
			return false;
		}
		
		speedOverGround = sensorValue;
		return true;
	}
	
	@Override
	public String toString() 
	{
		if(getTimestamp() != 0)
			return String.format(Locale.US, "Speed (%d %.1f)", getTimestamp(), speedOverGround);
		else
			return "Speed";
	}
}
