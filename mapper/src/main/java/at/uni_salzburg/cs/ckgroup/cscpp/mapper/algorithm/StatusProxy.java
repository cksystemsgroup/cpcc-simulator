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
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy;

public class StatusProxy implements IStatusProxy {
	
	Logger LOG = Logger.getLogger(StatusProxy.class);

	private String statusUrl;
	
	private PolarCoordinate currentPosition;
	
	private PolarCoordinate nextPosition;
	
	private Double velocity;
	
	public StatusProxy (String pilotUrl) {
		if (pilotUrl == null)
			throw new NullPointerException("Status URL may not be null!");
		this.statusUrl = pilotUrl + "/status";
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
	
}
