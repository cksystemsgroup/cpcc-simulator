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

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.VehicleStatus.Status;
import java.util.Map;
import org.apache.log4j.Logger;


public class SimpleMappingAlgorithm extends AbstractMappingAlgorithm {

	Logger LOG = Logger.getLogger(SimpleMappingAlgorithm.class);
        private IGeodeticSystem geodeticSystem = new WGS84();
        private String central_engine;
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		LOG.error("execute() not yet implemented!");
                
                if (virtualVehicleMap.isEmpty()) {
			LOG.info("No migration because of empty virtual vehicle map.");
			return;
		}
                
                if(statusProxyMap.isEmpty()) {
                        LOG.info("No migration because of empty status proxy map.");
                        return;                        
                }
                
                
                
                for (Map.Entry<String, Map<String, VehicleStatus>> vehicle : virtualVehicleMap.entrySet()) {
                    
                    if(vehicle == null)
                        continue;
                    else if(vehicle.getValue().isEmpty())
                        continue;
                    else {
                        
                        String eng_url = vehicle.getKey();
                        Map<String, VehicleStatus> vstat_map = vehicle.getValue();
                        
                        for(Map.Entry<String, VehicleStatus> vstat : vstat_map.entrySet()) {
                                VehicleStatus vs = (VehicleStatus) vstat.getValue();
                                String vv = vs.getId();

                                if(vs.getState() == Status.COMPLETED) {
                                    if(central_engine != null) {
                                        migrate(eng_url, vv, central_engine);
                                    }
                                    continue;
                                }
                                
                                String[] actions = vs.getActions();
                                PolarCoordinate pos = vs.getPosition();
                                double tol = vs.getTolerance();
                                
                                String eng_uri_new = getEngine(pos, tol, actions);
                                
                                if(eng_uri_new == null) {
                                    continue;
                                }
                                
                                if(eng_uri_new.equalsIgnoreCase(eng_url)) {
                                    continue;
                                }
                                else {
                                    migrate(eng_url, vv, eng_uri_new);
                                }
                        }
                     
                    }
                }
	}
        
        private String getEngine(PolarCoordinate pos, double tol, String[] sensors) {
            CartesianCoordinate virtualPosCart = geodeticSystem.polarToRectangularCoordinates(pos);
            
            for(Map.Entry<String,StatusProxy> sp :statusProxyMap.entrySet()) {
                String v = sp.getKey();
                StatusProxy s = sp.getValue();
                PolarCoordinate cur = s.getCurrentPosition();
                
                if(cur != null) {
                    CartesianCoordinate currentPosCart = geodeticSystem.polarToRectangularCoordinates(cur);
                    CartesianCoordinate nextPosCart = geodeticSystem.polarToRectangularCoordinates(s.getNextPosition());
                    if(isNear(currentPosCart, nextPosCart, virtualPosCart, tol)) {
                        RegData re_val = registrationData.get(v);
                        if(re_val != null) {
                            if(re_val.getSensors().toString().equalsIgnoreCase(sensors.toString())) {
                                return v;                
                            }
                        }
                    }    
                }
                else if (central_engine == null) central_engine = v;
            }
            return null;
        }
        
        private boolean isNear(CartesianCoordinate current, CartesianCoordinate next, CartesianCoordinate point, double tol) {
            // direction vec = next - current
            CartesianCoordinate dir = next.subtract(current);
            // d = |dir x (point - current)| / |dir|
            double d = (dir.crossProduct((current.subtract(point))).norm()) / dir.norm();
            
            if(d <= tol)
                return true;
            else
                return false;
        }
	
}
