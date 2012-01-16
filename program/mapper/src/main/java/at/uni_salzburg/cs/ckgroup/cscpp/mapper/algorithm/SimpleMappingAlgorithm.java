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

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;
import java.util.Map;
import org.apache.log4j.Logger;


public class SimpleMappingAlgorithm extends AbstractMappingAlgorithm {

	Logger LOG = Logger.getLogger(SimpleMappingAlgorithm.class);
	
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
                                String[] actions = vs.getActions();
                                PolarCoordinate pos = vs.getPosition();
                                String eng_uri_new = getEngine(pos, actions);
                                
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
        
        private String getEngine(PolarCoordinate pos, String[] sensors) {
            for(Map.Entry<String,StatusProxy> sp :statusProxyMap.entrySet()) {
                String v = sp.getKey();
                StatusProxy s = sp.getValue();
                if(s.getNextPosition().toString().equalsIgnoreCase(pos.toString())) {
                    RegData re_val = registrationData.get(v);
                    if(re_val != null) {
                        if(re_val.getSensors().toString().equalsIgnoreCase(sensors.toString())) {
                            return v;                
                        }
                    }
                }
            }
            return null;
        }
	
}
