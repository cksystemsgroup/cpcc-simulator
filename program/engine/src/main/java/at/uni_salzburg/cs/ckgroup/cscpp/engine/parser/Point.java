/*
 * @(#) Point.java
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

public class Point implements Serializable
//TODO check: why Serializable?
{
	
	private double latitude;
	private double longitude;
	private double altitude;

	public Point(double lat, double lon, double alt) {
		latitude = lat;
		longitude = lon;
		altitude = alt;
	}

	public double get_latitude() {
		return latitude;
	}

	public double get_longitude() {
		return longitude;
	}

	public double get_altitude() {
		return altitude;
	}
	
	public PolarCoordinate getPolarCoordinate() {
		return new PolarCoordinate(latitude, longitude, altitude);
	}

	public String toString() {
		return String.format(Locale.US, "%.8f %.8f %.3f", latitude, longitude, altitude);
	}
}