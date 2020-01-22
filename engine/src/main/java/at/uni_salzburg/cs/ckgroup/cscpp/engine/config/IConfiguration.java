//
// @(#) IConfiguration.java
//
// This code is part of the JNavigator project.
// Copyright (c) 2012 Clemens Krainer
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.config;

import java.net.URI;

public interface IConfiguration {
	
	/**
	 * @return true if there is a pilot attached.
	 */
	public boolean isPilotAvailable();

	/**
	 * @return the base URL of the associated auto pilot. 
	 */
	public URI getPilotUrl();

	/**
	 * @return the URL of the central mapper registry.
	 */
	public abstract URI getMapperRegistryUrl();
	
	/**
	 * @return the URL of this web application.
	 */
	public URI getWebApplicationBaseUrl();
}
