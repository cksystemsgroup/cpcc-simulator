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
import java.util.HashMap;
import java.util.Map;
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
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ServiceEntry;


@SuppressWarnings("serial")
public class EngineServlet extends HttpServlet implements IServletConfig {
	
	Logger LOG = Logger.getLogger(EngineServlet.class);
	
	public static final String CONTEXT_TEMP_DIR = "javax.servlet.context.tempdir";
	private static final String PROP_PATH_NAME = "engine.properties";
	public final static String PROP_CONFIG_FILE = "vehicle.config.file";
	
	private ServletConfig servletConfig;
	private Properties props = new Properties ();
	private VirtualVehicleBuilder vehicleBuilder = new VirtualVehicleBuilder();
	private Configuration configuration = new Configuration();
	private Map<String,IVirtualVehicle> vehicleMap = new HashMap<String,IVirtualVehicle>();
	private SensorProxy sensorProxy = new SensorProxy();
	private File contexTempDir;
	private File configFile;
	
	private ServiceEntry[] services = {
		new ServiceEntry("/vehicle/.*", new VehicleService(this)),
		new ServiceEntry("/config/.*", new ConfigService(this)),
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
			servletConfig.getServletContext().setAttribute("vehicleMap", vehicleMap);
			servletConfig.getServletContext().setAttribute("sensorProxy", sensorProxy);
			
			contexTempDir = (File)servletConfig.getServletContext().getAttribute(CONTEXT_TEMP_DIR);
			configuration.setWorkDir (contexTempDir);
			
			configFile = new File (contexTempDir, props.getProperty(PROP_CONFIG_FILE));
			reloadConfigFile();
		
		} catch (IOException e) {
			throw new ServletException (e);
		}

	}
	
	@Override
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
	
	@Override
    public void destroy () {
    	sensorProxy.terminate();
    	
    	for (IVirtualVehicle vehicle : vehicleMap.values()) {
    		if (vehicle.isActive()) {
				try {
					vehicle.suspend();
				} catch (IOException e) {
					LOG.error("Errors when suspending vehicle " + vehicle.getWorkDir(), e);
				}
    		}
    	}
    }
    
    @Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public File getContextTempDir() {
		return contexTempDir;
	}

	@Override
	public File getConfigFile() {
		return configFile;
	}

	@Override
	public void reloadConfigFile() throws IOException {
		if (configFile == null || !configFile.exists()) {
			LOG.error("No configuration file available.");
			return;
		}
		
		configuration.loadConfig(new FileInputStream(configFile));
		LOG.info("Loading configuration from " + configFile);
		
		URI pilotSensorUrl = configuration.getPilotSensorUrl();
		sensorProxy.setSensorUrl(pilotSensorUrl != null ? pilotSensorUrl.toASCIIString() : null);
		
		FileFilter vehicleFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && pathname.getName().matches("vehicle\\d+");
			}
		};
		
		for (File vehicleDir : contexTempDir.listFiles(vehicleFilter)) {
			IVirtualVehicle vehicle = vehicleMap.get(vehicleDir.getName());
			if (vehicle == null) {
				vehicle = vehicleBuilder.build(vehicleDir);
				vehicleMap.put(vehicleDir.getName(),vehicle);
			}
			if (!vehicle.isActive())
				vehicle.resume();
		}
		
		if (configuration.isPilotAvailable() && !sensorProxy.isAlive())
			sensorProxy.start();
	}
	
}
