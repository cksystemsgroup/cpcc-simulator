/*
 * @(#) SimConfServlet.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ServiceEntry;

@SuppressWarnings("serial")
public class SimConfServlet extends HttpServlet implements IServletConfig {
	
	Logger LOG = Logger.getLogger(SimConfServlet.class);
	
	public static final String CONTEXT_TEMP_DIR = "javax.servlet.context.tempdir";
	private static final String PROP_PATH_NAME = "simconf.properties";
	public final static String PROP_CONFIG_FILE = "simconf.config.file";
	
	private ServiceEntry[] services = {
		new ServiceEntry("/simconf/.*", new SimConfService(this)),
		new ServiceEntry(".*", new DefaultService(this))
	};
	
	private ServletConfig servletConfig;
	private SimConfParameters parameters = new SimConfParameters();
	private Configuration configuration = new Configuration();
	private File contexTempDir;
	private File configFile;
	private Properties props = new Properties();
	
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
			props.clear();
			props.load(propStream);
		} catch (IOException e) {
			throw new ServletException (e);
		}
		
		
		servletConfig.getServletContext().setAttribute("parameters", parameters);
		servletConfig.getServletContext().setAttribute("configuration", configuration);
		try {
			contexTempDir = (File)servletConfig.getServletContext().getAttribute(CONTEXT_TEMP_DIR);
			configuration.setWorkDir (contexTempDir);
			
			configFile = new File (contexTempDir, props.getProperty(PROP_CONFIG_FILE));
			if (!configFile.exists()) {
				props.store(new FileOutputStream(configFile), "");
			}
			
			reloadConfigFile();
		} catch (IOException e) {
			throw new ServletException (e);
		}
		
		for (ServiceEntry entry : services) {
			if (entry.getService() instanceof SimConfService) {
				SimConfService scs = (SimConfService)entry.getService();
				scs.setParameters(parameters);
			}
		}
	}
	
	@Override
	protected void service (HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String servicePath = request.getRequestURI();

		if (request.getRequestURI().startsWith(request.getContextPath())) {
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		}

		for (int k = 0; k < services.length; k++) {
			if (servicePath.matches(services[k].getPattern())) {
				services[k].getService().service(servletConfig, request, response);
				return;
			}
		}
		
		return;
	}
	
	@Override
    public void destroy () {
		super.destroy();
    }
	
	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public File getContextTempDir() {
		return contexTempDir;
	}

	@Override
	public File getConfigFile() {
		return configFile;
	}

	@Override
	public void reloadConfigFile() throws IOException {
		if (configFile == null || !configFile.exists()) {
			LOG.error("No configuration file available.");
			return;
		}
		
		configuration.loadConfig(new FileInputStream(configFile));
		LOG.info("Loading configuration from " + configFile);
	}

}
