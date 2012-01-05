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

public class EngineRegister implements Runnable{
    
    private boolean reg_run;
    private ServletConfig servletConfig;

    public EngineRegister(ServletConfig servletConfig) {
        reg_run = true;
        this.servletConfig = servletConfig;
    }
    
    public void run() {
        
            Object cfg = servletConfig.getServletContext().getAttribute("configuration");
            URI reg_uri = ((Configuration)cfg).getMapperRegistryUrl();
            URI pilot_uri = ((Configuration)cfg).getPilotSensorUrl();
            URI engineUri = ((Configuration)cfg).getWebApplicationBaseUrl();
            String url = reg_uri.toString() + "?sensoruri=" + pilot_uri.toString() + "&enguri=" + engineUri.toString();
            try {
              String ret = HttpQueryUtils.simpleQuery(url);
              if(ret.equalsIgnoreCase("ok"))
              {
                  reg_run = false;
              }
            } catch (IOException ex) { reg_run = true; }
            while(reg_run)
            {
                try {
                    
                    Thread.sleep(10000);
                    
                } catch (InterruptedException ie) { }
                try {
                    String ret = HttpQueryUtils.simpleQuery(url);
                    if(ret.equalsIgnoreCase("ok"))
                    {
                        reg_run = false;
                    }
                } catch (IOException ex) { reg_run = true; }
            }
    }
    
    public void setStop() {
        reg_run = false;
    }
}
