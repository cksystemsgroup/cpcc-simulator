/*
 * @(#) MigrationRunnable.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IMapper;

public class MigrationRunnable implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(MigrationRunnable.class);
	
	
	private IMapper mapper;
	private String engineUrl;
	private String vehicleName;
	private String targetEngineUrl;

	MigrationRunnable(IMapper mapper, String engineUrl, String vehicleName, String targetEngineUrl) {
		this.mapper = mapper;
		this.engineUrl = engineUrl;
		this.vehicleName = vehicleName;
		this.targetEngineUrl = targetEngineUrl;
	}

	@Override
	public void run() {
		LOG.info("Migrating vehicle " + vehicleName + " from " + engineUrl + " to " + targetEngineUrl);
		mapper.migrate(engineUrl, vehicleName, targetEngineUrl);
	}
}
