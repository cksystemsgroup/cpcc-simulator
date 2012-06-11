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
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.algorithms;

import java.util.Locale;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public class PolygonZone implements IZone {
	
	private IGeodeticSystem geodeticSystem;
	private TwoTuple[] vertices;
	private boolean positiveOrientation = true;
	private TwoTuple cog;
	
	public PolygonZone(PolarCoordinate[] v, IGeodeticSystem geodeticSystem) {
		this.geodeticSystem = geodeticSystem;
		
		vertices = new TwoTuple[v.length];
		
		for (int k=0, l=v.length; k < l; ++k) {
			CartesianCoordinate c = geodeticSystem.polarToRectangularCoordinates(v[k]);
			vertices[k] = new TwoTuple(c.x, c.y);
		}
		
		cog = getCenterOfGravity();
		positiveOrientation = isInside(cog.x, cog.y);
		System.out.println(this.toString());
	}
	
	public PolygonZone(TwoTuple[] vertices) {
		this.vertices = vertices;
		cog = getCenterOfGravity();
		positiveOrientation = isInside(cog.x, cog.y);
		System.out.println(this.toString());
	}

	public boolean isInside(PolarCoordinate p) {
		CartesianCoordinate c = geodeticSystem.polarToRectangularCoordinates(p.getLatitude(), p.getLongitude(), 0);
		return isInside(c.x, c.y);
	}
	
	private TwoTuple getCenterOfGravity () {
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
		
		return new TwoTuple(x/sixTimesArea, y/sixTimesArea);
	}
	
	public boolean isInside(double cx, double cy) {
		int right=0;
		int left=0;
		for (int k=0, l=vertices.length; k < l; ++k) {
			TwoTuple a = vertices[k];
			TwoTuple b = k+1 == l ? vertices[0] : vertices[k+1];
			double d = (cy - a.y) * (b.x - a.x) - (cx - a.x) * (b.y - a.y);
			if (d > 0) ++left; else if (d < 0) ++right;
			if (left > 0 && right > 0)
				return false;
		}
		
		return right > left ^ positiveOrientation;
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

		return String.format(Locale.US, "vertices: %s, cog=%s, orientation=%s", b.toString(), cog.toString(), positiveOrientation ? "positive" : "negative");
	}
	
	public static class TwoTuple {
		public double x;
		public double y;

		public TwoTuple(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public String toString() {
			return String.format(Locale.US, "(%.2f, %.2f)", x, y);
		}
	}
}
