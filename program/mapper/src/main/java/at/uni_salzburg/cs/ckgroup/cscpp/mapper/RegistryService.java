/*
 * @(#) RegistryService.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2012  Michael Kleber
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

import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;



public class RegistryService extends DefaultService{

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
	        // TODO reg service
	        //
	        
	        String eng_uri = request.getParameter("enguri");
	        String sensor_uri = request.getParameter("sensoruri");
	        
	        if(eng_uri == null || sensor_uri == null) {
	            
	            response.getWriter().print("error");

	            LOG.info("Erroneous registration: engine='" + eng_uri + "', sensor='" + sensor_uri + "'");
	        }
	        else {
		        if(eng_uri.trim().isEmpty() || sensor_uri.trim().isEmpty()) {
		        	// TODO No sensors available, which is OK. This is a central engine.
		        	// TODO Use this to migrate completed virtual vehicles to.
		        	LOG.info("Sucessful registration: central engine='" + eng_uri + "'");
		        	
		        	
		        } else {
		        	LOG.info("Sucessful registration: engine='" + eng_uri + "', sensor='" + sensor_uri + "'");
		        	
		        	
		        }
	        	
	        	// TODO add engine
	            
	            // all successfull
	            response.getWriter().print("ok");
	            
	            
	        }
        
		} else{
			LOG.error("Can not handle: " + servicePath);
			emit404(request, response);
			return;
		}
        
    }
    
}
