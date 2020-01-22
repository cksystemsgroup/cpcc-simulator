/*
 * @(#) MapperServlet.java
 *
 * This code is part of the ESE-CPCC project.
 * Copyright (c) 2011  Clemens Krainer, Michael Kleber
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.json.JsonQueryService;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.registry.RegistryPersistence;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ServiceEntry;


@SuppressWarnings("serial")
public class MapperServlet extends HttpServlet implements IRegistry, IServletConfig {
	
	Logger LOG = LoggerFactory.getLogger(MapperServlet.class);
	
	public static final String CONTEXT_TEMP_DIR = "javax.servlet.context.tempdir";
	public static final String PROP_PATH_NAME = "mapper.properties";
	public static final String PROP_CONFIG_FILE = "mapper.config.file";
	public static final String PROP_REGISTRY_FILE = "registry.file";
	
	private ServletConfig servletConfig;
	private Properties props = new Properties ();
	private Configuration configuration = new Configuration();
	private File contexTempDir;
	private File configFile;
	private File registryFile;
	private Map<String, IRegistrationData> regdata;
//	private Set<IZone> zones;
	private Set<IZone> neighborZones;
	private Set<String> centralEngines;
	private Mapper mapper = new Mapper();

	private ServiceEntry[] services = {
		new ServiceEntry("/config/.*", new ConfigService(this)),
		new ServiceEntry("/json/.*", new JsonQueryService(this, mapper)),
		new ServiceEntry("/status/.*", new StatusService(this)),
		new ServiceEntry("/registry/.*", new RegistryService(this)),
		new ServiceEntry(".*", new DefaultService(this))
	};
	
	@Override
	public void init (ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;
//		regdata = Collections.synchronizedMap(new TreeMap<String, IRegistrationData>());
		regdata = new ConcurrentSkipListMap<String, IRegistrationData>();
//		centralEngines = Collections.synchronizedSet(new HashSet<String>());
		centralEngines = new ConcurrentSkipListSet<String>();
//		zones = Collections.synchronizedSet(new HashSet<IZone>());
//		neighborZones = Collections.synchronizedSet(new HashSet<IZone>());
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
			
//			ZoneFactory.buildZones(zones);
//			servletConfig.getServletContext().setAttribute("zones", zones);
//			ZoneFactory.buildNeighborZones(neighborZones);
//			servletConfig.getServletContext().setAttribute("neighborZones", neighborZones);
			
			servletConfig.getServletContext().setAttribute("configuration", configuration);	
			servletConfig.getServletContext().setAttribute("regdata", regdata);
			servletConfig.getServletContext().setAttribute("centralEngines", centralEngines);
			
			contexTempDir = (File)servletConfig.getServletContext().getAttribute(CONTEXT_TEMP_DIR);
			configuration.setWorkDir (contexTempDir);
			configuration.addClassLoader(servletConfig.getClass().getClassLoader());
			configFile = new File (contexTempDir, props.getProperty(PROP_CONFIG_FILE));
			registryFile = new File (contexTempDir, props.getProperty(PROP_REGISTRY_FILE));
			servletConfig.getServletContext().setAttribute("registryFile", registryFile);
			reloadConfigFile();
			
			
			servletConfig.getServletContext().setAttribute("mapper", mapper);
			mapper.setRegistrationData(regdata);
			mapper.setZones(configuration.getZoneSet());
			mapper.setRegisteredCentralEngines(configuration.getCentralEngineUrls());
			mapper.setNeighborZones(neighborZones);
			mapper.start();
			
			
			
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
			if (servicePath.matches(services[k].getPattern())) {
				services[k].getService().service(servletConfig, request, response);
				return;
			}
		}
		
		return;
	}
	
	@Override
    public void destroy () {
		mapper.terminate();
    	// TODO check / implement
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
		if (configFile != null && configFile.exists()) {
			FileInputStream inStream = new FileInputStream(configFile);
			configuration.loadConfig(inStream);
			inStream.close();
			
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
		}
		if (registryFile != null && registryFile.exists()) {
			RegistryPersistence.loadRegistry(registryFile, regdata);
			LOG.info("Registry loaded from " + registryFile.getAbsolutePath());
		}
	}

	@Override
	public Map<String, IRegistrationData> getRegistrationData() {
		return regdata;
	}
	
}
