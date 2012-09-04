/*
 * @(#) CentralMappingAlgorithm.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.algorithms;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;

/**
 * The simple mapping algorithm works as follows:
 * 
 * <pre>
 * for all virtual vehicles do
 *    invoke migration to central engine
 * end for
 * </pre>
 */
public class CentralMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = Logger.getLogger(CentralMappingAlgorithm.class);
	
	private static final int NTHREADS = 10;
	
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm#execute(at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper)
	 */
	@Override
	public void execute(IMapper mapper) {
		virtualVehicleMapping(mapper);
	}
	

	/**
	 * Perform mapping of Virtual Vehicles.
	 * 
	 * @param mapper
	 *            the mapper instance.
	 */
	protected void virtualVehicleMapping(IMapper mapper) {
		
		Set<IZone> zones = mapper.getZones();
		
		Set<String> centralEngines = mapper.getCentralEngines();
		if (centralEngines.isEmpty()) {
			LOG.error("No central engines available!");
			return;
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
		
			String vehicleName = vehicleInfo.getVehicleName();
			String engineUrl = vehicleInfo.getEngineUrl();
			
			if (centralEngines.contains(engineUrl)) {

				PolarCoordinate pos = vehicleInfo.getVehicleStatus().getPosition();
				if (pos == null) {
					continue;
				}

				for (IZone z : zones) {
					if (z.isInside(pos)) {
						String targetEngineUrl = z.getZoneEngineUrl();
						if (targetEngineUrl != null && !engineUrl.equals(targetEngineUrl)) {
							LOG.info("Migrating vehicle " + vehicleName + " from " + engineUrl + " to " + targetEngineUrl);
//							mapper.migrate(engineUrl, vehicleName, targetEngineUrl);
							MigrationRunnable worker = new MigrationRunnable(mapper, engineUrl, vehicleName, targetEngineUrl);
						    executor.execute(worker);
						}
						break;
					}
				}
			
			}
		}
		
	    executor.shutdown();

	    while (!executor.isTerminated()) {
	    	try { Thread.sleep(20); } catch (InterruptedException e) { }
	    }
		
	}


}
