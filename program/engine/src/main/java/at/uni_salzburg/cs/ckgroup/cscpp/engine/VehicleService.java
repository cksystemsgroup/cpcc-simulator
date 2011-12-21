/*
 * @(#) VehicleService.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2011  Clemens Krainer
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.config.Configuration;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.VirtualVehicleBuilder;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.MimeEntry;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.MimeParser;


public class VehicleService extends DefaultService {
	
	Logger LOG = Logger.getLogger(VehicleService.class);
	
	public final static String CONTEXT_TEMP_DIR = "javax.servlet.context.tempdir";
	
	public final static String ACTION_CONFIG_UPLOAD = "configUpload";
	public final static String ACTION_VEHICLE_UPLOAD = "vehicleUpload";
	public final static String ACTION_VEHICLE_MIGRATION = "vehicleMigration";
	
	public final static String ACTION_VEHICLE_SUSPEND = "suspend";
	public final static String ACTION_VEHICLE_RESUME = "resume";
	
	public final static String PROP_CONFIG_FILE = "vehicle.config.file";
	
	public VehicleService (IServletConfig servletConfig) {
		super (servletConfig);
	}

	@Override
	public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		File contexTempDir = (File)config.getServletContext().getAttribute(CONTEXT_TEMP_DIR);
		
		String servicePath = request.getRequestURI();
		if (request.getRequestURI().startsWith(request.getContextPath()))
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		
		String[] cmd = servicePath.trim().split("/+");
		if (cmd.length < 3) {
			emit404(request, response);
			return;
		}
		
		boolean textMode = "text".equals(cmd[2]);
		String action = cmd[3];
		MimeEntry uploadedFile = null;
		
		if (request.getContentType() != null) {
			MimeParser parser = new MimeParser(request.getContentType());
			List<MimeEntry> list = parser.parse(request.getInputStream());
			String name = null;
			String fileName = null;
			
			for (MimeEntry entry : list) {
				Map<String, String> headerMap = entry.getHeaders();
				String contentDisposition = headerMap.get(MimeEntry.CONTENT_DISPOSITION);
				
				if (contentDisposition.matches(".*\\sname=\"(\\S*)\"[\000-\377]*"))
					name = contentDisposition.replaceFirst(".*\\sname=\"(\\S*)\".*", "$1");
				
				if (contentDisposition.matches(".*\\sfilename=\"(\\S*)\"[\000-\377]*"))
					fileName = contentDisposition.replaceFirst(".*\\sfilename=\"(\\S*)\".*", "$1");
				
				LOG.debug("cdName=" + name + ", fileName=" + fileName);
				
				if (name != null && fileName != null && !"".equals(fileName)) {
					uploadedFile = entry;
					break;
				}
			}		
		}
		
		String nextPage;

		if (ACTION_CONFIG_UPLOAD.equals(action)) {
			File confFile = new File (contexTempDir, servletConfig.getProperties().getProperty(PROP_CONFIG_FILE));
			if (uploadedFile != null) {
				saveFile (uploadedFile, confFile);
				nextPage = request.getContextPath() + "/config.tpl";
				Configuration cfg = (Configuration)config.getServletContext().getAttribute("configuration");
				cfg.loadConfig(new FileInputStream(confFile));
				LOG.info("Configuration uploaded.");
			} else {
				emit422(request, response);
				return;
			}
			
		} else if (ACTION_VEHICLE_UPLOAD.equals(action)) {
			if (uploadedFile != null) {
				nextPage = request.getContextPath() + "/vehicle.tpl";
				LOG.info("Virtual Vehicle uploaded.");
				
				File workDir = File.createTempFile("vehicle", "", contexTempDir); 
				workDir.delete();

				Object vbo = config.getServletContext().getAttribute("vehicleBuilder");
				VirtualVehicleBuilder vehicleBuilder = (VirtualVehicleBuilder)vbo;
				
				IVirtualVehicle vehicle = vehicleBuilder.build(workDir, new ByteArrayInputStream(uploadedFile.getBody()));
				
				Object vhl = config.getServletContext().getAttribute("vehicleList");
				List<IVirtualVehicle> vehicleList = (List<IVirtualVehicle>)vhl;
				vehicleList.add(vehicle);
				vehicle.resume();
				
			} else {
				emit422(request, response);
				return;
			}
			
		} else if (ACTION_VEHICLE_MIGRATION.equals(action)) {
			LOG.error("Virtual Vehicle migration not implemented yet.");
			
			
			
			
			nextPage = request.getContextPath() + "/vehicle.tpl";
			
		} else if (ACTION_VEHICLE_SUSPEND.equals(action)) {
			if (cmd.length > 4) {
				changeVehicleState(config, cmd[4], ACTION_VEHICLE_SUSPEND);
				nextPage = request.getContextPath() + "/vehicle.tpl";
			} else {
				emit422(request, response);
				return;
			}
			
		} else if (ACTION_VEHICLE_RESUME.equals(action)) {
			if (cmd.length > 4) {
				changeVehicleState(config, cmd[4], ACTION_VEHICLE_RESUME);
				nextPage = request.getContextPath() + "/vehicle.tpl";
			} else {
				emit422(request, response);
				return;
			}
			
		} else{
			LOG.error("Can not handle: " + servicePath);
			emit404(request, response);
			return;
		}
		
		if (textMode) {
			emit200 (request, response);
		} else {
			emit301 (request, response, nextPage);	
		}

	}

	private void saveFile(MimeEntry course, File file) throws IOException {
		if (file.exists() && file.isFile())
			file.delete();
			
		FileOutputStream w = new FileOutputStream(file);
		w.write(course.getBody());
		w.close();
		LOG.info("Written: " + file);
	}

	private void changeVehicleState (ServletConfig config, String vehicle, String state) throws IOException {
		
		Object vhl = config.getServletContext().getAttribute("vehicleList");
		List<IVirtualVehicle> vehicleList = (List<IVirtualVehicle>)vhl;
		
		for (IVirtualVehicle v : vehicleList) {
			if (v.getWorkDir().getName().equals(vehicle)) {
				LOG.info("Vehicle " + vehicle + ", action=" + state);
				if (ACTION_VEHICLE_RESUME.equals(state))
					v.resume();
				else if (ACTION_VEHICLE_SUSPEND.equals(state))
					v.suspend();
				break;
			}
		}
	}
	
	

}
