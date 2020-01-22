/*
 * @(#) HomeMappingAlgorithm.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IVirtualVehicleInfo;

/**
 * The simple mapping algorithm works as follows:
 * 
 * <pre>
 * for all virtual vehicles do
 *    invoke migration to central engine
 * end for
 * </pre>
 */
public class HomeMappingAlgorithm implements IMappingAlgorithm {
	
	private static final Logger LOG = LoggerFactory.getLogger(HomeMappingAlgorithm.class);
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm#execute(at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper)
	 */
	@Override
	public void execute(IMapper mapper) {
		        
		if (mapper.getRegistrationData().isEmpty()) {
			LOG.info("No migration because of empty registration map.");
			return;
		}
		        
		if(mapper.getStatusProxyMap().isEmpty()) {
			LOG.info("No migration because of empty status proxy map.");
			return;
		}
		
		virtualVehicleMapping(mapper);
	}
	

	/**
	 * Perform mapping of Virtual Vehicles.
	 * 
	 * @param mapper
	 *            the mapper instance.
	 */
	protected void virtualVehicleMapping(IMapper mapper) {
		
		List<IVirtualVehicleInfo> vehicleList = mapper.getVirtualVehicleList();
		
		for (IVirtualVehicleInfo vehicleInfo : vehicleList) {
		
			String vehicleName = vehicleInfo.getVehicleName();
			String engineUrl = vehicleInfo.getEngineUrl();
					
			Set<String> centralEngines = mapper.getCentralEngines();
			if (!centralEngines.isEmpty()) {
				String targetEngineUrl = centralEngines.iterator().next();
				if (!engineUrl.equals(targetEngineUrl)) {
					mapper.migrate(engineUrl, vehicleName, centralEngines.iterator().next());
				}
			}
		}
	}

}
