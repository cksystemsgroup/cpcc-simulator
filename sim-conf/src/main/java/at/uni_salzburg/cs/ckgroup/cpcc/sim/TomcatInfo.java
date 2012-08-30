/*
 * @(#) TomcatInfo.java
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

import java.util.List;

import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.TomcatInstance;

public class TomcatInfo {
	
	private String tomcatName;
	
	private String tomcatDir;
	
	private int tomcatNumber;
	
	private int serverPort;
	
	private int httpConnectorPort;
	
	private int httpsConnectorPort;
	
	private int ajpConnectorPort;
	
	private String catalinaBase;
	
	private String catalinaHome;
	
	private List<WebAppInfo> webAppInfo;

	private TomcatInstance tomcatInstanceConfig;
	
	public String getTomcatName() {
		return tomcatName;
	}
	
	public void setTomcatName(String tomcatName) {
		this.tomcatName = tomcatName;
	}
	
	public String getTomcatDir() {
		return tomcatDir;
	}
	
	public void setTomcatDir(String tomcatDir) {
		this.tomcatDir = tomcatDir;
	}
	
	public int getTomcatNumber() {
		return tomcatNumber;
	}

	public void setTomcatNumber(int tomcatNumber) {
		this.tomcatNumber = tomcatNumber;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getHttpConnectorPort() {
		return httpConnectorPort;
	}

	public void setHttpConnectorPort(int httpConnectorPort) {
		this.httpConnectorPort = httpConnectorPort;
	}

	public int getHttpsConnectorPort() {
		return httpsConnectorPort;
	}

	public void setHttpsConnectorPort(int httpsConnectorPort) {
		this.httpsConnectorPort = httpsConnectorPort;
	}

	public int getAjpConnectorPort() {
		return ajpConnectorPort;
	}

	public void setAjpConnectorPort(int ajpConnectorPort) {
		this.ajpConnectorPort = ajpConnectorPort;
	}

	public String getCatalinaBase() {
		return catalinaBase;
	}

	public void setCatalinaBase(String catalinaBase) {
		this.catalinaBase = catalinaBase;
	}

	public String getCatalinaHome() {
		return catalinaHome;
	}

	public void setCatalinaHome(String catalinaHome) {
		this.catalinaHome = catalinaHome;
	}

	public List<WebAppInfo> getWebAppInfo() {
		return webAppInfo;
	}

	public void setWebAppInfo(List<WebAppInfo> webAppInfo) {
		this.webAppInfo = webAppInfo;
	}
	
	public TomcatInstance getTomcatInstanceConfig() {
		return tomcatInstanceConfig;
	}
	
	public void setTomcatInstanceConfig(TomcatInstance tomcatInstanceConfig) {
		this.tomcatInstanceConfig = tomcatInstanceConfig;
	}
	
}
