/*
 * @(#) AcoTspTestCase.java
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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;

public class AcoTspTestCase {
	
	private static final Logger LOG = Logger.getLogger(AcoTspTestCase.class);

	private final static double[][] path_01 = {
		{ 47.82202216, 13.04082111 },	// start
		{ 47.82200505, 13.04081440 },	// depot
		{ 47.82203567, 13.04263562 },
		{ 47.82196183, 13.04082781 },
		{ 47.82203567, 13.04083988 },
		{ 47.82196183, 13.04263562 },
	};

	private final static double[][] expected_tsp_path_01 = {
		{ 47.82202216, 13.04082111 },	// start
		{ 47.82203567, 13.04083988 },
		{ 47.82203567, 13.04263562 },
		{ 47.82196183, 13.04263562 },
		{ 47.82196183, 13.04082781 },
		{ 47.82200505, 13.04081440 },	// depot
	};
	
	@Test
	public void test01() {
		LOG.info("test01()");
		
		AcoTsp tspSolver = new AcoTsp(new WGS84());
		
		List<PolarCoordinate> p = new ArrayList<PolarCoordinate>();
		for (double[] wp : path_01) {
			p.add(new PolarCoordinate(wp[0], wp[1], 0));
		}
		
		List<PolarCoordinate> tspPath = tspSolver.calculateBestPathWithDepot(p);
		int k=0;
		for (PolarCoordinate c : tspPath) {
			LOG.info(c.toString());
			double e[] = expected_tsp_path_01[k++];
			Assert.assertEquals(c.getLatitude(), e[0], 1E-9);
			Assert.assertEquals(c.getLongitude(), e[1], 1E-9);
		}
	}
	
		
	private final static double[][] path_02 = {
		{47.82209510,13.04159439},	// start
		{47.82153320,13.04159439},  // depot
		{47.82209510,13.04258144},
		{47.82153320,13.04258144},
	};
	
	private final static double[][] expected_tsp_path_02 = {
		{47.82209510,13.04159439},	// start
		{47.82209510,13.04258144},	// depot
		{47.82153320,13.04258144},
		{47.82153320,13.04159439},
	};
	
	
	@Test
	public void test02() {
		LOG.info("test02()");
		
		AcoTsp tspSolver = new AcoTsp(new WGS84());
		
		List<PolarCoordinate> p = new ArrayList<PolarCoordinate>();
		for (double[] wp : path_02) {
			p.add(new PolarCoordinate(wp[0], wp[1], 0));
		}
		
		List<PolarCoordinate> tspPath = tspSolver.calculateBestPathWithDepot(p);
		int k=0;
		for (PolarCoordinate c : tspPath) {
			LOG.info(c.toString());
			double e[] = expected_tsp_path_02[k++];
			Assert.assertEquals(c.getLatitude(), e[0], 1E-9);
			Assert.assertEquals(c.getLongitude(), e[1], 1E-9);
		}
	}
	
}
