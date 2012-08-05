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
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class Task implements JSONAware {
	
	private PolarCoordinate position;
	private Double tolerance;
	private List<IAction> actionList;

	public PolarCoordinate getPosition() {
		return position;
	}
	
	public void setPosition(PolarCoordinate position) {
		this.position = position;
	}
	
	public Double getTolerance() {
		return tolerance;
	}
	
	public void setTolerance(Double tolerance) {
		this.tolerance = tolerance;
	}
	
	public List<IAction> getActionList() {
		return actionList;
	}
	
	public void setActionList(List<IAction> actionList) {
		this.actionList = actionList;
	}
	
	public boolean isComplete() {
		for (IAction action : actionList) {
			if (!action.isComplete()) {
				return false;
			}
		}
		return true;
	}
	
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
		for (IAction action : actionList) {
			b.append("\n").append(action.toString());
		}
		return String.format(Locale.US, "Point %.8f %.8f %.8f Tolerance %.2f%s\n", position.getLatitude(), position.getLongitude(), position.getAltitude(), tolerance, b.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		
		JSONObject obj = new JSONObject();
		obj.put("point", getPosition());
		obj.put("tolerance", getTolerance());

		JSONArray act = new JSONArray();
		for (IAction a : getActionList()) {
			act.add(a);
		}
		obj.put("actions", act);
		
		return obj.toJSONString();
	}
	
	
}
