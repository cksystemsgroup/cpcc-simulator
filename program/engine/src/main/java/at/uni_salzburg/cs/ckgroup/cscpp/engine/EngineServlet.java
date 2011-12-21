/*
 * @(#) EngineServlet.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2011  Clemens Krainer
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.SensorProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.VirtualVehicleBuilder;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ServiceEntry;


@SuppressWarnings("serial")
public class EngineServlet extends HttpServlet implements IServletConfig {
	
	Logger LOG = Logger.getLogger(EngineServlet.class);
	
	private static final String PROP_PATH_NAME = "engine.properties";

	private ServletConfig servletConfig;
	private Properties props = new Properties ();
	private VirtualVehicleBuilder vehicleBuilder = new VirtualVehicleBuilder();
	private Configuration configuration = new Configuration();
	private List<IVirtualVehicle> vehicleList = new ArrayList<IVirtualVehicle>();
	private SensorProxy sensorProxy = new SensorProxy();
	
	private ServiceEntry[] services = {
		new ServiceEntry("/vehicle/.*", new VehicleService(this)),
		new ServiceEntry(".*", new DefaultService(this))
	};

	public void init (ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;
		super.init();
		myInit();
	}

	private void myInit () throws ServletException {

		InputStream propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROP_PATH_NAME);
		
		if (propStream == null)
			throw new ServletException ("Can not find file " + PROP_PATH_NAME + " in class path.");
		
		try {
			props.load(propStream);
					
			vehicleBuilder.setSensorProxy(sensorProxy);
			
			servletConfig.getServletContext().setAttribute("vehicleBuilder", vehicleBuilder);
			servletConfig.getServletContext().setAttribute("configuration", configuration);
			servletConfig.getServletContext().setAttribute("vehicleList", vehicleList);
			servletConfig.getServletContext().setAttribute("sensorProxy", sensorProxy);
			
			File contexTempDir = (File)servletConfig.getServletContext().getAttribute(VehicleService.CONTEXT_TEMP_DIR);
			configuration.setWorkDir (contexTempDir);
			
			File confFile = new File (contexTempDir, props.getProperty(VehicleService.PROP_CONFIG_FILE));
			if (confFile.exists()) {
				configuration.loadConfig(new FileInputStream(confFile));
				LOG.info("Loading existing configuration from " + confFile);
			}
			
			URI pilotSensorUrl = configuration.getPilotSensorUrl();
			if (pilotSensorUrl != null)
				sensorProxy.setSensorUrl(pilotSensorUrl.toASCIIString());
			
			FileFilter vehicleFilter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory() && pathname.getName().matches("vehicle\\d+");
				}
			};
			
			for (File vehicleDir : contexTempDir.listFiles(vehicleFilter)) {
				IVirtualVehicle vehicle = vehicleBuilder.build(vehicleDir);
				vehicleList.add(vehicle);				
				vehicle.resume();
			}
			
			sensorProxy.start();
		
		} catch (IOException e) {
			throw new ServletException (e);
		}

	}

	protected void service (HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String servicePath = request.getRequestURI();

		if (request.getRequestURI().startsWith(request.getContextPath())) {
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		}

		for (int k = 0; k < services.length; k++) {
			if (servicePath.matches(services[k].pattern)) {
				services[k].service.service(servletConfig, request, response);
				return;
			}
		}
		
		return;
	}
	
    public void destroy () {
    	sensorProxy.terminate();
    	// TODO check
    }

	public Properties getProperties() {
		return props;
	}
	
}
