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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Configuration implements IConfiguration {
	
	Logger LOG = Logger.getLogger(Configuration.class);
	
	/**
	 * The uploaded configuration.
	 */
	private Properties conf = new Properties();
	
	/**
	 * The errors in the configuration as a map. Key is the configuration
	 * parameter and value is the according error message. This map contains
	 * only messages of erroneous configuration parameters.
	 */
	private Map<String,String> configErrors = new HashMap<String,String>() {{
		put(PROP_PILOT_SENSOR_URL,ERROR_MESSAGE_MISSING_VALUE);
	}};
	
	/**
	 * The directory to be used for temporary files. 
	 */
	private File workDir;
	
	public static final String ERROR_MESSAGE_MISSING_VALUE = "# please provide a value!";
	public static final String ERROR_MESSAGE_INVALID_VALUE = "# invalid value!";
	public static final String ERROR_MESSAGE_UNKNOWN_TYPE = "# unknown type!";
	public static final String ERROR_MESSAGE_UNKNOWN_SENSOR_TYPE = "# unknown sensor type!";

	public static final String PROP_PILOT_SENSOR_URL = "pilot.sensor.url";

	/**
	 * The prefix of the sensor properties. 
	 */
	public static final String PROP_SENSOR_PREFIX = "sensor.";
	public static final String PROP_SENSOR_LIST = PROP_SENSOR_PREFIX + "list";
	
	/**
	 * The parameters and their default values. 
	 */
	public String [][] parameters = {
		{ PROP_PILOT_SENSOR_URL },
	};
	
	/**
	 * This variable is set to true, if and only if the configuration is OK.
	 */
	private boolean configOk = false;
	
	/**
	 * The base URL of the associated auto pilot sensors. 
	 */
	private URI pilotSensorUrl;
	

	
	/**
	 * Load a vehicle configuration from an <code>InputStream</code> and build it.
	 * 
	 * @param inStream the configuration's <code>InputStream</code>
	 * @throws IOException thrown in case of errors.
	 */
	public void loadConfig (InputStream inStream) throws IOException {
		conf.clear();
		conf.load(inStream);
		configErrors.clear();
		
		for (String[] entry : parameters) {
			String v = conf.getProperty(entry[0]);
			if (v == null && entry.length >= 2 && entry[1] != null)
				conf.setProperty(entry[0], entry[1]);
		}

		configOk = true;
		for (String[] entry : parameters) {
			String v = conf.getProperty(entry[0]);
			if (v == null && (entry.length != 3 || !"false".equals(conf.getProperty(entry[2],"false"))))
				configOk = false;
		}
		
		pilotSensorUrl = parseURI(PROP_PILOT_SENSOR_URL);
	}

	/**
	 * @param param the property to be parsed. 
	 * @return the parsed property as a <code>String</code> object.
	 */
	private String parseString (String param) {
		String p = conf.getProperty(param);
		if (p == null || "".equals(p.trim())) {
			configErrors.put(param, ERROR_MESSAGE_MISSING_VALUE);
			configOk = false;
		}
		return p;
	}
	
	/**
	 * @param param the property to be parsed.
	 * @param template the requested file name template. 
	 * @return the parsed property as a <code>String</code> object.
	 */
	private String parseTemplate (String param, String template) {
		String p =  parseString (param);
		String res = String.format(template, p);
		URL u = Thread.currentThread().getContextClassLoader().getResource(res);
		if (u == null) {
			configErrors.put(param, ERROR_MESSAGE_UNKNOWN_TYPE);
			configOk = false;
		}
		return p;
	}
	
	/**
	 * @param param the property to be parsed.
	 * @return the parsed property as an <code>URI</code> object.
	 */
	private URI parseURI (String param) {
		URI u = null;
		try {
			u = new URI(conf.getProperty(param));
			if (u.getScheme() == null || u.getHost() == null || u.getPort() <= 0) {
				configErrors.put(param, ERROR_MESSAGE_INVALID_VALUE);
				configOk = false;
			}
		} catch (Throwable e) {
			configErrors.put(param, ERROR_MESSAGE_MISSING_VALUE);
			configOk = false;
		}
		return u;
	}
	
	/**
	 * @param param the property to be parsed.
	 * @return the parsed property as a <code>Boolean</code> object.
	 */
	private Boolean parseBool (String param) {
		Boolean b = null;
		try {
			b = Boolean.parseBoolean(conf.getProperty(param));
		} catch (Throwable e) {
			configErrors.put(param, ERROR_MESSAGE_INVALID_VALUE);
			configOk = false;
		}
		return b;
	}
	
	/**
	 * @param param the property to be parsed.
	 * @return the parsed property as an <code>int</code>.
	 */
	private int parseInt (String param) {
		int i = -1;
		try {
			i = Integer.parseInt(conf.getProperty(param));
		} catch (Throwable e) {
			configErrors.put(param, ERROR_MESSAGE_INVALID_VALUE);
			configOk = false;
		}
		return i;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.config.IConfiguration#getPilotSensorUrl()
	 */
	public URI getPilotSensorUrl() {
		return pilotSensorUrl;
	}

	/**
	 * @return the current configuration as a list of strings.
	 */
	public Properties getConfig () {
		return conf;
	}

	/**
	 * @return the errors of the current configuration as a map.  
	 */
	public Map<String,String> getConfigErrors() {
		return configErrors;
	}
	
	/**
	 * @return the names of all known parameters as a list of strings.
	 */
	public List<String> getParameterNames () {
		List<String> names = new ArrayList<String>();
		for (String[] entry : parameters)
			names.add(entry[0]);
		return names; 
	}
	
	/**
	 * @return returns true if the configuration is consistent.
	 */
	public boolean isConfigOk() {
		return configOk;
	}

	/**
	 * @param workDir the directory to be used for temporary files.
	 */
	public void setWorkDir(File workDir) {
		this.workDir = workDir;
	}
	
	/**
	 * @param key the system property key of interest.
	 * @return the value of the required property.
	 */
	public String getSystemProperty(String key) {
		return System.getProperty(key);
	}
}
