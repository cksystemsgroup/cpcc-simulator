/*
 * @(#) IMapperThread.java
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

public interface IMapperThread {

	/**
	 * @return true if the mapper thread executes.
	 */
	boolean isRunning();
	
	/**
	 * @return true if the execution of mapping algorithms is suspended.
	 */
	boolean isPaused();

	/**
	 * Terminate the mapper.
	 */
	void terminate();
	
	/**
	 * Suspend the execution of mapping algorithms.
	 */
	void cease();
	
	/**
	 * Resume the execution of mapping algorithms.
	 */
	void proceed();
	
	/**
	 * Invoke the current mapping algorithm once, if the mapping algorithms is
	 * suspended. If the algorithm is not suspended, this method does nothing.
	 */
	void singleStep();
	
	/**
	 * Reset the mapper statistics.
	 */
	void resetStatistics();
	
}
