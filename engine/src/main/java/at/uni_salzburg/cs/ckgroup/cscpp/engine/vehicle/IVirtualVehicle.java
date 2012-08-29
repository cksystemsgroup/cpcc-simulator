/*
 * @(#) IVirtualVehicle.java
 *
 * This code is part of the ESE CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Task;


public interface IVirtualVehicle {

	/**
	 * Suspend the currently running program.
	 */
	void suspend () throws IOException ;
	
	/**
	 * Resume a suspended program.
	 */
	void resume () throws IOException ;
	
	/**
	 * @param out the virtual vehicle as an <code>OutputStream</code> object.
	 */
	void serialize (OutputStream out) throws IOException ;
	
	/**
	 * @return true if this virtual vehicle executes tasks, false if it is
	 *         suspended.
	 */
	boolean isActive();

	/**
	 * @return true if this virtual vehicle succeeded in performing all tasks.
	 */
	boolean isCompleted();
	
	/**
	 * @return true if this virtual vehicle is frozen, i.e., suspended and
	 *         invisible to the mapper.
	 */
	boolean isFrozen();
	
	/**
	 * Use true to freeze this virtual vehicle, i.e., suspend it and make it
	 * invisible to the mapper. <B>Note:</B> Unfreezing a virtual vehicle does
	 * not necessarily resume the vehicle.
	 * 
	 * @param frozen
	 *            true freezes this vehicle, false unfreezes it.
	 * @throws IOException
	 *             thrown in case suspend() fails.
	 */
	void setFrozen(boolean frozen) throws IOException;
	
	/**
	 * @return the working directory of this virtual vehicle. It contains the
	 *         virtual vehicle program, a set of properties, a log of events,
	 *         and a sub-directory containing all collected sensor data.
	 */
	File getWorkDir();
	
	/**
	 * @return the data directory of this virtual vehicle. It contains all
	 *         collected sensor data.
	 */
	File getDataDir();
	
	/**
	 * @return the current vehicle log as a <code>String</code> object.
	 */
	String getLog() throws IOException;
	
	/**
	 * @return the properties of this virtual vehicle.
	 */
	Properties getProperties();
	
	/**
	 * @return the virtual vehicle tasks as a list of strings.
	 */
	List<Task> getTaskList();

	/**
	 * @return the index of the current task in the task list.
	 */
	int getCurrentTaskIndex();
	
	/**
	 * @return the currently executing task.
	 */
	Task getCurrentTask();
	
	/**
	 * @return the current program is corrupted.
	 */
	boolean isProgramCorrupted();
	
	/**
	 * @return get the list of files in the data folder.
	 */
	String[] getDataFileNames();
	
	/**
	 * Add an entry to the virtual vehicle log.
	 * @param entry the message to be logged.
	 * @throws IOException in case of logging fails.
	 */
	void addLogEntry(String entry) throws IOException;
	
	/**
	 * Save the state of this virtual vehicle.
	 */
	void saveState();
	
	/**
	 * Save the virtual vehicle properties.
	 * 
	 * @throws IOException in case of I/O errors.
	 */
	void saveProperties();
	
	/**
	 * Mark this virtual vehicle as incomplete.
	 * 
	 * @throws IOException in case of resuming this virtual vehicle fails.
	 */
	void setIncomplete() throws IOException;

}
