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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.config.IConfiguration;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.VirtualVehicleBuilder;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.DefaultService;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.MimeEntry;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.MimeParser;


public class VehicleService extends DefaultService {
	
	Logger LOG = Logger.getLogger(VehicleService.class);
	
	public final static String ACTION_VEHICLE_UPLOAD = "vehicleUpload";
	public final static String ACTION_VEHICLE_DOWNLOAD = "vehicleDownload";
	public final static String ACTION_VEHICLE_MIGRATION = "vehicleMigration";
	public final static String ACTION_VEHICLE_DATA = "vehicleData";
	public final static String ACTION_VEHICLE_COMMAND = "vehicleCommand";
	
	public final static String ACTION_VEHICLE_SUSPEND = "suspend";
	public final static String ACTION_VEHICLE_RESUME = "resume";
	public final static String ACTION_VEHICLE_DELETE = "delete";
	public final static String ACTION_VEHICLE_FREEZE = "freeze";
	public final static String ACTION_VEHICLE_UNFREEZE = "unfreeze";
	
	public final static String ACTION_MAPPER_REGISTRATION = "mapperRegistration"; 
	
	public VehicleService (IServletConfig servletConfig) {
		super (servletConfig);
	}

	@Override
	public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		File contexTempDir = getServletConfig().getContextTempDir();
		
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
		
		if (ACTION_VEHICLE_UPLOAD.equals(action)) {
			if (uploadedFile != null) {
				nextPage = request.getContextPath() + "/vehicle.tpl";
				LOG.info("Virtual Vehicle uploaded.");
				
				File workDir = File.createTempFile("vehicle", "", contexTempDir); 
				workDir.delete();

				Object vbo = config.getServletContext().getAttribute("vehicleBuilder");
				VirtualVehicleBuilder vehicleBuilder = (VirtualVehicleBuilder)vbo;
				
				IVirtualVehicle vehicle = vehicleBuilder.build(workDir, new ByteArrayInputStream(uploadedFile.getBody()));
				
				Object vhl = config.getServletContext().getAttribute("vehicleMap");
				@SuppressWarnings("unchecked")
				Map<String, IVirtualVehicle> vehicleMap = (Map<String, IVirtualVehicle>)vhl;
				vehicleMap.put(workDir.getName(), vehicle);
				
				Object cnf = config.getServletContext().getAttribute("configuration");
				IConfiguration configuration = (IConfiguration)cnf;
				
				if (configuration.isPilotAvailable()) {
					vehicle.resume();
				}
				
			} else {
				emit422(request, response);
				return;
			}
			
		} else if (ACTION_VEHICLE_DOWNLOAD.equals(action)) {

			if (cmd.length > 4) {
				IVirtualVehicle v = getVehicle(config, cmd[4]);
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				v.serialize(bo);
				emitByteArray(response, "application/zip", bo.toByteArray());
				return;
			} else {
				emit422(request, response);
				return;
			}
			
			
		} else if (ACTION_VEHICLE_MIGRATION.equals(action)) {
			// * get upload-URL
			// * suspend vehicle
			// * zip vehicle to file
			// * upload file to remote machine
			// * destroy local copy if upload was successful
			
			String p1 = request.getParameter("vehicleDst");
			String p2 = request.getParameter("vehicleIDs");
			
			if (p1 == null || p1.trim().isEmpty()) {
				emit400(request, response, "Invalid or empty destination URL.");
				return;
			}
			if (p2 == null || p2.trim().isEmpty()) {
				emit400(request, response, "Invalid or empty virtual vehicle ID.");
				return;
			}
			
			for (String name : p2.split("\\s*,\\s*")) {
				File vd = new File(contexTempDir,name);
				
				try {
					if (!vd.exists()) {
						emit400(request, response, "No virtual vehicle " + name + " found!");
						return;
					}

					migrateVehicle(config, p1, name);
					LOG.info("Migration succeeded. Removing folder " + vd.getAbsolutePath());
					FileUtils.removeRecursively(vd);
					
				} catch (IOException e) {
					LOG.info("Problems at migrating virtual vehicle " + name, e);
					emit400(request, response, e.getMessage());
					return;
				}
			}
			
			if (textMode) {
				emitPlainText(response, "OK");
				return;
			}
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
			
		} else if (ACTION_VEHICLE_DELETE.equals(action)) {
//			if (cmd.length > 4) {
			if (cmd.length > 3) {
				String p2 = request.getParameter("deleteVehicleIDs");
				if ( (p2 == null || p2.trim().isEmpty()) &&  cmd.length > 4) {
					p2 = cmd[4];
				}
				if (p2 == null || p2.trim().isEmpty()) {
					emit400(request, response, "Invalid or empty virtual vehicle ID.");
					return;
				}
				
				removeVehicles(config, p2.trim());
				nextPage = request.getContextPath() + "/vehicle.tpl";
			} else {
				emit422(request, response);
				return;
			}
			
		} else if (ACTION_VEHICLE_FREEZE.equals(action) || ACTION_VEHICLE_UNFREEZE.equals(action)) {
			if (cmd.length > 3) {
				String p2 = request.getParameter("freezeVehicleIDs");
				if ( (p2 == null || p2.trim().isEmpty()) &&  cmd.length > 4) {
					p2 = cmd[4];
				}
				if (p2 == null || p2.trim().isEmpty()) {
					emit400(request, response, "Invalid or empty virtual vehicle ID.");
					return;
				}
				
				freezeVehicles(config, p2.trim(), ACTION_VEHICLE_FREEZE.equals(action));
				nextPage = request.getContextPath() + "/vehicle.tpl";
			} else {
				emit422(request, response);
				return;
			}
			
		} else if (ACTION_VEHICLE_DATA.equals(action)) {
			if (cmd.length > 5) {
				nextPage = request.getContextPath() + "/vehicleDetail.tpl?vehicle=" + cmd[4];
				IVirtualVehicle v = getVehicle(config, cmd[4]);
				File data = new File(v.getDataDir(),cmd[5]);
				if (data.exists()) {
					response.setContentType(getContentType(data));
					emitFile(response.getOutputStream(), new FileInputStream(data));
					return;
				}
			}
			emit422(request, response);
			return;
			
		} else if (ACTION_VEHICLE_COMMAND.equals(action)) {
			if (cmd.length > 4) {
				IVirtualVehicle v = getVehicle(config, cmd[4]);
				Command currentCommand = v.getCurrentCommand();
				String currentCommandString = currentCommand != null ? currentCommand.toString() : "";
				emitByteArray(response, "text/plain", currentCommandString.getBytes());
				return;
			}

			emit422(request, response);
			return;
			
		} else if (ACTION_MAPPER_REGISTRATION.equals(action)) {
			Object reg = config.getServletContext().getAttribute("engineRegistry");
			EngineRegister register = (EngineRegister)reg;
			LOG.info("Re-registration with " + register.getRegistrationUrl());
			register.register();
			nextPage = request.getContextPath() + "/config.tpl";
			
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

	private void migrateVehicle(ServletConfig config, String uploadUrl, String name) throws IOException {
		IVirtualVehicle vehicle = getVehicle(config, name);
		Object cnf = config.getServletContext().getAttribute("configuration");
		IConfiguration configuration = (IConfiguration)cnf;
		Object vhl = config.getServletContext().getAttribute("vehicleMap");
		@SuppressWarnings("unchecked")
		Map<String, IVirtualVehicle> vehicleMap = (Map<String, IVirtualVehicle>)vhl;
		vehicleMap.remove(name);
		vehicle.suspend();
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		vehicle.serialize(bo);
		String[] rc;
		try {
			rc = HttpQueryUtils.fileUpload(uploadUrl, name, bo.toByteArray());
			LOG.info("migrateVehicle: id=" + name + ", rc=" + rc[0] + " -- " + rc[1]);
			if (!"200".equals(rc[0])) {
				vehicleMap.put(name, vehicle);
				if (configuration.isPilotAvailable()) {
					vehicle.resume();
				}
				throw new IOException(rc[0] + " -- " + rc[1] + " -- " + rc[2]);
			}
			File workDir = vehicle.getWorkDir();
			FileUtils.removeRecursively(workDir);
		} catch (IOException e) {
			vehicleMap.put(name, vehicle);
			if (configuration.isPilotAvailable()) {
				vehicle.resume();
			}
			throw e;
		}
	}

	private void changeVehicleState (ServletConfig config, String vehicle, String state) throws IOException {
		IVirtualVehicle v = getVehicle(config, vehicle);
//		LOG.info("Vehicle " + vehicle + ", action=" + state);
		if (v != null) { 
			if (ACTION_VEHICLE_RESUME.equals(state))
				v.resume();
			else if (ACTION_VEHICLE_SUSPEND.equals(state))
				v.suspend();
		}
	}

	private void removeVehicles(ServletConfig config, String vehicles) {

		Object vhl = config.getServletContext().getAttribute("vehicleMap");
		@SuppressWarnings("unchecked")
		Map<String, IVirtualVehicle> vehicleMap = (Map<String, IVirtualVehicle>)vhl;
		for (String name : vehicles.split("\\s*,\\s*")) {
			IVirtualVehicle vehicle = vehicleMap.get(name);
			if (vehicle == null)
				return;
			
			if (vehicle.isActive()) {
				LOG.error("Vehicle " + name + " still collects data! Deletion not possible yet!");
				return;
			}
			
			LOG.info("Deleting vehicle " + name);
			
			try {
				vehicleMap.remove(name);
				vehicle.suspend();
				File workDir = vehicle.getWorkDir();
				FileUtils.removeRecursively(workDir);
			} catch (IOException e) {
				LOG.error("At deleting vehicle " + name, e);
			}
		}
	}

	private void freezeVehicles(ServletConfig config, String vehicles, boolean freeze) throws IOException {
		Object vhl = config.getServletContext().getAttribute("vehicleMap");
		@SuppressWarnings("unchecked")
		Map<String, IVirtualVehicle> vehicleMap = (Map<String, IVirtualVehicle>)vhl;
		for (String name : vehicles.split("\\s*,\\s*")) {
			IVirtualVehicle vehicle = vehicleMap.get(name);
			if (vehicle == null) {
				continue;
			}
			vehicle.setFrozen(freeze);
		}
	}

	private IVirtualVehicle getVehicle (ServletConfig config, String vehicle) {
		Object vhl = config.getServletContext().getAttribute("vehicleMap");
		@SuppressWarnings("unchecked")
		Map<String, IVirtualVehicle> vehicleMap = (Map<String, IVirtualVehicle>)vhl;
		return vehicleMap.get(vehicle);
	}

}
