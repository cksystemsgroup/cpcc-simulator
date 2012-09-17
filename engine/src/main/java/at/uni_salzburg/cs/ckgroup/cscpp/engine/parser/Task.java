/*
 * @(#) Task.java
 *
 * This code is part of the CPCC project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.util.List;
import java.util.Locale;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.IAction;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ISensorProxy;
import at.uni_salzburg.cs.ckgroup.cpcc.mapper.api.ITask;

public class Task implements ITask, JSONAware {
	
	private PolarCoordinate position;
	private Double tolerance;
	private long arrivalTime;
	private long activationTime;
	private long delayTime;
	private long lifeTime;
	private List<IAction> actionList;

	@Override
	public PolarCoordinate getPosition() {
		return position;
	}
	
	public void setPosition(PolarCoordinate position) {
		this.position = position;
	}
	
	@Override
	public Double getTolerance() {
		return tolerance;
	}
	
	public void setTolerance(Double tolerance) {
		this.tolerance = tolerance;
	}
	
	@Override
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	@Override
	public long getActivationTime() {
		return activationTime;
	}
	
	public void setActivationTime(long activationTime) {
		this.activationTime = activationTime;
	}
	
	@Override
	public long getDelayTime() {
		return delayTime;
	}
	
	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}
	
	@Override
	public long getLifeTime() {
		return lifeTime;
	}
	
	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}
	
	@Override
	public List<IAction> getActionList() {
		return actionList;
	}
	
	public void setActionList(List<IAction> actionList) {
		this.actionList = actionList;
	}
	
	@Override
	public boolean isComplete() {
		for (IAction action : actionList) {
			if (!action.isComplete()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void execute(ISensorProxy sprox) {
		for (IAction action : actionList) {
			if (!action.isComplete()) {
				action.execute(sprox);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (activationTime > 0) {
			b.append(" Activation ").append(activationTime);
		}
		if (delayTime >= 0) {
			b.append(" Delay ").append(delayTime);
		}
		if (lifeTime > 0) {
			b.append(" LifeTime").append(lifeTime);
		}
		for (IAction action : actionList) {
			b.append("\n").append(action.toString());
		}
		return String.format(Locale.US, "Point %.8f %.8f %.3f Tolerance %.1f\nArrival %d%s\n",
				position.getLatitude(), position.getLongitude(), position.getAltitude(),
				tolerance, arrivalTime, b.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		
		JSONObject obj = new JSONObject();
		obj.put("point", getPosition());
		obj.put("tolerance", getTolerance());
		// TODO arrival time, delay, and lifetime in JSON string?
		
		JSONArray act = new JSONArray();
		for (IAction a : getActionList()) {
			act.add(a);
		}
		obj.put("actions", act);
		
		return obj.toJSONString();
	}
	
	
}
