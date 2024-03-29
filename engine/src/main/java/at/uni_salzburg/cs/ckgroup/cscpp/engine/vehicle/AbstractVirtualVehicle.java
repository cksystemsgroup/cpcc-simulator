/*
 * @(#) AbstractVirtualVehicle.java
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ISensorProxy;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ITask;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public abstract class AbstractVirtualVehicle implements IVirtualVehicle, Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractVirtualVehicle.class);
	
	public static final String PROGRAM_PATH = "vehicle.prg";
	public static final String LOG_PATH = "vehicle.log";
	public static final String PROPERTY_PATH = "vehicle.properties";
	public static final String STATUS_PATH = "vehicle.status";
	public static final String STATUS_TXT_PATH = "vehicle-status.txt";
	public static final String DATA_SUBDIR = "data";
	public static final long timerDelay = 500;
	public static final long timerPeriod = 1000;
	
	public static final String PROP_VEHICLE_ID = "vehicle.id";
	public static final String PROP_VEHICLE_FROZEN = "vehicle.frozen";
	public static final String PROP_VEHICLE_TASK_DELAY_ALGORITHM = "vehicle.task.delay.algorithm";
	
	private Object lock = new Object[0];
	
	/**
	 * The working directory of this virtual vehicle. It contains the virtual
	 * vehicle program, a log of events, and a sub-directory containing all
	 * collected sensor data.
	 */
	protected File workDir;
	
	/**
	 * The virtual vehicle program to be executed.
	 */
	protected File program;
	
	/**
	 * The virtual vehicle status as a text file.
	 */
	protected File vehicleStatusTxt;

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
	private volatile boolean running = false;
	
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
	private volatile boolean completed = false;
	
	/**
	 * True this virtual vehicle is frozen, i.e., suspended and invisible to the
	 * mapper.
	 */
	private volatile boolean frozen;
	
	/**
	 * The current way-point list as a string.
	 */
	private String waypoints = null;
	
	/**
	 * Algorithm to delay the completion of tasks.
	 */
	private ITaskDelayAlgorithm delayAlgorithm;
		
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
		if (!program.exists()) {
			throw new IOException("Program file not found " + program);
		}
		
		vehicleStatusTxt = new File(workDir, STATUS_TXT_PATH);
		
		properties = new Properties();
		File propsFile = new File(workDir, PROPERTY_PATH);
		if (propsFile.exists()) {
			FileInputStream inStream = new FileInputStream(propsFile);
			properties.load(inStream);
			inStream.close();
		}

		if (properties.getProperty(PROP_VEHICLE_ID) == null) {
			properties.setProperty(PROP_VEHICLE_ID, UUID.randomUUID().toString());
			FileOutputStream outStream = new FileOutputStream(new File(workDir, PROPERTY_PATH));
			properties.store(outStream, "");
			outStream.close();
		}
		
		frozen = Boolean.valueOf(properties.getProperty(PROP_VEHICLE_FROZEN,"false"));

		String algorithmString = properties.getProperty(PROP_VEHICLE_TASK_DELAY_ALGORITHM);
		delayAlgorithm = TaskDelayAlgorithmBuilder.build(algorithmString, this);
		
		FileUtils.ensureDirectory(dataDir);
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#suspend()
	 */
	@Override
	public void suspend() throws IOException {
		boolean wasRunning = false;
		
		if (backGroundTimer != null) {
			backGroundTimer.cancel();
			backGroundTimer = null;
			wasRunning = true;
		}
		
		if (backGroundTimerTask != null) {
			backGroundTimerTask.cancel();
			backGroundTimerTask = null;
			wasRunning = true;
		}
		
		while (running) {
			LOG.info("Waiting for current task to finish.");
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
		}

		if (wasRunning) {
			LOG.info("Suspending vehicle " + workDir.getName());
			logVehiclePosition("suspend");
		} else {
			LOG.info("Vvehicle " + workDir.getName() + " has already been suspended.");
		}
		
		FileOutputStream outStream = new FileOutputStream(new File(workDir, PROPERTY_PATH));
		properties.store(outStream, "");
		outStream.close();
	}

	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#resume()
	 */
	@Override
	public void resume() throws IOException {
		if (frozen) {
			LOG.info("Vehicle " + workDir.getName() + " is frozen, resuming cancelled.");
			return;
		}

		if (completed) {
			LOG.info("Vehicle " + workDir.getName() + " is complete, resuming cancelled.");
			return;
		}

		if (running || backGroundTimer != null) {
			LOG.error("Vehicle is already running: " + workDir.getName());
			throw new IOException("Program is running.");
		}

		LOG.info("Resuming vehicle " + workDir.getName());
		
		logVehiclePosition("resume");
		backGroundTimer = new Timer();
		backGroundTimerTask = new MyTimerTask(this);
		backGroundTimer.schedule(backGroundTimerTask, timerDelay, timerPeriod);
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		
		if (frozen) {
			return;
		}
		
		if (isProgramCorrupted()) {
			try { suspend(); } catch (IOException e) { }
			return;
		}
		
		running = true;
		
		if (delayAlgorithm != null) {
			delayAlgorithm.tick();
		}
		
		if (sensorProxy != null) {
			logFlightSegmentChange();
			try {
				execute();
			} catch (Throwable t) {
				LOG.error("Virtual vehicle task failed.", t);
			}
		}
		
		running = false;
	}

	/**
	 * Log the current position, if the helicopter changes the flight segment.
	 */
	private void logFlightSegmentChange() {
		String newWaypoints = sensorProxy.getWaypoints();
		
		if ((waypoints != null && newWaypoints == null) ||
			(waypoints == null && newWaypoints != null) ||
			(waypoints != null && newWaypoints != null && !waypoints.equals(newWaypoints)))
		{
			logVehiclePosition("position");
		}
		waypoints = newWaypoints;
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
	
	/**
	 * Write the current position to the vehicle's log.
	 * @param action the action the position is logged for. 
	 */
	public abstract void logVehiclePosition(String action);

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
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#isFrozen()
	 */
	@Override
	public boolean isFrozen() {
		return frozen;
	}
	
	/* (non-Javadoc)
	 * @see at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle.IVirtualVehicle#setFrozen(boolean)
	 */
	public void setFrozen(boolean frozen) throws IOException {
		
		if (this.frozen != frozen) {
			this.frozen = frozen;
			saveProperties();
		}

		if (frozen) {
			suspend();
		}
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
	
	public ITaskDelayAlgorithm getDelayAlgorithm() {
		return delayAlgorithm;
	}
	
	public void setCompleted() throws IOException {
		if (isActive()) {
			running = false;
			suspend();
		}
		this.completed = true;
	}
	
	@Override
	public void setIncomplete() throws IOException {
		if (this.completed) {
			this.completed = false;
			resume();
		}
	}
	
	public void addLogEntry(String entry) throws IOException {
		PrintWriter vehicleLog = new PrintWriter(new FileWriter(new File(workDir, LOG_PATH), true));
		vehicleLog.printf("%d %s\n",System.currentTimeMillis(), entry);
		vehicleLog.close();
	}

	public void addLogEntry(String entry, Exception ex) throws IOException {
		PrintWriter vehicleLog = new PrintWriter(new FileWriter(new File(workDir, LOG_PATH), true));
		vehicleLog.printf("%d %s\n",System.currentTimeMillis(), entry);
		ex.printStackTrace(vehicleLog);
		vehicleLog.close();
	}
	
	public String getLog() throws IOException {
		File logFile = new File(workDir, LOG_PATH);
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
		return FileUtils.loadFileAsString(logFile);
	}

	@Override
	public void saveState() {
		synchronized (lock) {
			try {
				PrintWriter pw = new PrintWriter(vehicleStatusTxt);
				for (ITask task : getTaskList()) {
					pw.println(task.toString());
				}
				pw.close();
			} catch (FileNotFoundException e) {
				LOG.error("Can not save state of vehicle " + workDir, e);
			}
		}
	}
	
	@Override
	public void saveProperties() {
		synchronized (lock) {
			try {
				properties.setProperty(PROP_VEHICLE_FROZEN, Boolean.valueOf(frozen).toString());
				if (delayAlgorithm != null) {
					properties.setProperty(PROP_VEHICLE_TASK_DELAY_ALGORITHM, delayAlgorithm.getCurrentState());
				}
				FileOutputStream outStream = new FileOutputStream(new File(workDir, PROPERTY_PATH));
				properties.store(outStream, "");
				outStream.close();
			} catch (IOException e) {
				LOG.error("Can not save properties of vehicle " + workDir, e);
			}
		}
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
