/*
 * @(#) IRegistrationData.java
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONAware;


/**
 * This interface covers the information of a registered Engine.
 */
public interface IRegistrationData extends JSONAware {
	
    /**
     * @return the registered base URL of the Engine.
     */
    String getEngineUrl();

    /**
     * @return the associated base URL of the Real Vehicle.
     */
    String getPilotUrl();

    /**
     * @return the Real Vehicle's set course as a list of waypoints.
     */
    List<IWayPoint> getWaypoints();
    
    /**
     * @return the Real Vehicle's available sensors.
     */
    Set<String> getSensors();

	/**
	 * @return true, if the Engine is a central Engine, i.e., an Engine where
	 *         completed Virtual Vehicles will be migrated to.
	 */
	boolean isCentralEngine();
	
	/**
	 * @return true, if the limit of maximum allowed errors accessing an Engine
	 *         has been reached, false otherwise. If the limit has been reached,
	 *         the Mapper deregisters the Engine automatically.
	 */
	boolean isMaxAccessErrorsLimitReached();

	/**
	 * @return Real Vehicle configuration properties, e.g., the Real Vehicle's
	 *         name.
	 */
	Map<String, String> getPilotConfig();
	
	/**
	 * @return the zone this engine is assigned to.
	 */
	IZone getAssignedZone();
	
	/**
	 * Assign this Real Vehicle to a particular zone.
	 * 
	 * @param zone the zone to assign Real Vehicle to. 
	 */
	void setAssignedZone(IZone zone);
	
	/**
	 * @return the mapper specific data for this Real Vehicle.
	 */
	Object getMapperData();
	
	/**
	 * Set the mapper internal data for this Real Vehicle.
	 * 
	 * @param mapperData the mapper specific data.
	 */
	void setMapperData(Object mapperData);
}
