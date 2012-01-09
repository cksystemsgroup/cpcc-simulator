/*
 * @(#) MappingAlgrithmBuilder.java
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

import java.util.Map;

import javax.servlet.ServletException;

import at.uni_salzburg.cs.ckgroup.cscpp.mapper.RegData;

public class MappingAlgrithmBuilder {
	
	public static final String SIMPLE = "simple";
	public static final String RANDOM = "random";
	
	public static IMappingAlgorithm build(String algorithmName, Map<String, RegData> regdata) throws ServletException {
		
		AbstractMappingAlgorithm algorithm = null;
		
		if (SIMPLE.equals(algorithmName))
			algorithm = new SimpleMappingAlgorithm();
		else if (RANDOM.equals(algorithmName))
			algorithm = new RandomMappingAlgorithm();
		
		if (algorithm == null)
			throw new ServletException("Unknown mapping algorithm: " + algorithmName);
		
		if (algorithm != null) {
			algorithm.setRegistrationData(regdata);
			algorithm.start();
		}
		
		return algorithm;
	}

}
