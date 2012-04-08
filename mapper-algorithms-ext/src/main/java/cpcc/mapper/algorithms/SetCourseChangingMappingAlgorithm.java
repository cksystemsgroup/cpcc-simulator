/*
 * @(#) SetCourseChangingMappingAlgorithm.java
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
package cpcc.mapper.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRVCommand;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IStatusProxy;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandFlyTo;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandHover;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandLand;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.RVCommandTakeOff;

/**
 * This algorithm does not use any information concerning flight plans and
 * available sensors. After 20 mapper invocations it changes the set course of
 * one Real Vehicle.
 */
public class SetCourseChangingMappingAlgorithm implements IMappingAlgorithm {

	private static final Logger LOG = Logger.getLogger(SetCourseChangingMappingAlgorithm.class);
	
	/**
	 * This constant defines the number of execute() invocations needed to attempt a random migration.
	 */
	public static final int COUNTER_MAXIMUM = 20;
	
	/**
	 * Execution counter. Increases by one each time execute() is invocated.
	 */
	private int counter = 0;
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMappingAlgorithm#execute(at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper)
	 */
	@Override
	public void execute(IMapper mapper) {
		
		if (mapper.getVirtualVehicleList().isEmpty()) {
			LOG.info("No migration because of empty virtual vehicle list.");
			return;
		}
		
		if (counter < COUNTER_MAXIMUM) {
			++counter;
			return;
		}
		
		if (counter != COUNTER_MAXIMUM) {
			return;
		}
		
		Map<String, IStatusProxy> statusProxyMap = mapper.getStatusProxyMap();
		
		if (statusProxyMap.isEmpty()) {
			LOG.info("No real vehicles available. Changing set course cancelled.");
			return;
		}
		
		for (Entry<String, IStatusProxy>  entry : statusProxyMap.entrySet()) {
			IStatusProxy p = entry.getValue();
			List<IRVCommand> courseCommandList = new ArrayList<IRVCommand>();
			if (p.getCurrentPosition() != null && p.getCurrentPosition().getAltitude() < 1) {
				courseCommandList.add(new RVCommandTakeOff(1, 6));
			}
			courseCommandList.add(new RVCommandFlyTo(new PolarCoordinate(47.82201991, 13.04083988, 20), 1, 5));
			courseCommandList.add(new RVCommandFlyTo(new PolarCoordinate(47.82203567, 13.04086939, 20), 1, 5));
			courseCommandList.add(new RVCommandFlyTo(new PolarCoordinate(47.82203567, 13.04245591, 20), 1, 5));
			courseCommandList.add(new RVCommandFlyTo(new PolarCoordinate(47.82196903, 13.04245591, 20), 1, 5));
			courseCommandList.add(new RVCommandFlyTo(new PolarCoordinate(47.82196903, 13.04087609, 20), 1, 5));
			courseCommandList.add(new RVCommandFlyTo(new PolarCoordinate(47.82200955, 13.04083720, 20), 1, 5));
			
			courseCommandList.add(new RVCommandHover(7));
			courseCommandList.add(new RVCommandLand());
			p.changeSetCourse(courseCommandList , true);
			LOG.info("Changing set course of real vehicle that transports engine " + entry.getKey());
			break;
		}
		
		++counter;

	}


}
