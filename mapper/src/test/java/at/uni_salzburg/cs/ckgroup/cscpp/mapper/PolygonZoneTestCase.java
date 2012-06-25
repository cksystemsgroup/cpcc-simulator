/*
 * @(#) PolygonZoneTestCase.java
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

import junit.framework.Assert;

import org.junit.Test;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.PolygonZone;

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

		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(5, cog.getLatitude(), 1E-9);
		Assert.assertEquals(5, cog.getLongitude(), 1E-9);
		
		Assert.assertTrue(pz.isInside(v[0].x, v[0].y));
		Assert.assertTrue(pz.isInside(v[1].x, v[1].y));
		Assert.assertTrue(pz.isInside(v[2].x, v[2].y));
		Assert.assertTrue(pz.isInside(v[3].x, v[3].y));

		Assert.assertTrue(pz.isInside(5,5));
		Assert.assertTrue(pz.isInside(2.5,2.5));
		Assert.assertTrue(pz.isInside(2.5,7.5));
		Assert.assertTrue(pz.isInside(7.5,2.5));
		Assert.assertTrue(pz.isInside(7.5,7.5));
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

		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(5, cog.getLatitude(), 1E-9);
		Assert.assertEquals(5, cog.getLongitude(), 1E-9);
		
		Assert.assertTrue(pz.isInside(v[0].x, v[0].y));
		Assert.assertTrue(pz.isInside(v[1].x, v[1].y));
		Assert.assertTrue(pz.isInside(v[2].x, v[2].y));
		Assert.assertTrue(pz.isInside(v[3].x, v[3].y));

		Assert.assertTrue(pz.isInside(5,5));
		Assert.assertTrue(pz.isInside(2.5,2.5));
		Assert.assertTrue(pz.isInside(2.5,7.5));
		Assert.assertTrue(pz.isInside(7.5,2.5));
		Assert.assertTrue(pz.isInside(7.5,7.5));
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

		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(5, cog.getLatitude(), 1E-9);
		Assert.assertEquals(5, cog.getLongitude(), 1E-9);
		
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

		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(5, cog.getLatitude(), 1E-9);
		Assert.assertEquals(5, cog.getLongitude(), 1E-9);
		
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
		PolarCoordinate[] v = new PolarCoordinate[5];
		v[0] = new PolarCoordinate(48.0, 13.0, 0);
		v[1] = new PolarCoordinate(50.0, 13.0, 0);
		v[2] = new PolarCoordinate(50.0, 12.0, 0);
		v[3] = new PolarCoordinate(48.0, 12.0, 0);
		v[4] = new PolarCoordinate(48.0, 13.0, 0);
		
		PolygonZone pz = new PolygonZone(v);
		
		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(49, cog.getLatitude(), 1E-9);
		Assert.assertEquals(12.5, cog.getLongitude(), 1E-9);
		
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
		PolarCoordinate[] v = new PolarCoordinate[5];
		v[0] = new PolarCoordinate(48.0, 13.0, 0);
		v[1] = new PolarCoordinate(48.0, 12.0, 0);
		v[2] = new PolarCoordinate(50.0, 12.0, 0);
		v[3] = new PolarCoordinate(50.0, 13.0, 0);
		v[4] = new PolarCoordinate(48.0, 13.0, 0);
		
		PolygonZone pz = new PolygonZone(v);

		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(49, cog.getLatitude(), 1E-9);
		Assert.assertEquals(12.5, cog.getLongitude(), 1E-9);

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
	public void testCase07 () {
		PolarCoordinate[] v = new PolarCoordinate[7];
		v[0] = new PolarCoordinate(47.82213832,13.04132304,0.00);
		v[1] = new PolarCoordinate(47.82166287,13.04126403,0.00);
		v[2] = new PolarCoordinate(47.82157283,13.04209551,0.00);
		v[3] = new PolarCoordinate(47.82206268,13.04235300,0.00);
		v[4] = new PolarCoordinate(47.82235083,13.04188630,0.00);
		v[5] = new PolarCoordinate(47.82215273,13.04136971,0.00);
		v[6] = new PolarCoordinate(47.82213832,13.04132304,0.00);

		
		PolygonZone pz = new PolygonZone(v);

		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(47.82193751800164, cog.getLatitude(), 1E-9);
		Assert.assertEquals(13.04178658337083, cog.getLongitude(), 1E-9);

		Assert.assertTrue(pz.isInside(v[0]));
		Assert.assertTrue(pz.isInside(v[1]));
		Assert.assertTrue(pz.isInside(v[2]));
		Assert.assertTrue(pz.isInside(v[3]));
		
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82201586,13.04122648,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82174572,13.04135522,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82159804,13.04160199,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82154401,13.04198286,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82161965,13.04205796,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82162685,13.04216525,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82180335,13.04218134,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82198704,13.04237983,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82205548,13.04229400,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82211671,13.04232082,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82223917,13.04199895,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82235443,13.04195067,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82229320,13.04157516,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82209510,13.04137766,0.00)));
	}
	
	@Test
	public void testCase08 () {
		PolarCoordinate[] v = new PolarCoordinate[7];
		v[6] = new PolarCoordinate(47.82213832,13.04132304,0.00);
		v[5] = new PolarCoordinate(47.82166287,13.04126403,0.00);
		v[4] = new PolarCoordinate(47.82157283,13.04209551,0.00);
		v[3] = new PolarCoordinate(47.82206268,13.04235300,0.00);
		v[2] = new PolarCoordinate(47.82235083,13.04188630,0.00);
		v[1] = new PolarCoordinate(47.82215273,13.04136971,0.00);
		v[0] = new PolarCoordinate(47.82213832,13.04132304,0.00);
		
		PolygonZone pz = new PolygonZone(v);

		PolarCoordinate cog = pz.getDepotPosition();
		Assert.assertEquals(47.82193751800164, cog.getLatitude(), 1E-9);
		Assert.assertEquals(13.04178658337083, cog.getLongitude(), 1E-9);
		
		Assert.assertTrue(pz.isInside(v[0]));
		Assert.assertTrue(pz.isInside(v[1]));
		Assert.assertTrue(pz.isInside(v[2]));
		Assert.assertTrue(pz.isInside(v[3]));
		
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82201586,13.04122648,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82174572,13.04135522,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82159804,13.04160199,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82154401,13.04198286,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82161965,13.04205796,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82162685,13.04216525,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82180335,13.04218134,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82198704,13.04237983,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82205548,13.04229400,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82211671,13.04232082,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82223917,13.04199895,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82235443,13.04195067,0.00)));
		Assert.assertFalse(pz.isInside(new PolarCoordinate(47.82229320,13.04157516,0.00)));
		Assert.assertTrue (pz.isInside(new PolarCoordinate(47.82209510,13.04137766,0.00)));
	}
}
