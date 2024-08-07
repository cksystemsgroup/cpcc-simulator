/*
 * @(#) LocalFCFSMappingAlgorithm.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRVCommand;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ITask;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleStatus.Status;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone.Group;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandFlyTo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandGoAuto;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandHover;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandTakeOff;

public class LocalFCFSMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocalFCFSMappingAlgorithm.class);
	
	private static final double RV_SPEED = 10;
	private static final double RV_PRECISION = 2;
	
	private static final int NTHREADS = 10;
	
	private class RealVehicleInfo {
		
		public static final int MAX_INACTIVE_CYCLES = 120;
		
		public List<IVirtualVehicleInfo> vehicleList = new ArrayList<IVirtualVehicleInfo>();
		public boolean occupied = false;
		public boolean flyingToDepot = false;
		public int inactiveCounter = MAX_INACTIVE_CYCLES;
		
		public void resetInactiveCycles() {
			inactiveCounter = MAX_INACTIVE_CYCLES;
		}
		
		public boolean isInactive() {
			return inactiveCounter-- <= 0;
		}
	};
	

	/**
	 * The geodetic system to be used for coordinate transformations.
	 */
	private IGeodeticSystem geodeticSystem = new WGS84();

	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm#execute(at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper)
	 */
	@Override
	public void execute(IMapper mapper) {
		        
		if (mapper.getRegistrationData().isEmpty()) {
			LOG.info("No migration because of empty registration map.");
			return;
		}
		        
		if(mapper.getStatusProxyMap().isEmpty()) {
			LOG.info("No migration because of empty status proxy map.");
			return;
		}
		
		refreshRvInfo(mapper);
		
		migrateVirtualVehicles(mapper);
		
		assignCoursesToRealVehicles(mapper);
	}
	


	private void refreshRvInfo(IMapper mapper) {
		
		for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
			String engineUrl = statusProxyEntry.getKey();
			IRegistrationData rd = mapper.getRegistrationData().get(engineUrl);
			
			RealVehicleInfo rvInfo = (RealVehicleInfo)rd.getMapperData();
			if (rvInfo == null) {
				rvInfo = new RealVehicleInfo();
				rd.setMapperData(rvInfo);
			}
			
			rvInfo.vehicleList.clear();
			if (rvInfo.occupied && statusProxyEntry.getValue().isIdle()) {
				rvInfo.occupied = false;
			}
			
			if (rvInfo.occupied && rvInfo.isInactive() && 
					statusProxyEntry.getValue().getCurrentPosition() != null &&
					statusProxyEntry.getValue().getCurrentPosition().getAltitude() < 0.8)
			{
				rvInfo.occupied = false;
				rvInfo.resetInactiveCycles();
				LOG.info("resetInactiveCycles() called");
			}
		}
	}



	private void migrateVirtualVehicles(IMapper mapper) {
		
		ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
			
			Status status = vehicleInfo.getVehicleStatus().getState();
			if (status != Status.ACTIVE && status != Status.SUSPENDED && status != Status.NONE) {
				if (!mapper.getCentralEngines().isEmpty()) {
					String centralEngineUrl = mapper.getCentralEngines().iterator().next();
					LOG.info("Status=" + status.toString() + ": migrate " + vehicleInfo.getVehicleName() + " on " + vehicleInfo.getEngineUrl() + " to central engine " + centralEngineUrl);
//					mapper.migrate(vehicleInfo.getEngineUrl(), vehicleInfo.getVehicleName(), centralEngineUrl);
					MigrationRunnable worker = new MigrationRunnable(mapper, vehicleInfo.getEngineUrl(), vehicleInfo.getVehicleName(), centralEngineUrl);
				    executor.execute(worker);
					continue;
				}
			}
			
			PolarCoordinate virtualVehiclePosition = vehicleInfo.getVehicleStatus().getPosition();
			if (virtualVehiclePosition == null) {
				continue;
			}
			
			for (IZone zone : mapper.getZones()) {
				if (zone.isInside(virtualVehiclePosition)) {
					if (zone.getZoneEngineUrl() != null && !vehicleInfo.getEngineUrl().equals(zone.getZoneEngineUrl())) {
						LOG.info("not in RV zone: migrate " + vehicleInfo.getVehicleName() + " on " + vehicleInfo.getEngineUrl() + " to engine " + zone.getZoneEngineUrl());
//						mapper.migrate(vehicleInfo.getEngineUrl(), vehicleInfo.getVehicleName(), zone.getZoneEngineUrl());
						MigrationRunnable worker = new MigrationRunnable(mapper, vehicleInfo.getEngineUrl(), vehicleInfo.getVehicleName(), zone.getZoneEngineUrl());
					    executor.execute(worker);
					}
					if (zone.getZoneGroup() == Group.LOCAL) {
						IRegistrationData rd = mapper.getRegistrationData().get(vehicleInfo.getEngineUrl());
						RealVehicleInfo rvInfo = (RealVehicleInfo)rd.getMapperData();
						if (rvInfo != null) {
							rvInfo.vehicleList.add(vehicleInfo);
						}
					}					
				}
			}
		}
		
	    executor.shutdown();

	    while (!executor.isTerminated()) {
	    	try { Thread.sleep(20); } catch (InterruptedException e) { }
	    }
	}


	private void assignCoursesToRealVehicles(IMapper mapper) {
		
		for (Entry<String, IRegistrationData> entry : mapper.getRegistrationData().entrySet()) {

			String engineUrl = entry.getKey();
			IRegistrationData rd = entry.getValue();
			
			if (rd.getAssignedZone() == null) {
				continue;
			}
			
			PolarCoordinate depotPosition = rd.getAssignedZone().getDepotPosition();
			if (depotPosition.getAltitude() < 1.0) {
				depotPosition.setAltitude(1.0);
			}
			
			RealVehicleInfo rvInfo = (RealVehicleInfo)rd.getMapperData();
			if (rvInfo == null) {
				continue;
			}
			
			LOG.debug("Status: " + engineUrl + " occupied=" + rvInfo.occupied + ", flyingToDepot=" + rvInfo.flyingToDepot);
			
			if (rvInfo.occupied) {
				continue;
			}
			
			IStatusProxy statusProxy = mapper.getStatusProxyMap().get(engineUrl);
			PolarCoordinate currentPosition = statusProxy.getCurrentPosition();

			List<IRVCommand> courseCommandList = new ArrayList<IRVCommand>();
			
			if (statusProxy.isIdle() && (currentPosition == null || currentPosition.getAltitude() < 0.3)) {
				LOG.info("Upload takeoff sequence to " + engineUrl);
				courseCommandList.add(new RVCommandGoAuto());
				courseCommandList.add(new RVCommandTakeOff(1, 5));
				rvInfo.occupied = true;
				mapper.getStatusProxyMap().get(engineUrl).changeSetCourse(courseCommandList , true);
				continue;
			}
			
			ITask task = null;
			long arrivalTime = Long.MAX_VALUE;
			
			for (IVirtualVehicleInfo e : rvInfo.vehicleList) {
				Status status = e.getVehicleStatus().getState();
				ITask t = e.getVehicleStatus().getCurrentTask();

				if (status != Status.ACTIVE || t == null || t.getPosition() == null || t.getArrivalTime() > System.currentTimeMillis()) {
					continue;
				}
				
				if (t.getArrivalTime() < arrivalTime) {
					arrivalTime = t.getArrivalTime();
					task = t;
				}
			}
			
//			LOG.info("Current position of " + engineUrl + " is " + (currentPosition != null ? currentPosition.toString() : "null"));
			
			if (task != null)  { 
				PolarCoordinate wayPoint = task.getPosition();
				LOG.debug("Engine " + engineUrl + " flies to " + wayPoint.toString());
				
				if (wayPoint.getAltitude() < 0.5) {
					wayPoint = new PolarCoordinate(wayPoint.getLatitude(), wayPoint.getLongitude(), 0.5);
				}

				courseCommandList.add(new RVCommandFlyTo(wayPoint, RV_PRECISION, RV_SPEED));
				if (task.getDelayTime() > 500) {
					courseCommandList.add(new RVCommandHover((long)(0.5 + task.getDelayTime() / 1000)));
				}
				rvInfo.occupied = true;
				rvInfo.flyingToDepot = false;
				
			} else if (!rvInfo.flyingToDepot && currentPosition != null) {
				double distanceToDepot = 10;
			
				CartesianCoordinate curr = geodeticSystem.polarToRectangularCoordinates(currentPosition);
				CartesianCoordinate dep = geodeticSystem.polarToRectangularCoordinates(depotPosition);
				distanceToDepot = curr.subtract(dep).norm();
				
				if (distanceToDepot > 5) {
					LOG.info("Noting to do for " + engineUrl + " -> flying back to depot " + depotPosition);
					courseCommandList.add(new RVCommandFlyTo(depotPosition, RV_PRECISION, RV_SPEED));
					rvInfo.flyingToDepot = true;
				} else {
					LOG.debug("Noting to do for " + engineUrl + ", depot already reached.");
				}
			}
			
			if (courseCommandList.size() > 0) {
				LOG.info("Uploading new course to " + engineUrl);
				mapper.getStatusProxyMap().get(engineUrl).changeSetCourse(courseCommandList , true);
			} else {
				LOG.debug("No course changes for " + engineUrl);
			}
			
			rvInfo.vehicleList.clear();
		}
		
	}


}
