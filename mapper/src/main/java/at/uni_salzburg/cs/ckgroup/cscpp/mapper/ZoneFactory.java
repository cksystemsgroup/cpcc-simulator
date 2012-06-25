/*
 * @(#) RegData.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper;

import java.util.Set;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;

public class ZoneFactory {
	
	private static final Logger LOG = Logger.getLogger(ZoneFactory.class);
	
	private static final PolarCoordinate[][] ZONES = {
		{
			new PolarCoordinate(47.69243237,13.38507593,0.00),			new PolarCoordinate(47.69243237,13.38573986,0.00),
			new PolarCoordinate(47.69216517,13.38599735,0.00),			new PolarCoordinate(47.69189796,13.38573986,0.00),
			new PolarCoordinate(47.69189796,13.38507593,0.00),			new PolarCoordinate(47.69216517,13.38481844,0.00),
			new PolarCoordinate(47.69243237,13.38507593,0.00),
		},
		{
			new PolarCoordinate(47.69296678,13.38503838,0.00),			new PolarCoordinate(47.69296678,13.38573039,0.00),
			new PolarCoordinate(47.69269958,13.38599735,0.00),			new PolarCoordinate(47.69243237,13.38573986,0.00),
			new PolarCoordinate(47.69243237,13.38507593,0.00),			new PolarCoordinate(47.69269958,13.38481844,0.00),
			new PolarCoordinate(47.69296678,13.38503838,0.00),
		},
		{
			new PolarCoordinate(47.69323399,13.38599735,0.00),			new PolarCoordinate(47.69323399,13.38666128,0.00),
			new PolarCoordinate(47.69296678,13.38692824,0.00),			new PolarCoordinate(47.69269958,13.38666128,0.00),
			new PolarCoordinate(47.69269958,13.38599735,0.00),			new PolarCoordinate(47.69296678,13.38573039,0.00),
			new PolarCoordinate(47.69323399,13.38599735,0.00),
		},
		{
			new PolarCoordinate(47.69269958,13.38599735,0.00),			new PolarCoordinate(47.69269958,13.38666128,0.00),
			new PolarCoordinate(47.69243237,13.38692824,0.00),			new PolarCoordinate(47.69216517,13.38666128,0.00),
			new PolarCoordinate(47.69216517,13.38599735,0.00),			new PolarCoordinate(47.69243237,13.38573986,0.00),
			new PolarCoordinate(47.69269958,13.38599735,0.00),
		},
		{
			new PolarCoordinate(47.69216517,13.38599735,0.00),			new PolarCoordinate(47.69216517,13.38666128,0.00),
			new PolarCoordinate(47.69189796,13.38692824,0.00),			new PolarCoordinate(47.69163076,13.38666128,0.00),
			new PolarCoordinate(47.69163076,13.38599735,0.00),			new PolarCoordinate(47.69189796,13.38573986,0.00),
			new PolarCoordinate(47.69216517,13.38599735,0.00),
		},
		{
			new PolarCoordinate(47.69296678,13.38692824,0.00),			new PolarCoordinate(47.69296678,13.38758270,0.00),
			new PolarCoordinate(47.69269958,13.38784019,0.00),			new PolarCoordinate(47.69243237,13.38758270,0.00),
			new PolarCoordinate(47.69243237,13.38692824,0.00),			new PolarCoordinate(47.69269958,13.38666128,0.00),
			new PolarCoordinate(47.69296678,13.38692824,0.00),
		},
		{
			new PolarCoordinate(47.69243237,13.38692824,0.00),			new PolarCoordinate(47.69243237,13.38758270,0.00),
			new PolarCoordinate(47.69216517,13.38784019,0.00),			new PolarCoordinate(47.69189796,13.38758270,0.00),
			new PolarCoordinate(47.69189796,13.38692824,0.00),			new PolarCoordinate(47.69216517,13.38666128,0.00),
			new PolarCoordinate(47.69243237,13.38692824,0.00),
		}
	};
	
	public static void buildZones(Set<IZone> zones) {
		zones.clear();
		for(PolarCoordinate[] zone : ZONES) {
			zones.add(new PolygonZone(zone));
		}
		for (IZone z : zones) {
			LOG.info("Build zone: " + z.toJSONString());
		}
	}
	
	public static void buildNeighborZones(Set<IZone> neighborZones) {
		neighborZones.clear();
		// TODO implement me
	}

}
