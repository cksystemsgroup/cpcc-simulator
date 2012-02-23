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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class RandomMappingAlgorithm extends AbstractMappingAlgorithm {

	private static final Logger LOG = Logger.getLogger(RandomMappingAlgorithm.class);
	
	public static final int COUNTER_MAXIMUM = 5;
	
	private int counter = 0;
	
	@Override
	public void execute() {
		
		if (getVirtualVehicleMap().isEmpty()) {
			LOG.info("No migration because of empty virtual vehicle map.");
			return;
		}
		
		if (counter++ < COUNTER_MAXIMUM) {
			return;
		}
		
		counter = 0;
		
		Entry<String, Map<String, VehicleStatus>> engine1 = getRandomEngine();
		Entry<String, Map<String, VehicleStatus>> engine2 = getRandomEngine();
		
		if (engine1 == null || engine2 == null) {
			LOG.info("Random virtual vehicle migration cancelled because engine1 or engine2 is null.");
			return;
		}
		
		if (engine1.getValue().isEmpty() && engine2.getValue().isEmpty()) {
			LOG.info("Random virtual vehicle migration cancelled because engine1 and engine2 have no virtual vehicles.");
			return;
		}
		
		String engine1Url = engine1.getKey();
		String engine2Url = engine2.getKey();
		
		if (engine1Url.equals(engine2Url)) {
			LOG.debug("Source engine is target engine, migration cancelled.");
			return;
		}

		if (!engine1.getValue().isEmpty()) {
			String vehicle1 = getRandomVehicle(engine1.getValue());
			if (vehicle1 != null) {
				migrate (engine1Url, vehicle1, engine2Url);
				return;
			}
		}
		
		if (!engine2.getValue().isEmpty()) {
			String vehicle2 = getRandomVehicle(engine2.getValue());
			if (vehicle2 != null) {
				migrate (engine2Url, vehicle2, engine1Url);
				return;
			}
		}
	}

	private Entry<String, Map<String, VehicleStatus>> getRandomEngine () {
		int engineNumber = (int)(Math.random() * getVirtualVehicleMap().size());
		for (Entry<String, Map<String, VehicleStatus>> v : getVirtualVehicleMap().entrySet())
			if (engineNumber-- <= 0)
				return v;
		return null;
	}
	
	private String getRandomVehicle (Map<String, VehicleStatus> engine) {
		if (engine.isEmpty())
			return null;
		String[] vehicles = engine.keySet().toArray(new String[engine.size()]);
		int number = (int)(Math.random() * engine.size());
		return vehicles[number];
	}
}
