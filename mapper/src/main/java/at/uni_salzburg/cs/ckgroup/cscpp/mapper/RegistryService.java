/*
 * @(#) RegistryService.java
 *
 * This code is part of the CPCC project.
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
 */
package at.uni_salzburg.cs.ckgroup.cscpp.mapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IWayPoint;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.course.WayPointQueryService;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.registry.RegistryPersistence;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.SensorProxy;



public class RegistryService extends DefaultService {

    Logger LOG = Logger.getLogger(RegistryService.class);
    
    public static final String ACTION_ENGINE_REGISTRATION = "engineRegistration";
    
    public RegistryService(IServletConfig servletConfig){
        super(servletConfig);
    }
    
    @Override
    public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String servicePath = request.getRequestURI();
		if (request.getRequestURI().startsWith(request.getContextPath()))
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		
		String[] cmd = servicePath.trim().split("/+");
		if (cmd.length < 2) {
			emit404(request, response);
			return;
		}
		String action = cmd[2];
		
		if (ACTION_ENGINE_REGISTRATION.equals(action)) {
			
	        String eng_uri = request.getParameter("enguri");
	        String pilot_uri = request.getParameter("piloturi");
	        
	        if(eng_uri == null || eng_uri.trim().isEmpty()) {
	            
	            response.getWriter().print("error");
	            LOG.info("Erroneous registration: engine='" + eng_uri + "', pilot='" + pilot_uri + "'");
	        
	        } else {
	        	
				@SuppressWarnings("unchecked")
				Map<String, IRegistrationData> regdata = (Map<String, IRegistrationData>)config.getServletContext().getAttribute("regdata");

		        if(pilot_uri == null || pilot_uri.trim().isEmpty()) {
		        	// No sensors available, which is OK. This is a central engine.
		        	LOG.info("Sucessful registration: central engine='" + eng_uri + "'");
					regdata.put(eng_uri.trim(), new RegData(eng_uri, null, null, null, null, null));
					
					@SuppressWarnings("unchecked")
					Set<String> centralEngines = (Set<String>)config.getServletContext().getAttribute("centralEngines");
					centralEngines.add(eng_uri.trim());
		        	
		        } else {
		        	LOG.info("Sucessful registration: engine='" + eng_uri + "', pilot='" + pilot_uri + "'");
		        	
		        	LOG.info("Retrieving way-point list:");
		        	List<IWayPoint> wayPointList = null;
			        try {
						wayPointList = WayPointQueryService.getWayPointList(pilot_uri);
						for (IWayPoint p : wayPointList)
							LOG.info("Waypoint: " + p);
					} catch (ParseException e) {
						LOG.error("Error at retrieving way-point list", e);
					}
			        
		        	LOG.info("Retrieving available sensors:");		        
			        SensorProxy sensorProxy = new SensorProxy();
			        sensorProxy.setPilotUrl(pilot_uri);
			        Set<String> sensors = null;
			        Map<String,String> pilotConfig = null;
					try {
						sensors = sensorProxy.getAvailableSensors();
	
				        for (String sensor : sensors) {
				        	LOG.info("Sensor: " + sensor);
				        }
				        
				        pilotConfig = sensorProxy.getPilotConfig();
				        LOG.info("Pilot configuration: " + pilotConfig);
				        
					} catch (ParseException e) {
						LOG.error("Error at retrieving available sensors", e);
					}

					regdata.put(eng_uri.trim(), new RegData(eng_uri, pilot_uri, wayPointList, sensors, pilotConfig, null));

		        }

	            response.getWriter().print("ok");
	            
				File registryFile = (File)config.getServletContext().getAttribute("registryFile");
	            RegistryPersistence.storeRegistry(registryFile, regdata);
	        }
            return;

            
		} else{
			LOG.error("Can not handle: " + servicePath);
			emit404(request, response);
			return;
		}
        
    }

}
