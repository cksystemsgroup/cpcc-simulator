/*
 * @(#) GatedTspMappingAlgorithm.java
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
package cpcc.mapper.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandFlyTo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandGoAuto;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandTakeOff;

public class GatedTspMappingAlgorithm extends SimpleMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = LoggerFactory.getLogger(GatedTspMappingAlgorithm.class);

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

	
	private AcoTsp tspSolver = new AcoTsp(geodeticSystem);
	
	
	/**
	 * SPQ course planner.
	 * 
	 * @param mapper
	 *            the Mapper instance.
	 */
	@Override
	protected void coursePlanning(IMapper mapper) {
		assignZonesToEngines(mapper);
		
		assignVirtualVehiclesToZones(mapper);
		
		assignCoursesToRealVehicles(mapper);
	}

	private void assignZonesToEngines (IMapper mapper) {
	
		Set<IZone> freeZonesSet = null;
		
		for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
			String engineUrl = statusProxyEntry.getKey();
			IRegistrationData rd = mapper.getRegistrationData().get(engineUrl);
			if (rd.getAssignedZone() == null) {
				if (freeZonesSet == null) {
					freeZonesSet = getFreeZones(mapper);
				}
				if (freeZonesSet.isEmpty()) {
					break;
				}
				IZone zone = freeZonesSet.iterator().next();
				rd.setAssignedZone(zone);
				freeZonesSet.remove(zone);
			}
			
			if (rd.getMapperData() == null) {
				rd.setMapperData(new RealVehicleInfo());
			}
			
			RealVehicleInfo rvInfo = (RealVehicleInfo)rd.getMapperData();
			rvInfo.vehicleList.clear();
			if (rvInfo.occupied && statusProxyEntry.getValue().isIdle()) {
				rvInfo.occupied = false;
			}
			
			if (rvInfo.occupied && rvInfo.isInactive() && statusProxyEntry.getValue().getCurrentPosition().getAltitude() < 0.5) {
				rvInfo.occupied = false;
				rvInfo.resetInactiveCycles();
				LOG.info("resetInactiveCycles() called");
			}
		}
	}
	
	private Set<IZone> getFreeZones(IMapper mapper) {
		
		Set<IZone> freeZonesSet = new HashSet<IZone>();
		freeZonesSet.addAll(mapper.getZones());
		
		for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
			String engineUrl = statusProxyEntry.getKey();
			IRegistrationData rd = mapper.getRegistrationData().get(engineUrl);
			if (rd.getAssignedZone() != null) {
				freeZonesSet.remove(rd.getAssignedZone());
			}
		}
		
		return freeZonesSet;
	}
	
	private void assignVirtualVehiclesToZones(IMapper mapper) {
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {

			PolarCoordinate virtualVehiclePosition = vehicleInfo.getVehicleStatus().getPosition();
			
			for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
				String engineUrl = statusProxyEntry.getKey();
				
				IRegistrationData rd = mapper.getRegistrationData().get(engineUrl);
				if (rd.getAssignedZone() != null && rd.getAssignedZone().isInside(virtualVehiclePosition)) {
					LOG.info("Zone found for " + vehicleInfo.getVehicleName() + "/" + vehicleInfo.getVehicleStatus().getId());
					RealVehicleInfo rvInfo = (RealVehicleInfo)rd.getMapperData();
					rvInfo.vehicleList.add(vehicleInfo);
					break;
				}
			}
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
			if (depotPosition.getAltitude() < 0.5) {
				depotPosition.setAltitude(0.5);
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
			
			List<PolarCoordinate> wayPoints = new ArrayList<PolarCoordinate>();
			wayPoints.add(currentPosition);
			wayPoints.add(depotPosition);
			for (IVirtualVehicleInfo e : rvInfo.vehicleList) {
				wayPoints.add(e.getVehicleStatus().getPosition());
			}
			
//			LOG.info("Current position of " + engineUrl + " is " + (currentPosition != null ? currentPosition.toString() : "null"));

			if (wayPoints.size() > 2)  {
				LOG.info("Waypoint list of " + engineUrl + " has " + wayPoints.size() + " entries.");
				
				wayPoints = tspSolver.calculateBestPathWithDepot(wayPoints);
				wayPoints.remove(0); // remove current position
				wayPoints.remove(wayPoints.size()-1);	// remove depot
				
				for (PolarCoordinate wp : wayPoints) {
					if (wp.getAltitude() < 0.5) {
						wp.setAltitude(0.5);
					}
					courseCommandList.add(new RVCommandFlyTo(wp, 1, 5));
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
					courseCommandList.add(new RVCommandFlyTo(depotPosition, 2, 5));
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
