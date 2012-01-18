/*
 * @(#) VehicleStatus.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public class VehicleStatus implements JSONAware {
	
	enum Status {
		NONE, SUSPENDED, ACTIVE, CORRUPT, COMPLETED
	};
	
	private String name;
	private String id;
	private Status state;
	private PolarCoordinate position = null;
	private double tolerance = Double.NaN;
	private Set<String> actions = null;
	
	public VehicleStatus(JSONObject obj) {
		name = (String)obj.get("name");
		id = (String)obj.get("vehicle.id");
		state = Status.valueOf(((String)obj.get("state")).toUpperCase());
		if (state == Status.ACTIVE || state == Status.SUSPENDED) {
			double latitude = ((Double)obj.get("latitude")).doubleValue();
			double longitude = ((Double)obj.get("longitude")).doubleValue();
			double altitude = ((Double)obj.get("altitude")).doubleValue();
			position = new PolarCoordinate(latitude, longitude, altitude);
			tolerance = (Double)obj.get("tolerance");
			JSONArray as = (JSONArray)obj.get("actions");
			actions = new HashSet<String>();
			for (int k=0; k < as.size(); ++k) {
				actions.add((String)as.get(k));
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public Status getState() {
		return state;
	}

	public PolarCoordinate getPosition() {
		return position;
	}

	public double getTolerance() {
		return tolerance;
	}

	public Set<String> getActions() {
		return actions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("vehicle.id", id);
		obj.put("state", state.toString().toLowerCase());
		if (position != null) {
			obj.put("latitude", position.getLatitude());
			obj.put("longitude", position.getLongitude());
			obj.put("altitude", position.getAltitude());
		}
		obj.put("tolerance", tolerance);
		
		if (actions != null) {
			List<String> as = new ArrayList<String>();
			as.addAll(actions);
			obj.put("actions", as);
		}
		return obj.toJSONString();
	}

}
