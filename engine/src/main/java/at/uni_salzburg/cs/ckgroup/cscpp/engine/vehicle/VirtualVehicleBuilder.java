/*
 * @(#) VirtualVehicleBuilder.java
 *
 * This code is part of the ESE CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.SensorProxy;


public class VirtualVehicleBuilder {

	Logger LOG = LoggerFactory.getLogger(VirtualVehicleBuilder.class);
	
	private SensorProxy sensorProxy = null;

	private VehicleStorage vehicleStorage = null;
	
	
	public void setSensorProxy(SensorProxy sensorProxy) {
		this.sensorProxy = sensorProxy;
	}

	public void setVehicleStorage(VehicleStorage vehicleStorage) {
		this.vehicleStorage  = vehicleStorage;
	}

	/**
	 * @param workDir the work directory for the new virtual vehicle.
	 * @param data the virtual vehicle as a stream.
	 * @return the newly constructed virtual vehicle.
	 * @throws IOException thrown in case of IO or configuration errors.
	 */
	public IVirtualVehicle build(File workDir, InputStream data) throws IOException {
		
		FileUtils.ensureDirectory(workDir);
		
		ZipInputStream zip = new ZipInputStream(data);
		
		LOG.info("Unpacking Virtual Vehicle Archive.");
		ZipEntry entry;
		while ( (entry = zip.getNextEntry()) != null) {
			String name = entry.getName();
			File f = new File(workDir,name);
			
			if (entry.isDirectory()) {
				LOG.info("Creating directory " + f.getAbsolutePath());
				f.mkdirs();
				if (!f.exists())
					throw new IOException("Can not create folder " + f.getAbsolutePath());
				
			} else {
				long size = entry.getSize();
				long time = entry.getTime();
				LOG.info("Deflating file " + f.getAbsolutePath() + " size=" + size);
				FileOutputStream fOut = new FileOutputStream(f);
				int l;
				byte[] tmp = new byte[2048];
				while ((l = zip.read(tmp)) != -1) {
					fOut.write(tmp, 0, l);
				}
				f.setLastModified(time);
				fOut.close();
			}
		}
		
		LOG.info("Instantiating Virtual Vehicle " + workDir.getName());
		VirtualVehicle vehicle = null;
		try {
			vehicle = new VirtualVehicle(workDir);
			vehicle.setSensorProxy(sensorProxy);
		} catch (IOException e) {
			LOG.error("Can not instantiate virtual vehicle " + workDir);
//			FileUtils.removeRecursively(workDir);
			vehicleStorage.removeVehicleWorkDir(workDir);
			throw new IOException("Can not instantiate virtual vehicle " + workDir, e);
		}
		
		
		return vehicle;
	}
	
	/**
	 * @param workDir the work directory containing an existing virtual vehicle.
	 * @return the newly constructed virtual vehicle.
	 * @throws IOException thrown in case of IO or configuration errors.
	 */
	public IVirtualVehicle build(File workDir) throws IOException {
		LOG.info("Instantiating Virtual Vehicle " + workDir.getName());
		VirtualVehicle vehicle = new VirtualVehicle(workDir);
		vehicle.setSensorProxy(sensorProxy);
		return vehicle;
	}

}
