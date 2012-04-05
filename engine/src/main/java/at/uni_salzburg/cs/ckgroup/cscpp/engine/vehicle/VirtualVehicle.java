/*
 * @(#) VirtualVehicle.java
 *
 * This code is part of the ESE CPCC project.
 * Copyright (c) 2011  Clemens Krainer, Andreas Schroecker, Bernhard Zechmeister
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.ActionPicture;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Parser;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Position;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Scanner;

public class VirtualVehicle extends AbstractVirtualVehicle {
	
	Logger LOG = Logger.getLogger(VirtualVehicle.class);
	
	private boolean programCorrupted = true;
        
    private ListIterator<Command> listIter;
	private List<Command> commandList;
    private Command currentCommand;

    private IGeodeticSystem geodeticSystem = new WGS84();
    private String postponedLogging = null;
    
	/**
	 * Construct a virtual vehicle.
	 * 
	 * @param workDir the working directory of this virtual vehicle.
	 * @throws IOException thrown in case of missing files or folders.
	 */
	public VirtualVehicle (File workDir) throws IOException
	{
		super(workDir);
		
		boolean parse = !readVehicleState();
		
		try 
		{
			currentCommand = null;
			
			if (parse)
			{
				Scanner sc = new Scanner(program.getAbsolutePath());
				Parser pa = new Parser(dataDir);
	
				commandList = pa.run(sc);
			}
			
			listIter = commandList.listIterator();
			
			// get first command which have to be executed
			while (listIter.hasNext())
			{
				currentCommand = listIter.next();
				
				if (!currentCommand.isFinished())
					break;
			}
			
			if (currentCommand==null || currentCommand.isFinished()) {
				currentCommand = null;
				setCompleted();
			}
			
			programCorrupted = false;

			storeVehicleState();
		} 
		catch(Exception e)
		{
			addLogEntry("Vehicle " + workDir.getName() + " is corrupt. Execution refused.", e);
			LOG.error("Vehicle " + workDir.getName() + " is corrupt. Execution refused.", e);
		}

	}

	@SuppressWarnings("unchecked")
	private boolean readVehicleState()
	{
		if (!vehicleStatus.exists() || vehicleStatus.length() == 0) {
			LOG.info("Vehicle " + workDir + " has no state yet. Reading state cancelled.");
			return false;
		}
		
		try 
		{
			FileInputStream fin = new FileInputStream(vehicleStatus);
			ObjectInputStream objStream = new ObjectInputStream(fin);
			commandList = (List<Command>)objStream.readObject();
			for (Command cmd : commandList) {
				for (IAction action : cmd.getActions()) {
					if (action instanceof ActionPicture) {
						((ActionPicture)action).setDataDir(dataDir);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			LOG.error("Error at reading state of vehicle " + workDir + " from file.", e);
			return false;
		}
		
		return true;
	}
	
	private void storeVehicleState()
	{
		super.saveState();
		try 
		{
			FileOutputStream fout = new FileOutputStream(vehicleStatus);
			ObjectOutputStream objStream = new ObjectOutputStream(fout);
			objStream.writeObject(commandList);
		} 
		catch (IOException e) 
		{
			LOG.error("Error at storing state of vehicle " + workDir + " to file.", e);
		}
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.AbstractVirtualVehicle#execute()
	 */
	@Override
	public void execute() 
	{
		//get current position
		PolarCoordinate currentPosition = sensorProxy.getCurrentPosition();
		Double altitudeOverGround = sensorProxy.getAltitudeOverGround();
		if (currentPosition == null || isCompleted() || currentCommand == null || altitudeOverGround == null)
			return;
		currentPosition = new PolarCoordinate(currentPosition.getLatitude(), currentPosition.getLongitude(), altitudeOverGround.doubleValue());
		CartesianCoordinate currentPosCartesian = geodeticSystem.polarToRectangularCoordinates(currentPosition);
		
		if (postponedLogging != null) {
			logVehiclePosition(postponedLogging, currentPosition.getLatitude(), currentPosition.getLongitude(), altitudeOverGround);
			postponedLogging = null;
		}
		
		//check if command position is reached
		Position position = currentCommand.getPosition();
		PolarCoordinate commandPosition = position.getPoint();
		CartesianCoordinate commandPosCartesian = geodeticSystem.polarToRectangularCoordinates(commandPosition);
		double distance = commandPosCartesian.subtract(currentPosCartesian).norm();
		
		if (distance <= position.getTolerance()) {
			currentCommand.execute(sensorProxy);
			
			if (currentCommand.isFinished()) {
				if (listIter.hasNext()) {
					currentCommand = listIter.next();
				} else {
					currentCommand = null;
					try {
						setCompleted();
					} catch (IOException e) {
						LOG.error("Can not set vehicle " + getWorkDir() + " to completed.", e);
					}
				}
			}

			storeVehicleState();
		}
		
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getCommandList()
	 */
	@Override
	public List<Command> getCommandList() {
		return commandList;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getCurrentCommandIndex()
	 */
	@Override
	public int getCurrentCommandIndex() {
		if (listIter == null || commandList == null || currentCommand == null)
			return -1;
		
		int next = listIter.nextIndex();
		return next > commandList.size() ? -1 : next-1;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getCurrentCommand()
	 */
	@Override
	public Command getCurrentCommand() {
		return currentCommand;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#isProgramCorrupted()
	 */
	@Override
	public boolean isProgramCorrupted() {
		return programCorrupted;
	}

	@Override
	public void logVehiclePosition(String action) {
		PolarCoordinate currentPosition = sensorProxy.getCurrentPosition();
		Double altitudeOverGround = sensorProxy.getAltitudeOverGround();
		if (currentPosition == null || altitudeOverGround == null) {
			postponedLogging = action;
			return;
		}
		logVehiclePosition(action,currentPosition.getLatitude(), currentPosition.getLongitude(), altitudeOverGround);
	}
	
	private void logVehiclePosition(String action, double latitude, double longitude, double altitude) {
		String msg = String.format(Locale.US, "%s at (%.8f,%.8f,%.3f)", action, latitude, longitude, altitude);
		try {
			addLogEntry(msg);
		} catch (IOException e) {
			LOG.error("Can not write to virtual vehicle log: " + msg, e);
		}
	}

}
