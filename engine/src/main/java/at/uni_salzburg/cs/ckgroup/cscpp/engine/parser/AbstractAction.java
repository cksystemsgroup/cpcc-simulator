/*
 * @(#) ActionTemperature.java
 *
 * This code is part of the ESE CPCC project.
 * Copyright (c) 2012  Clemens Krainer, Michael Kleber, Andreas Schroecker, Bernhard Zechmeister
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

import java.io.Serializable;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;


public abstract class AbstractAction implements IAction, Serializable
{
	private static final long serialVersionUID = -2728056184176401730L;
	private long timestamp = 0;

	
	public long getTimestamp() 
	{
		return timestamp;
	}
	
	@Override
	public boolean isComplete() {
		return timestamp > 0;
	}
	
	@Override
	public final boolean execute(ISensorProxy sprox) {
		
		if (isComplete()) {
			return true;
		}
		
		if (retrieveValue(sprox)) {
			timestamp = System.currentTimeMillis();
			return true;
		}
		
		return false;
	}

	protected abstract boolean retrieveValue(ISensorProxy sprox);
}
