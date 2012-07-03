/*
 * @(#) ActionPointQuery.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.json;

import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class ActionPointQuery implements IQuery {
	
//	private static final Logger LOG = Logger.getLogger(ActionPointQuery.class);

	private static final String PROP_VEHICLE_LOCAL_NAME = "name";
	private static final String PROP_VEHICLE_STATE = "state";
//	private static final String PROP_VEHICLE_LATITUDE = "latitude";
//	private static final String PROP_VEHICLE_LONGITUDE = "longitude";
//	private static final String PROP_VEHICLE_ALTITUDE = "altitude";
//	private static final String PROP_VEHICLE_TOLERANCE = "tolerance";
	private static final String PROP_VEHICLE_ACTIONS = "actions";
	private static final String PROP_VEHICLE_ACTION_POINT = "actionPoint";

	private Map<String, IVirtualVehicle> vehicleMap;
	
//	@SuppressWarnings("serial")
//	private Map<String,String> actionsMap = new HashMap<String, String>() {{
//		put("AIRPRESSURE", "airPressure");
//		put("ALTITUDE", "altitude");
//		put("COURSE", "course");
//		put("PICTURE", "photo");
//		put("RANDOM", "random");
//		put("SONAR", "sonar");
//		put("SPEED", "speed");
//		put("TEMPERATURE", "temperature");
//	}};

	public void setVehicleMap(Map<String, IVirtualVehicle> vehicleMap) {
		this.vehicleMap = vehicleMap;
	}
	
	@SuppressWarnings("unchecked")
	public String execute(IServletConfig config, String[] parameters) {

		String vehicleId = parameters[3];
		String apIndexString = parameters[4];
		
		if (vehicleId == null || "".equals(vehicleId) || apIndexString == null || !apIndexString.matches("\\d+")) {
			return JSONValue.toJSONString("Invalid query!");
		}
		
		IVirtualVehicle vehicle = vehicleMap.get(vehicleId);
			
		if (vehicle == null) {
			return JSONValue.toJSONString("Vehicle not found (anymore)!");
		}
		
		int actionPointIndex = Integer.valueOf(apIndexString);

		if (actionPointIndex > vehicle.getCommandList().size()-1) {
			return JSONValue.toJSONString("No such action point!");
		}
		
		JSONObject props = new JSONObject();
		for (Entry<Object, Object> e : vehicle.getProperties().entrySet()) {
			props.put((String)e.getKey(), e.getValue());
		}
		
		props.put(PROP_VEHICLE_LOCAL_NAME, vehicle.getWorkDir().getName());
		
		if(vehicle.isProgramCorrupted()) {
			props.put(PROP_VEHICLE_STATE, "corrupt");
		} else if(vehicle.isCompleted()) {
			props.put(PROP_VEHICLE_STATE, "completed");
		} else if (vehicle.isActive()) {
			props.put(PROP_VEHICLE_STATE, "active");
		} else {
			props.put(PROP_VEHICLE_STATE, "suspended");
		}
		
		Command cmd = vehicle.getCommandList().get(actionPointIndex);
			
		props.put(PROP_VEHICLE_ACTION_POINT, cmd.getPosition());

		JSONArray act = new JSONArray();
		for (IAction a : cmd.getActions()) {
			act.add(a);
		}
		props.put(PROP_VEHICLE_ACTIONS, act);
		
		return JSONValue.toJSONString(props);
	}

}
