/*
 * @(#) RegistryQuery.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.json;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.api.IStatusProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class MapperStatusQuery implements IQuery {
	
	private static final Logger LOG = Logger.getLogger(MapperStatusQuery.class);
	private IMapper mapper;
	
	public MapperStatusQuery(IMapper mapper) {
		this.mapper = mapper;
	}
	
	@SuppressWarnings("unchecked")
	public String execute(IServletConfig config, String[] parameters) {
		
//		if (config instanceof IRegistry) {
//			IRegistry r = (IRegistry)config;
//			Map<String, IRegistrationData> registrationData = r.getRegistrationData();
////			LOG.info("rd=" + registrationData);
//			
//			JSONArray obj = new JSONArray();
//			for (Entry<String, IRegistrationData> entry : registrationData.entrySet()) {
//				if (entry.getValue() instanceof JSONAware) {
//					obj.add(entry.getValue());
//				}
//			}
//			return JSONValue.toJSONString(obj);
//		}
		
		if (mapper == null) {
			return "";
		}
		
		Map<String, IRegistrationData> registrationData = mapper.getRegistrationData();
		Map<String, IStatusProxy> statusProxyMap = mapper.getStatusProxyMap();
		List<IVirtualVehicleInfo> virtualVehicleList = mapper.getVirtualVehicleList();
		
		
		JSONArray a = new JSONArray();
		for (Entry<String, IRegistrationData> rdEntry : registrationData.entrySet()) {
//			String engineUrl = rdEntry.getKey();
			IRegistrationData rd = rdEntry.getValue();
			
			JSONObject o = new JSONObject();
			o.put("regDat", rd);
			a.add(o);
			
//			JSONObject status = new JSONObject();
//			IStatusProxy statusProxy = statusProxyMap.get(engineUrl);
//			status.put("name", "");
//			status.put("position", statusProxy.getCurrentPosition());
			
			
		}
		
		
		
		return JSONValue.toJSONString(a);
	}

}
