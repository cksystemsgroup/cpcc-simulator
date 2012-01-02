package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.sensor.ISensorProxy;

public class ActionPicture implements IAction, Serializable {

	private byte[] data = null;
	
	@Override
	public boolean execute(ISensorProxy sprox) 
	{
		InputStream photo = sprox.getSensorValueAsStream(sprox.SENSOR_NAME_PHOTO);

		// TODO store photo
		
		int avl = 0;
		try 
		{
			
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
		return new String("Picture");
	}

	
	

	
}
