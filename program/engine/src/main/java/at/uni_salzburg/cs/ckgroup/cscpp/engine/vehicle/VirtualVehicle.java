/*
 * @(#) VirtualVehicle.java
 *
 * This code is part of the JNavigator project.
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
	
	private List<Command> commandList;
    private boolean programCorrupted = true;
        
    private ListIterator<Command> listIter;
    private Command currentCommand;

    private IGeodeticSystem geodeticSystem = new WGS84();
    
    
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
				
				if (!currentCommand.is_finished())
					break;
			}
			
			if (currentCommand==null || currentCommand.is_finished())
				completed = true;
			
			programCorrupted = false;

			storeVehicleState();
		} 
		catch(Exception e)
		{
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
				for (IAction action : cmd.get_actions()) {
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
		PolarCoordinate currentPosition = sensorProxy.getCurrentPosition();
		Double altitudeOverGround = sensorProxy.getAltitudeOverGround();
		if (currentPosition == null || completed || currentCommand == null || altitudeOverGround == null)
			return;
		
		currentPosition.setAltitude(altitudeOverGround.doubleValue());
		CartesianCoordinate currentPosCartesian = geodeticSystem.polarToRectangularCoordinates(currentPosition);
		
		Position pos = currentCommand.get_position();
		PolarCoordinate commandPosition = pos.getPt();
		CartesianCoordinate commandPosCartesian = geodeticSystem.polarToRectangularCoordinates(commandPosition);
		
		double distance = commandPosCartesian.subtract(currentPosCartesian).norm();
		boolean at_pos = distance <= pos.getTolerance();
		
		if (at_pos)
		{
			currentCommand.execute(sensorProxy);
			
			if (listIter.hasNext())
				currentCommand = listIter.next();
			else 
				completed = true;
		}
		
		storeVehicleState();
		
		
//		Double altitudeOverGround = sensorProxy.getAltitudeOverGround();
//		Double courseOverGround = sensorProxy.getCourseOverGround();
//		Double speedOverGround = sensorProxy.getSpeedOverGround();
//		Double airPressure = sensorProxy.getSensorValueAsDouble(SensorProxy.SENSOR_NAME_AIR_PRESSURE);
//		Integer random = sensorProxy.getSensorValueAsInteger(SensorProxy.SENSOR_NAME_RANDOM);
//		String sonar = sensorProxy.getSensorValue(SensorProxy.SENSOR_NAME_SONAR);
//		InputStream photo = sensorProxy.getSensorValueAsStream(SensorProxy.SENSOR_NAME_PHOTO);
//		
//		LOG.error("VirtualVehicle.execute() is not implemented yet! position=" + currentPosition +
//				", alt=" + altitudeOverGround + ", course=" + courseOverGround +
//				", speed=" + speedOverGround
//				);
	}
	
	
	public VirtualVehicleState getState() {
		VirtualVehicleState vss = new VirtualVehicleState();

		for (Command cmd : commandList) {
			if (cmd.is_finished())
				vss.CommandsExecuted++;
			else
				vss.CommandsToExecute++;
		}

		return vss;
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
		if (listIter == null || commandList == null)
			return -1;
		
		int next = listIter.nextIndex();
		return next == commandList.size() ? -1 : next-1;
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

}
