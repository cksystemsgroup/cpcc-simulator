/*
 * @(#) EngineInfo.java
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

public class EngineInfo {
	
	private String pilotName;
	private String positionUrl;
	private String waypointsUrl;
	private String vehicleStatusUrl;
	private String actionPointUrl;
	private String vehicleDataUrl;
	private String temperatureUrl;
	
	public EngineInfo(String pilotName, String positionUrl, String waypointsUrl, String vehicleStatusUrl, String actionPointUrl, String vehicleDataUrl, String temperatureUrl) {
		this.pilotName = pilotName;
		this.positionUrl = positionUrl;
		this.waypointsUrl = waypointsUrl;
		this.vehicleStatusUrl = vehicleStatusUrl;
		this.actionPointUrl = actionPointUrl;
		this.vehicleDataUrl = vehicleDataUrl;
		this.temperatureUrl = temperatureUrl;
	}
	
	public String getPilotName() {
		return pilotName;
	}

	public String getPositionUrl() {
		return positionUrl;
	}

	public String getWaypointsUrl() {
		return waypointsUrl;
	}

	public String getVehicleStatusUrl() {
		return vehicleStatusUrl;
	}

	public String getActionPointUrl() {
		return actionPointUrl;
	}

	public String getVehicleDataUrl() {
		return vehicleDataUrl;
	}
	
	public String getTemperatureUrl() {
		return temperatureUrl;
	}
}
