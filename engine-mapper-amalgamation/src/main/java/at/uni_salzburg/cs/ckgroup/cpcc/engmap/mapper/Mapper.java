/*
 * @(#) Mapper.java
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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.IMapperThread;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.StatusProxy;
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
	
	private Map<String,Long> vvStatistics = null;

	private Map<String, IStatusProxy> statusProxyMap = new HashMap<String, IStatusProxy>();
	// private List<IVirtualVehicleInfo> virtualVehicleList = new
	// ArrayList<IVirtualVehicleInfo>();
	private Map<String, IRegistrationData> registrationData;
	private Set<String> centralEngines = new HashSet<String>();
	private Set<String> registeredCentralEngines = new HashSet<String>();
	private Set<IZone> zones;
	private Set<IZone> neighborZones;
	private List<IMappingAlgorithm> mappingAlgorithms;

	private Map<String, IVirtualVehicle> vehicleMap;

	private URI localEngineUrl;
	

	public void setVehicleMap(Map<String, IVirtualVehicle> vehicleMap) {
		this.vehicleMap = vehicleMap;
	}

	public void setLocalEngineUrl(URI localEngineUrl) {
		this.localEngineUrl = localEngineUrl;
	}

	public void setRegistrationData(
			Map<String, IRegistrationData> registrationData) {
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
		return (long) (executionTimeAvg + 0.5);
	}

	public long getExecutions() {
		return executions;
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
	
	public Map<String, Long> getVvStatistics() {
		return vvStatistics;
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

			try {
				Thread.sleep(cycleTime);
			} catch (InterruptedException e) {
			}
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
				executionTimeAvg = (executionTimeAvg * (executions - 1.0) + executionTime)
						/ executions;
			}
		}

		long active, completed, corrupt, frozen, total;
		active = completed = corrupt = frozen = total = 0;
		for (IVirtualVehicle v : vehicleMap.values()) {
			if (v.isActive()) {
				++active;
			}
			if (v.isCompleted()) {
				++completed;
			}
			if (v.isFrozen()) {
				++frozen;
			}
			if (v.isProgramCorrupted()) {
				++corrupt;
			}
			++total;
		}
		
		Map<String,Long> stats = new TreeMap<String, Long>();
		stats.put("Active", active);
		stats.put("Completed", completed);
		stats.put("Corrupt", corrupt);
		stats.put("Frozen", frozen);
		stats.put("Total", total);
		vvStatistics = stats;
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

	private void step() {

		renewStatusProxyMap();
		
		getPilotStatii();
		
		long start = System.currentTimeMillis();

		if (mappingAlgorithms != null) {
			for (IMappingAlgorithm algorithm : mappingAlgorithms) {
				try {
					algorithm.execute(this);
				} catch (Throwable e) {
					LOG.error(e);
				}
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
			((at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.IStatusProxy)proxy).fetchCurrentStatus();
		}
	}
	
	@Override
	public void migrate(String sourceEngineUrl, String vehicleName,
			String targetEngineUrl) {

		if (sourceEngineUrl.equals(targetEngineUrl)) {
			LOG.debug("Migration cancelled, because source engine is target engine: "
					+ targetEngineUrl);
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
				long migrationTime = System.nanoTime() - start;
				synchronized (statLock) {
					++migrationsOk;
					migrationTimeAvg = (migrationTimeAvg * (migrationsOk - 1.0) + migrationTime/1000000.0) / migrationsOk;
				}
				LOG.info("Migration succeeded. " + migrationUrl + ", " + ret + ", migrationTime=" + (migrationTime/1000000.0) + "ms");
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
	public Map<String, IStatusProxy> getStatusProxyMap() {
		return statusProxyMap;
	}

	@Override
	public List<IVirtualVehicleInfo> getVirtualVehicleList() {
		List<IVirtualVehicleInfo> virtualVehicleList = new ArrayList<IVirtualVehicleInfo>();

		for (Entry<String, IVirtualVehicle> entry : vehicleMap.entrySet()) {
			String name = entry.getKey();
			IVirtualVehicle vehicle = entry.getValue();
			if (vehicle.isFrozen() || vehicle.isProgramCorrupted()) {
				continue;
			}
			virtualVehicleList.add(new VirtualVehicleInfo(name, localEngineUrl.toASCIIString(), vehicle));
		}
		
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
