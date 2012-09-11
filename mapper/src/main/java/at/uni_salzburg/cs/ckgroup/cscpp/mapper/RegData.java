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
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.course.WayPoint;

public class RegData implements IRegistrationData {
    
	private static final Logger LOG = Logger.getLogger(RegData.class);
	
	private static final int MAX_ACCESS_ERRORS = 5;
	
    private String engineUri;
    private String pilotUri;
    private List<IWayPoint> wayPoints;
    private Set<String> sensors;
    private boolean centralEngine;
    private int accessErrors = 0;
    private Map<String, String> pilotConfig;
    private IZone assignedZone;
    private Object mapperData;
    
    public RegData(String engineUri, String pilotUri, List<IWayPoint> wayPoints, Set<String> sensors, Map<String, String> pilotConfig, IZone assignedZone) {
        this.engineUri = engineUri;
        this.pilotUri = pilotUri;
        this.wayPoints = wayPoints;
        this.sensors = sensors;
        this.centralEngine = pilotUri == null || pilotUri.trim().isEmpty();
        this.pilotConfig = pilotConfig;
        this.assignedZone = assignedZone;
    }
    
	public RegData(JSONObject obj) {
		LOG.info("new RegData(): obj=" + obj.toJSONString());
        engineUri = (String)obj.get("engUri");
        Object ce = obj.get("centralEngine");
        centralEngine = ce == null ? false : ((Boolean)ce).booleanValue();
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
        	wayPoints = null;
        } else {
            wayPoints = new ArrayList<IWayPoint>();
			for (Object entry : (JSONArray)array) {
				WayPoint wayPoint = new WayPoint((JSONObject)entry);
				wayPoints.add(wayPoint);
			}
        }
        
        assignedZone = null;
	}
    
	@Override
    public String getEngineUrl() {
        return engineUri;
    }

	@Override
    public String getPilotUrl() {
        return pilotUri;
    }

	@Override
    public List<IWayPoint> getWaypoints() {
        return wayPoints;
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

	@Override
	public IZone getAssignedZone() {
		return assignedZone;
	}
	
	public void setAssignedZone(IZone assignedZone) {
		this.assignedZone = assignedZone;
	}
	
	@Override
	public Object getMapperData() {
		return mapperData;
	}
	
	@Override
	public void setMapperData(Object mapperData) {
		this.mapperData = mapperData;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
	    JSONObject obj = new JSONObject();
	    obj.put("engUri", engineUri);
	    obj.put("centralEngine", Boolean.valueOf(centralEngine));
	    if (pilotConfig != null) {
		    obj.put("pilotUri", pilotUri);
	    	obj.put("pilotName", pilotConfig.get("pilotName"));	
	    }
	    if (wayPoints != null) {
	    	obj.put("waypoints", wayPoints);	
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
