/*
 * @(#) ITask.java
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

import java.util.List;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public interface ITask {

	public PolarCoordinate getPosition();
	
	public Double getTolerance();
	
	public long getArrivalTime();
	
	public long getActivationTime();
	
	public void setActivationTime(long now);

	public long getDelayTime();

	public void setDelayTime(long l);
	
	public long getLifeTime();
	
	public void setLifeTime(long lifeTime);
	
	public List<IAction> getActionList();
	
	public boolean isComplete();
	
	public void execute(ISensorProxy sprox);
	
}
