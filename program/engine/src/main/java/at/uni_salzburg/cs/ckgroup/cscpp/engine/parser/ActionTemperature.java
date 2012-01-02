package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.Serializable;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy;

// TODO GNU header


public class ActionTemperature implements IAction, Serializable 
// TODO check: why Serializable?
{
	Integer temperature = 0;
	@Override
	public boolean execute(ISensorProxy sprox) 
	{
		// TODO Auto-generated method stub
		Integer temperature = sprox.getSensorValueAsInteger(ISensorProxy.SENSOR_NAME_RANDOM);
		// TODO check: temperature is of type double
		// TODO store temperature
		
		return false;
	}
	
	public String toString()
	{
		// TODO use: 'return "Temperature";' instead.
		return new String("Temperature");
	}


}
