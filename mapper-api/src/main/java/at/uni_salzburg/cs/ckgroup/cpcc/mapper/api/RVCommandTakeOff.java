/*
 * @(#) RVCommandTakeOff.java
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

/**
 * This class contains the attributes of the Vehicle Control Language <i>take off</i> command.
 */
public class RVCommandTakeOff implements IRVCommand {
	
	/**
	 * The time the Real Vehicle should take to lift of the ground in seconds.
	 */
	private long time;
	
	/**
	 * The take-off altitude. 
	 */
	private double altitude;

	/**
	 * @param time the time the Real Vehicle should take to lift of the ground in seconds.
	 */
	public RVCommandTakeOff(double altitude, long time) {
		this.altitude = altitude;
		this.time = time;
	}

	/**
	 * @return the time the Real Vehicle should take to lift of the ground in seconds.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time the Real Vehicle should take to lift of the ground in seconds.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the take-off altitude.
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the take-off altitude.
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	
}
