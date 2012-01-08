/*
 * @(#) WayPointQueryService.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.course;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;

public class WayPointQueryService {

	public static List<WayPoint> getWayPointList(String pilotUrl) throws IOException, ParseException {
		
	    String jsonString = HttpQueryUtils.simpleQuery(pilotUrl+"/json/waypoints");
	    if (jsonString == null || jsonString.isEmpty())
	    	return null;
	    
	    JSONParser parser = new JSONParser();
	    JSONArray array = (JSONArray)parser.parse(jsonString);
	    List<WayPoint> wayPointList = new ArrayList<WayPoint>();
	    
		for (Object entry : array) {
			WayPoint wayPoint = new WayPoint((JSONObject)entry);
			wayPointList.add(wayPoint);
		}
		
		return wayPointList;
	}
}
