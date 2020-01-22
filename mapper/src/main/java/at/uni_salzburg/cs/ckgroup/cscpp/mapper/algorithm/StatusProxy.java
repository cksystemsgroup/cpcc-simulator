/*
 * @(#) StatusProxy.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRVCommand;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandFlyTo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandGoAuto;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandGoManual;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandHover;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandLand;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandTakeOff;

public class StatusProxy implements IStatusProxy {
	
	Logger LOG = LoggerFactory.getLogger(StatusProxy.class);

	private String statusUrl;
	
	private String setCourseUrl;
	
	private PolarCoordinate currentPosition;
	
	private PolarCoordinate nextPosition;
	
	private Double velocity;

	private boolean idle;
	
	public StatusProxy (String pilotUrl) {
		if (pilotUrl == null)
			throw new NullPointerException("Status URL may not be null!");
		this.statusUrl = pilotUrl + "/status";
		this.setCourseUrl = pilotUrl + "/admin/json/courseUpload";
	}
	
	@Override
	public void fetchCurrentStatus() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(statusUrl);
		HttpResponse response;

		String responseString = "";
		try {
			response = httpclient.execute(httpget);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200)
				responseString = EntityUtils.toString(response.getEntity());
			else
				LOG.error("Error at accessing " + statusUrl + " code=" + statusCode + " reason=" + response.getStatusLine().getReasonPhrase());
		} catch (Exception e) {
			LOG.error("Can not access " + statusUrl ,e);
		}
			
		Map<String,String> valueMap = new HashMap<String, String>();
		for (String l : responseString.split("\n")) {
			String[] kv = l.trim().split(":\\s+",2);
			if (kv.length == 2)
				valueMap.put(kv[0], kv[1]);
		}
		
		currentPosition = parsePosition(valueMap, "Latitude", "Longitude", "AltitudeOverGround");
		nextPosition = parsePosition(valueMap, "NextLatitude", "NextLongitude", "NextAltitudeOverGround");
		velocity = parseDouble(valueMap, "Velocity");
		idle = parseBoolean(valueMap, "Idle", false);
	}
	
	private PolarCoordinate parsePosition(Map<String, String> valueMap, String latKey, String lonKey, String altKey) {
		String lat = valueMap.get(latKey);
		String lon = valueMap.get(lonKey);
		String alt = valueMap.get(altKey);
		
		if (lat == null || lon == null || alt == null)
			return null;
		
		return new PolarCoordinate(Double.parseDouble(lat), Double.parseDouble(lon), Double.parseDouble(alt));
	}

	private Double parseDouble(Map<String, String> valueMap, String key) {
		String v = valueMap.get(key);
		
		if (v == null)
			return null;
		
		return Double.valueOf(v);
	}

	private boolean parseBoolean(Map<String, String> valueMap, String key, boolean def) {
		String v = valueMap.get(key);
		
		if (v == null)
			return def;
		
		return Boolean.parseBoolean(v);
	}
	
	@Override
	public PolarCoordinate getCurrentPosition() {
		return currentPosition;
	}

	@Override
	public PolarCoordinate getNextPosition() {
		return nextPosition;
	}

	@Override
	public Double getVelocity() {
		return velocity;
	}

	@Override
	public boolean isIdle() {
		return idle;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void changeSetCourse(List<IRVCommand> courseCommandList, boolean immediate) {
		
		JSONArray a = new JSONArray();
		
		for (IRVCommand cmd : courseCommandList) {
			a.add(rvCommandToJSON(cmd));
		}
		
		JSONObject o = new JSONObject();
		o.put("immediate", Boolean.valueOf(immediate));
		o.put("course", a);
		
		
		String x = JSONValue.toJSONString(a);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(setCourseUrl);
		HttpResponse response;
	
		String responseString = "";
		try {
			StringEntity entity = new StringEntity(x, "setcourse/json", null);
			httppost.setEntity(entity);
			
			response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200)
				responseString = EntityUtils.toString(response.getEntity());
			else
				LOG.error("Error at accessing " + setCourseUrl + " code=" + statusCode + " reason=" + response.getStatusLine().getReasonPhrase());
			
			LOG.info("Upload new course to " + setCourseUrl + " response=" + responseString);
		} catch (Exception e) {
			LOG.error("Can not access " + setCourseUrl ,e);
		}
		
		
		// TODO return a value ?
		
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject rvCommandToJSON(IRVCommand cmd) {
		JSONObject o = new JSONObject();
		
		if (cmd instanceof RVCommandHover) {
			RVCommandHover c = (RVCommandHover)cmd;
			o.put("cmd", "hover");
			o.put("time", c.getTime());
			return o;
		}
		
		if (cmd instanceof RVCommandFlyTo) { 
			RVCommandFlyTo c = (RVCommandFlyTo)cmd;
			o.put("cmd", "flyTo");
			o.put("latitude", c.getPoint().getLatitude());
			o.put("longitude", c.getPoint().getLongitude());
			o.put("altitude", c.getPoint().getAltitude());
			o.put("precision", c.getPrecision());
			o.put("velocity", c.getVelocity());
			return o;
		}
		
		if (cmd instanceof RVCommandLand) {
			o.put("cmd", "land");
			return o;
		}
		
		if (cmd instanceof RVCommandTakeOff) {
			RVCommandTakeOff c = (RVCommandTakeOff)cmd;
			o.put("cmd", "takeOff");
			o.put("time", c.getTime());
			o.put("altitude", c.getAltitude());
			return o;
		}
		
		if (cmd instanceof RVCommandGoAuto) {
			o.put("cmd", "goAuto");
			return o;
		}

		if (cmd instanceof RVCommandGoManual) {
			o.put("cmd", "goManual");
			return o;
		}

		LOG.error("Unknown RVCommand: " + cmd.getClass().getName());
		
		return null;
	}
	
}
