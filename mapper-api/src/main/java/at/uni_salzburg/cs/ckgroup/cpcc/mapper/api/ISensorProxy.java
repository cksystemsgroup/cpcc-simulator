/*
 * @(#) ISensorProxy.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2011  Clemens Krainer
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

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public interface ISensorProxy {
	
	String SENSOR_NAME_SONAR = "sonar";
	String SENSOR_NAME_POSITION = "position";
	String SENSOR_NAME_WAYPOINTS = "waypoints";
	String SENSOR_NAME_RANDOM = "random";
	String SENSOR_NAME_PHOTO = "photo";
	String SENSOR_NAME_VIDEO = "video";
	String SENSOR_NAME_AIR_PRESSURE = "airPressure";
	String SENSOR_NAME_TEMPERATURE = "temperature";
	
	/**
	 * @return the current position as a <code>PolarCoordinate</code> object.
	 */
	PolarCoordinate getCurrentPosition();
	
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

	/**
	 * @param name the name of the designated sensor, e.g., sonar. 
	 * @return the current sensor value as an <code>Double</code> object.
	 */
	String getSensorValue(String name);
	
	/**
	 * @param name the name of the designated sensor, e.g., sonar. 
	 * @return the current sensor value as an <code>Double</code> object.
	 */
	Double getSensorValueAsDouble(String name);
	
	/**
	 * @param name the name of the designated sensor, e.g., sonar. 
	 * @return the current sensor value as an <code>Integer</code> object.
	 */
	Integer getSensorValueAsInteger(String name);
	
	/**
	 * @param name the name of the designated sensor, e.g., sonar. 
	 * @return the current sensor value as an <code>InputStream</code> object.
	 */
	InputStream getSensorValueAsStream(String name);
	
	/**
	 * @return the list of available sensors.
	 */
	Set<String> getAvailableSensors() throws ParseException;
	
	/**
	 * @return the associated pilot's configuration parameters.
	 */
	Map<String,String> getPilotConfig() throws ParseException;
	
	/**
	 * @return the list of way-points as JSON string.
	 */
	String getWaypoints();
}
