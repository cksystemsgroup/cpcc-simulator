/*
 * @(#) RegData.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IWayPoint;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.course.WayPoint;

public class RegData implements IRegistrationData {
    
	private static final Logger LOG = Logger.getLogger(RegData.class);
	
	private static final int MAX_ACCESS_ERRORS = 5;
	
    private String eng_uri;
    private String pilotUri;
    private List<IWayPoint> waypoints;
    private Set<String> sensors;
    private boolean centralEngine;
    private int accessErrors = 0;
    private Map<String, String> pilotConfig;
    
    public RegData(String eu, String pu, List<IWayPoint> wp, Set<String> sensors2, Map<String, String> pilotConfig) {
        eng_uri = eu;
        pilotUri = pu;
        waypoints = wp;
        sensors = sensors2;
        centralEngine = pu == null || pu.trim().isEmpty();
        this.pilotConfig = pilotConfig;
    }
    
	public RegData(JSONObject obj) {
		LOG.info("new RegData(): obj=" + obj.toJSONString());
        eng_uri = (String)obj.get("engUri");
        pilotUri = (String)obj.get("pilotUri");
        pilotConfig = new HashMap<String, String>();
        pilotConfig.put("pilotName", (String)obj.get("pilotName"));
        
        Object array = obj.get("sensors");
        if (array == null) {
        	sensors = null;
        } else {
        	sensors = new HashSet<String>();
	        for (Object entry : (JSONArray)array) {
	        	sensors.add((String)entry);
	        }
        }
        
        array = (JSONArray)obj.get("waypoints");
        if (array == null) {
        	waypoints = null;
        } else {
            waypoints = new ArrayList<IWayPoint>();
			for (Object entry : (JSONArray)array) {
				WayPoint wayPoint = new WayPoint((JSONObject)entry);
				waypoints.add(wayPoint);
			}
        }
	}
    
	@Override
    public String getEngineUri() {
        return eng_uri;
    }

	@Override
    public String getPilotUri() {
        return pilotUri;
    }

	@Override
    public List<IWayPoint> getWaypoints() {
        return waypoints;
    }
    
	@Override
    public Set<String> getSensors() {
        return sensors;
    }

	@Override
	public boolean isCentralEngine() {
		return centralEngine;
	}
	
	@Override
	public boolean isMaxAccessErrorsLimitReached() {
		return ++accessErrors > MAX_ACCESS_ERRORS;
	}
	
	public void resetAccessErrors() {
		accessErrors = 0;
	}

	@Override
	public Map<String, String> getPilotConfig() {
		return pilotConfig;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
	    JSONObject obj = new JSONObject();
	    obj.put("engUri", eng_uri);
	    if (pilotConfig != null) {
		    obj.put("pilotUri", pilotUri);
	    	obj.put("pilotName", pilotConfig.get("pilotName"));	
	    }
	    if (waypoints != null) {
	    	obj.put("waypoints", waypoints);	
	    }
	    if (sensors != null) {
		    List<String> ss = new ArrayList<String>();
		    ss.addAll(sensors);
		    Collections.sort(ss);
		    obj.put("sensors", ss);
	    }
	    return obj.toString();
	}

}
