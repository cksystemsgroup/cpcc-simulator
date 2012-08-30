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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.IConfiguration;
import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.TemplateFiles;
import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.TomcatInstance;
import at.uni_salzburg.cs.ckgroup.cpcc.sim.config.WebApplication;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.VariableReplacer;

public class SimConfFactoryOld {
	
	private static final Logger LOG = Logger.getLogger(SimConfFactoryOld.class);

	private static final String PROP_WEB_APP_CONTEXT = "webapp.context";
	private static final String PROP_WEB_APP_MASTER_CONTEXT = "webapp.master.context";
	private static final String PROP_WEB_APP_SERIAL_NUMBER = "webapp.serial.number";
	private static final String PROP_TOMCAT_NAME = "tomcat.name";
	private static final String PROP_TOMCAT_DIR = "tomcat.dir";
	
//	tomcat.required.directories

	private static final String PROP_BASE_PORT_NUMBER = "basePortNumber";
	
	private static final String PROP_TOMCAT_SERVER_PORT = "serverPort";
	private static final String PROP_TOMCAT_HTTP_CONNECTOR_PORT = "httpConnectorPort";
	private static final String PROP_TOMCAT_HTTPS_CONNECTOR_PORT = "httpsConnectorPort";
	private static final String PROP_TOMCAT_AJP_CONNECTOR_PORT = "ajpConnectorPort";
	
	private static final String PROP_PLANT_LISTENER_PORT = "plantListener";
	private static final String PROP_LOCATION_SYSTEM_LISTENER_PORT = "locationSystemListener";
	private static final String PROP_CONTROLLER_CONNECTOR_PORT = "controllerConnector";
	
	private int basePortNumber = 9000;
	private int webAppPortNumberDistance = 10;
	
	private SimConfParameters parameters;
	private IConfiguration configuration;
	private VariableReplacer variableReplacer;
	private Properties webAppProps;
	private Map<String, WebApplication> deployedWebApps;
	
	private ArrayList<String> zones;


	public SimConfFactoryOld(SimConfParameters parameters, IConfiguration configuration) {
		this.parameters = parameters;
		this.configuration = configuration;
		webAppProps = new Properties();
		variableReplacer = new VariableReplacer(webAppProps);
		webAppProps.setProperty(PROP_BASE_PORT_NUMBER, Integer.toString(basePortNumber));
		deployedWebApps = new TreeMap<String, WebApplication>();
	}

	public byte[] build() throws IOException {
		
		calculateZones();
		
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		ZipOutputStream os = new ZipOutputStream(boas);

		int totalRvWebApps = parameters.getHorizontalZones() * parameters.getVerticalZones();
		int totalRvTomcats = totalRvWebApps / parameters.getRvsPerTomcat();
		if (totalRvWebApps % parameters.getRvsPerTomcat() != 0) {
			++totalRvTomcats;
		}
		
		int tomcatNumber = 0;
		for (Entry<String, TomcatInstance> entry : configuration.getTomcatInstances().entrySet()) {
			TomcatInstance tcInst = entry.getValue();
			int maxInst = tcInst.isMultiple() ? totalRvTomcats : 1;

			for (int k=0; k < maxInst; ++k) {
				calculateTomcatPortNumbers(tomcatNumber);
				String baseDir = buildTomcatPartOne(os, tcInst, k);
				
				int numberRvWebApps;
				if (k+1 == totalRvTomcats) {
					numberRvWebApps = totalRvWebApps - k * parameters.getRvsPerTomcat();
				} else {
					numberRvWebApps = parameters.getRvsPerTomcat();	
				}
				LOG.info("Generate tomcat " + k + ", numberRvWebApps=" + numberRvWebApps + ", totalRvTomcats=" + totalRvTomcats);
				
				deployedWebApps.clear();
				for (String webAppName : tcInst.getWebApps()) {
					WebApplication webApp = configuration.getWebApplications().get(webAppName);
					if (webApp == null) {
						LOG.error("Can not find web application " + webAppName);
						System.out.println("Can not find web application " + webAppName);
						continue;
					}
					int maxWebApps = webApp.isMultiple() ? numberRvWebApps : 1;
					for (int w=0; w < maxWebApps; ++w) {
						calculateWebAppPortNumbers(tomcatNumber, w);
						String context = deployWebApp(os, baseDir, webApp, k, w);
						deployedWebApps.put(context, webApp);
					}
				}
				
				buildTomcatPartTwo(os, baseDir, configuration.getTomcatTemplateFiles(), k);
				++tomcatNumber;
			}
		}
		
		os.close();
		
		return boas.toByteArray();
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
		
		zones = new ArrayList<String>();
		
		for (int h=0; h < hor; ++h) {
			for (int v=0; v < vert; ++v) {
				PolarCoordinate a = gs.walk(southWestCorner, -v*height, h*width, 0);
				PolarCoordinate b = gs.walk(a, 0, width, 0);
				PolarCoordinate c = gs.walk(a, -height, width, 0);
				PolarCoordinate d = gs.walk(a, -height, 0, 0);
				zones.add(zoneToString(new PolarCoordinate[]{a,b,c,d,a}));
			}
		}
	}
	
	private String zoneToString(PolarCoordinate[] polarCoordinates) {
		
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

	private String buildTomcatPartOne(ZipOutputStream os, TomcatInstance ti, int number) throws IOException {
		
		String name = ti.getNameTemplate();
		if (ti.isMultiple()) {
			name = String.format(Locale.US,	name, number);
		}
		String tomcatDir = "tomcat-" + name;
		webAppProps.setProperty(PROP_TOMCAT_NAME, name);
		webAppProps.setProperty(PROP_TOMCAT_DIR, tomcatDir);
		return tomcatDir;
	}
	
	private void buildTomcatPartTwo(ZipOutputStream os, String tomcatDir, TemplateFiles tf, int number) throws IOException {
		
		for (String dir : configuration.getTomcatRequiredDirectories()) {
			ZipEntry e = new ZipEntry(tomcatDir + "/" + dir + "/");
			e.setTime(System.currentTimeMillis());
			os.putNextEntry(e);
			os.closeEntry();
		}
		
		for (Entry<String, String> entry : tf.getTemplateFiles().entrySet()) {
			String template = "templates/" + entry.getKey();
			InputStream tplStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(template);
			if (tplStream == null) {
				LOG.error("Can not find template " + template);
				System.out.println("Can not find template " + template);
				continue;
			}
			
			String target = entry.getValue();
			target = variableReplacer.replace(target);
			ZipEntry e = new ZipEntry(tomcatDir + "/" + target);
			e.setTime(System.currentTimeMillis());
			os.putNextEntry(e);
			
			if (template.endsWith(".vm")) {
				processTemplate(os, tplStream, number, 0);
			} else {
				FileUtils.copyStream(tplStream, os, false);
			}
			
			os.closeEntry();
		}
	}
	
	
	private String deployWebApp (ZipOutputStream os, String baseDir, WebApplication wa, int tomcatNumber, int webAppNumber) throws IOException {
		
		String context = wa.getContextTemplate();
		String masterContext = wa.getMasterContextTemplate();
		if (wa.isMultiple()) {
			context = String.format(Locale.US,	context, webAppNumber);
			if (masterContext != null) {
				masterContext = String.format(Locale.US, masterContext, webAppNumber);
				webAppProps.setProperty(PROP_WEB_APP_MASTER_CONTEXT, masterContext);
			}
		}
		webAppProps.setProperty(PROP_WEB_APP_CONTEXT, context);
		webAppProps.setProperty(PROP_WEB_APP_SERIAL_NUMBER, String.format(Locale.US, "%03d", tomcatNumber * parameters.getRvsPerTomcat() + webAppNumber));

		for (Entry<String, String> entry : wa.getTemplateFiles().getTemplateFiles().entrySet()) {
			String template = "templates/" + entry.getKey();
			InputStream tplStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(template);
			if (tplStream == null) {
				LOG.error("Can not find template " + template);
				System.out.println("Can not find template " + template);
				continue;
			}
			
			String target = entry.getValue();
			target = variableReplacer.replace(target);
			ZipEntry e = new ZipEntry(baseDir + "/" + target);
			e.setTime(System.currentTimeMillis());
			os.putNextEntry(e);
			
			if (template.endsWith(".vm")) {
				processTemplate(os, tplStream, tomcatNumber, webAppNumber);
			} else {
				FileUtils.copyStream(tplStream, os, false);
			}
			
			os.closeEntry();
		}
		
		return context;
	}
	
	
	private void processTemplate(OutputStream os, InputStream tplStream, int tomcatNumber, int webAppNumber) {
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
        context.put("tomcatNumber", Integer.valueOf(tomcatNumber));
        context.put("webAppNumber", Integer.valueOf(webAppNumber));
        context.put("webAppProps", webAppProps);
        context.put("zones", zones);
        context.put("deployedWebApps", deployedWebApps);
        
		ve.evaluate(context, pw, "lala", reader);
		pw.flush();
	}
	
	private void calculateTomcatPortNumbers(int tomcatNumber) {
//		mapper.registry.url = http\://localhost\:9040/mapper/registry
		
		int base = basePortNumber + webAppPortNumberDistance * tomcatNumber * parameters.getRvsPerTomcat();
		
		int serverPort = base + 5;
		int httpConnectorPort = base + 0;
		int httpsConnectorPort = base + 3;
		int ajpConnectorPort = base + 9;
		
		webAppProps.setProperty(PROP_TOMCAT_SERVER_PORT, Integer.toString(serverPort));
		webAppProps.setProperty(PROP_TOMCAT_HTTP_CONNECTOR_PORT, Integer.toString(httpConnectorPort));
		webAppProps.setProperty(PROP_TOMCAT_HTTPS_CONNECTOR_PORT, Integer.toString(httpsConnectorPort));
		webAppProps.setProperty(PROP_TOMCAT_AJP_CONNECTOR_PORT, Integer.toString(ajpConnectorPort));
		
	}
	
	
	private void calculateWebAppPortNumbers(int tomcatNumber, int webAppNumber) {
		int base = basePortNumber + webAppPortNumberDistance * (webAppNumber + tomcatNumber * parameters.getRvsPerTomcat());
		
		int plantListener = base + 1;
		int locationSystemListener = base + 2;
		int controllerConnector = base + 4;
		
		webAppProps.setProperty(PROP_PLANT_LISTENER_PORT, Integer.toString(plantListener));
		webAppProps.setProperty(PROP_LOCATION_SYSTEM_LISTENER_PORT, Integer.toString(locationSystemListener));
		webAppProps.setProperty(PROP_CONTROLLER_CONNECTOR_PORT, Integer.toString(controllerConnector));
	}
}
