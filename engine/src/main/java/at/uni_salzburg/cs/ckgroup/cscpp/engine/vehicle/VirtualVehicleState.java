/*
 * @(#) VirtualVehicleState.java
 * 
 * This code is part of the ESE CPCC project.
 * Copyright (c) 2011  Clemens Krainer, Michael Kleber, Andreas Schroecker, Bernhard Zechmeister
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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.parser.Command;

public class VirtualVehicleState  implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2475790607625163260L;

	// TODO add all info which is needed

	public VirtualVehicleState() 
	{

	}

	public List<Command> commandList;

	public int CommandsExecuted() {
		int commandsExecuted = 0;
		for (Command cmd : commandList) {
			if (cmd.isFinished())
				commandsExecuted++;

		}
		return commandsExecuted;
	}

	public int CommandsToExecute() {

		int commandsToExecute = 0;
		for (Command cmd : commandList) {
			if (!cmd.isFinished())
				commandsToExecute++;
		}
		return commandsToExecute;
	}
	
	public List<Waypoint> track = new LinkedList<Waypoint>();
}