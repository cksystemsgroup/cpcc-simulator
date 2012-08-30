/*
 * @(#) AbstractZone.java
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

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IZone;

public abstract class AbstractZone implements IZone {
	
	private PolarCoordinate depotPosition;
	private String zoneEngineUrl;
	private Group zoneGroup; 
	
	@Override
	public PolarCoordinate getDepotPosition() {
		return depotPosition;
	}

	@Override
	public void setDepotPosition(PolarCoordinate depotPosition) {
		this.depotPosition = depotPosition;
	}

	@Override
	public String getZoneEngineUrl() {
		return zoneEngineUrl;
	}

	@Override
	public void setZoneEngineUrl(String zoneEngineUrl) {
		this.zoneEngineUrl = zoneEngineUrl;
	}

	@Override
	public Group getZoneGroup() {
		return zoneGroup;
	}

	@Override
	public void setZoneGroup(Group zoneGroup) {
		this.zoneGroup = zoneGroup;

	}

}
