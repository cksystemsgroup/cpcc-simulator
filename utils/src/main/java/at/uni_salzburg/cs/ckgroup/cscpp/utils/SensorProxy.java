/*
 * @(#) SensorProxy.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ISensorProxy;

public class SensorProxy extends Thread implements ISensorProxy {
	
	private static final Logger LOG = Logger.getLogger(SensorProxy.class);
	
	private String pilotUrl;
	
	private boolean running = false;
	
	private static final long CYCLE = 1000;
	
	private PolarCoordinate currentPosition = null;
	
	private Double speedOverGround = null;
	
	private Double orientationOverGround = null;
	
	private Double altitudeOverGround = null;

	private String waypoints = null;
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getCurrentPosition()
	 */
	@Override
	public PolarCoordinate getCurrentPosition() {
		return currentPosition;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getSpeedOverGround()
	 */
	@Override
	public Double getSpeedOverGround() {
		return speedOverGround;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getOrientationOverGround()
	 */
	@Override
	public Double getCourseOverGround() {
		return orientationOverGround;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getAltitudeOverGround()
	 */
	@Override
	public Double getAltitudeOverGround() {
		return altitudeOverGround;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy#getListOfAvailableSensors()
	 */
	@Override
	public Set<String> getAvailableSensors() throws ParseException {
		String jsonString = getSensorValue("sensors");
		if (jsonString == null) {
			return null;
		}
		
		JSONParser parser = new JSONParser();
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)parser.parse(jsonString);
		Set<String> result = new HashSet<String>();
		for (Object entry : list) {
			result.add(entry.toString());
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy#getPilotConfig()
	 */
	@Override
	public Map<String,String> getPilotConfig() throws ParseException {
		String jsonString = getSensorValue("config");
		if (jsonString == null) {
			return null;
		}
		
		JSONParser parser = new JSONParser();
		JSONObject obj = (JSONObject)parser.parse(jsonString);
		Map<String,String> result = new HashMap<String, String>();
		for (Object entry : obj.entrySet()) {
			@SuppressWarnings("unchecked")
			Entry<String, String> e = (Entry<String, String>)entry;
			result.put(e.getKey(), e.getValue());
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy#getWaypointsAsString()
	 */
	@Override
	public String getWaypoints() {
		return waypoints ;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getSensorValueAsDouble(java.lang.String)
	 */
	@Override
	public Double getSensorValueAsDouble(String name) {
		String value = getSensorValue(name);
		
		if (value != null) {
			try {
				return Double.valueOf(value);
			} catch (NumberFormatException e) {
				LOG.error("Can not parse double value for sensor: '" + name + "'" + ", value='" + value + "'");
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getSensorValueAsInteger(java.lang.String)
	 */
	@Override
	public Integer getSensorValueAsInteger(String name) {
		String value = getSensorValue(name);
		
		if (value != null) {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException e) {
				LOG.error("Can not parse integer value for sensor: '" + name + "'" + ", value='" + value + "'");
			}	
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getSensorValue(java.lang.String)
	 */
	@Override
	public String getSensorValue(String name) {
		InputStream inStream = getSensorValueAsStream(name);
		if (inStream == null) {
			return null;
		}
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();

		int l;
		byte[] tmp = new byte[2048];
		try {
			while ((l = inStream.read(tmp)) != -1) {
				bo.write(tmp, 0, l);
			}
		} catch (IOException e) {
			LOG.error("Can not copy stream.", e);
			return null;
		}

		return bo.toString();
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy#getSensorValueAsInputStream(java.lang.String)
	 */
	@Override
	public InputStream getSensorValueAsStream(String name) {
		if (pilotUrl == null) {
			return null;
		}
		
		String url = pilotUrl+"/sensor/"+name;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;

		try {
			response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new IOException(String.format("%d -- %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase())); 
			}
			HttpEntity entity = response.getEntity();
			return entity.getContent();
		} catch (Exception e) {
			LOG.error("Can not access " + url,e);
		}
		return null;
	}
	
	/**
	 * @param pilotUrl the base URL to access the associated real vehicle.
	 */
	public void setPilotUrl(String pilotUrl) {
		this.pilotUrl = pilotUrl;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		running = true;
		while (running) {
			String positionString = getSensorValue(SENSOR_NAME_POSITION);
			setPosition(positionString);
			
			String sonarString = getSensorValue(SENSOR_NAME_SONAR);
			setSonar(sonarString);
			
			String wayPointString = getSensorValue(SENSOR_NAME_WAYPOINTS);
			setWaypoints(wayPointString);
			
			try { Thread.sleep(CYCLE); } catch (InterruptedException e) { }
		}
	}
	
	/**
	 * Terminate this thread.
	 */
	public void terminate() {
		running = false;
		this.interrupt();
	}
	
	/**
	 * @return true if this thread is active.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param positionString the current position as a string.
	 */
	void setPosition(String positionString) {
		if (positionString == null) {
			currentPosition = null;
			speedOverGround = null;
			orientationOverGround = null;
			return;
		}

		parseValues(positionString);
	}
	
	/**
	 * @param altitudeString the current altitude over ground as a string.
	 */
	void setSonar(String altitudeString) {
		if (altitudeString == null) {
			altitudeOverGround = null;
			return;
		}
		
		parseValues(altitudeString);
	}
	
	/**
	 * @param waypoints the current way-points as a string.
	 */
	public void setWaypoints(String waypoints) {
		this.waypoints = waypoints;
	}

	/**
	 * @param values the values to be parsed.
	 */
	private void parseValues(String values) {
		String[] lines = values.trim().split("\n");
		for (String l : lines) {
			String[] kv = l.trim().split(":\\s+");
			if (kv.length < 2) {
				continue;
			}
			
			Double val = Double.parseDouble(kv[1]);
			
			if ("Latitude".equals(kv[0])) {
				if (currentPosition == null) {
					currentPosition = new PolarCoordinate();
				}
				currentPosition.latitude = val.doubleValue();
				
			} else if ("Longitude".equals(kv[0])) {
				if (currentPosition == null) {
					currentPosition = new PolarCoordinate();
				}
				currentPosition.longitude = val.doubleValue();
				
			} else if ("Altitude".equals(kv[0])) {
				if (currentPosition == null) {
					currentPosition = new PolarCoordinate();
				}
				currentPosition.altitude = val.doubleValue();
				
			} else if ("CourseOverGround".equals(kv[0])) {
				orientationOverGround = val;
				
			} else if ("SpeedOverGround".equals(kv[0])) {
				speedOverGround = val;
				
			} else if ("AltitudeOverGround".equals(kv[0])) {
				altitudeOverGround = val;
				
			} else {
				LOG.error("Can not parse '" + l + "'");
			}
		}
		
	}
	
}
