/*
 * @(#) Configuration.java
 *
 * This code is part of the JNavigator project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigurationParser;

public class Configuration extends ConfigurationParser implements IConfiguration {
	
	Logger LOG = Logger.getLogger(Configuration.class);
	
	public static final String PROP_HOME_BASE_ENGINE_URL = "home.base.engine.url";
	
	/**
	 * The parameters and their default values. 
	 */
	public static final String [][] parameters = {
		{ PROP_HOME_BASE_ENGINE_URL },
	};
	
	/**
	 * The errors in the configuration as a map. Key is the configuration
	 * parameter and value is the according error message. This map contains
	 * only messages of erroneous configuration parameters.
	 */
	@SuppressWarnings("serial")
	private static final Map<String,String> configErrors = new HashMap<String,String>() {{
		put(PROP_HOME_BASE_ENGINE_URL,ERROR_MESSAGE_MISSING_VALUE);
	}};
	
	/**
	 * The base URL of the associated home base engine. 
	 */
	private URI homeBaseEngineUrl;
	
	/**
	 * Construct a <code>Configuration</code> object.
	 */
	public Configuration () {
		super(parameters, configErrors);
	}
	
	/**
	 * Load the mapper configuration from an <code>InputStream</code> and build it.
	 * 
	 * @param inStream the configuration's <code>InputStream</code>
	 * @throws IOException thrown in case of errors.
	 */
	@Override
	public void loadConfig (InputStream inStream) throws IOException {
		super.loadConfig(inStream);
		homeBaseEngineUrl = parseURI(PROP_HOME_BASE_ENGINE_URL);
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.mapper.config.IConfiguration#getHomeBaseEngineUrl()
	 */
	public URI getHomeBaseEngineUrl() {
		return homeBaseEngineUrl;
	}

}