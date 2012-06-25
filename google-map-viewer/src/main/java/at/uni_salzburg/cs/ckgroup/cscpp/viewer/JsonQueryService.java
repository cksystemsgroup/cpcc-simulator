/*
 * @(#) JsonQueryService.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.viewer;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.QueryService;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.PositionQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.RealVehicleQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.VirtualVehicleQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.WaypointsQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.ZoneQuery;


public class JsonQueryService extends QueryService {
	
	public JsonQueryService (IServletConfig servletConfig, IMapperProxy mapperProxy) {
		super (servletConfig);
		queries.put("position", new PositionQuery(mapperProxy));
		queries.put("waypoints", new WaypointsQuery(mapperProxy));
		queries.put("virtualVehicle", new VirtualVehicleQuery(mapperProxy));
		queries.put("realVehicle", new RealVehicleQuery(mapperProxy));
		queries.put("zones", new ZoneQuery(mapperProxy));
	}

}
