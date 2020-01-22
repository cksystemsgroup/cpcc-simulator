//
// @(#) EngineRegister.java
//
// This code is part of the CPCC project.
// Copyright (c) 2011 Michael Kleber
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
//
package at.uni_salzburg.cs.ckgroup.cscpp.engine;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;

public class EngineRegister extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(EngineRegister.class);

    private boolean regRun = true;

    private String registrationUrl;

    private boolean registrationOk = false;

    private ServletConfig servletConfig;

    public EngineRegister(ServletConfig servletConfig)
    {
        this.servletConfig = servletConfig;
    }

    @Override
    public void run()
    {
        while (regRun && !registrationOk)
        {
            register();

            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException ie)
            {
                // Intentionally empty.
            }
        }

        regRun = false;
    }

    public void setStop()
    {
        regRun = false;
        this.interrupt();
    }

    /**
     * @return true if registering succeeded.
     */
    public void register()
    {
        Configuration cfg = (Configuration) servletConfig.getServletContext().getAttribute("configuration");
        URI regUri = cfg.getMapperRegistryUrl();
        if (regUri == null)
        {
            return;
        }

        URI pilotUri = cfg.getPilotUrl();
        URI engineUri = cfg.getWebApplicationBaseUrl();
        boolean pilotAvailable = cfg.isPilotAvailable();
        registrationUrl = regUri.toString() +
            "/engineRegistration?enguri=" + (engineUri != null ? engineUri.toString() : "") +
            "&piloturi=" + (pilotAvailable && pilotUri != null ? pilotUri.toString() : "");
        registrationOk = false;

        try
        {
            String ret = HttpQueryUtils.simpleQuery(registrationUrl);
            if (ret.equalsIgnoreCase("ok"))
            {
                LOG.info("Mapper registration succeeded. {}", registrationUrl);
                registrationOk = true;
                return;
            }
            LOG.error("Mapper registration failed. {} -- rc={}", registrationUrl, ret);
        }
        catch (IOException ex)
        {
            LOG.error("Mapper registration failed. {}", registrationUrl, ex);
        }
    }

    public String getRegistrationUrl()
    {
        return registrationUrl;
    }

    public boolean isRegistrationOk()
    {
        return registrationOk;
    }

}
