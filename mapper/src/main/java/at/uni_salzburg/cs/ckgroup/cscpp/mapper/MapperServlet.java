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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm.MappingAlgrithmBuilder;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.registry.RegistryPersistence;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ServiceEntry;


@SuppressWarnings("serial")
public class MapperServlet extends HttpServlet implements IServletConfig {
	
	Logger LOG = Logger.getLogger(MapperServlet.class);
	
	public static final String CONTEXT_TEMP_DIR = "javax.servlet.context.tempdir";
	public static final String PROP_PATH_NAME = "mapper.properties";
	public static final String PROP_CONFIG_FILE = "mapper.config.file";
	public static final String PROP_REGISTRY_FILE = "registry.file";
	public static final String PROP_MAPPER_ALGORITHM = "mapper.algorithm";
	
	private ServletConfig servletConfig;
	private Properties props = new Properties ();
	private Configuration configuration = new Configuration();
	private File contexTempDir;
	private File configFile;
	private File registryFile;
	private Map<String, RegData> regdata;
	private Set<String> centralEngines;
	private IMappingAlgorithm mappingAlgorithm;

	private ServiceEntry[] services = {
		new ServiceEntry("/config/.*", new ConfigService(this)),
		new ServiceEntry("/status/.*", new StatusService(this)),
		new ServiceEntry("/registry/.*", new RegistryService(this)),
		new ServiceEntry(".*", new DefaultService(this))
	};
	
	@Override
	public void init (ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;
		regdata = Collections.synchronizedMap(new TreeMap<String, RegData>());
		centralEngines = Collections.synchronizedSet(new HashSet<String>());
		super.init();
		myInit();
	}

	private void myInit () throws ServletException {

		InputStream propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROP_PATH_NAME);
		
		if (propStream == null)
			throw new ServletException ("Can not find file " + PROP_PATH_NAME + " in class path.");
		
		try {
			props.load(propStream);
			
			servletConfig.getServletContext().setAttribute("configuration", configuration);	
			servletConfig.getServletContext().setAttribute("regdata", regdata);
			servletConfig.getServletContext().setAttribute("centralEngines", centralEngines);
			
			contexTempDir = (File)servletConfig.getServletContext().getAttribute(CONTEXT_TEMP_DIR);
			configuration.setWorkDir (contexTempDir);
			
			configFile = new File (contexTempDir, props.getProperty(PROP_CONFIG_FILE));
			registryFile = new File (contexTempDir, props.getProperty(PROP_REGISTRY_FILE));
			servletConfig.getServletContext().setAttribute("registryFile", registryFile);
			reloadConfigFile();
			
			String algorithmName = props.getProperty(PROP_MAPPER_ALGORITHM, "random");
			mappingAlgorithm = MappingAlgrithmBuilder.build(algorithmName.trim(), regdata, centralEngines);
			servletConfig.getServletContext().setAttribute("mappingAlgorithm", mappingAlgorithm);
			
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
		mappingAlgorithm.terminate();
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
			configuration.loadConfig(new FileInputStream(configFile));
			LOG.info("Loading configuration from " + configFile);
		}
		if (registryFile != null && registryFile.exists()) {
			RegistryPersistence.loadRegistry(registryFile, regdata);
			LOG.info("Registry loaded from " + registryFile.getAbsolutePath());
		}
	}

}
