/*
 * @(#) Temperature.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.actions;

import java.util.Locale;

import org.json.simple.JSONObject;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;

public class Temperature extends AbstractAction {

	private Double temperature;
	
	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
	
	@Override
	protected boolean retrieveValue(ISensorProxy sprox) {
		return null != (temperature = sprox.getSensorValueAsDouble(ISensorProxy.SENSOR_NAME_TEMPERATURE));
	}
	
	@Override
	public String toString() 
	{
		if (isComplete()) {
			return String.format(Locale.US, "Temperature (%d, %.1f)", getTimestamp(), temperature);
		}
		return "Temperature";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject o = new JSONObject();
		o.put("type", ISensorProxy.SENSOR_NAME_TEMPERATURE);
		if (getTimestamp() != 0) {
			o.put("time", getTimestamp());
			o.put("value", temperature);
		}
		return o.toJSONString();
	}
}