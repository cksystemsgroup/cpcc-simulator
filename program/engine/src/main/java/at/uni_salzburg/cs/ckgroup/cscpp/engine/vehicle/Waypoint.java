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
	public String host; //remember host (real vehicle) to differ between "cyber mobility" and "physical mobility"
	public long timestamp;
	

	
	public Waypoint()
	{
		
	}
	
	public Waypoint(PolarCoordinate pos, String host)
	{
		this.pos = pos;
		this.host = host;
		timestamp = System.currentTimeMillis();
	}
	

	/*
	enum MobilityTypes
	{
		CyberMobility,
		RealMobility
	}
	
	public MobilityTypes mobilityType;
	*/

}
