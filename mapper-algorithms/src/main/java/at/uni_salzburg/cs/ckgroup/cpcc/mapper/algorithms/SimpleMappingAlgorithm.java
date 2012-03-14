/*
 * @(#) SimpleMappingAlgorithm.java
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleStatus;

public class SimpleMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = Logger.getLogger(SimpleMappingAlgorithm.class);
	private IGeodeticSystem geodeticSystem = new WGS84();
	
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
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
		
			String vehicleName = vehicleInfo.getVehicleName();
			String engineUrl = vehicleInfo.getEngineUrl();
			IVirtualVehicleStatus virtualVehicleStatus = vehicleInfo.getVehicleStatus();
					
			if (virtualVehicleStatus.getState() == IVirtualVehicleStatus.Status.COMPLETED) {
				Set<String> centralEngines = mapper.getCentralEngines();
				if (!centralEngines.isEmpty()) {
					mapper.migrate(engineUrl, vehicleName, centralEngines.iterator().next());
				} else {
					LOG.debug("Can not migrate completed VV " + vehicleName + " to a central engine.");
				}
				continue;
			}
			
			Set<String> actionSet = virtualVehicleStatus.getActions();
			PolarCoordinate virtualVehiclePosition = virtualVehicleStatus.getPosition();
			double tolerance = virtualVehicleStatus.getTolerance();
			
			String newEngineUrl = findEngine(virtualVehiclePosition, tolerance, actionSet, mapper);
			if (newEngineUrl == null || newEngineUrl.equals(engineUrl)) {
				continue;
			}
			
			mapper.migrate(engineUrl, vehicleName, newEngineUrl);
		}
	}
		
	private String findEngine(PolarCoordinate pos, double tolerance, Set<String> actionSet, IMapper mapper) {
		
		if (pos == null) {
			return null;
		}
		
		Double minimumTime = Double.MAX_VALUE;
		String foundEngineUrl = null;
		CartesianCoordinate virtualPosCart = geodeticSystem.polarToRectangularCoordinates(pos);
		
		for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
		    String engineUrl = statusProxyEntry.getKey();
		    IStatusProxy statusProxy = statusProxyEntry.getValue();
		    
		    PolarCoordinate currentPosition = statusProxy.getCurrentPosition();
		    PolarCoordinate nextPosition = statusProxy.getNextPosition();
		    Double velocity = statusProxy.getVelocity();
		    if(currentPosition != null && nextPosition != null && velocity != null) {
		        CartesianCoordinate currentPosCart = geodeticSystem.polarToRectangularCoordinates(currentPosition);
		        CartesianCoordinate nextPosCart = geodeticSystem.polarToRectangularCoordinates(nextPosition);
		        Double timeToVvActionPoint = calculateTimeToVvActionPoint(currentPosCart, nextPosCart, virtualPosCart, tolerance, velocity);
		        
		        if(timeToVvActionPoint != null && timeToVvActionPoint < minimumTime) {
		    		minimumTime = timeToVvActionPoint;
		            IRegistrationData registrationData = mapper.getRegistrationData().get(engineUrl);
		            if(registrationData != null) {
		            	for (String action : actionSet) {
		            		if (registrationData.getSensors().contains(action)) {
		            			foundEngineUrl = engineUrl;
		            			break;
		            		}
		            	}
		            }
		        }    
		    }
		}
		
		return foundEngineUrl;
	}

	private Double calculateTimeToVvActionPoint(CartesianCoordinate currentRvPosition, CartesianCoordinate nextRvWayPoint, CartesianCoordinate vvActionPoint, double tolerance, double velocity) {
		// Are we in the tolerance sphere ?
		CartesianCoordinate pmc = vvActionPoint.subtract(currentRvPosition);
		double pmcNorm = pmc.norm();
		if (pmcNorm <= tolerance) {
			return pmcNorm / velocity;
		}
		
		// Are we heading towards the VV action point?
		CartesianCoordinate dir = nextRvWayPoint.subtract(currentRvPosition);
		if (dir.multiply(pmc) < 0) {
			return null;
		}
		
		// We are heading towards the VV action point, but do we reach it?
		double dirNorm = dir.norm();
		double d = dir.crossProduct(pmc).norm() / dirNorm;
		return pmcNorm <= dirNorm + tolerance && d <= tolerance ? pmcNorm / velocity : null;
	}
	
}
