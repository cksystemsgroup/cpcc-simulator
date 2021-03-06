/*
 * @(#) VirtualVehicle.java
 *
 * This code is part of the ESE CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;
import at.uni_salzburg.cs.ckgroup.course.IGeodeticSystem;
import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.course.WGS84;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ITask;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Scanner;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.TaskListBuilder;


public class VirtualVehicle extends AbstractVirtualVehicle {

	Logger LOG = LoggerFactory.getLogger(VirtualVehicle.class);
	
	private boolean programCorrupted = true;
        
	private List<ITask> taskList;
    private ITask currentTask;
    private int currentIndex;

    private IGeodeticSystem geodeticSystem = new WGS84();
    private String postponedLogging = null;
    
	/**
	 * Construct a virtual vehicle.
	 * 
	 * @param workDir the working directory of this virtual vehicle.
	 * @throws IOException thrown in case of missing files or folders.
	 */
	public VirtualVehicle (File workDir) throws IOException {
		
		super(workDir);
		
		File input = vehicleStatusTxt.exists() ? vehicleStatusTxt : program;
		
		FileInputStream inStream = new FileInputStream(input);
		Scanner scanner = new Scanner(inStream);
		
		TaskListBuilder	builder = new TaskListBuilder(dataDir);
		
		try {
			currentIndex = 0;
			taskList = builder.build(scanner);
			
			currentTask = null;
			for (ITask task : taskList) {
				if (!task.isComplete()) {
					currentTask = task;
					break;
				}
				++currentIndex;
			}
			
			if (currentTask == null) {
				setCompleted();
			}
			
			programCorrupted = false;

			saveState();
			
		} catch(Exception e) {
			addLogEntry("Vehicle " + workDir.getName() + " is corrupt. Execution refused.", e);
			LOG.error("Vehicle " + workDir.getName() + " is corrupt. Execution refused.", e);
		}

		inStream.close();
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
		if (!isCompleted() && currentTask == null) {
			currentIndex = 0;
			for (ITask task : taskList) {
				if (!task.isComplete()) {
					currentTask = task;
					break;
				}
				++currentIndex;
			}
		}
		
		if (currentPosition == null || isCompleted() || currentTask == null || altitudeOverGround == null) {
			return;
		}
		currentPosition = new PolarCoordinate(currentPosition.getLatitude(), currentPosition.getLongitude(), altitudeOverGround.doubleValue());
		CartesianCoordinate currentPosCartesian = geodeticSystem.polarToRectangularCoordinates(currentPosition);
		
		if (postponedLogging != null) {
			logVehiclePosition(postponedLogging, currentPosition.getLatitude(), currentPosition.getLongitude(), altitudeOverGround);
			postponedLogging = null;
		}
		
		long now = System.currentTimeMillis();
		
		// check if task has arrived
		if (currentTask.getArrivalTime() > now) {
			return;
		}
		
		// activate the task
		if (currentTask.getActivationTime() == 0) {
			currentTask.setActivationTime(now);
			saveState();
		}
		
		// check if task position is reached
		PolarCoordinate taskPosition = currentTask.getPosition();
		CartesianCoordinate taskPosCartesian = geodeticSystem.polarToRectangularCoordinates(taskPosition);
		
		if (currentTask.getDelayTime() < 0) {
			
			ITaskDelayAlgorithm delayAlgorithm = getDelayAlgorithm();
			
			if (delayAlgorithm != null && currentIndex > 0) {
				ITask oldTask = taskList.get(currentIndex-1);
				PolarCoordinate oldTaskPosition = oldTask.getPosition();
				CartesianCoordinate oldTaskPosCartesian = geodeticSystem.polarToRectangularCoordinates(oldTaskPosition);
				
				double distance = oldTaskPosCartesian.subtract(taskPosCartesian).norm();
				long delay = oldTask.getDelayTime() >= 0 ? oldTask.getDelayTime() : 0;
				long arrival = oldTask.getArrivalTime();
				double v1 = 1000.0 * distance / (now - arrival - delay);
				
				if (!delayAlgorithm.checkFreeResources((int)v1)) {
					return;
				}
	
				delayAlgorithm.consumeResources((int)v1);
			}

			currentTask.setDelayTime(now - currentTask.getActivationTime());
			saveState();
		}
		
		double distance = taskPosCartesian.subtract(currentPosCartesian).norm();
		
		if (distance <= currentTask.getTolerance()) {
			currentTask.execute(sensorProxy);
			
			if (currentTask.isComplete()) {
				if (currentIndex >= taskList.size()-1) {
					currentTask = null;
					++currentIndex;
					try {
						setCompleted();
					} catch (IOException e) {
						LOG.error("Can not set vehicle " + getWorkDir() + " to completed.", e);
					}
				} else {
					currentTask = taskList.get(++currentIndex);
				}
			}

			saveState();
		}
		
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getTaskList()
	 */
	@Override
	public List<ITask> getTaskList() {
		return taskList;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getCurrentTaskIndex()
	 */
	@Override
	public int getCurrentTaskIndex() {
		return currentIndex;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getCurrentTask()
	 */
	@Override
	public ITask getCurrentTask() {
		ITask ct = currentTask;
		
		if (ct == null) {
			return null;
		}

//		long now = System.currentTimeMillis();
//		long delay = ct.getDelayTime() < 0 ? 0 : ct.getDelayTime();
//		
//		if (ct.getArrivalTime() + delay >= now) {
//			return null;
//		}
		
		return ct;
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
