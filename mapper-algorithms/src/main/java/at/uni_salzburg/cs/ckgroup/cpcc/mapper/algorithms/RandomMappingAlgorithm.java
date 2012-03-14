/*
 * @(#) RandomMappingAlgorithm.java
 *
 * This code is part of the JNavigator project.
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

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;

/**
 * This mapping algorithm randomly selects a virtual vehicle and migrates it to a randomly chosen engine.
 */
public class RandomMappingAlgorithm implements IMappingAlgorithm {

	private static final Logger LOG = Logger.getLogger(RandomMappingAlgorithm.class);
	
	public static final int COUNTER_MAXIMUM = 5;
	
	private int counter = 0;
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.mapper.api.IMappingAlgorithm#execute(at.uni_salzburg.cs.ckgroup.cscpp.mapper.api.IMapper)
	 */
	@Override
	public void execute(IMapper mapper) {
		
		if (mapper.getVirtualVehicleList().isEmpty()) {
			LOG.info("No migration because of empty virtual vehicle list.");
			return;
		}
		
		if (counter++ < COUNTER_MAXIMUM) {
			return;
		}
		
		counter = 0;
		
		String targetEngineUrl = getRandomEngine(mapper);
		
		if (targetEngineUrl == null || targetEngineUrl.isEmpty()) {
			LOG.info("Random virtual vehicle migration cancelled, because less than one engine registered.");
			return;
		}
		
		IVirtualVehicleInfo vehicleInfo = getRandomVehicle(mapper);
		if (vehicleInfo == null) {
			LOG.info("Random virtual vehicle migration cancelled, because there are no virtual vehicles available.");
			return;
		}
		
		if (targetEngineUrl.equals(vehicleInfo.getEngineUrl())) {
			LOG.debug("Source engine is target engine, migration cancelled.");
			return;
		}

		mapper.migrate (vehicleInfo.getEngineUrl(), vehicleInfo.getVehicleName(), targetEngineUrl);
	}

	/**
	 * Randomly select an engine.
	 * 
	 * @return the engine's base URL
	 */
	private String getRandomEngine (IMapper mapper) {
		if (mapper.getRegistrationData().size() <= 1) {
			return null;
		}
		
		int engineNumber = (int)(Math.random() * mapper.getRegistrationData().size());
		for (String engineURL : mapper.getRegistrationData().keySet()) {
			if (engineNumber-- <= 0)
				return engineURL;
		}
		return null;
	}
	
	/**
	 * Randomly select a virtual vehicle.
	 * 
	 * @return a <code>VehicleInfo</code> object mainly containing the name and engine of this virtual vehicle.  
	 */
	private IVirtualVehicleInfo getRandomVehicle (IMapper mapper) {
		if (mapper.getVirtualVehicleList().isEmpty()) {
			return null;
		}
		
		int vehicleNumber = (int)(Math.random() * mapper.getVirtualVehicleList().size());
		return mapper.getVirtualVehicleList().get(vehicleNumber);
	}

}
