/*
 * @(#) AbstractMappingAlgorithm.java
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;

public abstract class AbstractMappingAlgorithm extends Thread implements IMappingAlgorithm {
	
	Logger LOG = Logger.getLogger(AbstractMappingAlgorithm.class);
	
	private boolean running = false;
	
	private boolean paused = false;

	private long cycleTime = 1000;
	
	protected Map<String,StatusProxy> statusProxyMap = new HashMap<String, StatusProxy>();
	protected Map<String,Map<String,VehicleStatus>> virtualVehicleMap = new HashMap<String, Map<String,VehicleStatus>>(); 
	
	private Map<String,RegData> registrationData;
	
	public void setRegistrationData(Map<String, RegData> registrationData) {
		this.registrationData = registrationData;
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
		
	private void getEngineStatii() throws IOException, ParseException {

		JSONParser parser = new JSONParser();
		virtualVehicleMap.clear();
		for (RegData rd : registrationData.values()) {
			String key = rd.getEngineUri();
			String engineVehicleURL = key + "/json/vehicle";
			String position = HttpQueryUtils.simpleQuery(engineVehicleURL);
			if (position.trim().isEmpty()) {
				continue;
			}
			JSONObject obj = (JSONObject)parser.parse(position);
			
			Map<String, VehicleStatus> vehicles = new HashMap<String, VehicleStatus>(); 
			for (Object o : obj.entrySet()) {
				@SuppressWarnings("unchecked")
				Entry<String, JSONObject> entry = (Entry<String, JSONObject>) o;
				vehicles.put(entry.getKey(), new VehicleStatus(entry.getValue()));
			}
			
			virtualVehicleMap.put(key, vehicles);
		}
	}
	
	protected void migrate(String sourceEngineUrl, String vehicleName, String targetEngineUrl) {

		String migrationUrl = sourceEngineUrl
				+ "/vehicle/text/vehicleMigration?vehicleIDs=" + vehicleName
				+ "&vehicleDst=" + targetEngineUrl
				+ "/vehicle/text/vehicleUpload";
		LOG.info("random migration: " + migrationUrl);

		try {
			String ret = HttpQueryUtils.simpleQuery(migrationUrl);
			LOG.info("random migration succeeded. " + migrationUrl + ", " + ret);
		} catch (IOException ex) {
			LOG.error("random migration railed. " + migrationUrl, ex);
		}

	}

	public abstract void execute();

}
