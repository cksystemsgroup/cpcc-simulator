/*
 * @(#) IConfiguration.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.sim.config;

import java.util.Map;


public interface IConfiguration {

	Map<String, TomcatInstance> getTomcatInstances();

	TemplateFiles getTomcatTemplateFiles();
	
	String[] getTomcatRequiredDirectories();

	Map<String, WebApplication> getWebApplications();

	int getTomcatBasePort();

	int getWebAppPortDistance();
	
}
