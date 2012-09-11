/*
 * @(#) EngMapServlet.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.engmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cpcc.engmap.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cpcc.engmap.json.JsonQueryService;
import at.uni_salzburg.cs.ckgroup.cpcc.engmap.mapper.Mapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone.Group;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.TimeFormatter;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.VehicleService;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.VehicleStorage;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.VirtualVehicleBuilder;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.StatusService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.SensorProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ServiceEntry;


@SuppressWarnings("serial")
public class EngMapServlet extends HttpServlet implements IServletConfig {
	
	Logger LOG = Logger.getLogger(EngMapServlet.class);
	
	private static final String CONTEXT_TEMP_DIR = "javax.servlet.context.tempdir";
	private static final String PROP_PATH_NAME = "engmap.properties";
	private static final String PROP_CONFIG_FILE = "engmap.config.file";
	private static final String PROP_STORAGE_DEPTH = "engmap.storage.depth";
	
	private ServletConfig servletConfig;
	private Properties props = new Properties ();
	private VirtualVehicleBuilder vehicleBuilder = new VirtualVehicleBuilder();
	private Configuration configuration = new Configuration();
	private Map<String,IVirtualVehicle> vehicleMap = new ConcurrentHashMap<String,IVirtualVehicle>();
	private SensorProxy sensorProxy = new SensorProxy();
	private File contexTempDir;
	private File configFile;
	private VehicleStorage vehicleStorage;
	
	private Map<String, IRegistrationData> regdata;
	private Set<IZone> neighborZones;
	private Set<String> centralEngines;
	private Mapper mapper = new Mapper();
        
	private ServiceEntry[] services = {
		new ServiceEntry("/vehicle/.*", new VehicleService(this)),
		new ServiceEntry("/status/.*", new StatusService(this)),
		new ServiceEntry("/config/.*", new ConfigService(this)),
		new ServiceEntry("/json/.*", new JsonQueryService(this, mapper)),
		new ServiceEntry(".*", new DefaultService(this))
	};

	public void init (ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;
		regdata = new ConcurrentSkipListMap<String, IRegistrationData>();
		centralEngines = new ConcurrentSkipListSet<String>();
		neighborZones = new ConcurrentSkipListSet<IZone>();
		super.init();
		myInit();
	}

	private void myInit () throws ServletException {

		InputStream propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROP_PATH_NAME);
		
		if (propStream == null)
			throw new ServletException ("Can not find file " + PROP_PATH_NAME + " in class path.");
		
		try {
			props.load(propStream);
			propStream.close();
					
			vehicleBuilder.setSensorProxy(sensorProxy);
			
			servletConfig.getServletContext().setAttribute("vehicleBuilder", vehicleBuilder);
			servletConfig.getServletContext().setAttribute("configuration", configuration);
			servletConfig.getServletContext().setAttribute("vehicleMap", vehicleMap);
			servletConfig.getServletContext().setAttribute("sensorProxy", sensorProxy);
			servletConfig.getServletContext().setAttribute("timeFormatter", new TimeFormatter());
			servletConfig.getServletContext().setAttribute("regdata", regdata);
			servletConfig.getServletContext().setAttribute("centralEngines", centralEngines);
			
			contexTempDir = (File)servletConfig.getServletContext().getAttribute(CONTEXT_TEMP_DIR);
			configuration.setWorkDir (contexTempDir);
			
			int storageDepth = Integer.parseInt(props.getProperty(PROP_STORAGE_DEPTH, "0"));
			if (storageDepth > 4) {
				storageDepth = 4;
			} else if (storageDepth < 0) {
				storageDepth = 0;
			}
			vehicleStorage = new VehicleStorage(contexTempDir, storageDepth);
			servletConfig.getServletContext().setAttribute("vehicleStorage", vehicleStorage);
			vehicleBuilder.setVehicleStorage(vehicleStorage);
			
			configFile = new File (contexTempDir, props.getProperty(PROP_CONFIG_FILE));
			reloadConfigFile();
		
		} catch (IOException e) {
			throw new ServletException (e);
		}
		
		for (ServiceEntry entry : services) {
			if (entry.getService() instanceof JsonQueryService) {
				JsonQueryService jqs = (JsonQueryService)entry.getService();
				jqs.setVehicleMap(vehicleMap);
			}
		}
		
		servletConfig.getServletContext().setAttribute("mapper", mapper);
		mapper.setRegistrationData(regdata);
		mapper.setZones(configuration.getZoneSet());
		mapper.setRegisteredCentralEngines(configuration.getCentralEngineUrls());
		mapper.setNeighborZones(neighborZones);
		mapper.setVehicleMap(vehicleMap);
		mapper.setLocalEngineUrl(configuration.getWebApplicationBaseUrl());
		mapper.start();
	}
	
	private void registerMySelfWithMe() throws IOException {
		
		URI baseUrl = configuration.getWebApplicationBaseUrl();
		if (baseUrl == null) {
			LOG.error("Web application URL is not configured!");
			return;
		}
		
		String engineUri = baseUrl.toASCIIString();
		
		if (configuration.isPilotAvailable() && configuration.getPilotUrl() == null) {
			LOG.error("Pilot URL is not configured!");
			return;
		}
		
		String pilotUri = configuration.isPilotAvailable() ? configuration.getPilotUrl().toASCIIString() : null;
		Set<String> availableSensors;
		Map<String, String> pilotConfig;
		try {
			availableSensors = configuration.isPilotAvailable() ? sensorProxy.getAvailableSensors() : null;
			pilotConfig = configuration.isPilotAvailable() ? sensorProxy.getPilotConfig() : null;
		} catch (ParseException e) {
			throw new IOException(e);
		}
		
		IZone assignedZone = null;
		for (IZone z : configuration.getZoneSet()) {
			if (z.getZoneGroup() == Group.LOCAL) {
				assignedZone = z;
				break;
			}
		}
		
		RegData rd = new RegData(engineUri, pilotUri, null, availableSensors, pilotConfig, assignedZone);
		regdata.put(engineUri, rd);
	}

	@Override
	protected void service (HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String servicePath = request.getRequestURI();

		if (request.getRequestURI().startsWith(request.getContextPath())) {
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		}

		for (int k = 0; k < services.length; k++) {
			if (servicePath.matches(services[k].getPattern())) {
				services[k].getService().service(servletConfig, request, response);
				return;
			}
		}
		
		return;
	}
	
	@Override
    public void destroy () {
    	sensorProxy.terminate();
    	mapper.terminate();
    	
    	for (IVirtualVehicle vehicle : vehicleMap.values()) {
    		if (vehicle.isActive()) {
				try {
					vehicle.suspend();
				} catch (IOException e) {
					LOG.error("Errors when suspending vehicle " + vehicle.getWorkDir(), e);
				}
    		}
    	}
    	
//    	if(reg_thread.isAlive() || reg_thread.isInterrupted())
//    		reg_thread.setStop();
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
		
		FileInputStream inStream = new FileInputStream(configFile);
		configuration.loadConfig(inStream);
		inStream.close();
		LOG.info("Loading configuration from " + configFile);
		
		URI pilotUrl = configuration.getPilotUrl();
		sensorProxy.setPilotUrl(pilotUrl != null ? pilotUrl.toASCIIString() : null);
		
		LOG.info("Loading configuration from " + configFile);
		List<IMappingAlgorithm> algorithms = new ArrayList<IMappingAlgorithm>();
		List<Class<IMappingAlgorithm>> mapperAlgorithmClassList = configuration.getMapperAlgorithmClassList();
		if (mapperAlgorithmClassList != null) {
			for (Class<IMappingAlgorithm> cls : mapperAlgorithmClassList) {
				try {
					algorithms.add(cls.newInstance());
				} catch (Exception e) {
					LOG.error("Can not instantiate mapper algorithm", e);
					throw new IOException("Can not instantiate mapper algorithm", e);
				}
			}
		}
		mapper.setMappingAlgorithms(algorithms);
		
		
		for (File vehicleDir : vehicleStorage.listVehicleFolder()) {
			IVirtualVehicle vehicle = vehicleMap.get(vehicleDir.getName());
			if (vehicle == null) {
				try {
					vehicle = vehicleBuilder.build(vehicleDir);
					if (!vehicle.isActive() && configuration.isPilotAvailable()) {
						vehicle.resume();
					}
					vehicleMap.put(vehicleDir.getName(),vehicle);
				} catch (IOException e) {
					vehicleMap.remove(vehicleDir.getName());
					vehicleStorage.removeVehicleWorkDir(vehicleDir);
					LOG.error("Virtual vehicle in " + vehicleDir.getName() + " is corrupt and has been removed.");
				}
			}
		}
		
		if (configuration.isPilotAvailable() && !sensorProxy.isRunning()) {
			sensorProxy.start();
		}
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					registerMySelfWithMe();
				} catch (IOException e) {
					LOG.error("Self registration failed", e); 
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
}
