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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.course.WayPoint;

public class RegData implements JSONAware {
    
	private static final Logger LOG = Logger.getLogger(RegData.class);
	
    private String eng_uri;
    private String pilotUri;
    private List<WayPoint> waypoints;
    private Set<String> sensors;
    private boolean centralEngine;
    
    public RegData(String eu, String pu, List<WayPoint> wp, Set<String> sensors2) {
        eng_uri = eu;
        pilotUri = pu;
        waypoints = wp;
        sensors = sensors2;
        centralEngine = pu == null || pu.trim().isEmpty();
    }
    
	public RegData(JSONObject obj) {
		LOG.info("new RegData(): obj=" + obj.toJSONString());
        eng_uri = (String)obj.get("engUri");
        pilotUri = (String)obj.get("pilotUri");
        
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
            waypoints = new ArrayList<WayPoint>();
			for (Object entry : (JSONArray)array) {
				WayPoint wayPoint = new WayPoint((JSONObject)entry);
				waypoints.add(wayPoint);
			}
        }
	}
    
    public String getEngineUri() {
        return eng_uri;
    }

    public String getPilotUri() {
        return pilotUri;
    }

    public List<WayPoint> getWaypoints() {
        return waypoints;
    }
    
    public Set<String> getSensors() {
        return sensors;
    }

	public boolean isCentralEngine() {
		return centralEngine;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
	    JSONObject obj = new JSONObject();
	    obj.put("engUri", eng_uri);
	    obj.put("pilotUri", pilotUri);
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
