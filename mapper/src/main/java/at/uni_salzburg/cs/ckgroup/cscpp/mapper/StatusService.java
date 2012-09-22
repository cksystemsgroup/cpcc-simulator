/*
 * @(#) StatusService.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;

public class StatusService extends DefaultService {
	
	private static final Logger LOG = Logger.getLogger(RegistryService.class);
	
	public static final String ACTION_MAPPER_SUSPEND = "mapperSuspend";
	public static final String ACTION_MAPPER_RESUME = "mapperResume";
	public static final String ACTION_MAPPER_SINGLE_STEP = "mapperSingleStep";
	public static final String ACTION_MAPPER_RESET_STATISTICS = "mapperResetStats";
	
	public StatusService (IServletConfig configuraton) {
		super (configuraton);
	}
	
	@Override
	public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String servicePath = request.getRequestURI();
		if (request.getRequestURI().startsWith(request.getContextPath())) {
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		}
		
		String[] cmd = servicePath.trim().split("/+");
		if (cmd.length < 2) {
			emit404(request, response);
			return;
		}
		String action = cmd[2];
		
		String nextPage;
		
		if (ACTION_MAPPER_SUSPEND.equals(action)) {
			IMapperThread mapperThread = (IMapperThread)config.getServletContext().getAttribute("mapper");
			mapperThread.cease();
			nextPage = request.getContextPath() + "/status.tpl";
			
		} else if (ACTION_MAPPER_RESUME.equals(action)) {
			IMapperThread mapperThread = (IMapperThread)config.getServletContext().getAttribute("mapper");
			mapperThread.proceed();
			nextPage = request.getContextPath() + "/status.tpl";
			
		} else if (ACTION_MAPPER_SINGLE_STEP.equals(action)) {
			IMapperThread mapperThread = (IMapperThread)config.getServletContext().getAttribute("mapper");
			mapperThread.singleStep();
			nextPage = request.getContextPath() + "/status.tpl";

		} else if (ACTION_MAPPER_RESET_STATISTICS.equals(action)) {
			IMapperThread mapperThread = (IMapperThread)config.getServletContext().getAttribute("mapper");
			mapperThread.resetStatistics();
			nextPage = request.getContextPath() + "/status.tpl";

		} else {
			LOG.error("Can not handle: " + servicePath);
			emit404(request, response);
			return;
		}
		
		emit301 (request, response, nextPage);
	}

}
