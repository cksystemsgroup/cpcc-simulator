/*
 * @(#) SimConfFactory.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.IConfiguration;
import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.TomcatInstance;
import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.WebApplication;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public class SimConfFactory {
	
	private static final Logger LOG = Logger.getLogger(SimConfFactory.class);

	private SimConfParameters parameters;
	private IConfiguration configuration;
//	private VariableReplacer variableReplacer;
//	private Properties webAppProps;
	
	private List<ZoneInfo> zones;
	
	private List<TomcatInfo> tomcatInfoList = new ArrayList<TomcatInfo>();
	
	private Set<WebAppInfo> centralEngines = new HashSet<WebAppInfo>();


	public SimConfFactory(SimConfParameters parameters, IConfiguration configuration) {
		this.parameters = parameters;
		this.configuration = configuration;
//		webAppProps = new Properties();
//		variableReplacer = new VariableReplacer(webAppProps);
	}

	public byte[] build() throws IOException {
		
		calculateZones();

		int totalRvWebApps = parameters.getHorizontalZones() * parameters.getVerticalZones();
		int totalRvTomcats = totalRvWebApps / parameters.getRvsPerTomcat();
		if (totalRvWebApps % parameters.getRvsPerTomcat() != 0) {
			++totalRvTomcats;
		}
		
		int tomcatNumber = 0;
		int zoneNumber = 0;
		for (Entry<String, TomcatInstance> entry : configuration.getTomcatInstances().entrySet()) {
			TomcatInstance tcInst = entry.getValue();
			int maxInst = tcInst.isMultiple() ? totalRvTomcats : 1;

			for (int k=0; k < maxInst; ++k) {
				TomcatInfo tomcatInfo = new TomcatInfo();
				tomcatInfo.setTomcatInstanceConfig(tcInst);
				tomcatInfoList.add(tomcatInfo);
				
				calculateTomcatPortNumbers(tomcatInfo, tomcatNumber);
				
				buildTomcatPartOne(tomcatInfo, tcInst, k);
				
				int numberRvWebApps;
				if (k+1 == totalRvTomcats) {
					numberRvWebApps = totalRvWebApps - k * parameters.getRvsPerTomcat();
				} else {
					numberRvWebApps = parameters.getRvsPerTomcat();	
				}
				LOG.info("Generate tomcat " + k + ", numberRvWebApps=" + numberRvWebApps + ", totalRvTomcats=" + totalRvTomcats);
				
				List<WebAppInfo> webAppInfoList = new ArrayList<WebAppInfo>();
				
				for (String webAppName : tcInst.getWebApps()) {
					WebApplication webApp = configuration.getWebApplications().get(webAppName);
					if (webApp == null) {
						LOG.error("Can not find web application " + webAppName);
						System.out.println("Can not find web application " + webAppName);
						continue;
					}
					int maxWebApps = webApp.isMultiple() ? numberRvWebApps : 1;

					for (int w=0; w < maxWebApps; ++w) {
						WebAppInfo webAppInfo = new WebAppInfo();
						webAppInfo.setTomcatInfo(tomcatInfo);
						webAppInfo.setWebApplicationConfig(webApp);
						calculateWebAppPortNumbers(webAppInfo, tomcatNumber, w);
						deployWebApp(webAppInfo, webApp, k, w);
						if (webApp.isZoneAssigned()) {
							ZoneInfo zoneInfo = zones.get(zoneNumber);
							zoneInfo.setWebAppInfo(webAppInfo);
							webAppInfo.setZoneInfo(zoneInfo);
							++zoneNumber;
						}
						if (webApp.isCentralEngine()) {
							centralEngines.add(webAppInfo);
						}
						webAppInfoList.add(webAppInfo);
					}
				}
				
				tomcatInfo.setWebAppInfo(webAppInfoList);
				
				// assign any zone information to slaves also.
				for (WebAppInfo webApp : webAppInfoList) {
					ZoneInfo zoneInfo = webApp.getZoneInfo();
					String slaveContext = webApp.getSlaveContext();
					if (zoneInfo == null || slaveContext == null) {
						continue;
					}
					for (WebAppInfo wa : webAppInfoList) {
						if (slaveContext.equals(wa.getContext()) && wa.getWebApplicationConfig().isMultiple()) {
							wa.setZoneInfo(zoneInfo);
							break;
						}
					}
				}
				
				++tomcatNumber;
			}
		}

		return packageZipOutputStream();
	}

	
	private byte[] packageZipOutputStream() throws IOException {
				
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		ZipOutputStream os = new ZipOutputStream(boas);
		
		
		for (TomcatInfo tomcatInfo : tomcatInfoList) {
			
			String tomcatDir = tomcatInfo.getTomcatDir();
			
			for (String dir : configuration.getTomcatRequiredDirectories()) {
				ZipEntry e = new ZipEntry(tomcatDir + "/" + dir + "/");
				e.setTime(System.currentTimeMillis());
				os.putNextEntry(e);
				os.closeEntry();
			}
			
			for (Entry<String, String> entry : configuration.getTomcatTemplateFiles().getTemplateFiles().entrySet()) {
				String template = "templates/" + entry.getKey();
				String target = entry.getValue();
				emitTemplateFile(os, template, tomcatDir, target, tomcatInfo, null);
			}
			
			for (WebAppInfo webAppInfo : tomcatInfo.getWebAppInfo()) {
				for (Entry<String, String> entry : webAppInfo.getWebApplicationConfig().getTemplateFiles().getTemplateFiles().entrySet()) {
					String template = "templates/" + entry.getKey();
					String target = entry.getValue();
					emitTemplateFile(os, template, tomcatDir, target, tomcatInfo, webAppInfo);
				}
			}
		}
		os.close();
		
		return boas.toByteArray();
	}

	private void emitTemplateFile(ZipOutputStream os, String template, String baseDir, String target, TomcatInfo tomcatInfo, WebAppInfo webAppInfo) throws IOException {
		
		InputStream tplStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(template);
		if (tplStream == null) {
			LOG.error("Can not find template " + template);
			System.out.println("Can not find template " + template);
			return;
		}
		
		target = processTemplate(target, tomcatInfo, webAppInfo);
		
		ZipEntry e = new ZipEntry(baseDir + "/" + target);
		e.setTime(System.currentTimeMillis());
		os.putNextEntry(e);
		
		if (template.endsWith(".vm")) {
			processTemplate(os, tplStream, tomcatInfo, webAppInfo);
		} else {
			FileUtils.copyStream(tplStream, os, false);
		}
		
		os.closeEntry();
	}

	private void calculateZones() {
		
		IGeodeticSystem gs = new WGS84();
		
		int hor = parameters.getHorizontalZones();
		int vert = parameters.getVerticalZones();
		double latitude = parameters.getZoneCenterLat();
		double longitude = parameters.getZoneCenterLng();
		double height = parameters.getZoneHeight();
		double width = parameters.getZoneWidth();
		
		double totalWidth = width * hor;
		double totalHeigth = height * vert;

		PolarCoordinate pos = new PolarCoordinate(latitude, longitude, 0);
		
		PolarCoordinate southWestCorner = gs.walk(pos, totalHeigth/2.0, -totalWidth/2.0, 0);
		
		zones = new ArrayList<ZoneInfo>();
		
		for (int h=0; h < hor; ++h) {
			for (int v=0; v < vert; ++v) {
				PolarCoordinate a = gs.walk(southWestCorner, -v*height, h*width, 0);
				PolarCoordinate b = gs.walk(a, 0, width, 0);
				PolarCoordinate c = gs.walk(a, -height, width, 0);
				PolarCoordinate d = gs.walk(a, -height, 0, 0);
				PolarCoordinate depot = gs.walk(a, -height/2.0, width/2.0, 1.0);
				ZoneInfo zInfo = new ZoneInfo();
				zInfo.setVertices(verticesToString(new PolarCoordinate[]{a,b,c,d,a}));
				zInfo.setDepotPosition(depot);
				zones.add(zInfo);
			}
		}
	}
	
	private String verticesToString(PolarCoordinate[] polarCoordinates) {
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (PolarCoordinate p : polarCoordinates) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append("(").append(String.format(Locale.US, "%.8f", p.getLatitude())).append(", ").
				append(String.format(Locale.US, "%.8f", p.getLongitude())).append(")");
		}
		
		return sb.toString();
	}

	private void buildTomcatPartOne(TomcatInfo tomcatInfo, TomcatInstance ti, int number) throws IOException {
		
		String name = ti.getNameTemplate();
		if (ti.isMultiple()) {
			name = String.format(Locale.US,	name, number);
		}
		String tomcatDir = "tomcat-" + name;
		
		tomcatInfo.setTomcatName(name);
		tomcatInfo.setTomcatDir(tomcatDir);
	}

	private void deployWebApp (WebAppInfo webAppInfo, WebApplication wa, int tomcatNumber, int webAppNumber) throws IOException {
		
		String context = wa.getContextTemplate();
		String masterContext = wa.getMasterContextTemplate();
		String slaveContext = wa.getSlaveContextTemplate();
		if (wa.isMultiple()) {
			context = String.format(Locale.US,	context, webAppNumber);
			if (masterContext != null) {
				masterContext = String.format(Locale.US, masterContext, webAppNumber);
			}
			if (slaveContext != null) {
				slaveContext = String.format(Locale.US, slaveContext, webAppNumber);
			}
		}
		webAppInfo.setMasterContext(masterContext);
		webAppInfo.setSlaveContext(slaveContext);

		webAppInfo.setGroupId(wa.getGroupId());
		webAppInfo.setArtifactId(wa.getArtifactId());
		webAppInfo.setVersion(wa.getVersion());
		webAppInfo.setClassifier(wa.getClassifier());
		webAppInfo.setPackaging(wa.getType());
		webAppInfo.setContext(context);
		webAppInfo.setWebAppSerialNumber(String.format(Locale.US, "%03d", tomcatNumber * parameters.getRvsPerTomcat() + webAppNumber));
		String baseUrl = "http://localhost:" + webAppInfo.getTomcatInfo().getHttpConnectorPort() + "/" + webAppInfo.getContext();
		webAppInfo.setBaseUrl(baseUrl);
	}
	
	
	private void processTemplate(OutputStream os, InputStream tplStream, TomcatInfo tomcatInfo, WebAppInfo webAppInfo) {
		PrintWriter pw = new PrintWriter(os);
		InputStreamReader reader = new InputStreamReader(tplStream);
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityEngine ve = new VelocityEngine();
        ve.init(props);
        VelocityContext context = new VelocityContext();
        context.put("configuration", configuration);
        context.put("parameters", parameters);
        context.put("systemProperties", System.getProperties());
        context.put("tomcatInfo", tomcatInfo);
        context.put("webAppInfo", webAppInfo);
//        context.put("webAppProps", webAppProps);
        context.put("tomcatInfoList", tomcatInfoList);
        context.put("zones", zones);
        context.put("centralEngines", centralEngines);
//        context.put("deployedWebApps", deployedWebApps);
        
		ve.evaluate(context, pw, "lala", reader);
		pw.flush();
	}
	
	private String processTemplate(String template, TomcatInfo tomcatInfo, WebAppInfo webAppInfo) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayInputStream bais = new ByteArrayInputStream(template.getBytes());
		processTemplate(baos, bais, tomcatInfo, webAppInfo);
		
		return baos.toString();
	}
	
	private void calculateTomcatPortNumbers(TomcatInfo tomcatInfo, int tomcatNumber) {
		
		int base = configuration.getTomcatBasePort() + configuration.getWebAppPortDistance() * tomcatNumber * parameters.getRvsPerTomcat();
		
		int serverPort = base + 5;
		int httpConnectorPort = base + 0;
		int httpsConnectorPort = base + 3;
		int ajpConnectorPort = base + 9;
		
		tomcatInfo.setServerPort(serverPort);
		tomcatInfo.setHttpConnectorPort(httpConnectorPort);
		tomcatInfo.setHttpsConnectorPort(httpsConnectorPort);
		tomcatInfo.setAjpConnectorPort(ajpConnectorPort);
	}
	
	
	private void calculateWebAppPortNumbers(WebAppInfo webAppInfo, int tomcatNumber, int webAppNumber) {
		
		int base = configuration.getTomcatBasePort() + configuration.getWebAppPortDistance() * (webAppNumber + tomcatNumber * parameters.getRvsPerTomcat());
		
		int plantListener = base + 1;
		int locationSystemListener = base + 2;
		int controllerConnector = base + 4;
		
		webAppInfo.setPlantListener(plantListener);
		webAppInfo.setLocationSystemListener(locationSystemListener);
		webAppInfo.setControllerConnector(controllerConnector);
	}
}
