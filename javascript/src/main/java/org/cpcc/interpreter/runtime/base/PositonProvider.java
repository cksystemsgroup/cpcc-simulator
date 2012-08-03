/*
 * @(#) PositonProvider.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2012  Clemens Krainer, Michael Lippautz
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
package org.cpcc.interpreter.runtime.base;

import org.cpcc.interpreter.runtime.types.LatLngAlt;


public interface PositonProvider {
	
	/**
	 * @return the current position as a <code>PolarCoordinate</code> object.
	 */
	LatLngAlt getCurrentPosition();
	
	/**
	 * @return the current speed over ground in meters per second.
	 */
	Double getSpeedOverGround();
	
	/**
	 * @return the current course over ground in degrees. Zero degrees is north.
	 */
	Double getCourseOverGround();
	
	/**
	 * @return the current altitude over ground in meters.
	 */
	Double getAltitudeOverGround();
}
