/*
 * @(#) QueryService.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.json;

import java.util.Map;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.QueryService;


public class JsonQueryService extends QueryService {
	
	Logger LOG = Logger.getLogger(QueryService.class);
	
	public JsonQueryService (IServletConfig servletConfig) {
		super (servletConfig);
		queries.put("vehicle", new VehicleQuery());
		queries.put("actionPoint", new ActionPointQuery());
	}

	public void setVehicleMap (Map<String,IVirtualVehicle> vehicleMap) {
		for (IQuery query : queries.values()) {
			if (query instanceof VehicleQuery) {
				VehicleQuery vehicleQuery = (VehicleQuery)query;
				vehicleQuery.setVehicleMap(vehicleMap);
			} else if (query instanceof ActionPointQuery) {
				ActionPointQuery actionPointQuery = (ActionPointQuery)query;
				actionPointQuery.setVehicleMap(vehicleMap);
			}
		}
	}
}
