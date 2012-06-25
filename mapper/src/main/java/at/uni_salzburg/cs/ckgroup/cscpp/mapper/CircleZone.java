/*
 * @(#) CircleZone.java
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

import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;

public class CircleZone implements IZone {
	
	private PolarCoordinate center;
	private CartesianCoordinate centerCart;
	private double radius;
	private IGeodeticSystem geodeticSystem;
	private PolarCoordinate depotPosition;
	

	public CircleZone (PolarCoordinate center, double radius, IGeodeticSystem geodeticSystem) {
		this.center = new PolarCoordinate(center);
		this.center.setAltitude(0);
		this.radius = radius;
		this.geodeticSystem = geodeticSystem;
		depotPosition = getCenterOfGravity ();
		centerCart = geodeticSystem.polarToRectangularCoordinates(center);
	}

	@Override
	public boolean isInside(PolarCoordinate position) {
		PolarCoordinate p = new PolarCoordinate(position);
		p.setAltitude(0);
		CartesianCoordinate pCart = geodeticSystem.polarToRectangularCoordinates(p);
		double distance = centerCart.subtract(pCart).norm();
		return distance <= radius;
	}

	@Override
	public PolarCoordinate getDepotPosition() {
		return depotPosition;
	}

	@Override
	public void setDepotPosition(PolarCoordinate depotPosition) {
		this.depotPosition = depotPosition;
	}

	public PolarCoordinate getCenterOfGravity () {
		return new PolarCoordinate(center);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject o = new JSONObject();
		o.put("type", "circle");
		
		JSONObject c = new JSONObject();
		c.put("lat", new Double(center.getLatitude()));
		c.put("lon", new Double(center.getLongitude()));
		o.put("center", c);
		
		JSONObject d = new JSONObject();
		d.put("lat", new Double(depotPosition.getLatitude()));
		d.put("lon", new Double(depotPosition.getLongitude()));
		o.put("depot", c);
		
		return o.toJSONString();
	}
}
