package at.uni_salzburg.cs.ckgroup.cscpp.engine.vehicle;

import java.io.Serializable;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

public class Waypoint implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2437465449668760468L;
	public PolarCoordinate pos;
	public long timestamp;
	
	public Waypoint()
	{
		
	}
	
	public Waypoint(PolarCoordinate pos)
	{
		this.pos = pos;
		timestamp = System.currentTimeMillis();
	}

}
