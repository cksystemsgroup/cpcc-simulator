/*
 * @(#) AcoTsp.java
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

import java.util.ArrayList;
import java.util.List;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public class AcoTsp {
	
	/**
	 * This is the index of the vehicle's current position. The calculated path starts at this position.  
	 */
	public static final int START_POINT_INDEX = 0;
	
	/**
	 * This is the index of the vehicle's depot position. The calculated ends at this position.
	 */
	public static final int END_POINT_INDEX = 1;
	
	/**
	 * The geodetic system to be used for coordinate transformations.
	 */
	private IGeodeticSystem geodeticSystem;
	
	/**
	 * Construct a <code>AcoTsp</code> instance.
	 * 
	 * @param geodeticSystem the geodetic system to be used for coordinate transformations.
	 */
	public AcoTsp(IGeodeticSystem geodeticSystem) {
		this.geodeticSystem = geodeticSystem;
	}
	
	/**
	 * Calculate the optimal path from a given list of positions.
	 * 
	 * @param path
	 *            the given list of positions to meet. The first coordinate in
	 *            this list is the current position of the real vehicle and the
	 *            second coordinate is the real vehicle's depot. The remaining
	 *            coordinates are positions the real vehicle has to visit.
	 * @return a list of positions to meet, optimized according to traveling
	 *         distance.
	 */
	public List<PolarCoordinate> calculateBestPathWithDepot(List<PolarCoordinate> path) {
		
		if (path.size() < 2) {
			return path;
		}
		
		List<CartesianCoordinate> cList = new ArrayList<CartesianCoordinate>();
		
		for (PolarCoordinate p : path) {
			CartesianCoordinate c = geodeticSystem.polarToRectangularCoordinates(p);
			c.z = 0;
			cList.add(c);
		}

		int n = path.size();
		double[][] costMatrix = new double[n][n];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				costMatrix[i][j] = cList.get(j).subtract(cList.get(i)).norm();
			}
		}
		costMatrix[START_POINT_INDEX][END_POINT_INDEX] = 0;
		costMatrix[END_POINT_INDEX][START_POINT_INDEX] = 0;
		
		List<Integer> bestPath = AcoTspSimple.calculateBestPath(costMatrix, 2000, 3);
		bestPath = reorderPath(bestPath, START_POINT_INDEX, END_POINT_INDEX);
		
		List<PolarCoordinate> r = new ArrayList<PolarCoordinate>();
		for (Integer k : bestPath) {
			r.add(path.get(k));
		}
		
		return r;
	}
	
	/**
	 * Calculate the optimal path from a given list of positions.
	 * 
	 * @param path
	 *            the given list of positions to meet. The first coordinate in
	 *            this list is the current position of the real vehicle and the
	 *            second coordinate is the real vehicle's depot. The remaining
	 *            coordinates are positions the real vehicle has to visit.
	 * @return a list of positions to meet, optimized according to traveling
	 *         distance.
	 */
	public List<PolarCoordinate> calculateBestPathWithOutDepot(List<PolarCoordinate> path) {
		
		if (path.size() < 2) {
			return path;
		}
		
		List<CartesianCoordinate> cList = new ArrayList<CartesianCoordinate>();
		
		for (PolarCoordinate p : path) {
			CartesianCoordinate c = geodeticSystem.polarToRectangularCoordinates(p);
			c.z = 0;
			cList.add(c);
		}

		int n = path.size();
		double[][] costMatrix = new double[n][n];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				costMatrix[i][j] = cList.get(j).subtract(cList.get(i)).norm();
			}
		}
		List<Integer> bestPath = AcoTspSimple.calculateBestPath(costMatrix, 1000, 3);
		bestPath = reorderPath(bestPath, START_POINT_INDEX, START_POINT_INDEX);
		
		List<PolarCoordinate> r = new ArrayList<PolarCoordinate>();
		for (Integer k : bestPath) {
			r.add(path.get(k));
		}
		
		return r;
	}
	
	/**
	 * Reorder a given path.
	 * 
	 * @param path
	 *            the path to be reordered.
	 * @param first
	 *            the index number of the element to be first element.
	 * @param last
	 *            the index number of the element to be last element.
	 * @return the reordered path.
	 */
	private static List<Integer> reorderPath (List<Integer> path, int first, int last) {
		
		int f = path.indexOf(first);
		int l = path.indexOf(last);
		
		if (f < 0) {
			throw new IllegalStateException("First index " + first + " is not element of the given path " + path.toString());
		}
		
		List<Integer> newPath = new ArrayList<Integer>();
		
		if (l+1 == f) {
			for (int k=0, len=path.size(); k < len; ++k) {
				newPath.add(path.get((k+f) % len));
			}
		} else {
			for (int len=path.size(), k=len; k > 0; --k) {
				newPath.add(path.get((k+f) % len));
			}
		}
		
		return newPath;        
	}

}
