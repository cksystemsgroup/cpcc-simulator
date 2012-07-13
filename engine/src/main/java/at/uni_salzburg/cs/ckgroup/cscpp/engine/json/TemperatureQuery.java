/*
 * @(#) TemperatureQuery.java
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

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.ActionTemperature;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class TemperatureQuery implements IQuery {
	
	private Map<String, IVirtualVehicle> vehicleMap;

	public void setVehicleMap(Map<String, IVirtualVehicle> vehicleMap) {
		this.vehicleMap = vehicleMap;
	}
	
	@SuppressWarnings("unchecked")
	public String execute(IServletConfig config, String[] parameters) {

		JSONArray obj = new JSONArray();
		
		for (IVirtualVehicle vehicle : vehicleMap.values()) {
			if (vehicle.isFrozen()) {
				continue;
			}
			
			List<Command> cmdList = vehicle.getCommandList();
			
			for (Command cmd : cmdList) {
				for (IAction action : cmd.getActions()) {
					if (action instanceof ActionTemperature && action.isComplete()) {
						ActionTemperature actionTemp = (ActionTemperature)action;
						JSONObject o = new JSONObject();
						o.put("lat", Double.valueOf(cmd.getPosition().getPoint().getLatitude()));
						o.put("lon", Double.valueOf(cmd.getPosition().getPoint().getLongitude()));
//						o.put("alt", Double.valueOf(cmd.getPosition().getPoint().getAltitude()));
						o.put("temp", Double.valueOf(actionTemp.getTemperature()));
						obj.add(o);
					}
				}
			}
		}
		
		return JSONValue.toJSONString(obj);
	}
}
