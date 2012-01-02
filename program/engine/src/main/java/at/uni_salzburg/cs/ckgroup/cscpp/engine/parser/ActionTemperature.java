package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.Serializable;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy;


public class ActionTemperature implements IAction, Serializable 
{
	Integer temperature = 0;
	@Override
	public boolean execute(ISensorProxy sprox) 
	{
		// TODO Auto-generated method stub
		Integer temperature = sprox.getSensorValueAsInteger(sprox.SENSOR_NAME_RANDOM);
		
		return false;
	}
	
	public String toString()
	{
		return new String("Temperature");
	}


}
