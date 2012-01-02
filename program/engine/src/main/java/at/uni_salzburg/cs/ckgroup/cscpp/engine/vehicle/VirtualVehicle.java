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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.SensorProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Position;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Scanner;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Parser;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.ParserException;

public class VirtualVehicle extends AbstractVirtualVehicle {
	
	Logger LOG = Logger.getLogger(VirtualVehicle.class);
        List<Command> commandList;
        boolean programCorrupted = true;
	
        
    private ListIterator<Command> listIter;
    private Command currentCommand;
        
	/**
	 * Construct a virtual vehicle.
	 * 
	 * @param workDir the working directory of this virtual vehicle.
	 * @throws IOException thrown in case of missing files or folders.
	 */
	public VirtualVehicle (File workDir) throws IOException {
		super(workDir);
		try 
		{
			currentCommand=null;
			
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
		catch(ParserException e)
		{
			LOG.error(e.getMessage());
		}
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			LOG.error(e.getMessage(), e);
		}
		catch(IOException e) 
		{
			// TODO Auto-generated catch block
			LOG.error(e.getMessage(), e);
		}
		//TODO: implement file parsing, program verification, set programCorrupted flag
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.AbstractVirtualVehicle#execute()
	 */
	@Override
	public void execute() 
	{
		// TODO Auto-generated method stub

		PolarCoordinate p = sensorProxy.getCurrentPosition();		
		Position pos = currentCommand.get_position();
		
		boolean at_pos = true;
		
		// TODO: Check if p is in the tolerance area of pos
		// @Clemens -> Do we have a function to calculate the differnce im meters of 
		// two points in polar coordinates ?
		
		if (at_pos)
		{
			currentCommand.execute(sensorProxy);
			
			if (listIter.hasNext())
				currentCommand = listIter.next();
			else 
				completed = true;
		}
		
		
//		Double altitudeOverGround = sensorProxy.getAltitudeOverGround();
//		Double courseOverGround = sensorProxy.getCourseOverGround();
//		Double speedOverGround = sensorProxy.getSpeedOverGround();
//		Double airPressure = sensorProxy.getSensorValueAsDouble(SensorProxy.SENSOR_NAME_AIR_PRESSURE);
//		Integer random = sensorProxy.getSensorValueAsInteger(SensorProxy.SENSOR_NAME_RANDOM);
//		String sonar = sensorProxy.getSensorValue(SensorProxy.SENSOR_NAME_SONAR);
//		InputStream photo = sensorProxy.getSensorValueAsStream(SensorProxy.SENSOR_NAME_PHOTO);
		
//		LOG.error("VirtualVehicle.execute() is not implemented yet! position=" + p +
//				", alt=" + altitudeOverGround + ", course=" + courseOverGround +
//				", speed=" + speedOverGround
//				);
            
            //TODO: implement interpreter
		
		
	}
	
	
	public VirtualVehicleState getState()
	{
		VirtualVehicleState vss = new VirtualVehicleState();
		
		for (Command cmd : commandList)
		{
			if (cmd.is_finished())
				vss.CommandsExecuted++;
			else	
				vss.CommandsToExecute++;
		}
		
		return vss;
	}

}
