/*
 * @(#) Command.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.ISensorProxy;


public class Command implements Serializable
{
	private static final long serialVersionUID = 424412301491147477L;
	private Position position;
	private List<IAction> actionList;
	
	private boolean finished;
		
	public Command(Position position, List<IAction> actionList)
	{
		this.position = position;
		this.actionList = actionList;
		this.finished = false;
	}
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		
		s.append(position.toString());
		s.append("\n");
		
		for (IAction action : actionList) {
			s.append(action.toString());
			s.append("\n");
		}
		
		return s.toString();
	}
	
	public Position getPosition()
	{
		return position;
	}
	
	public List<IAction> getActions()
	{
		return actionList;
	}
	
	public boolean isFinished()
	{
		return finished;
	}
	
	public boolean execute(ISensorProxy sproxy)
	{
		if (!finished)
		{
			IAction act = null;
		    ListIterator<IAction> iter = actionList.listIterator();
		    boolean allFinished = true;
		    
		    while (iter.hasNext())
		    {
		    	act = iter.next();
		    	if(!act.isComplete())
		    	{
		    		act.execute(sproxy);
		    		if(!act.isComplete())
		    			allFinished=false;
		    	}
		    	
		    		
		    }
		    
		    finished = allFinished;
		}
		
		return true;
	}
	
} 