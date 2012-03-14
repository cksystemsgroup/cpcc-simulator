/*
 * @(#) MapperProxy.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2012  Clemens Krainer
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
package at.uni_salzburg.cs.ckgroup.cscpp.viewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cscpp.viewer.config.Configuration;

public class MapperProxy extends Thread implements IMapperProxy {
	
	private static final Logger LOG = Logger.getLogger(MapperProxy.class);
	
	private static final long CYCLE = 1000;
	
//	private String mapperUrl;
	private boolean running;

	private Configuration configuration;
	private JSONParser parser = new JSONParser();
	private List<EngineInfo> engineInfoList;
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		running = true;
		while (running) {
			List<EngineInfo> newEngineInfoList = new ArrayList<EngineInfo>();
			String jsonString = getMapperValue("mapperStatus");
			if (jsonString != null && !jsonString.isEmpty()) {
				try {
					JSONArray obj = (JSONArray)parser.parse(jsonString);
					
					for (int k=0, l=obj.size(); k < l; ++k) {
						JSONObject o = (JSONObject)obj.get(k);
						
						JSONObject rd = (JSONObject)o.get("regDat");
						String engineUrl = (String)rd.get("engUri");
						String pilotUri = (String)rd.get("pilotUri");
						String pilotName = (String)rd.get("pilotName");
						String positionUrl = null;
						String waypointsUrl = null;
						if (pilotUri != null && !pilotUri.isEmpty()) {
							positionUrl = pilotUri + "/json/position";
							waypointsUrl = pilotUri + "/json/waypoints";
						}
						String vehicleStatusUrl = engineUrl + "/json/vehicle";
									
						newEngineInfoList.add(new EngineInfo(pilotName, positionUrl, waypointsUrl, vehicleStatusUrl));			
					}
				} catch (ParseException e1) {
					LOG.error("Error at parsing JSON string '" + jsonString + "'", e1);
				}
			}
			engineInfoList = newEngineInfoList;
			
			try { Thread.sleep(CYCLE); } catch (InterruptedException e) { }
		}
	}

	@Override
	public String getMapperValue(String name) {
		InputStream inStream = getMapperValueAsStream(name);
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
	
	public InputStream getMapperValueAsStream(String name) {
		String mapperUrl = configuration.getMapperUrl().toString();
		if (mapperUrl == null) {
			return null;
		}
		
		String url = mapperUrl+"/json/"+name;
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

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public List<EngineInfo> getEngineInfoList() {
		return engineInfoList;
	}
	
	public void terminate() {
		running = false;
	}
	
}
