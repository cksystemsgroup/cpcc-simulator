/*
 * @(#) WayPoint.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.course;

import java.util.Locale;
import java.util.Map;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public class WayPoint {
	
	private PolarCoordinate point;
	private double precision;
	private double velocity;
	
	public WayPoint (Map<String,Object> jsonMap) {
		double longitude = ((Double)jsonMap.get("longitude")).doubleValue();
		double latitude = ((Double)jsonMap.get("latitude")).doubleValue();
		double altitude = ((Double)jsonMap.get("altitude")).doubleValue();
		point = new PolarCoordinate(latitude, longitude, altitude);
		precision = (Double)jsonMap.get("precision");
		velocity = (Double)jsonMap.get("velocity");		
	}

	public PolarCoordinate getPoint() {
		return point;
	}

	public double getPrecision() {
		return precision;
	}

	public double getVelocity() {
		return velocity;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "(%.8f, %.8f, %.3f) precision %.0f velocity %.1f",
				point.getLatitude(), point.getLongitude(), point.getAltitude(), precision, velocity);
	}
}
