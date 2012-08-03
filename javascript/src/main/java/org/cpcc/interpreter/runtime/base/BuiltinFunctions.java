/*
 * @(#) BuiltinFunctions.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2012  Clemens Krainer, Michael Lippautz
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
package org.cpcc.interpreter.runtime.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.cpcc.interpreter.JSInterpreter;
import org.cpcc.interpreter.runtime.types.LatLngAlt;
import org.cpcc.interpreter.runtime.types.SensorValue;
import org.cpcc.interpreter.runtime.types.VirtualVehicleActionPoint;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;


public class BuiltinFunctions implements Serializable {

	private static final long serialVersionUID = -5978941480045547826L;

	/**
	 * Function acquires the latest available position and returns immediately. 
	 * 
	 * @name getpos
	 * @function
	 * @memberOf _global_
	 * @return {LatLng} The latest available position.
	 */
	public static LatLngAlt getpos() throws Exception {
		PositonProvider positionProvider = JSInterpreter.getPositionProvider();
		if(positionProvider == null) {
			throw new NullPointerException("positionprovider == null");	
		}
		return positionProvider.getCurrentPosition();
	}
	
	/**
	 * Prints all provided arguments, followed by a newline. 
	 * 
	 * @name println
	 * @function
	 * @memberOf _global_
	 * @param {Object} vargs The objects to print, separated by commas.
	 */
	public static void println(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object o : args){
			if (first) {
				sb.append(o.toString());
				first = false;
			} else {
				sb.append(" ").append(o.toString());
			}
		}
		JSInterpreter.getConsoleProvider().print(sb.toString()+"\n");
	}
	
	/**
	 * Causes the currently executing thread to sleep (temporarily cease
	 * execution) for the specified number of milliseconds.
	 * 
	 * @name sleep
	 * @function
	 * @memberOf _global_
	 * @param millis the length of time to sleep in milliseconds.
	 */
	public static void sleep(Object arg) {
		long millis = ((Double)arg).longValue();
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// we don't care
		}
	}
	
	/**
	 * Migrate back to the groundstation and initialize a mapping step.
	 * 
	 * @name migrate
	 * @function
	 * @memberOf _global_
	 */
	public static void migrate() {
		Context cx = Context.enter();
		try {
			ContinuationPending cp = cx.captureContinuation();
			throw cp;
		} finally {
			Context.exit();
		}
	}
	
	/**
	 * Returns a double value with a positive sign, greater than or equal to 0.0
	 * and less than 1.0. Returned values are chosen pseudo-randomly with
	 * (approximately) uniform distribution from that range.
	 * 
	 * @name sleep
	 * @function
	 * @memberOf _global_
	 */
	public static double random() {
		return Math.random();
	}
	
	/**
	 * Fly to the next action point.
	 * 
	 * @name flyTo
	 * @function
	 * @memberOf _global_
	 * @param {Object} vargs one instance of LatLngAlt followed by one or more sensor names (Strings)
	 */
	public static VirtualVehicleActionPoint flyTo(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
		
		if (args.length < 3 || args[0] == null || args[1] == null || !(args[0] instanceof LatLngAlt) || !(args[1] instanceof Double)) {
			throw new IllegalArgumentException("Too few parameters or first parameter is not a LatLngAlt instance or second parameter is not a number.");
		}
		
		LatLngAlt latLonAlt = (LatLngAlt)args[0];
		Double tolerance = (Double)args[1];
		List<SensorValue> s = new ArrayList<SensorValue>();
		for (int k=2; k < args.length; ++k) {
			if (args[k] != null && !"undefined".equals(args[k])) {
				SensorValue v = new SensorValue();
				v.jsConstructor((String)args[k], null, null);
				s.add(v);
			}
		}
		
		if (s.size() == 0) {
			return null;
		}
		
		VirtualVehicleActionPoint actionPoint = new VirtualVehicleActionPoint();
		actionPoint.jsConstructor(latLonAlt, tolerance, new NativeArray(s.toArray(new SensorValue[0])));
		Context cx = Context.enter();
		try {
			ContinuationPending cp = cx.captureContinuation();
			cp.setApplicationState(actionPoint);
			throw cp;
		} finally {
			Context.exit();
		}
	}
}
