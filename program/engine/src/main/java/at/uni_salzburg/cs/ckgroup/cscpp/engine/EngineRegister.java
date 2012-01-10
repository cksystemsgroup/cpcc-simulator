/*
 * @(#) EngineServlet.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2011  Michael Kleber
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;
import java.io.IOException;
import java.net.URI;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

public class EngineRegister extends Thread {
	
	Logger LOG = Logger.getLogger(EngineRegister.class);
    
    private boolean reg_run = true;

    private String registrationUrl;
    
    private boolean registrationOk = false;

	private ServletConfig servletConfig;
    
    public EngineRegister(ServletConfig servletConfig) {
    	this.servletConfig = servletConfig;
    }
    
    @Override
	public void run() {

		while (reg_run && !registrationOk) {
			register();
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ie) {
			}
		}
		reg_run = false;
	}
    
	public void setStop() {
		reg_run = false;
		this.interrupt();
	}
    
    /**
     * @return true if registering succeeded.
     */
    public void register() {
    	Configuration cfg = (Configuration)servletConfig.getServletContext().getAttribute("configuration");
        URI reg_uri = cfg.getMapperRegistryUrl();
        if (reg_uri == null)
        	return;
        
        URI pilot_uri = cfg.getPilotUrl();
        URI engineUri = cfg.getWebApplicationBaseUrl();
        boolean pilotAvailable = cfg.isPilotAvailable();
        registrationUrl = reg_uri.toString() +
        		"/engineRegistration?enguri=" + (engineUri != null ? engineUri.toString() : "") +
        		"&piloturi=" + (pilotAvailable && pilot_uri != null ? pilot_uri.toString() : ""); 
    	registrationOk = false;
    	
    	try {
		    String ret = HttpQueryUtils.simpleQuery(registrationUrl);
		    if(ret.equalsIgnoreCase("ok"))
			{
		    	LOG.info("Mapper registration succeeded. " + registrationUrl);
		    	registrationOk = true;
		    	return;
			}
		} catch (IOException ex) {
		}
		LOG.info("Mapper registration failed. " + registrationUrl);
    }

	public String getRegistrationUrl() {
		return registrationUrl;
	}

	public boolean isRegistrationOk() {
		return registrationOk;
	}
    
}
