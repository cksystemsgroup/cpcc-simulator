/*
 * @(#) VehicleStatusTestCase.java
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class VehicleStatusTestCase {

	@Test
	public void testCase01() throws ParseException {
		String status = "{\"vehicle.id\":\"b2345527-2796-4ea9-b500-d842d6f2b638\",\"state\":\"active\",\"latitude\":47.82211311,\"longitude\":13.04076076,\"altitude\":30.0," +
				"\"tolerance\":1.2,\"actions\":\"photo\"}";
		JSONParser parser = new JSONParser();
		VehicleStatus s = new VehicleStatus((JSONObject)parser.parse(status));
		Assert.assertEquals("b2345527-2796-4ea9-b500-d842d6f2b638", s.getId());
		Assert.assertEquals("active", s.getState().toString().toLowerCase());
		Assert.assertEquals(47.82211311, s.getPosition().getLatitude(), 1E-9);
		Assert.assertEquals(13.04076076, s.getPosition().getLongitude(), 1E-9);
		Assert.assertEquals(30.0, s.getPosition().getAltitude(), 1E-9);
		Assert.assertEquals(1.2, s.getTolerance(), 1E-9);
		Assert.assertArrayEquals(new String[]{"photo"}, s.getActions());
		
		s = new VehicleStatus((JSONObject)parser.parse(s.toJSONString()));
		Assert.assertEquals("b2345527-2796-4ea9-b500-d842d6f2b638", s.getId());
		Assert.assertEquals("active", s.getState().toString().toLowerCase());
		Assert.assertEquals(47.82211311, s.getPosition().getLatitude(), 1E-9);
		Assert.assertEquals(13.04076076, s.getPosition().getLongitude(), 1E-9);
		Assert.assertEquals(30.0, s.getPosition().getAltitude(), 1E-9);
		Assert.assertEquals(1.2, s.getTolerance(), 1E-9);
		Assert.assertArrayEquals(new String[]{"photo"}, s.getActions());
		
	}

	@Test
	public void testCase02() throws ParseException {
		String status = "{\"vehicle.id\":\"732d463b-6836-487c-989d-47c59285c17d\",\"state\":\"corrupt\",\"latitude\":47.8226984,\"longitude\":13.04211393,\"altitude\":25.0," +
				"\"tolerance\":12.0,\"actions\":\"photo,temperature\"}";
		JSONParser parser = new JSONParser();
		VehicleStatus s = new VehicleStatus((JSONObject)parser.parse(status));
		Assert.assertEquals("732d463b-6836-487c-989d-47c59285c17d", s.getId());
		Assert.assertEquals("corrupt", s.getState().toString().toLowerCase());
		Assert.assertNull(s.getPosition());
		Assert.assertEquals(0.0, s.getTolerance(), 1E-9);
		Assert.assertNull(s.getActions());
		
		s = new VehicleStatus((JSONObject)parser.parse(s.toJSONString()));
		Assert.assertEquals("732d463b-6836-487c-989d-47c59285c17d", s.getId());
		Assert.assertEquals("corrupt", s.getState().toString().toLowerCase());
		Assert.assertNull(s.getPosition());
		Assert.assertEquals(0.0, s.getTolerance(), 1E-9);
		Assert.assertNull(s.getActions());
	}
	
	@Test
	public void testCase03() throws ParseException {
		String status = "{\"vehicle.id\":\"c2345527-2796-4ea9-b500-d842d6f2b638\",\"state\":\"suspended\",\"latitude\":48.82211311,\"longitude\":18.04076076,\"altitude\":38.0," +
				"\"tolerance\":8.2,\"actions\":\"temperature\"}";
		JSONParser parser = new JSONParser();
		VehicleStatus s = new VehicleStatus((JSONObject)parser.parse(status));
		Assert.assertEquals("c2345527-2796-4ea9-b500-d842d6f2b638", s.getId());
		Assert.assertEquals("suspended", s.getState().toString().toLowerCase());
		Assert.assertEquals(48.82211311, s.getPosition().getLatitude(), 1E-9);
		Assert.assertEquals(18.04076076, s.getPosition().getLongitude(), 1E-9);
		Assert.assertEquals(38.0, s.getPosition().getAltitude(), 1E-9);
		Assert.assertEquals(8.2, s.getTolerance(), 1E-9);
		Assert.assertArrayEquals(new String[]{"temperature"}, s.getActions());
		
		s = new VehicleStatus((JSONObject)parser.parse(s.toJSONString()));
		Assert.assertEquals("c2345527-2796-4ea9-b500-d842d6f2b638", s.getId());
		Assert.assertEquals("suspended", s.getState().toString().toLowerCase());
		Assert.assertEquals(48.82211311, s.getPosition().getLatitude(), 1E-9);
		Assert.assertEquals(18.04076076, s.getPosition().getLongitude(), 1E-9);
		Assert.assertEquals(38.0, s.getPosition().getAltitude(), 1E-9);
		Assert.assertEquals(8.2, s.getTolerance(), 1E-9);
		Assert.assertArrayEquals(new String[]{"temperature"}, s.getActions());
	}
	
	@Test
	public void testCase04() throws ParseException {
		String status = "{\"vehicle.id\":\"532d463b-6836-487c-989d-47c59285c17d\",\"state\":\"complete\",\"latitude\":57.8226984,\"longitude\":15.04211393,\"altitude\":5.0," +
				"\"tolerance\":52.0,\"actions\":\"temperature,photo\"}";
		JSONParser parser = new JSONParser();
		VehicleStatus s = new VehicleStatus((JSONObject)parser.parse(status));
		Assert.assertEquals("532d463b-6836-487c-989d-47c59285c17d", s.getId());
		Assert.assertEquals("complete", s.getState().toString().toLowerCase());
		Assert.assertEquals(57.8226984, s.getPosition().getLatitude(), 1E-9);
		Assert.assertEquals(15.04211393, s.getPosition().getLongitude(), 1E-9);
		Assert.assertEquals(5.0, s.getPosition().getAltitude(), 1E-9);
		Assert.assertEquals(52.0, s.getTolerance(), 1E-9);
		Assert.assertArrayEquals(new String[]{"temperature","photo"}, s.getActions());
		
		s = new VehicleStatus((JSONObject)parser.parse(s.toJSONString()));
		Assert.assertEquals("532d463b-6836-487c-989d-47c59285c17d", s.getId());
		Assert.assertEquals("complete", s.getState().toString().toLowerCase());
		Assert.assertEquals(57.8226984, s.getPosition().getLatitude(), 1E-9);
		Assert.assertEquals(15.04211393, s.getPosition().getLongitude(), 1E-9);
		Assert.assertEquals(5.0, s.getPosition().getAltitude(), 1E-9);
		Assert.assertEquals(52.0, s.getTolerance(), 1E-9);
		Assert.assertArrayEquals(new String[]{"temperature","photo"}, s.getActions());
	}
}
