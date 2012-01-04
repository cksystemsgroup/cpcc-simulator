/*
 * @(#) VirtualVehicle.java
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
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;
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
	public VirtualVehicle (File workDir) throws IOException {
		super(workDir);
		
		// TODO read vehicle state from file. Use vehicleStatus as file name.
		
		try 
		{
			currentCommand = null;
			
			Scanner sc = new Scanner(program.getAbsolutePath());
			Parser pa = new Parser();
	
			commandList = pa.run(sc);
			listIter = commandList.listIterator();
			
			if (listIter.hasNext())
				currentCommand = listIter.next();
			else
				completed = true;
			
			programCorrupted = false;
		} 
		catch(Exception e)
		{
			LOG.error(e.getMessage());
		}

		// TODO store current vehicle state in a file. Use vehicleStatus as file name.
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.AbstractVirtualVehicle#execute()
	 */
	@Override
	public void execute() 
	{
		PolarCoordinate currentPosition = sensorProxy.getCurrentPosition();
		if (currentPosition == null || completed || currentCommand == null)
			return;
		
		CartesianCoordinate currentPosCartesian = geodeticSystem.polarToRectangularCoordinates(currentPosition);
		
		Position pos = currentCommand.get_position();
		PolarCoordinate commandPosition = pos.getPt();
		CartesianCoordinate commandPosCartesian = geodeticSystem.polarToRectangularCoordinates(commandPosition);
		
		double distance = commandPosCartesian.subtract(currentPosCartesian).norm();
		
		// Check if p is in the tolerance area of pos
		// @Clemens -> Do we have a function to calculate the differnce im meters of 
		// two points in polar coordinates ?
		// TODO yes we have. See code above.
		
		boolean at_pos = distance <= pos.getTolerance();
		
		if (at_pos)
		{
			currentCommand.execute(sensorProxy);
			
			if (listIter.hasNext())
				currentCommand = listIter.next();
			else 
				completed = true;
		}
		
		// TODO store current vehicle state in a file after each command execution. Use vehicleStatus as file name.

		
		

//		Double altitudeOverGround = sensorProxy.getAltitudeOverGround();
//		Double courseOverGround = sensorProxy.getCourseOverGround();
//		Double speedOverGround = sensorProxy.getSpeedOverGround();
//		Double airPressure = sensorProxy.getSensorValueAsDouble(SensorProxy.SENSOR_NAME_AIR_PRESSURE);
//		Integer random = sensorProxy.getSensorValueAsInteger(SensorProxy.SENSOR_NAME_RANDOM);
//		String sonar = sensorProxy.getSensorValue(SensorProxy.SENSOR_NAME_SONAR);
//		InputStream photo = sensorProxy.getSensorValueAsStream(SensorProxy.SENSOR_NAME_PHOTO);
//		
//		LOG.error("VirtualVehicle.execute() is not implemented yet! position=" + p +
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
