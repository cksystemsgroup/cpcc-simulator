/*
 * @(#) SimpleMappingAlgorithm.java
 *
 * This code is part of the ESE-CPCC project.
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
 */package at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.VehicleStatus.Status;


public class SimpleMappingAlgorithm extends AbstractMappingAlgorithm {

	private static final Logger LOG = Logger.getLogger(SimpleMappingAlgorithm.class);
	private IGeodeticSystem geodeticSystem = new WGS84();
	
	@Override
	public void execute() {
                
		if (getVirtualVehicleMap().isEmpty()) {
			LOG.info("No migration because of empty virtual vehicle map.");
			return;
		}
                
		if(getStatusProxyMap().isEmpty()) {
			LOG.info("No migration because of empty status proxy map.");
			return;
		}
		
		List<VehicleInfo> vehicleList = getVirtualVehicleList();
		
		for (VehicleInfo vehicleInfo : vehicleList) {

			String vv = vehicleInfo.getVehicleName();
			String eng_url = vehicleInfo.getEngineUrl();
			VehicleStatus vs = vehicleInfo.getVehicleStatus();
					
			if (vs.getState() == Status.COMPLETED) {
				if (getCentralEngineUrl() != null) {
					migrate(eng_url, vv, getCentralEngineUrl());
				} else {
					LOG.debug("Can not migrate completed VV " + vv + " to a central engine.");
				}
				continue;
			}
			
			Set<String> actions = vs.getActions();
			PolarCoordinate pos = vs.getPosition();
			double tol = vs.getTolerance();
			
			String eng_uri_new = getEngine(pos, tol, actions);
			if (eng_uri_new == null || eng_uri_new.equalsIgnoreCase(eng_url)) {
				continue;
			}
			
			migrate(eng_url, vv, eng_uri_new);
		}
		
	}
        
    private String getEngine(PolarCoordinate pos, double tol, Set<String> actions) {
    	if (pos == null) {
    		return null;
    	}
        Double minimumTime = Double.MAX_VALUE;
        String engineUrl = null;
        CartesianCoordinate virtualPosCart = geodeticSystem.polarToRectangularCoordinates(pos);
        
        for(Map.Entry<String,StatusProxy> sp : getStatusProxyMap().entrySet()) {
            String v = sp.getKey();
            StatusProxy s = sp.getValue();
            
            PolarCoordinate cur = s.getCurrentPosition();
            PolarCoordinate nxt = s.getNextPosition();
            Double velocity = s.getVelocity();
            if(cur != null && nxt != null && velocity != null) {
                CartesianCoordinate currentPosCart = geodeticSystem.polarToRectangularCoordinates(cur);
                CartesianCoordinate nextPosCart = geodeticSystem.polarToRectangularCoordinates(nxt);
                Double timeToPoint = isNear(currentPosCart, nextPosCart, virtualPosCart, tol, velocity);
                
                if(timeToPoint != null && timeToPoint < minimumTime) {
            		minimumTime = timeToPoint;
                    RegData re_val = getRegistrationData().get(v);
                    if(re_val != null) {
                    	for (String a : actions) {
                    		if (re_val.getSensors().contains(a)) {
                    			engineUrl = v;
                    			break;
                    		}
                    	}
                    }
                }    
            }
        }

        return engineUrl;
    }
        
    private Double isNear(CartesianCoordinate current, CartesianCoordinate next, CartesianCoordinate point, double tol, double velocity) {
        // direction vec = next - current
        CartesianCoordinate dir = next.subtract(current);
        // CN = c + t*dir -> t
        double t = ((current.multiply(dir)*(-1)) + (point.multiply(dir)) / dir.multiply(dir));
        // t in CN -> L
        CartesianCoordinate l = new CartesianCoordinate(current.getX() + t*dir.getX(), current.getY() + t*dir.getY(), current.getZ() + t*dir.getZ());
        
        if(current.norm() <= l.norm() && l.norm() <= next.norm()) {
            // d = | P - L |
            double d = point.subtract(l).norm();
            
            return d <= tol ? point.subtract(current).norm()/velocity : null;
        }
        else return null;
    }
	
}
