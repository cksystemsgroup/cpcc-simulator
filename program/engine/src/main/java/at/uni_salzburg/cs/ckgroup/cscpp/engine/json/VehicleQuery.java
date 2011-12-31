/*
 * @(#) VehicleQuery.java
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONValue;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class VehicleQuery implements IQuery {

	private Map<String, IVirtualVehicle> vehicleMap;

	public void setVehicleMap(Map<String, IVirtualVehicle> vehicleMap) {
		this.vehicleMap = vehicleMap;
	}
	
	public String execute(IServletConfig config, String[] parameters) {

		Map<String, Object> obj=new LinkedHashMap<String, Object>();
		
		for (IVirtualVehicle vehicle : vehicleMap.values()) {
			Map<String, Object> props = new LinkedHashMap<String, Object>();
			for (Entry<Object, Object> e : vehicle.getProperties().entrySet())
				props.put((String)e.getKey(), e.getValue());
			obj.put(vehicle.getWorkDir().getName(), props);
		}
		
		return JSONValue.toJSONString(obj);
	}

}
