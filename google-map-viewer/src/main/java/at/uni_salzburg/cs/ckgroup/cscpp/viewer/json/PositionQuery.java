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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.EngineInfo;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.IMapperProxy;

public class PositionQuery implements IJsonQuery {

	private static final Logger LOG = LoggerFactory.getLogger(PositionQuery.class);
	
	private IMapperProxy mapperProxy;
	private JSONParser parser = new JSONParser();
	
	public PositionQuery(IMapperProxy mapperProxy) {
		this.mapperProxy = mapperProxy;
	}

	public String execute(IServletConfig config, String[] parameters) throws IOException {
		
		if (mapperProxy.getEngineInfoList() == null) {
			return "";
		}
		
		// TODO do this in parallel
		Map<String, Object> pilotPositions = new LinkedHashMap<String, Object>();
		
		int pilotNumber = 0;
		for (EngineInfo engineInfo : mapperProxy.getEngineInfoList()) {
			String position = null;
			String pilot = String.format(Locale.US, "pilot%03d", ++pilotNumber);
			try {
				String pilotName = engineInfo.getPilotName();
				String pilotPosURL = engineInfo.getPositionUrl();
				if (pilotPosURL != null) {
					position = HttpQueryUtils.simpleQuery(pilotPosURL);
					Map<String, Object> p = new LinkedHashMap<String, Object>();
					p.put("name", pilotName);				
					p.put("position", parser.parse(position));
					pilotPositions.put(pilot, p);
				}
			} catch (Exception e) {
				LOG.info("Can not query pilot " + pilot + ": " + position, e);
			}
		}

		return JSONValue.toJSONString(pilotPositions);
	}
	
}
