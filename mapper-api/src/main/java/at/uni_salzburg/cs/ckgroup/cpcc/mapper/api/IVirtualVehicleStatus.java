/*
 * @(#) IVirtualVehicleStatus.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.api;

import java.util.Set;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

/**
 * This interface covers a Virtual Vehicle's status.
 */
public interface IVirtualVehicleStatus {
	
	enum Status {
		NONE, SUSPENDED, ACTIVE, CORRUPT, COMPLETED, FROZEN
	};

	/**
	 * @return the current Engine internal Virtual Vehicle name. Engines use
	 *         this name for migrating Virtual Vehicles to other Engines.
	 */
	String getName();

	/**
	 * @return the Virtual Vehicle identification.
	 */
	String getId();

	/**
	 * @return the current Virtual Vehicle state.
	 */
	Status getState();

	/**
	 * @return the position of the currently active Action Point in polar
	 *         coordinates, i.e., latitude, longitude, and altitude above
	 *         ground.
	 */
	PolarCoordinate getPosition();

	/**
	 * @return the radius of the tolerance sphere all around the currently
	 *         active Action Point.
	 */
	double getTolerance();

	/**
	 * @return the set of actions to be performed at the currently active Action
	 *         Point.
	 */
	Set<String> getActions();
	
	
	/**
	 * @return the Virtual Vehicle's current task or null.
	 */
	ITask getCurrentTask();
}
