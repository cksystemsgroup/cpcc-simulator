/*
 * @(#) MapperServlet.java
 *
 * This code is part of the ESE-CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ServiceEntry;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.SnoopService;


@SuppressWarnings("serial")
public class MapperServlet extends HttpServlet implements IServletConfig {
	
	Logger LOG = Logger.getLogger(MapperServlet.class);
	
	private static final String PROP_PATH_NAME = "mapper.properties";

	private ServletConfig servletConfig;
	private Properties props = new Properties ();
	
	private ServiceEntry[] services = {
		new ServiceEntry("/snoop.*", new SnoopService(this)),
//		new ServiceEntry("/admin/.*", new AdminService(this)),
//		new ServiceEntry("/sensor/.*", new SensorService(this)),
		new ServiceEntry(".*", new DefaultService(this))
	};

	public void init (ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;
		super.init();
		myInit();
	}

	private void myInit () throws ServletException {

		InputStream propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROP_PATH_NAME);
		
		if (propStream == null)
			throw new ServletException ("Can not find file " + PROP_PATH_NAME + " in class path.");
		
		try {
			props.load(propStream);
					
//			servletConfig.getServletContext().setAttribute("aviator", aviator);
			
//			File contexTempDir = (File)servletConfig.getServletContext().getAttribute(AdminService.CONTEXT_TEMP_DIR);
		
		} catch (IOException e) {
			throw new ServletException (e);
		}

	}

	protected void service (HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String servicePath = request.getRequestURI();

		if (request.getRequestURI().startsWith(request.getContextPath())) {
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		}

		for (int k = 0; k < services.length; k++) {
			if (servicePath.matches(services[k].pattern)) {
				services[k].service.service(servletConfig, request, response);
				return;
			}
		}
		
		return;
	}
	
    public void destroy () {
//    	backGroundTimer.cancel();
//    	backGroundTimerTask.finish();
//    	aviator.destroy();
    }

	public Properties getProperties() {
		return props;
	}

}