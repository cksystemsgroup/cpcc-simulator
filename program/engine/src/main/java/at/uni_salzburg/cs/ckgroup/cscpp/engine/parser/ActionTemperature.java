/*
 * @(#) ActionTemperature.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2011  Clemens Krainer, Michael Kleber, Andreas Schröcker, Bernhard Zechmeister
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

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class ActionTemperature implements IAction, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6435936757132071656L;
	private double temperature = 0;
	private long timestamp;

	public double getTemperature() {
		return temperature;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean execute(ISensorProxy sprox) {
		temperature = sprox
				.getSensorValueAsDouble(ISensorProxy.SENSOR_NAME_TEMPERATURE);
		timestamp = System.currentTimeMillis();
		return false;
	}

	public String toString() {
		return "Temperature";
	}
}
