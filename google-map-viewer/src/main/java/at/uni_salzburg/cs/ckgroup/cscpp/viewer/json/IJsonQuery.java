/*
 * @(#) IJsonQuery.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.viewer.json;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;


public interface IJsonQuery extends IQuery {

	String PROP_PILOT_PREFIX = "pilot.";
	String PROP_PILOT_LIST = PROP_PILOT_PREFIX + "list";
	String PROP_PILOT_NAME = ".name";
	String PROP_PILOT_POSITION_URL = ".position.url";
	String PROP_PILOT_WAYPOINTS_URL = ".waypoints.url";
	String PROP_PILOT_VEHICLE_STATUS_URL = ".vehicle.status.url";
	
//	void setMapperProxy(IMapperProxy mapperProxy);
}
