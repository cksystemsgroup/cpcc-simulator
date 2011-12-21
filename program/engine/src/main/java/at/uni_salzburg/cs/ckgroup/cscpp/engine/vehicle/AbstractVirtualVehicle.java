/*
 * @(#) AbstractVirtualVehicle.java
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public abstract class AbstractVirtualVehicle implements IVirtualVehicle, Runnable {
	
	Logger LOG = Logger.getLogger(AbstractVirtualVehicle.class);
	
	public static final String PROGRAM_PATH = "vehicle.prg";
	public static final String LOG_PATH = "vehicle.log";
	public static final String DATA_SUBDIR = "data";
	public static final long timerDelay = 500;
	public static final long timerPeriod = 5000;
	
	/**
	 * The working directory of this virtual vehicle. It contains the virtual
	 * vehicle program, a log of events, and a sub-directory containing all
	 * collected sensor data.
	 */
	protected File workDir;
	
	/**
	 * The activity log of the vehicle.
	 */
	protected FileWriter vehicleLog = null;
	
	/**
	 * The virtual vehicle program to be executed.
	 */
	protected File program;
	
	/**
	 * The sub-directory containing all collected sensor data.
	 */
	protected File dataDir;
	
	/**
	 * True if the currently loaded program is running.
	 */
	private boolean running = false;
	
	/**
	 * The proxy instance to access sensor data of real vehicles.
	 */
	protected ISensorProxy sensorProxy = null;
	
	/**
	 * The timer running the virtual vehicle program.
	 */
	private Timer backGroundTimer;
	
	/**
	 * The timer task wrapper for this class.
	 */
	private TimerTask backGroundTimerTask;
	
	/**
	 * True if all tasks of this virtual vehicle have been successful.
	 */
	private boolean completed = false;
	
	/**
	 * Construct a virtual vehicle instance.
	 * 
	 * @param workDir the working directory of this virtual vehicle.
	 * @throws IOException thrown in case of missing files or folders.
	 */
	public AbstractVirtualVehicle(File workDir) throws IOException {
		this.workDir = workDir;
		this.dataDir = new File(workDir, DATA_SUBDIR);
		FileUtils.ensureDirectory(workDir);
		FileUtils.ensureDirectory(dataDir);
		
		program = new File(workDir, PROGRAM_PATH);
		if (!program.exists())
			throw new IOException("Program file not found " + program);
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#suspend()
	 */
	@Override
	public void suspend() throws IOException {
		backGroundTimer.cancel();
		backGroundTimer = null;
		backGroundTimerTask.cancel();
		backGroundTimerTask = null;
		
		while (running) {
			LOG.info("Waiting for current command to finish.");
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
		}
		
		if (vehicleLog != null) {
			vehicleLog.close();
			vehicleLog = null;
		}
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#resume()
	 */
	@Override
	public void resume() throws IOException {
		if (vehicleLog == null) {
			vehicleLog = new FileWriter(new File(workDir, LOG_PATH), true);
		}
		
		if (running || backGroundTimer != null)
			throw new IOException("Program is running.");
		
		backGroundTimer = new Timer();
		backGroundTimerTask = new MyTimerTask(this);
		backGroundTimer.schedule(backGroundTimerTask, timerDelay, timerPeriod);
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		running = true;
		
		if (sensorProxy != null)
			execute();
		
		running = false;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#serialize(java.io.OutputStream)
	 */
	@Override
	public void serialize(OutputStream out) {
		// TODO Auto-generated method stub
		LOG.error("serialize not iplemented yet.");
	}

	/**
	 * @param sensorProxy the sensor proxy to be used.
	 */
	public void setSensorProxy(ISensorProxy sensorProxy) {
		this.sensorProxy = sensorProxy;
	}

	/**
	 * Run one statement of the currently loaded program.
	 */
	public abstract void execute();
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#isActive()
	 */
	public boolean isActive() {
		return backGroundTimer != null;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#isCompleted()
	 */
	public boolean isCompleted() {
		return completed;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getWorkDir()
	 */
	public File getWorkDir() {
		return workDir;
	}
	
	private static class MyTimerTask extends TimerTask {

		private Runnable runnable;
		
		public MyTimerTask (Runnable runnable) {
			this.runnable = runnable;
		}
		
		@Override
		public void run() {
			runnable.run();
		}
		
	}
}
