package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy;

// TODO GNU header

public class ActionPicture implements IAction, Serializable {
	// TODO check: why Serializable?

	private byte[] data = null;
	
	@Override
	public boolean execute(ISensorProxy sprox) 
	{
		InputStream photo = sprox.getSensorValueAsStream(ISensorProxy.SENSOR_NAME_PHOTO);
		// TODO check for null! A photo is avaliable if the InputStream is not null.
		
		// TODO store photo
		// TODO use FileOutputStream to store picture. Use dataDir folder from (Abstract)VirtualVehicle
		
		int avl = 0;
		try 
		{
			// TODO check documentation, see HttpQueryUtils.simpleQuery() how to copy the stream
			
			avl = photo.available();
			
			data = new byte[avl];	
			
			avl = photo.read(data);
			
			
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return false;
	}

	public String toString()
	{
		// TODO use: 'return "Picture";' instead.
		return new String("Picture");
	}

	
	

	
}
