/*
 * @(#) IStatusProxy.java
 *
 * This code is part of the CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.api;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

/**
 * The Mapper provides an instance of <code>IStatusProxy</code> to allow mapping
 * algorithms access Real Vehicle status information.
 */
public interface IStatusProxy {

	/**
	 * Fetch the current Real Vehicle status.
	 */
	void fetchCurrentStatus();
	
	/**
	 * @return the current position of the Real Vehicle in polar coordinates,
	 *         i.e., latitude, longitude, and altitude above ground.
	 */
	PolarCoordinate getCurrentPosition();

	/**
	 * @return the end of the current Real Vehicle set course flight segment in
	 *         polar coordinates, i.e., latitude, longitude, and altitude above
	 *         ground.
	 */
	PolarCoordinate getNextPosition();

	/**
	 * @return the Real Vehicle average velocity in the current set course
	 *         flight segment.
	 */
	Double getVelocity();
	
}
