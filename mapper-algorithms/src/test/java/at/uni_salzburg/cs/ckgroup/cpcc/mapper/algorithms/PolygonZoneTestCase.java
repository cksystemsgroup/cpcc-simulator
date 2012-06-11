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

import junit.framework.Assert;

import org.junit.Test;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;

public class PolygonZoneTestCase {

	@Test
	public void testCase01 () {

		PolygonZone.TwoTuple[] v = new PolygonZone.TwoTuple[5];
		v[0] = new PolygonZone.TwoTuple( 0.0,  0.0);
		v[1] = new PolygonZone.TwoTuple(10.0,  0.0);
		v[2] = new PolygonZone.TwoTuple(10.0, 10.0);
		v[3] = new PolygonZone.TwoTuple( 0.0, 10.0);
		v[4] = new PolygonZone.TwoTuple( 0.0,  0.0);
		
		PolygonZone pz = new PolygonZone(v);

		Assert.assertTrue(pz.isInside(v[0].x, v[0].y));
		Assert.assertTrue(pz.isInside(v[1].x, v[1].y));
		Assert.assertTrue(pz.isInside(v[2].x, v[2].y));
		Assert.assertTrue(pz.isInside(v[3].x, v[3].y));

		Assert.assertTrue(pz.isInside(5,5));
		Assert.assertFalse(pz.isInside(-1,5));
		Assert.assertFalse(pz.isInside(11,5));
		Assert.assertFalse(pz.isInside(5,-1));
		Assert.assertFalse(pz.isInside(5,11));
	}

	@Test
	public void testCase02 () {

		PolygonZone.TwoTuple[] v = new PolygonZone.TwoTuple[5];
		v[0] = new PolygonZone.TwoTuple( 0.0,  0.0);
		v[1] = new PolygonZone.TwoTuple( 0.0, 10.0);
		v[2] = new PolygonZone.TwoTuple(10.0, 10.0);
		v[3] = new PolygonZone.TwoTuple(10.0,  0.0);
		v[4] = new PolygonZone.TwoTuple( 0.0,  0.0);
		
		PolygonZone pz = new PolygonZone(v);

		Assert.assertTrue(pz.isInside(v[0].x, v[0].y));
		Assert.assertTrue(pz.isInside(v[1].x, v[1].y));
		Assert.assertTrue(pz.isInside(v[2].x, v[2].y));
		Assert.assertTrue(pz.isInside(v[3].x, v[3].y));

		Assert.assertTrue(pz.isInside(5,5));
		Assert.assertFalse(pz.isInside(-1,5));
		Assert.assertFalse(pz.isInside(11,5));
		Assert.assertFalse(pz.isInside(5,-1));
		Assert.assertFalse(pz.isInside(5,11));
	}
	
	@Test
	public void testCase03 () {

		PolygonZone.TwoTuple[] v = new PolygonZone.TwoTuple[5];
		v[0] = new PolygonZone.TwoTuple( 5.0,  0.0);
		v[1] = new PolygonZone.TwoTuple(10.0,  5.0);
		v[2] = new PolygonZone.TwoTuple( 5.0, 10.0);
		v[3] = new PolygonZone.TwoTuple( 0.0,  5.0);
		v[4] = new PolygonZone.TwoTuple( 5.0,  0.0);

		PolygonZone pz = new PolygonZone(v);

		Assert.assertTrue(pz.isInside(v[0].x, v[0].y));
		Assert.assertTrue(pz.isInside(v[1].x, v[1].y));
		Assert.assertTrue(pz.isInside(v[2].x, v[2].y));
		Assert.assertTrue(pz.isInside(v[3].x, v[3].y));

		Assert.assertTrue(pz.isInside(5,5));
		Assert.assertFalse(pz.isInside(-1,5));
		Assert.assertFalse(pz.isInside(11,5));
		Assert.assertFalse(pz.isInside(5,-1));
		Assert.assertFalse(pz.isInside(5,11));
	}
	
	@Test
	public void testCase04 () {

		PolygonZone.TwoTuple[] v = new PolygonZone.TwoTuple[5];
		v[0] = new PolygonZone.TwoTuple( 5.0,  0.0);
		v[1] = new PolygonZone.TwoTuple( 0.0,  5.0);
		v[2] = new PolygonZone.TwoTuple( 5.0, 10.0);
		v[3] = new PolygonZone.TwoTuple(10.0,  5.0);
		v[4] = new PolygonZone.TwoTuple( 5.0,  0.0);
		
		PolygonZone pz = new PolygonZone(v);

		Assert.assertTrue(pz.isInside(v[0].x, v[0].y));
		Assert.assertTrue(pz.isInside(v[1].x, v[1].y));
		Assert.assertTrue(pz.isInside(v[2].x, v[2].y));
		Assert.assertTrue(pz.isInside(v[3].x, v[3].y));

		Assert.assertTrue(pz.isInside(5,5));
		Assert.assertFalse(pz.isInside(-1,5));
		Assert.assertFalse(pz.isInside(11,5));
		Assert.assertFalse(pz.isInside(5,-1));
		Assert.assertFalse(pz.isInside(5,11));
	}
	
	@Test
	public void testCase05 () {
		WGS84 geodeticSystem = new WGS84();

		PolarCoordinate[] v = new PolarCoordinate[5];
		v[0] = new PolarCoordinate(48.0, 13.0, 0);
		v[1] = new PolarCoordinate(50.0, 13.0, 0);
		v[2] = new PolarCoordinate(50.0, 12.0, 0);
		v[3] = new PolarCoordinate(48.0, 12.0, 0);
		v[4] = new PolarCoordinate(48.0, 13.0, 0);
		
		PolygonZone pz = new PolygonZone(v, geodeticSystem);

		Assert.assertTrue(pz.isInside(v[0]));
		Assert.assertTrue(pz.isInside(v[1]));
		Assert.assertTrue(pz.isInside(v[2]));
		Assert.assertTrue(pz.isInside(v[3]));

		Assert.assertTrue(pz.isInside(new PolarCoordinate(48.5, 12.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.5, 12.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(50.5, 12.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(48.5, 11.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(48.5, 13.5, 0)));
	}
	
	@Test
	public void testCase06 () {
		WGS84 geodeticSystem = new WGS84();

		PolarCoordinate[] v = new PolarCoordinate[5];
		v[0] = new PolarCoordinate(48.0, 13.0, 0);
		v[1] = new PolarCoordinate(48.0, 12.0, 0);
		v[2] = new PolarCoordinate(50.0, 12.0, 0);
		v[3] = new PolarCoordinate(50.0, 13.0, 0);
		v[4] = new PolarCoordinate(48.0, 13.0, 0);
		
		PolygonZone pz = new PolygonZone(v, geodeticSystem);

		Assert.assertTrue(pz.isInside(v[0]));
		Assert.assertTrue(pz.isInside(v[1]));
		Assert.assertTrue(pz.isInside(v[2]));
		Assert.assertTrue(pz.isInside(v[3]));

		Assert.assertTrue(pz.isInside(new PolarCoordinate(48.5, 12.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.5, 12.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(50.5, 12.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(48.5, 11.5, 0)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(48.5, 13.5, 0)));
	}

}
