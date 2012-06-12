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
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRVCommand;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandFlyTo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandGoAuto;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandTakeOff;

public class GatedTspMappingAlgorithm extends SimpleMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = Logger.getLogger(GatedTspMappingAlgorithm.class);

	private class RealVehicleInfo {
		public IZone zone;
		public PolarCoordinate depot;
		public List<IVirtualVehicleInfo> vehicleList = new ArrayList<IVirtualVehicleInfo>();
		public boolean occupied = false;
		public boolean flyingToDepot = false;
		
		public RealVehicleInfo(IZone zone, PolarCoordinate depot) {
			this.zone = zone;
			this.depot = depot;
		}
	};
	
//	private static final PolarCoordinate[] zone_1 = {
//		new PolarCoordinate(47.82202396, 13.04082245, 0),	new PolarCoordinate(47.82376633, 13.04082245, 0),
//		new PolarCoordinate(47.82376633, 13.03839827, 0),	new PolarCoordinate(47.82202396, 13.03839827, 0)	};
	
	private static final IZone zone_1 = new SquareZone (47.82202396, 47.82376633, 13.03839827, 13.04082245);
	private static final PolarCoordinate depot_1 = new PolarCoordinate(47.822895145, 13.039610360, 1);
	
//	private static final PolarCoordinate[] zone_2 = {
//		new PolarCoordinate(47.82202396, 13.04082245, 0),	new PolarCoordinate(47.82376633, 13.04082245, 0), 
//		new PolarCoordinate(47.82376633, 13.04324663, 0),	new PolarCoordinate(47.82202396, 13.04324663, 0)	};
	
	private static final IZone zone_2 = new SquareZone (47.82202396, 47.82376633, 13.04082245, 13.04324663);
	private static final PolarCoordinate depot_2 = new PolarCoordinate(47.822895145, 13.042034540, 1);
	
//	private static final PolarCoordinate[] zone_3 = {
//		new PolarCoordinate(47.82202396, 13.04082245, 0),	new PolarCoordinate(47.82028159, 13.04082245, 0),
//		new PolarCoordinate(47.82028159, 13.03839827, 0),	new PolarCoordinate(47.82202396, 13.03839827, 0)	};

	private static final IZone zone_3 = new SquareZone (47.82028159, 47.82202396, 13.03839827, 13.04082245);
	private static final PolarCoordinate depot_3 = new PolarCoordinate(47.821152775, 13.039610360, 1);
	
//	private static final PolarCoordinate[] zone_4 = {
//		new PolarCoordinate(47.82202396, 13.04082245, 0),	new PolarCoordinate(47.82028159, 13.04082245, 0),
//		new PolarCoordinate(47.82028159, 13.04324663, 0),	new PolarCoordinate(47.82202396, 13.04324663, 0)	};

	private static final IZone zone_4 = new SquareZone (47.82028159, 47.82202396, 13.04082245, 13.04324663);
	private static final PolarCoordinate depot_4 = new PolarCoordinate(47.821152775, 13.042034540, 1);

	/**
	 * The geodetic system to be used for coordinate transformations.
	 */
	private IGeodeticSystem geodeticSystem = new WGS84();

	@SuppressWarnings("serial")
	private List<RealVehicleInfo> unassignedZones = new ArrayList<RealVehicleInfo>() {{
//		add(new RealVehicleInfo(new PolygonZone(zone_1, geodeticSystem), depot_1));
//		add(new RealVehicleInfo(new PolygonZone(zone_2, geodeticSystem), depot_2));
//		add(new RealVehicleInfo(new PolygonZone(zone_3, geodeticSystem), depot_3));
//		add(new RealVehicleInfo(new PolygonZone(zone_4, geodeticSystem), depot_4));
		add(new RealVehicleInfo(zone_1, depot_1));
		add(new RealVehicleInfo(zone_2, depot_2));
		add(new RealVehicleInfo(zone_3, depot_3));
		add(new RealVehicleInfo(zone_4, depot_4));	
	}};

	
	private Map<String,RealVehicleInfo> realVehicles = new HashMap<String, RealVehicleInfo>();
	
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
	
		for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
		    String engineUrl = statusProxyEntry.getKey();
		
		    if (!realVehicles.containsKey(engineUrl) && !unassignedZones.isEmpty()) {
		    	LOG.info("Assigning zone " + unassignedZones.get(0).toString() + "to engine " + engineUrl);
		    	realVehicles.put(engineUrl, unassignedZones.get(0));
		    	unassignedZones.remove(0);
		    }
		    
		    RealVehicleInfo rvInfo = realVehicles.get(engineUrl);
		    if (rvInfo != null) {
		    	rvInfo.vehicleList.clear();
		    	if (rvInfo.occupied && statusProxyEntry.getValue().isIdle()) {
			    	LOG.info("Real vehicle " + engineUrl + " is now ready for new assignments.");
			    	rvInfo.occupied = false;
//		 			rvInfo.flyingToDepot = false;
			    }
		    }
		}
	}
	
	private void assignVirtualVehiclesToZones(IMapper mapper) {
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
		
			PolarCoordinate virtualVehiclePosition = vehicleInfo.getVehicleStatus().getPosition();
			
//			boolean found = false;
			for (Entry<String, RealVehicleInfo> rvEntry : realVehicles.entrySet()) {
				if (rvEntry.getValue().occupied) {
					continue;
				}
				
				if (rvEntry.getValue().zone.isInside(virtualVehiclePosition)) {
					LOG.info("Zone found for " + vehicleInfo.getVehicleName() + "/" + vehicleInfo.getVehicleStatus().getId());
					rvEntry.getValue().vehicleList.add(vehicleInfo);
//					found = true;
					break;
				}
			}
			
//			if (!found) {
//				LOG.info("No zone found for " + vehicleInfo.getVehicleName() + "/" + vehicleInfo.getVehicleStatus().getId() + ". Ignoring it.");
//			}
			
		}
		
	}

	private void assignCoursesToRealVehicles(IMapper mapper) {
		
		for (Entry<String, RealVehicleInfo> rvEntry : realVehicles.entrySet()) {
			String engineUrl = rvEntry.getKey();
			RealVehicleInfo rvInfo = rvEntry.getValue();
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
			wayPoints.add(rvInfo.depot);
			for (IVirtualVehicleInfo e : rvInfo.vehicleList) {
				wayPoints.add(e.getVehicleStatus().getPosition());
			}
			
//			LOG.info("Current position of " + engineUrl + " is " + (currentPosition != null ? currentPosition.toString() : "null"));

			if (wayPoints.size() > 2)  {
				LOG.info("Waypoint list of " + engineUrl + " has " + wayPoints.size() + " entries.");
				
				wayPoints = tspSolver.calculateBestPathWithDepot(wayPoints);
				wayPoints.remove(0); // remove current position
				wayPoints.remove(wayPoints.size()-1);	// remove depot
				
//				wayPoints = tspSolver.calculateBestPathWithOutDepot(wayPoints);
//				wayPoints.remove(0); // remove current position
				
				for (PolarCoordinate wp : wayPoints) {
					courseCommandList.add(new RVCommandFlyTo(wp, 1, 5));
				}
				
				
				rvInfo.occupied = true;
				rvInfo.flyingToDepot = false;
				
			} else if (!rvInfo.flyingToDepot && currentPosition != null) {
				double distanceToDepot = 10;
			
				CartesianCoordinate curr = geodeticSystem.polarToRectangularCoordinates(currentPosition);
				CartesianCoordinate dep = geodeticSystem.polarToRectangularCoordinates(rvInfo.depot);
				distanceToDepot = curr.subtract(dep).norm();
				
				if (distanceToDepot > 5) {
					LOG.info("Noting to do for " + engineUrl + " -> flying back to depot " + rvInfo.depot);
					courseCommandList.add(new RVCommandFlyTo(rvInfo.depot, 2, 5));
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
