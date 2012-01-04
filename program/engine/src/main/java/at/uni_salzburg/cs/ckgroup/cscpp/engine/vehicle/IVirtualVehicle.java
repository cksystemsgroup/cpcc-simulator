/*
 * @(#) IVirtualVehicle.java
 *
 * This code is part of the JNavigator project.
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

import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;

public interface IVirtualVehicle {

	/**
	 * Suspend the currently running program.
	 */
	public void suspend () throws IOException ;
	
	/**
	 * Resume a suspended program.
	 */
	public void resume () throws IOException ;
	
	/**
	 * @param out the virtual vehicle as an <code>OutputStream</code> object.
	 */
	public void serialize (OutputStream out) throws IOException ;
	
	/**
	 * @return true if this virtual vehicle executes tasks, false if it is suspended.
	 */
	public boolean isActive();

	/**
	 * @return true if this virtual vehicle succeeded in performing all tasks.
	 */
	public boolean isCompleted();
	
	/**
	 * @return the working directory of this virtual vehicle. It contains the
	 *         virtual vehicle program, a set of properties, a log of events,
	 *         and a sub-directory containing all collected sensor data.
	 */
	public File getWorkDir();
	
	/**
	 * @return the data directory of this virtual vehicle. It contains all
	 *         collected sensor data.
	 */
	public File getDataDir();
	
	/**
	 * @return the properties of this virtual vehicle.
	 */
	public Properties getProperties();
	
	/**
	 * @return the virtual vehicle commands as a list of strings.
	 */
	public List<Command> getCommandList();

	/**
	 * @return the index of the current command in the command list.
	 */
	public int getCurrentCommandIndex();
	
	/**
	 * @return the currently executing command.
	 */
	public Command getCurrentCommand();
	
	/**
	 * @return the current program is corrupted.
	 */
	public boolean isProgramCorrupted();
	
	/**
	 * @return get the list of files in the data folder.
	 */
	public String[] getDataFileNames();


	
}
