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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Position;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class VehicleQuery implements IQuery {
	
	private static final Logger LOG = Logger.getLogger(VehicleQuery.class);

	private static final String PROP_VEHICLE_LOCAL_NAME = "name";
	private static final String PROP_VEHICLE_STATE = "state";
	private static final String PROP_VEHICLE_LATITUDE = "latitude";
	private static final String PROP_VEHICLE_LONGITUDE = "longitude";
	private static final String PROP_VEHICLE_ALTITUDE = "altitude";
	private static final String PROP_VEHICLE_TOLERANCE = "tolerance";
	private static final String PROP_VEHICLE_ACTIONS = "actions";
	private static final String PROP_VEHICLE_ACTION_POINTS = "actionPoints";
	private static final String PROP_VEHICLE_PATH = "vehiclePath";
	
	private Map<String, IVirtualVehicle> vehicleMap;
	
	@SuppressWarnings("serial")
	private Map<String,String> actionsMap = new HashMap<String, String>() {{
		put("AIRPRESSURE", "airPressure");
		put("ALTITUDE", "altitude");
		put("COURSE", "course");
		put("PICTURE", "photo");
		put("RANDOM", "random");
		put("SONAR", "sonar");
		put("SPEED", "speed");
		put("TEMPERATURE", "temperature");
	}};

	public void setVehicleMap(Map<String, IVirtualVehicle> vehicleMap) {
		this.vehicleMap = vehicleMap;
	}
	
	@SuppressWarnings("unchecked")
	public String execute(IServletConfig config, String[] parameters) {

		Map<String, Object> obj=new LinkedHashMap<String, Object>();
		
		for (IVirtualVehicle vehicle : vehicleMap.values()) {
			if (vehicle.isFrozen()) {
				continue;
			}
			
			Map<String, Object> props = new LinkedHashMap<String, Object>();
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

			if (vehicle.getCurrentCommand() != null) {
				Position p = vehicle.getCurrentCommand().getPosition();
				props.put(PROP_VEHICLE_LATITUDE, Double.valueOf(p.getPoint().getLatitude()));
				props.put(PROP_VEHICLE_LONGITUDE, Double.valueOf(p.getPoint().getLongitude()));
				props.put(PROP_VEHICLE_ALTITUDE, Double.valueOf(p.getPoint().getAltitude()));
				props.put(PROP_VEHICLE_TOLERANCE, Double.valueOf(p.getTolerance()));
				
				List<IAction> al = vehicle.getCurrentCommand().getActions();
				JSONArray openActions = new JSONArray();
				for (IAction action : al) {
					if (!action.isComplete()) {
						openActions.add(actionsMap.get(action.toString().toUpperCase()));	
					}
				}
				props.put(PROP_VEHICLE_ACTIONS, openActions);
			}
			
			JSONArray actionPoints = new JSONArray();
			for (Command cmd : vehicle.getCommandList()) {
				JSONObject p = new JSONObject();
				p.put("latitude", cmd.getPosition().getPoint().getLatitude());
				p.put("longitude", cmd.getPosition().getPoint().getLongitude());
//				p.put("altitude", cmd.getPosition().getPt().getAltitude());
				p.put("completed", Boolean.valueOf(cmd.isFinished()));
				actionPoints.add(p);
			}
			props.put(PROP_VEHICLE_ACTION_POINTS, actionPoints);
			
			try {
				String vehicleLog = vehicle.getLog();
				VehicleLogConverter c = new VehicleLogConverter();
				JSONArray a = c.convertToVirtualVehiclePath(vehicleLog);
				props.put(PROP_VEHICLE_PATH, a);
				
			} catch (IOException e1) {
				e1.printStackTrace();
				LOG.error("Can not read log of vehicle " + vehicle.getWorkDir().getName());
			}
			
			obj.put(vehicle.getWorkDir().getName(), props);
		}
		
		return JSONValue.toJSONString(obj);
	}

}
