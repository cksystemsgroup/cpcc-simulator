/*
 * @(#) VehicleStorage.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public class VehicleStorage {
	
	private static final Logger LOG = Logger.getLogger(VehicleStorage.class);
	
	private static final String VEHICLE_FILE_FILTER = "vehicle\\d+";
	
	private int storageDepth;
	private File baseDir;

	public VehicleStorage(File baseDir, int storageDepth) {
		this.baseDir = baseDir;
		this.storageDepth = storageDepth;
	}

	public List<File> listVehicleFolder () {
		List<File> fileList = new ArrayList<File>();
		listRecursively(fileList, baseDir);
		LOG.info(fileList.size() + " vehicles listed in storage.");
		return fileList;
	}
	
	private void listRecursively(List<File> fileList, File folder) {
		
		for (File file : folder.listFiles()) {
			
			if (!file.isDirectory()) {
				continue;
			}
			
			if (file.getName().matches(VEHICLE_FILE_FILTER)) {
				fileList.add(file);
			} else {
				listRecursively(fileList, file);
			}
		}
		
	}

	public File createVehicleWorkDir() throws IOException {
		
		File tmpDir = File.createTempFile("vehicle", "", baseDir); 
		tmpDir.delete();
		
		if (storageDepth == 0) {
			return tmpDir;
		}
		
		String h = String.format("%08x", tmpDir.getName().hashCode());
		File workDir = baseDir;
		for (int k=0; k < storageDepth; ++k) {
			String z = h.substring(2*k, 2*k+2);
			workDir = new File(workDir,z);
		}

		workDir = new File(workDir, tmpDir.getName());
		
//		LOG.info("Created vehicle folder " + workDir.getAbsolutePath());
		return workDir; 
	}
	
	public File findVehicleWorkDir(String name) {
		
		String h = String.format("%08x", name.hashCode());
		File workDir = baseDir;
		for (int k=0; k < storageDepth; ++k) {
			String z = h.substring(2*k, 2*k+2);
			File vd = new File(workDir, name);
			if (vd.exists() && vd.isDirectory()) {
				return vd;
			}
			workDir = new File(workDir,z);
		}

		workDir = new File(workDir, name);
		
		return workDir;
	}
	
	public void removeVehicleWorkDir(File workDir) throws IOException {
		
//		LOG.debug("Removing vehicle " + workDir.getAbsolutePath());
		FileUtils.removeRecursively(workDir);
//		workDir = workDir.getParentFile();
//
//		String basePath = baseDir.getAbsolutePath();
//		while (workDir.getParentFile().getAbsolutePath().startsWith(basePath)) {
//			String[] list = workDir.list();
//			if (list.length == 0) {
////				LOG.info("Removing folder " + workDir.getAbsolutePath());
//				workDir.delete();
//			}
//			workDir = workDir.getParentFile();
//		}
	}


	
}
