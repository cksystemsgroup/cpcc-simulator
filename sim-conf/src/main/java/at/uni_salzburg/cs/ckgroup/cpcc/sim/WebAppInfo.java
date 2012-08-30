/*
 * @(#) WebAppInfo.java
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

import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.WebApplication;

public class WebAppInfo {

	private TomcatInfo tomcatInfo;
	
	private String webAppSerialNumber;
	
	private String groupId;
	
	private String artifactId;
	
	private String version;
	
	private String packaging;
	
	private String context;
	
	private String masterContext;
	
	private String slaveContext;
	
	private int plantListener;
	
	private int locationSystemListener;
	
	private int controllerConnector;
	
	private WebApplication webApplicationConfig;
	
	private ZoneInfo zoneInfo;
	
	private String baseUrl;

	
	public TomcatInfo getTomcatInfo() {
		return tomcatInfo;
	}
	
	public void setTomcatInfo(TomcatInfo tomcatInfo) {
		this.tomcatInfo = tomcatInfo;
	}
	
	public String getWebAppSerialNumber() {
		return webAppSerialNumber;
	}
	
	public void setWebAppSerialNumber(String webAppSerialNumber) {
		this.webAppSerialNumber = webAppSerialNumber;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getMasterContext() {
		return masterContext;
	}
	
	public void setMasterContext(String masterContext) {
		this.masterContext = masterContext;
	}
	
	public String getSlaveContext() {
		return slaveContext;
	}
	
	public void setSlaveContext(String slaveContext) {
		this.slaveContext = slaveContext;
	}
	
	public int getPlantListener() {
		return plantListener;
	}

	public void setPlantListener(int plantListener) {
		this.plantListener = plantListener;
	}

	public int getLocationSystemListener() {
		return locationSystemListener;
	}

	public void setLocationSystemListener(int locationSystemListener) {
		this.locationSystemListener = locationSystemListener;
	}

	public int getControllerConnector() {
		return controllerConnector;
	}

	public void setControllerConnector(int controllerConnector) {
		this.controllerConnector = controllerConnector;
	}

	public WebApplication getWebApplicationConfig() {
		return webApplicationConfig;
	}

	public void setWebApplicationConfig(WebApplication webApplicationConfig) {
		this.webApplicationConfig = webApplicationConfig;
	}

	public ZoneInfo getZoneInfo() {
		return zoneInfo;
	}
	
	public void setZoneInfo(ZoneInfo zoneInfo) {
		this.zoneInfo = zoneInfo;
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
