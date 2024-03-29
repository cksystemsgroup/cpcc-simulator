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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.CircleZone;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.PolygonZone;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ConfigurationParser;

public class Configuration extends ConfigurationParser implements IConfiguration {
	
	Logger LOG = LoggerFactory.getLogger(Configuration.class);
	
	private static final String PROP_MAPPER_ALGORITHM = "mapper.algorithm";
	
	private static final String PROP_ZONE_PREFIX = "zone.";
	private static final String PROP_ZONE_LIST = PROP_ZONE_PREFIX + "list";
	private static final String PROP_ZONE_ENGINE_POSTFIX = ".engine";
	private static final String PROP_ZONE_GROUP_POSTFIX = ".group";
	private static final String PROP_ZONE_TYPE_POSTFIX = ".type";
	private static final String PROP_ZONE_VERTICES_POSTFIX = ".vertices";
	private static final String PROP_ZONE_POSITION_POSTFIX = ".position";
	private static final String PROP_ZONE_RADIUS_POSTFIX = ".radius";
	
	private static final String PROP_CENTRAL_ENGINES = "mapper.central.engines";
	
	/**
	 * The parameters and their default values. 
	 */
	public static final String [][] parameters = {
		{ PROP_MAPPER_ALGORITHM },
		{ PROP_ZONE_LIST },
	};
	
	/**
	 * The errors in the configuration as a map. Key is the configuration
	 * parameter and value is the according error message. This map contains
	 * only messages of erroneous configuration parameters.
	 */
	@SuppressWarnings("serial")
	private static final Map<String,String> configErrors = new HashMap<String,String>() {{
		put(PROP_MAPPER_ALGORITHM, ERROR_MESSAGE_MISSING_VALUE);
		put(PROP_ZONE_LIST, ERROR_MESSAGE_MISSING_VALUE);
	}};

	
	/**
	 * The class name of the mapping algorithm. 
	 */
	private List<Class<IMappingAlgorithm>> mapperAlgorithmClassList;
	
	/**
	 * 
	 */
	private Set<IZone> zoneSet;

	/**
	 * 
	 */
	private IGeodeticSystem geodeticSystem = new WGS84();
	
	/**
	 * 
	 */
	private Set<String> centralEngineUrls;
	
	/**
	 * Construct a <code>Configuration</code> object.
	 */
	public Configuration () {
		super(parameters, configErrors);
		zoneSet = Collections.synchronizedSet(new HashSet<IZone>());
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
		mapperAlgorithmClassList = new ArrayList<Class<IMappingAlgorithm>>();
		mapperAlgorithmClassList.addAll(parseClassName(PROP_MAPPER_ALGORITHM, IMappingAlgorithm.class));
		
		List<String[]> pars = getParameters();
		
		centralEngineUrls = new HashSet<String>();
		String propCenralEngines = parseString(PROP_CENTRAL_ENGINES, "").trim();
		if (!"".equals(propCenralEngines)) {
			pars.add(new String[] {propCenralEngines});
			String[] ces = propCenralEngines.split("\\s*,\\s*");
			for (String ce : ces) {
				centralEngineUrls.add(ce);
			}
		}
		
		zoneSet.clear();
		
		String zoneListString = parseString(PROP_ZONE_LIST);
		if (zoneListString == null) {
			return;
		}
		
        Pattern p = Pattern.compile ("\\([^()]+\\)");

		String[] zoneNames = zoneListString.trim().split("\\s*,\\s*");
		for (String name : zoneNames) {
			
			String propEngine = PROP_ZONE_PREFIX + name + PROP_ZONE_ENGINE_POSTFIX;
			String engine = parseString(propEngine, "");
			if (!"".equals(engine)) {
				pars.add(new String[] {propEngine});
			}
			
			String propGroup = PROP_ZONE_PREFIX + name + PROP_ZONE_GROUP_POSTFIX;
			String group = parseString(propGroup, "local");
			pars.add(new String[] {propGroup});

			String propType = PROP_ZONE_PREFIX + name + PROP_ZONE_TYPE_POSTFIX;
			String type = parseString(propType);
			pars.add(new String[] {propType});
			
			IZone zone = null;
			
			if ("polygon".equals(type)) {
				String propVertices = PROP_ZONE_PREFIX + name + PROP_ZONE_VERTICES_POSTFIX;
				pars.add(new String[] {propVertices});
				
				String verticesString = parseString(propVertices);
				if (verticesString == null) {
					continue;
				}
				
				List<PolarCoordinate> vertices = new ArrayList<PolarCoordinate>();
				
                Matcher m = p.matcher (verticesString);

                while (m.find ())
                {
                	String verticeString = verticesString.substring (m.start () + 1, m.end () - 1).trim();
                	String[] ll = verticeString.split("\\s*,\\s*");
                	if (ll.length != 2) {
                		configErrors.put(propVertices, ERROR_MESSAGE_INVALID_VALUE);
                		setConfigOk(false);
                	}
                	double latitude = Double.parseDouble(ll[0]);
                	double longitude = Double.parseDouble(ll[1]);
                	vertices.add(new PolarCoordinate(latitude, longitude, 0.0));
                }

                zone = new PolygonZone(vertices.toArray(new PolarCoordinate[0]));
				
			} else if ("circle".equals(type)) {
				String propPosition = PROP_ZONE_PREFIX + name + PROP_ZONE_POSITION_POSTFIX;
				String propRadius = PROP_ZONE_PREFIX + name + PROP_ZONE_RADIUS_POSTFIX;
				pars.add(new String[] {propPosition});
				pars.add(new String[] {propRadius});
				
				String positionString = parseString(propPosition);
				if (positionString == null) {
					continue;
				}
				
				if (!positionString.matches("\\s*\\(\\s*\\d+.\\d+\\s*,\\s*\\d+.\\d+\\)\\s*")) {
            		configErrors.put(propPosition, ERROR_MESSAGE_INVALID_VALUE);
            		setConfigOk(false);
				}
				
				String[] ll = positionString.trim().replaceAll("\\(\\s*", "").replaceAll("\\s*\\)", "").split("\\s*,\\s*");
            	if (ll.length != 2) {
            		configErrors.put(propPosition, ERROR_MESSAGE_INVALID_VALUE);
            		setConfigOk(false);
            	}
            	double latitude = Double.parseDouble(ll[0]);
            	double longitude = Double.parseDouble(ll[1]);
				double radius = parseDouble(propRadius);
				
				zone = new CircleZone(new PolarCoordinate(latitude, longitude, 0.0), radius, geodeticSystem);
				
			} else {
				configErrors.put(propType, ERROR_MESSAGE_INVALID_VALUE);
				setConfigOk(false);
			}
			
			if (zone != null) {
				zone.setZoneEngineUrl(engine);
				zone.setZoneGroup(IZone.Group.valueOf(group.toUpperCase()));
				zoneSet.add(zone);
			}
			
		}
		 
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.mapper.config.IConfiguration#getMapperAlgorithmClass()
	 */
	@Override
	public List<Class<IMappingAlgorithm>> getMapperAlgorithmClassList() {
		return mapperAlgorithmClassList;
	}

	@Override
	public Set<IZone> getZoneSet() {
		return zoneSet;
	}
	
	@Override
	public Set<String> getCentralEngineUrls() {
		return centralEngineUrls;
	}
	
}
