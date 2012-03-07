/*
 * @(#) Position.java
 * This code is part of the ESE CPCC project.
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

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public class Position implements Serializable
{
	private static final long serialVersionUID = 4207943401820191787L;
	private double tolerance;

	private double latitude;
	private double longitude;
	private double altitude;
	
	
	public Position(PolarCoordinate pt, double tol) {
		latitude = pt.getLatitude();
		longitude = pt.getLongitude();
		altitude = pt.getAltitude();
		
		tolerance = tol;
	}

	public PolarCoordinate getPoint() {
		return new PolarCoordinate(latitude, longitude, altitude);
	}

	public double getTolerance() {
		return tolerance;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.US, "Point %.8f %.8f %.3f tolerance %.1f", 
				latitude, longitude, altitude, tolerance);
	}
}
