/*
 * @(#) PositionQuery.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class PositionQuery implements IJsonQuery {

	public String execute(IServletConfig config, String[] parameters) {

//		PolarCoordinate pos = config.getVehicleBuilder().getPositionProvider().getCurrentPosition();
//		Double courseOverGround = config.getVehicleBuilder().getPositionProvider().getCourseOverGround();
//		Double speedOverGround = config.getVehicleBuilder().getPositionProvider().getSpeedOverGround();
//		Double altitudeOverGround = config.getVehicleBuilder().getAutoPilot().getAltitudeOverGround();
//		
//		Map<String, Object> obj=new LinkedHashMap<String, Object>();
//		obj.put("latitude", pos.getLatitude());
//		obj.put("longitude", pos.getLongitude());
//		obj.put("altitude", pos.getAltitude());
//		obj.put("courseOverGround", courseOverGround);
//		obj.put("speedOverGround", speedOverGround);
//		obj.put("altitudeOverGround", altitudeOverGround);
//		
//		return JSONValue.toJSONString(obj);
		
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://localhost:8080/pilot/sensor/position");
			HttpResponse response;

			response = httpclient.execute(httpget);
			ByteArrayOutputStream bo = new ByteArrayOutputStream();

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				int l;
				byte[] tmp = new byte[2048];
				while ((l = instream.read(tmp)) != -1) {
					bo.write(tmp, 0, l);
				}
			}
			
			System.out.println ("result="+bo.toString());
			

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
