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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public abstract class AbstractVirtualVehicle implements IVirtualVehicle, Runnable {
	
	Logger LOG = Logger.getLogger(AbstractVirtualVehicle.class);
	
	public static final String PROGRAM_PATH = "vehicle.prg";
	public static final String LOG_PATH = "vehicle.log";
	public static final String PROPERTY_PATH = "vehicle.properties";
	public static final String STATUS_PATH = "vehicle.status";
	public static final String DATA_SUBDIR = "data";
	public static final long timerDelay = 500;
	public static final long timerPeriod = 1000;
	
	public static final String PROP_VEHICLE_ID = "vehicle.id";
	
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
	 * The virtual vehicle status.
	 */
	protected File vehicleStatus;
	
	/**
	 * The properties of the virtual vehicle.
	 */
	protected Properties properties;
	
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
	protected boolean completed = false;
	
	/**
	 * Construct a virtual vehicle instance.
	 * 
	 * @param workDir the working directory of this virtual vehicle.
	 * @throws IOException thrown in case of missing files or folders.
	 */
	public AbstractVirtualVehicle(File workDir) throws IOException {
		this.workDir = workDir;
		this.dataDir = new File(workDir, DATA_SUBDIR);
		
		program = new File(workDir, PROGRAM_PATH);
		if (!program.exists())
			throw new IOException("Program file not found " + program);
		
		vehicleStatus = new File(workDir, STATUS_PATH);
		if (!vehicleStatus.exists())
			vehicleStatus.createNewFile();
		
		properties = new Properties();
		File propsFile = new File(workDir, PROPERTY_PATH);
		if (propsFile.exists())
			properties.load(new FileInputStream(propsFile));
		
		if (properties.getProperty(PROP_VEHICLE_ID) == null) {
			properties.setProperty(PROP_VEHICLE_ID, UUID.randomUUID().toString());
			properties.store(new FileOutputStream(new File(workDir, PROPERTY_PATH)), "");
		}
		
		FileUtils.ensureDirectory(dataDir);
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#suspend()
	 */
	@Override
	public void suspend() throws IOException {
		LOG.info("Suspending vehicle " + workDir.getName());
		if (backGroundTimer != null)
			backGroundTimer.cancel();
		backGroundTimer = null;
		if (backGroundTimerTask != null)
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
		
		properties.store(new FileOutputStream(new File(workDir, PROPERTY_PATH)), "");
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#resume()
	 */
	@Override
	public void resume() throws IOException {
		LOG.info("Resuming vehicle " + workDir.getName());
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
		
		if (isProgramCorrupted()) {
			try { suspend(); } catch (IOException e) { }
			return;
		}
		
		running = true;
		
		if (sensorProxy != null)
			execute();
		
		running = false;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#serialize(java.io.OutputStream)
	 */
	@Override
	public void serialize(OutputStream out) throws IOException {
		LOG.info("serializing vehicle " + workDir.getName() + " to stream.");
		
		boolean active = isActive();
		if (active)
			suspend();
		
		ZipOutputStream zOut = new ZipOutputStream(out);
		FileUtils.zipToStream(workDir, workDir.getAbsolutePath().length()+1, zOut);
		zOut.close();
		
		if (active)
			resume();
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
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getDataDir()
	 */
	public File getDataDir() {
		return dataDir;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getProperties()
	 */
	public Properties getProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#getDataFileNames()
	 */
	public String[] getDataFileNames() {
		return dataDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		});
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
