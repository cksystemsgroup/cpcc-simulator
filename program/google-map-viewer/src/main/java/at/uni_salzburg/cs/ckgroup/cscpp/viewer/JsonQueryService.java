/*
 * @(#) JsonQueryService.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.viewer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.IJsonQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.PositionQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.RealVehicleQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.VirtualVehicleQuery;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.json.WaypointsQuery;


public class JsonQueryService extends DefaultService {
	
	Logger LOG = Logger.getLogger(JsonQueryService.class);
	
	@SuppressWarnings("serial")
	private final static Map<String,IJsonQuery> actions = new HashMap<String, IJsonQuery>() {{
		put("position", new PositionQuery());
		put("waypoints", new WaypointsQuery());
		put("virtualVehicle", new VirtualVehicleQuery());
		put("realVehicle", new RealVehicleQuery());
	}};
	
	public JsonQueryService (IServletConfig servletConfig) {
		super (servletConfig);
	}

	@Override
	public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String servicePath = request.getRequestURI();
		if (request.getRequestURI().startsWith(request.getContextPath()))
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		
		String[] cmd = servicePath.trim().split("/+");
		if (cmd.length < 3) {
			emit404(request, response);
			return;
		}
		
//		String[] newCmds = (String[])ArrayUtils.subarray(cmd, 2, cmd.length);
		IJsonQuery action = null;
		
		try {
			action = actions.get(cmd[2]);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.println("len=" + cmd.length);
		}
		
		if (action == null) {
			emit404(request, response);
			return;
		}
		
		String result = action.execute(servletConfig, cmd);
		if (result == null)
			emit200(request, response);
		
		emitPlainText(response, result);
	}

}