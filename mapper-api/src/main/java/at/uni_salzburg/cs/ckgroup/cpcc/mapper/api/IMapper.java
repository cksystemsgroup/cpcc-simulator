/*
 * @(#) IMapper.java
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

/**
 * The Mapper provides this interface to mapping algorithms. 
 */
public interface IMapper {

	/**
	 * @return the map of <code>IStatusProxy</code> instances. Key is the base URL of the associated Engine.
	 */
	Map<String, IStatusProxy> getStatusProxyMap();

	/**
	 * @return the list of all existing Virtual Vehicles.
	 */
	List<IVirtualVehicleInfo> getVirtualVehicleList();

	/**
	 * @return the map of registered Engines. Key is the base URL of the associated Engine.
	 */
	Map<String, IRegistrationData> getRegistrationData();

	/**
	 * @return the set of Engines where completed Virtual Vehicles will be migrated to.
	 */
	Set<String> getCentralEngines();
	
	/**
	 * Migrate a Virtual Vehicle from one Engine to another.
	 * 
	 * @param sourceEngineUrl the base URL of the Engine currently transporting the Virtual Vehicle.
	 * @param vehicleName the vehicle's local name.
	 * @param targetEngineUrl the base URL of the new Engine. 
	 */
	void migrate(String sourceEngineUrl, String vehicleName, String targetEngineUrl);
}
