/*
 * @(#) NearestNeighborMappingAlgorithm.java
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
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleStatus;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandFlyTo;

/**
 * A nearest neighbor mapping algorithm.
 * 
 */
public class NearestNeighborMappingAlgorithm extends SimpleMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = LoggerFactory.getLogger(NearestNeighborMappingAlgorithm.class);
	
	/**
	 * The current Real Vehicle courses.
	 */
	private Map<String,CourseEntry> currentCourse = new HashMap<String, NearestNeighborMappingAlgorithm.CourseEntry>();
	
	/**
	 * The geodetic system to be used for coordinate transformations.
	 */
	private IGeodeticSystem geodeticSystem = new WGS84();
	
	/**
	 * Find the nearest Real Vehicle for each Virtual Vehicle action point and
	 * tell the Real Vehicles where to fly to.
	 * 
	 * @param mapper
	 *            the Mapper instance.
	 */
	@Override
	protected void coursePlanning(IMapper mapper) {
		
		Map<String,CourseEntry> newCourse = new HashMap<String, NearestNeighborMappingAlgorithm.CourseEntry>();
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
		
			IVirtualVehicleStatus virtualVehicleStatus = vehicleInfo.getVehicleStatus();
			
			Set<String> actionSet = virtualVehicleStatus.getActions();
			PolarCoordinate virtualVehiclePosition = virtualVehicleStatus.getPosition();
			
			findNearestEngine(newCourse, virtualVehiclePosition, actionSet, mapper);
		}
		
		for (Entry<String, CourseEntry> nc : newCourse.entrySet()) {
			String engineUrl = nc.getKey();
			CourseEntry newCe = nc.getValue();
			CourseEntry oldCe = currentCourse.get(engineUrl);
			
			if (oldCe == null || 
				newCe.position.getLatitude() != oldCe.position.getLatitude() || 
				newCe.position.getLongitude() != oldCe.position.getLongitude() || 
				newCe.position.getAltitude() != oldCe.position.getAltitude() )
			{
				LOG.info("New position for " + engineUrl + " is " + newCe.position.toString() + ", distance=" + newCe.distance);
				List<IRVCommand> courseCommandList = new ArrayList<IRVCommand>();
				courseCommandList.add(new RVCommandFlyTo(newCe.position, 1, 5));
				mapper.getStatusProxyMap().get(engineUrl).changeSetCourse(courseCommandList , true);
			}
		}
		
		currentCourse = newCourse;
	}

	/**
	 * Find the nearest Real Vehicle for a specific Virtual Vehicle action
	 * point.
	 * 
	 * @param course
	 *            the new Real Vehicle courses
	 * @param virtualVehiclePosition
	 *            the action point position of the Virtual Vehicle to be checked
	 * @param actionSet
	 *            the set of not completed Virtual Vehicle actions
	 * @param mapper
	 *            the mapper instance
	 */
	private void findNearestEngine(Map<String, CourseEntry> course, PolarCoordinate virtualVehiclePosition, Set<String> actionSet, IMapper mapper) {

		if (virtualVehiclePosition == null) {
			return;
		}
		
		Double minimumDistance = Double.MAX_VALUE;
		String nearestEngineUrl = null;
		
		CartesianCoordinate virtualPositionPosCart = geodeticSystem.polarToRectangularCoordinates(virtualVehiclePosition);
		
		for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
		    String engineUrl = statusProxyEntry.getKey();
		    
		    IStatusProxy statusProxy = statusProxyEntry.getValue();
		    PolarCoordinate currentPosition = statusProxy.getCurrentPosition();
		    
		    if (currentPosition == null || currentPosition.getAltitude() < 0.9) {
		    	// Real vehicle does not fly, so we ignore it.
		    	continue;
		    }
		    
		    CartesianCoordinate currentPosCart = geodeticSystem.polarToRectangularCoordinates(currentPosition);
		    
		    double dist = currentPosCart.subtract(virtualPositionPosCart).norm();
		    
            IRegistrationData registrationData = mapper.getRegistrationData().get(engineUrl);
            if(registrationData != null) {
            	for (String action : actionSet) {
            		if (registrationData.getSensors().contains(action)) {
            			if (dist < minimumDistance) {
	            			minimumDistance = dist;
	            			nearestEngineUrl = engineUrl;
	            			break;
	            		}
            		}
            	}
		    }
		}
		
		if (nearestEngineUrl == null) {
			return;
		}
		
		if (course.containsKey(nearestEngineUrl)) {
			CourseEntry e = course.get(nearestEngineUrl);
			
			if (minimumDistance < e.distance) {
				e.position = virtualVehiclePosition;
				e.distance = minimumDistance;
			}
		} else {
			course.put(nearestEngineUrl, new CourseEntry(virtualVehiclePosition, minimumDistance));
		}
		
	}

	
	private class CourseEntry {
		public PolarCoordinate position;
		public double distance;
		
		public CourseEntry (PolarCoordinate position, double distance) {
			this.position = position;
			this.distance = distance;
		}
	}
	
}
