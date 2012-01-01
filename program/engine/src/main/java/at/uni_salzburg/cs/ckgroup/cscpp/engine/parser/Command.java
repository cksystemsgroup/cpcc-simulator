package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.util.LinkedList;
import java.util.List;


public class Command
{

	
	private Position position;
	private List<Action> lst_actions;
		
	public Command(Position pos, List<Action> actions)
	{
		position = pos;
		lst_actions = actions;
		
	}
	
	public String toString()
	{
		String s = new String();
		
		s+=position.toString();
		s+=lst_actions.toString();
		
		return s;
	}
	
} 
