/*
 * @(#) RegistryService.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2012  Michael Kleber
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

import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class RegistryService extends DefaultService{

    
    
    public RegistryService(IServletConfig servletConfig){
        super(servletConfig);
    }
    
    @Override
    public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
     
        // TODO reg service
        //
        
        String eng_uri = request.getParameter("enguri");
        String sensor_uri = request.getParameter("sensoruri");
        
        if(eng_uri == null || sensor_uri == null || eng_uri.trim().isEmpty() || sensor_uri.trim().isEmpty()){
            
            response.getWriter().print("error");
        }
        else {
            // TODO add engine
            
            // all successfull
            response.getWriter().print("ok");
        }
    }
    
}
