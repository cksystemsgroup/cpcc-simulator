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

import org.apache.log4j.Logger;

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
public class NearestNeighborMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = Logger.getLogger(NearestNeighborMappingAlgorithm.class);
	
	/**
	 * The current Real Vehicle courses.
	 */
	private Map<String,CourseEntry> currentCourse = new HashMap<String, NearestNeighborMappingAlgorithm.CourseEntry>();
	
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
		
		coursePlanning(mapper);
		
		virtualVehicleMapping(mapper);
	}
	
	/**
	 * Find the nearest Real Vehicle for each Virtual Vehicle action point and
	 * tell the Real Vehicles where to fly to.
	 * 
	 * @param mapper
	 *            the Mapper instance.
	 */
	private void coursePlanning(IMapper mapper) {
		
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

	/**
	 * Perform mapping of Virtual Vehicles.
	 * 
	 * @param mapper
	 *            the mapper instance.
	 */
	private void virtualVehicleMapping(IMapper mapper) {
		
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
		
	/**
	 * This method searches for a given Virtual Vehicle an Engine to migrate to.
	 * 
	 * @param virtualVehiclePosition
	 *            the position of the inspected Virtual Vehicle Action Point
	 * @param tolerance
	 *            the radius of the tolerance sphere all around the inspected
	 *            Virtual Vehicle Action Point.
	 * @param actionSet
	 *            the set of actions to be performed at the inspected Virtual
	 *            Vehicle Action Point.
	 * @param mapper
	 *            the Mapper instance.
	 * @return an Engine base URL where the Virtual Vehicle should be migrated
	 *         to.
	 */
	private String findEngine(PolarCoordinate virtualVehiclePosition, double tolerance, Set<String> actionSet, IMapper mapper) {
		
		if (virtualVehiclePosition == null) {
			return null;
		}
		
		Double minimumTime = Double.MAX_VALUE;
		String newEngineUrl = null;
		CartesianCoordinate virtualPositionPosCart = geodeticSystem.polarToRectangularCoordinates(virtualVehiclePosition);
		
		for(Map.Entry<String,IStatusProxy> statusProxyEntry : mapper.getStatusProxyMap().entrySet()) {
		    String engineUrl = statusProxyEntry.getKey();
		    IStatusProxy statusProxy = statusProxyEntry.getValue();
		    
		    PolarCoordinate currentPosition = statusProxy.getCurrentPosition();
		    PolarCoordinate nextPosition = statusProxy.getNextPosition();
		    Double velocity = statusProxy.getVelocity();
		    if(currentPosition != null && nextPosition != null && velocity != null) {
		        CartesianCoordinate currentPosCart = geodeticSystem.polarToRectangularCoordinates(currentPosition);
		        CartesianCoordinate nextPosCart = geodeticSystem.polarToRectangularCoordinates(nextPosition);
		        Double timeToVvActionPoint = calculateTimeToVvActionPoint(currentPosCart, nextPosCart, virtualPositionPosCart, tolerance, velocity);
		        
		        if(timeToVvActionPoint != null && timeToVvActionPoint < minimumTime) {
		    		minimumTime = timeToVvActionPoint;
		            IRegistrationData registrationData = mapper.getRegistrationData().get(engineUrl);
		            if(registrationData != null) {
		            	for (String action : actionSet) {
		            		if (registrationData.getSensors().contains(action)) {
		            			newEngineUrl = engineUrl;
		            			break;
		            		}
		            	}
		            }
		        }    
		    }
		}
		
		return newEngineUrl;
	}

	/**
	 * This method calculates the time a Real Vehicle will need to arrive a
	 * given Virtual Vehicle Action Point.
	 * 
	 * @param currentRvPosition
	 *            the current Real Vehicle position.
	 * @param nextRvWayPoint
	 *            the end of the current set course flight segment.
	 * @param vvActionPoint
	 *            the inspected Virtual Vehicle Action Point.
	 * @param tolerance
	 *            the radius of the tolerance sphere all around the inspected
	 *            Virtual Vehicle Action Point.
	 * @param velocity
	 *            the current velocity of the Real Vehicle.
	 * @return the time the Real Vehicle will need to arrive the inspected
	 *         Virtual Vehicle Action Point, or null if the Real Vehicle will
	 *         not get near it.
	 */
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
	
	
	private class CourseEntry {
		public PolarCoordinate position;
		public double distance;
		
		public CourseEntry (PolarCoordinate position, double distance) {
			this.position = position;
			this.distance = distance;
		}
	}
	
}
