/*
 * @(#) SimConfService.java
 *
 * This code is part of the CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cpcc.sim;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.IConfiguration;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class SimConfService extends DefaultService {
	
	private static final String ACTION_GENERATE = "generate";
	private SimConfParameters parameters;

	public SimConfService (IServletConfig servletConfig) {
		super (servletConfig);
	}

	public void setParameters(SimConfParameters parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String servicePath = request.getRequestURI();
		if (request.getRequestURI().startsWith(request.getContextPath()))
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		
		String[] cmd = servicePath.trim().split("/+");
		if (cmd.length < 3) {
			emit404(request, response);
			return;
		}
		
		String nextPage;
		String action = cmd[3];
		
		if (ACTION_GENERATE.equals(action)) {
			
			Object cnf = config.getServletContext().getAttribute("configuration");
			IConfiguration configuration = (IConfiguration)cnf;
			
			parameters.loadParametersFromMap(request.getParameterMap());
			SimConfFactory factory = new SimConfFactory(parameters, configuration);
			byte[] simConf = factory.build();
			emitByteArray(response, "application/zip", simConf);
			return;
		}
		
		nextPage = request.getContextPath() + "/simconf.tpl";
		
		emit301 (request, response, nextPage);
	}

}
