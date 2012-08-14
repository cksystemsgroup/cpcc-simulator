/*
 * @(#) Configuration.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.sim.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigurationParser;

public class Configuration extends ConfigurationParser implements IConfiguration {
	
	Logger LOG = Logger.getLogger(Configuration.class);
	
	private static final String PROP_TOMCAT_PREFIX = "tomcat.";
	private static final String PROP_TOMCAT_INSTANCES = PROP_TOMCAT_PREFIX + "instances";
	private static final String PROP_TOMCAT_REQUIRED_DIRECTORIES = PROP_TOMCAT_PREFIX + "required.directories";
	
	
	private static final String PROP_WEBAPP_PREFIX = "webapp.";
	private static final String PROP_WEBAPP_LIST = PROP_WEBAPP_PREFIX + "list";
	
	
	/**
	 * The parameters and their default values. 
	 */
	public static final String [][] parameters = {
		{ PROP_TOMCAT_INSTANCES },
		{ PROP_TOMCAT_REQUIRED_DIRECTORIES },
		{ PROP_WEBAPP_LIST }
	};
	
	/**
	 * The errors in the configuration as a map. Key is the configuration
	 * parameter and value is the according error message. This map contains
	 * only messages of erroneous configuration parameters.
	 */
	@SuppressWarnings("serial")
	private static final Map<String,String> configErrors = new HashMap<String,String>() {{
		put(PROP_TOMCAT_INSTANCES, ERROR_MESSAGE_MISSING_VALUE);
		put(PROP_TOMCAT_REQUIRED_DIRECTORIES, ERROR_MESSAGE_MISSING_VALUE);
		put(PROP_WEBAPP_LIST, ERROR_MESSAGE_MISSING_VALUE);
	}};
	
	private Map<String, TomcatInstance> tomcatInstances = new HashMap<String, TomcatInstance>();
	private String[] tomcatRequiredDirectories = null;
	private TemplateFiles tomcatTemplateFiles = new TemplateFiles();
	private Map<String,WebApplication> webApplications = new HashMap<String, WebApplication>();

	public Configuration() {
		super(parameters, configErrors);
	}

	/**
	 * Load a vehicle configuration from an <code>InputStream</code> and build it.
	 * 
	 * @param inStream the configuration's <code>InputStream</code>
	 * @throws IOException thrown in case of errors.
	 */
	public void loadConfig (InputStream inStream) throws IOException {
		super.loadConfig(inStream);
		
		List<String[]> pars = getParameters();
		
		tomcatInstances.clear();
		String tomcatInstanceString = parseString(PROP_TOMCAT_INSTANCES);
		if (tomcatInstanceString == null || !"".equals(tomcatInstanceString.trim())) {
			String[] tomcatInstanceNames = tomcatInstanceString.trim().split("\\s*,\\s*");
			for (String name : tomcatInstanceNames) {
				TomcatInstance tcInst = new TomcatInstance();
				tcInst.setFromProperties(PROP_TOMCAT_PREFIX + name + ".", getConfig(), pars, configErrors);
				tomcatInstances.put(name, tcInst);
			}
		}
		
		String tomcatRequiredDirectoriesString = parseString(PROP_TOMCAT_REQUIRED_DIRECTORIES);
		if (tomcatRequiredDirectoriesString == null || "".equals(tomcatRequiredDirectoriesString.trim())) {
			tomcatRequiredDirectories = new String[0];
		} else {
			tomcatRequiredDirectories = tomcatRequiredDirectoriesString.trim().split("\\s+");
		}
		
		tomcatTemplateFiles.setFromProperties(PROP_TOMCAT_PREFIX, getConfig(), pars, configErrors);
		
		String webAppListString = parseString(PROP_WEBAPP_LIST);
		
		webApplications.clear();
		String[] webAppNames =  webAppListString.split("\\s*,\\s*");
		for (String name : webAppNames) {
			WebApplication webApplication = new WebApplication();
			webApplication.setFromProperties(PROP_WEBAPP_PREFIX + name + ".", getConfig(), pars, configErrors);
			webApplications.put(name, webApplication);
		}
		
		LOG.info("Configuration loaded.");
	}
	
	@Override
	public Map<String, TomcatInstance> getTomcatInstances() {
		return tomcatInstances;
	}
	
	@Override
	public TemplateFiles getTomcatTemplateFiles() {
		return tomcatTemplateFiles;
	}
	
	@Override
	public Map<String, WebApplication> getWebApplications() {
		return webApplications;
	}
	
	@Override
	public String[] getTomcatRequiredDirectories() {
		return tomcatRequiredDirectories;
	}
}
