package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

public class Action 
{
	public enum ActionType
	{
		TEMPERATURE, PICTURE
	}
	
	private ActionType type;
	
	public Action(ActionType t)
	{
		type = t;
	}
	
	public String toString()
	{
		switch (type)
		{
		case TEMPERATURE: 	return "Temperature";
		case PICTURE: 		return "Action";
		
		}
		
		return "UNKNOWN ACTION";
	}
}
