/*
 * @(#) Configuration.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2011  Clemens Krainer
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
package at.uni_salzburg.cs.ckgroup.cscpp.mapper.registry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IRegistrationData;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IWayPoint;
import at.uni_salzburg.cs.ckgroup.cscpp.mapper.course.WayPoint;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public class RegistryPersistenceTestCase {

	@Test
	public void test() throws IOException, ParseException {
		URL storage = RegistryPersistenceTestCase.class.getResource("storage01.dat");
		File storagePath = new File (storage.getPath());
		
		Map<String, IRegistrationData> registrationData = new HashMap<String, IRegistrationData>();
		RegistryPersistence.loadRegistry(storagePath, registrationData);
		IRegistrationData d1 = registrationData.get("http://localhost:8080/engine");
		Assert.assertNotNull(d1);
		String engineUri = d1.getEngineUrl();
		Assert.assertEquals("http://localhost:8080/engine", engineUri);
		
		String pilotUri = d1.getPilotUrl();
		Assert.assertEquals("http://localhost:8080/pilot", pilotUri);
		
		Set<String> sensors = d1.getSensors();
		Assert.assertEquals(7,sensors.size());
		Assert.assertTrue(sensors.contains("sonar"));
		Assert.assertTrue(sensors.contains("position"));
		Assert.assertTrue(sensors.contains("random"));
		Assert.assertTrue(sensors.contains("photo"));
		Assert.assertTrue(sensors.contains("video"));
		Assert.assertTrue(sensors.contains("airPressure"));
		Assert.assertTrue(sensors.contains("temperature"));
		
		
		List<IWayPoint> wayPoints = d1.getWaypoints();
		Assert.assertEquals(17,wayPoints.size());
		
		Assert.assertTrue(wayPoints.get(0) instanceof WayPoint);
		WayPoint wayPoint = (WayPoint)wayPoints.get(0);
		
		System.out.println("test() " + wayPoint.toJSONString());
		
		Assert.assertEquals("{\"precision\":1.0,\"altitude\":1.0,\"velocity\":1.0,\"longitude\":13.04092571,\"latitude\":47.82204197}", wayPoint.toJSONString());
		Assert.assertEquals("(47.82204197, 13.04092571, 1.000) precision 1 velocity 1.0", wayPoints.get(0).toString());
		
		File tmpFile = File.createTempFile("mapper", ".dat");
		RegistryPersistence.storeRegistry(tmpFile, registrationData);
		
		String expected = FileUtils.loadFileAsString(storagePath);
		Assert.assertEquals(1815, expected.length());
		
		String actual = FileUtils.loadFileAsString(tmpFile);
		Assert.assertEquals(1815, actual.length());
		
		Assert.assertEquals(expected, actual);
		tmpFile.delete();
	}

}
