/*
 * @(#) WaypointsQuery.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.viewer.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class WaypointsQuery implements IJsonQuery {
	
	private static final Logger LOG = Logger.getLogger(WaypointsQuery.class);
	
	public String execute(IServletConfig config, String[] parameters) {
		
		Properties props = config.getProperties();
		
		String pilotListString = props.getProperty(IJsonQuery.PROP_PILOT_LIST);
		String[] pilotList = pilotListString.trim().split("\\s*,\\s*");
		JSONParser parser = new JSONParser();
		
		// TODO do this in parallel
		Map<String, Object> pilotWaypoints = new LinkedHashMap<String, Object>();
		for (String pilot : pilotList) {
			String waypoints = null;
			try {
				String pilotWaypointsURL = props.getProperty(PROP_PILOT_PREFIX+pilot+PROP_PILOT_WAYPOINTS_URL);
				waypoints = HttpQueryUtils.simpleQuery(pilotWaypointsURL);
				Map<String, Object> p = new LinkedHashMap<String, Object>();
				p.put("waypoints", parser.parse(waypoints));
				pilotWaypoints.put(pilot, p);
			} catch (Exception e) {
				LOG.info("Can not query pilot " + pilot + ": " + waypoints);
			}
		}

		return JSONValue.toJSONString(pilotWaypoints);
	}

}
