/*
 * @(#) PolygonZone.java
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

import java.util.Locale;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;

public class PolygonZone implements IZone {
	
	private static final double epsilon = 1E-9;
	
	private TwoTuple[] vertices;
	private double maxLat = Double.NEGATIVE_INFINITY;
	private double minLat = Double.POSITIVE_INFINITY;
	private double maxLon = Double.NEGATIVE_INFINITY;
	private double minLon = Double.POSITIVE_INFINITY;
	private PolarCoordinate depotPosition;
	
	public PolygonZone(PolarCoordinate[] v) {
		
		vertices = new TwoTuple[v.length];

		for (int k=0, l=v.length; k < l; ++k) {
			double x = v[k].getLatitude();
			double y = v[k].getLongitude();
			vertices[k] = new TwoTuple(x,y);
		}
		
		findBoundingBox();
	}


	public PolygonZone(TwoTuple[] vertices) {
		this.vertices = vertices;
		findBoundingBox();
	}

	
	private void findBoundingBox() {
		for (int k=0, l=vertices.length; k < l; ++k) {
			if (vertices[k].x < minLat) minLat = vertices[k].x;
			if (vertices[k].x > maxLat) maxLat = vertices[k].x;
			if (vertices[k].y < minLon) minLon = vertices[k].y;
			if (vertices[k].y > maxLon) maxLon = vertices[k].y;
		}
		
		depotPosition = getCenterOfGravity();
	}
	
	@Override
	public boolean isInside(PolarCoordinate p) {
		if (p == null) {
			return false;
		}
		return isInside(p.getLatitude(), p.getLongitude());
	}
	
	public boolean isInside (double cx, double cy) {
		
		if (cx < minLat || cx > maxLat || cy < minLon || cy > maxLon)
			return false;
		
		int right=0;
		int left=0;
		for (int i=0, l=vertices.length; i < l; ++i) {
			double ax = vertices[i].x;
			double ay = vertices[i].y;
			double bx = i+1 == l ? vertices[0].x : vertices[i+1].x;
			double by = i+1 == l ? vertices[0].y : vertices[i+1].y;
			
			if (cx == ax && cy == ay)
				return true;

			if (cy == ay) ay += epsilon;
			if (cy == by) by += epsilon;

			if ((cy < ay && cy < by) || (cy > ay && cy > by))
				continue;
			
			if (cx <= ax && cx <= bx) {
				++right;
				continue;
			}
			
			if (cx >= ax && cx >= bx) {
				++left;
				continue;
			}
			
			double k = (by - ay) / (bx - ax) ;
			double x = ax + (cy - ay) / k;
			if (cx <= x)
				++right;
			else
				++left;
		}
		
		if (right != 0)
			return right % 2 == 1;
		
		return left % 2 == 1;
	}
	
	@Override
	public PolarCoordinate getDepotPosition () {
		return depotPosition;
	}
	
	@Override
	public void setDepotPosition(PolarCoordinate depotPosition) {
		this.depotPosition = depotPosition;
	}
	
	public PolarCoordinate getCenterOfGravity () {
		double x = 0, y = 0;
		double doubleArea = 0;
		
		for (int k=0, l=vertices.length-1; k < l; ++k) {
			TwoTuple a = vertices[k];
			TwoTuple b = vertices[k+1];
			double t = (a.x * b.y - b.x * a.y);
			x += (a.x + b.x) * t;
			y += (a.y + b.y) * t;
			doubleArea += (a.x * b.y - b.x * a.y);
		}
		
		double sixTimesArea = 3.0 * doubleArea;
		return new PolarCoordinate(x/sixTimesArea, y/sixTimesArea, 0);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (TwoTuple t : vertices) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(t);
		}

		return String.format(Locale.US, "vertices: %s", b.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		for (TwoTuple t : vertices) {
			a.add(t);
		}
		o.put("type", "polygon");
		o.put("vertices", a);
		o.put("depot", new TwoTuple(depotPosition.getLatitude(), depotPosition.getLongitude()));
		return o.toJSONString();
	}
	
	public static class TwoTuple implements JSONAware {
		public double x;
		public double y;

		public TwoTuple(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public String toString() {
			return String.format(Locale.US, "(%.8f, %.8f)", x, y);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public String toJSONString() {
			JSONObject o = new JSONObject();
			o.put("lat", new Double(x));
			o.put("lon", new Double(y));
			return o.toJSONString();
		}
	}
}
