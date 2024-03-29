/*
 * @(#) SquareZone.java
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

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public class SquareZone extends AbstractZone {

	private double minLatitude;
	private double maxLatitude;
	private double minLongitude;
	private double maxLongitude;
	
	public SquareZone(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
		this.minLatitude = minLatitude;
		this.maxLatitude = maxLatitude;
		this.minLongitude = minLongitude;
		this.maxLongitude = maxLongitude;
		setDepotPosition(getCenterOfGravity());
	}
	
	@Override
	public boolean isInside(PolarCoordinate p) {
		if (p == null) {
			return false;
		}
		return	minLatitude  <= p.getLatitude()  && p.getLatitude()  <= maxLatitude &&
				minLongitude <= p.getLongitude() && p.getLongitude() <= maxLongitude;
	}
		
	public PolarCoordinate getCenterOfGravity () {
		return new PolarCoordinate((maxLatitude + minLatitude) / 2, (maxLongitude - minLongitude) / 2, 0);
	}
	
	@Override
	public String toString() {
		return String.format("SquareZone: lat %3.8f - %3.8f, lon %3.8f - %3.8f", minLatitude, maxLatitude, minLongitude, maxLongitude);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject o = new JSONObject();
		o.put("type", "square");
		
		JSONObject l = new JSONObject();
		l.put("lat", new Double(maxLatitude));
		l.put("lon", new Double(minLongitude));
		o.put("leftTop", l);
		
		JSONObject r = new JSONObject();
		r.put("lat", new Double(minLatitude));
		r.put("lon", new Double(maxLongitude));
		o.put("rightBottom", r);

		JSONObject d = new JSONObject();
		d.put("lat", new Double(getDepotPosition().getLatitude()));
		d.put("lon", new Double(getDepotPosition().getLongitude()));
		o.put("depot", d);
		
		return null;
	}

}
