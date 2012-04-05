/*
 * @(#) RVCommandFlyTo.java
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
 * This class contains the attributes of the Vehicle Control Language <i>fly to</i> command.
 */
public class RVCommandFlyTo implements IRVCommand, IWayPoint {

	/**
	 * The waypoint the Real Vehicle has to pass in polar coordinates, i.e.,
	 * latitude, longitude, and altitude above ground.
	 */
	private PolarCoordinate point;
	
	/**
	 * The precision in meters the Real Vehicle has to come near the specified
	 * waypoint to consider it as reached.
	 */
	private double precision;
	
	/**
	 * The average velocity of the Real Vehicle to approach the waypoint.
	 */
	private double velocity;
	
	/**
	 * @param point
	 *            the waypoint the Real Vehicle has to pass in polar
	 *            coordinates, i.e., latitude, longitude, and altitude above
	 *            ground.
	 * @param precision
	 *            the precision in meters the Real Vehicle has to come near the
	 *            specified waypoint to consider it as reached.
	 * @param velocity
	 *            the average velocity of the Real Vehicle to approach the
	 *            waypoint.
	 */
	public RVCommandFlyTo(PolarCoordinate point, double precision, double velocity) {
		this.point = point;
		this.precision = precision;
		this.velocity = velocity;
	}

	/**
	 * @param point
	 *            the waypoint the Real Vehicle has to pass in polar
	 *            coordinates, i.e., latitude, longitude, and altitude above
	 *            ground.
	 */
	public void setPoint(PolarCoordinate point) {
		this.point = point;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IWayPoint#getPoint()
	 */
	@Override
	public PolarCoordinate getPoint() {
		return point;
	}

	/**
	 * @param precision
	 *            the precision in meters the Real Vehicle has to come near the
	 *            specified waypoint to consider it as reached.
	 */
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IWayPoint#getPrecision()
	 */
	@Override
	public double getPrecision() {
		return precision;
	}
	
	/**
	 * @param velocity
	 *            the average velocity of the Real Vehicle to approach the
	 *            waypoint.
	 */
	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IWayPoint#getVelocity()
	 */
	@Override
	public double getVelocity() {
		return velocity;
	}

}
