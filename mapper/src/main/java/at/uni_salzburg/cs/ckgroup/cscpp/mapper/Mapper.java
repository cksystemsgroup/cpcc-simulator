/*
 * @(#) Mapper.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.IStatusProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.StatusProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.VehicleInfo;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.VehicleStatus;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;

public class Mapper extends Thread implements IMapperThread, IMapper {

	private static final Logger LOG = Logger.getLogger(Mapper.class);
	
	private static final Object statLock = new Object[0];
	
	private boolean running = false;
	
	private boolean paused = false;

	private long cycleTime = 1000;
	
	private long executionTime = 0;
	private long executionTimeMax = 0;
	private long executionTimeMin = 0;
	private double executionTimeAvg = 0;
	private long executions = 0;
	
	private double migrationTimeAvg = 0;
	private long migrationsOk = 0;
	private long migrationsFailed = 0;
	
	private Map<String,at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy> statusProxyMap = new HashMap<String, at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy>();
	private List<IVirtualVehicleInfo> virtualVehicleList = new ArrayList<IVirtualVehicleInfo>();
	private Map<String,IRegistrationData> registrationData;
	private Set<String> centralEngines = new HashSet<String>();
	private Set<String> registeredCentralEngines = new HashSet<String>();
	private Set<IZone> zones;
	private Set<IZone> neighborZones;
	private List<IMappingAlgorithm> mappingAlgorithms;
	
	public void setRegistrationData(Map<String, IRegistrationData> registrationData) {
		this.registrationData = registrationData;
	}
	
	public void setMappingAlgorithms(List<IMappingAlgorithm> mappingAlgorithm) {
		this.mappingAlgorithms = mappingAlgorithm;
	}

	public List<IMappingAlgorithm> getMappingAlgorithm() {
		return mappingAlgorithms;
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
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	public long getExecutionTimeMin() {
		return executionTimeMin;
	}
	
	public long getExecutionTimeMax() {
		return executionTimeMax;
	}
	
	public long getExecutionTimeAvg() {
		return (long)(executionTimeAvg+0.5);
	}
	
	public long getMigrationsFailed() {
		return migrationsFailed;
	}
	
	public long getMigrationsOk() {
		return migrationsOk;
	}
	
	public long getMigrationTimeAvg() {
		return (long) migrationTimeAvg;
	}
	
	public long getExecutions() {
		return executions;
	}
	
	@Override
	public void run() {
		if (registrationData == null)
			throw new NullPointerException("Registration data not available!");
		
		running = true;
		proceed();
		while (running) {
			if (!paused) {
				step();
			}

			try { Thread.sleep(cycleTime); } catch (InterruptedException e) { }
		}
	}
	
	private void calculateStatistics() {
		
		synchronized (statLock) {
			++executions;
			
			if (executionTime > executionTimeMax) {
				executionTimeMax = executionTime;
			}
			
			if (executionTime != 0 && executionTime < executionTimeMin) {
				executionTimeMin = executionTime;
			}
			
			if (executions == 1) {
				executionTimeAvg = executionTime;
			} else {
				executionTimeAvg = (executionTimeAvg * (executions - 1.0) + executionTime) / executions;
			}
		}
	}

	@Override
	public void singleStep() {
		if (paused) {
			step();
		}
	}
	
	@Override
	public void resetStatistics() {
		synchronized (statLock) {
			executionTime = 0;
			executionTimeMax = 0;
			executionTimeMin = 0;
			executionTimeAvg = 0;
			executions = 0;
			
			migrationTimeAvg = 0;
			migrationsOk = 0;
			migrationsFailed = 0;
		}
	}
	
	private void step () {

		long start = System.currentTimeMillis();

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
		
		if (mappingAlgorithms != null) {
			for (IMappingAlgorithm algorithm : mappingAlgorithms) {
				algorithm.execute(this);
			}
		} else {
			LOG.error("No mapping algorithm found. Mapping stopped.");
			cease();
		}
		
		executionTime = System.currentTimeMillis() - start;
		calculateStatistics();
	}
	
	private void renewStatusProxyMap() {
		for (IRegistrationData rd : registrationData.values()) {
			if (!statusProxyMap.containsKey(rd.getEngineUrl()) && rd.getPilotUrl() != null) {
				statusProxyMap.put(rd.getEngineUrl(), new StatusProxy(rd.getPilotUrl()));
			}
			if (rd.isCentralEngine()) {
				centralEngines.add(rd.getEngineUrl());
			}
		}
		
		for (String key : statusProxyMap.keySet()) {
			if (!registrationData.containsKey(key)) {
				statusProxyMap.remove(key);
				centralEngines.remove(key);
			}
		}
		
		centralEngines.addAll(registeredCentralEngines);	
	}
	
	private void getPilotStatii() {
		for (at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy proxy : statusProxyMap.values()) {
			((IStatusProxy)proxy).fetchCurrentStatus();
		}
	}
	
	private void getEngineStatii() throws ParseException {
		
		List<String> toBeUnregistered = new ArrayList<String>();
		
		JSONParser parser = new JSONParser();
		virtualVehicleList.clear();
		for (IRegistrationData rd : registrationData.values()) {
			String key = rd.getEngineUrl();
			String engineVehicleURL = key + "/json/vehicle/noAPs,noVvPath";
			
			String position = null;
			try {
				position = HttpQueryUtils.simpleQuery(engineVehicleURL);
			} catch (IOException e) {
				LOG.error("Can not query Engine at " + key + ": " + e.getMessage());
				if (!rd.isMaxAccessErrorsLimitReached()) {
					continue;
				} else {
					toBeUnregistered.add(key);
				}
			}
			
			if (position == null || position.trim().isEmpty() || position.trim().startsWith("<")) {
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
				data.setEngineUrl(rd.getEngineUrl());
				data.setVehicleStatus(status);
				virtualVehicleList.add(data);
			}
		}
		
		for (String engineUrl : toBeUnregistered) {
			LOG.info("Unregistering extinct Engine at " + engineUrl);
			registrationData.remove(engineUrl);
		}
	}
	

	@Override
	public void migrate(String sourceEngineUrl, String vehicleName, String targetEngineUrl) {
		
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
			long start = System.nanoTime();
			String ret = HttpQueryUtils.simpleQuery(migrationUrl);
			if ("OK".equals(ret)) {
				long migrationTime = 0;
				synchronized (statLock) {
					++migrationsOk;
					migrationTime = System.nanoTime() - start;
					migrationTimeAvg = (migrationTimeAvg * (migrationsOk - 1.0) + migrationTime/1000.0) / migrationsOk;
				}
				LOG.info("Migration succeeded. " + migrationUrl + ", " + ret + ", migrationTime=" + (migrationTime/1000.0) + "ms");
			} else {
				LOG.error("Migration failed. " + migrationUrl + ", " + ret);
				++migrationsFailed;
			}
		} catch (IOException ex) {
			LOG.error("Migration failed. " + migrationUrl, ex);
			++migrationsFailed;
		}
		
	}
	
	@Override
	public Map<String, at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy> getStatusProxyMap() {
		return statusProxyMap;
	}

	@Override
	public List<IVirtualVehicleInfo> getVirtualVehicleList() {
		return virtualVehicleList;
	}

	@Override
	public Map<String, IRegistrationData> getRegistrationData() {
		return registrationData;
	}

	@Override
	public Set<String> getCentralEngines() {
		return centralEngines;
	}
	
	public void setRegisteredCentralEngines(Set<String> registeredCentralEngines) {
		this.registeredCentralEngines = registeredCentralEngines;
	}
	
	@Override
	public Set<IZone> getZones() {
		return zones;
	}
	
	public void setZones(Set<IZone> zones) {
		this.zones = zones;
	}
	
	@Override
	public Set<IZone> getNeighborZones() {
		return neighborZones;
	}
	
	public void setNeighborZones(Set<IZone> neighborZones) {
		this.neighborZones = neighborZones;
	}
}
