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
    
    private boolean reg_run;

    private String registrationUrl;
    
    private boolean registrationOk = false;
    
    public EngineRegister(ServletConfig servletConfig) {
        reg_run = true;
        Object cfg = servletConfig.getServletContext().getAttribute("configuration");
        URI reg_uri = ((Configuration)cfg).getMapperRegistryUrl();
        URI pilot_uri = ((Configuration)cfg).getPilotSensorUrl();
        URI engineUri = ((Configuration)cfg).getWebApplicationBaseUrl();
        boolean pilotAvailable = ((Configuration)cfg).isPilotAvailable();
        registrationUrl = reg_uri.toString() + "/engineRegistration?" + "enguri=" + engineUri.toString() +
        		"&sensoruri=" + (pilotAvailable ? pilot_uri.toString() : ""); 
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
