package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy;

//TODO GNU header

public interface IAction 
{
	public boolean execute(ISensorProxy sprox);
}
