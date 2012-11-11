/*
 * @(#) VirtualVehicleInfo.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.engmap.mapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IAction;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ITask;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleStatus;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;

public class VirtualVehicleInfo implements IVirtualVehicleInfo {

	@SuppressWarnings("serial")
	static Map<String,String> actionsMap = new HashMap<String, String>() {{
		put("AIRPRESSURE", "airPressure");
		put("ALTITUDE", "altitude");
		put("COURSE", "course");
		put("PICTURE", "photo");
		put("RANDOM", "random");
		put("SONAR", "sonar");
		put("SPEED", "speed");
		put("TEMPERATURE", "temperature");
	}};
	
	private String name;
	private String engineUrl;
	private IVirtualVehicle vehicle;

	public VirtualVehicleInfo(String name, String engineUrl, IVirtualVehicle vehicle) {
		this.name = name;
		this.engineUrl = engineUrl;
		this.vehicle = vehicle;
	}

	@Override
	public String getVehicleName() {
		return name;
	}

	@Override
	public String getEngineUrl() {
		return engineUrl;
	}

	@Override
	public IVirtualVehicleStatus getVehicleStatus() {
		return new VirtualVehicleStatus();
	}
	
	private class VirtualVehicleStatus implements IVirtualVehicleStatus {
		
		@Override
		public String getName() {
			return vehicle.getWorkDir().getName();
		}

		@Override
		public String getId() {
			return vehicle.getProperties().getProperty("vehicle.id");
		}

		@Override
		public Status getState() {
			if (vehicle.isCompleted()) {
				return Status.COMPLETED;
			}
			
			if (vehicle.isProgramCorrupted()) {
				return Status.CORRUPT;
			}

			if (vehicle.isFrozen()) {
				return Status.FROZEN;
			}
			
			return vehicle.isActive() ? Status.ACTIVE : Status.SUSPENDED;
		}

		@Override
		public PolarCoordinate getPosition() {
			return vehicle.getCurrentTask() != null ? vehicle.getCurrentTask().getPosition() : null;
		}

		@Override
		public double getTolerance() {
			return vehicle.getCurrentTask().getTolerance();
		}

		@Override
		public Set<String> getActions() {
			Set<String> actions = new HashSet<String>();
			
			for (IAction action : vehicle.getCurrentTask().getActionList()) {
				if (!action.isComplete()) {
					actions.add(actionsMap.get(action.toString().toUpperCase()));
				}
			}
			
			return actions;
		}
		
		public ITask getCurrentTask() {
			return vehicle.getCurrentTask();
		}
	}

}
