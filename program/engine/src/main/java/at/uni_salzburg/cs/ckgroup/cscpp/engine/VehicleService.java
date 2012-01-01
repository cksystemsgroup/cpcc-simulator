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
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

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
	
//	public final static String ACTION_CONFIG_UPLOAD = "configUpload";
	public final static String ACTION_VEHICLE_UPLOAD = "vehicleUpload";
	public final static String ACTION_VEHICLE_DOWNLOAD = "vehicleDownload";
	public final static String ACTION_VEHICLE_MIGRATION = "vehicleMigration";
	
	public final static String ACTION_VEHICLE_SUSPEND = "suspend";
	public final static String ACTION_VEHICLE_RESUME = "resume";
	
	
	public VehicleService (IServletConfig servletConfig) {
		super (servletConfig);
	}

	@Override
	public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		File contexTempDir = servletConfig.getContextTempDir();
		
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
				vehicle.resume();
				
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
			LOG.error("Virtual Vehicle migration not implemented yet.");
			
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

	private void migrateVehicle(ServletConfig config, String uploadUrl, String name) throws IOException {
		IVirtualVehicle vehicle = getVehicle(config, name);
		Object vhl = config.getServletContext().getAttribute("vehicleMap");
		@SuppressWarnings("unchecked")
		Map<String, IVirtualVehicle> vehicleMap = (Map<String, IVirtualVehicle>)vhl;
		vehicleMap.remove(name);
		vehicle.suspend();
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		vehicle.serialize(bo);
		String[] rc = HttpQueryUtils.fileUpload(uploadUrl, name, bo.toByteArray());
		LOG.info("migrateVehicle: id=" + name + ", rc=" + rc[0] + " -- " + rc[1]);
		if (!"200".equals(rc[0])) {
			vehicleMap.put(name, vehicle);
			vehicle.resume();
			throw new IOException(rc[0] + " -- " + rc[1] + " -- " + rc[2]);
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
	
	private IVirtualVehicle getVehicle (ServletConfig config, String vehicle) {
		Object vhl = config.getServletContext().getAttribute("vehicleMap");
		@SuppressWarnings("unchecked")
		Map<String, IVirtualVehicle> vehicleMap = (Map<String, IVirtualVehicle>)vhl;
		return vehicleMap.get(vehicle);
	}

}
