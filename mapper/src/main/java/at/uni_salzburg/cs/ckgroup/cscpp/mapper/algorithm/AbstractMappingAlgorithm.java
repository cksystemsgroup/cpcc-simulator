/*
 * @(#) AbstractMappingAlgorithm.java
 *
 * This code is part of the JESE-CPCC project.
 * Copyright (c) 2012  Clemens Krainer, Michael Kleber
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;

@Deprecated
public abstract class AbstractMappingAlgorithm extends Thread implements IMappingAlgorithm {
	
	private static final Logger LOG = Logger.getLogger(AbstractMappingAlgorithm.class);
	
	private boolean running = false;
	
	private boolean paused = false;

	private long cycleTime = 1000;
	
	private Map<String,StatusProxy> statusProxyMap = new HashMap<String, StatusProxy>();
	private List<VehicleInfo> virtualVehicleList = new ArrayList<VehicleInfo>();
	private Map<String,RegData> registrationData;
	private Set<String> centralEngines;
	
	public void setRegistrationData(Map<String, RegData> registrationData) {
		this.registrationData = registrationData;
	}

	public void setCentralEngines(Set<String> centralEngines) {
		this.centralEngines = centralEngines;
	}
	
	@Override
	public boolean isRunning() {
		return running;
	}
	
	@Override
	public boolean isPaused() {
		return paused;
	}

	@Override
	public void terminate() {
		running = false;
		this.interrupt();
	}
	
	@Override
	public void cease() {
		paused = true;
	}
	
	@Override
	public void proceed() {
		paused = false;
	}
	
	@Override
	public void run() {
		if (registrationData == null)
			throw new NullPointerException("Registration data not available!");
		
		running = true;
		proceed();
		while (running) {
			
			if (!paused) {
				// create/remove StatusProxy objects.
				renewStatusProxyMap();
				
				// get real vehicle status
				getPilotStatii();
				
				// get virtual vehicle status
				try {
					getEngineStatii();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				execute();
			}
			try { Thread.sleep(cycleTime); } catch (InterruptedException e) { }
		}
	}

	private void renewStatusProxyMap() {
		for (RegData rd : registrationData.values()) {
			if (!statusProxyMap.containsKey(rd.getEngineUri()) && rd.getPilotUri() != null) {
				statusProxyMap.put(rd.getEngineUri(), new StatusProxy(rd.getPilotUri()));
			}
		}
		
		for (String key : statusProxyMap.keySet()) {
			if (!registrationData.containsKey(key)) {
				statusProxyMap.remove(key);
			}
		}
	}
	
	private void getPilotStatii() {
		for (StatusProxy proxy : statusProxyMap.values()) {
			proxy.fetchCurrentStatus();
		}
	}
		
	private void getEngineStatii() throws ParseException {
		
		List<String> toBeUnregistered = new ArrayList<String>();
		
		JSONParser parser = new JSONParser();
		virtualVehicleList.clear();
		for (RegData rd : registrationData.values()) {
			String key = rd.getEngineUri();
			String engineVehicleURL = key + "/json/vehicle";
			
			String position = null;
			try {
				position = HttpQueryUtils.simpleQuery(engineVehicleURL);
			} catch (IOException e) {
				LOG.error("Can not query Engine at " + key, e);
				if (!rd.isMaxAccessErrorsLimitReached()) {
					continue;
				} else {
					toBeUnregistered.add(key);
				}
			}
			
			if (position == null || position.trim().isEmpty()) {
				continue;
			}
			JSONObject obj = (JSONObject)parser.parse(position);
			
			Map<String, VehicleStatus> vehicles = new HashMap<String, VehicleStatus>(); 
			for (Object o : obj.entrySet()) {
				@SuppressWarnings("unchecked")
				Entry<String, JSONObject> entry = (Entry<String, JSONObject>) o;
				VehicleStatus status = new VehicleStatus(entry.getValue());
				vehicles.put(entry.getKey(), status);
				
				VehicleInfo data = new VehicleInfo();
				data.setVehicleName(entry.getKey());
				data.setEngineUrl(rd.getEngineUri());
				data.setVehicleStatus(status);
				virtualVehicleList.add(data);
			}
		}
		
		for (String engineUrl : toBeUnregistered) {
			LOG.info("Unregistering extinct Engine at " + engineUrl);
			registrationData.remove(engineUrl);
		}
	}
	
	protected void migrate(String sourceEngineUrl, String vehicleName, String targetEngineUrl) {
		
		if (sourceEngineUrl.equals(targetEngineUrl)) {
			LOG.debug("Migration cancelled, because source engine is target engine: " + targetEngineUrl);
			return;
		}

		String migrationUrl = sourceEngineUrl
				+ "/vehicle/text/vehicleMigration?vehicleIDs=" + vehicleName
				+ "&vehicleDst=" + targetEngineUrl
				+ "/vehicle/text/vehicleUpload";
		LOG.info("Migration: " + migrationUrl);

		try {
			String ret = HttpQueryUtils.simpleQuery(migrationUrl);
			if ("OK".equals(ret)) {
				LOG.info("Migration succeeded. " + migrationUrl + ", " + ret);
			} else {
				LOG.error("Migration failed. " + migrationUrl + ", " + ret);
			}
		} catch (IOException ex) {
			LOG.error("Migration failed. " + migrationUrl, ex);
		}

	}
	
	public Map<String, StatusProxy> getStatusProxyMap() {
		return statusProxyMap;
	}

	public List<VehicleInfo> getVirtualVehicleList() {
		return virtualVehicleList;
	}

	public Map<String, RegData> getRegistrationData() {
		return registrationData;
	}

	public String getCentralEngineUrl() {
		return centralEngines.isEmpty() ? null : (String)centralEngines.toArray()[0];
	}

	public abstract void execute();

}
