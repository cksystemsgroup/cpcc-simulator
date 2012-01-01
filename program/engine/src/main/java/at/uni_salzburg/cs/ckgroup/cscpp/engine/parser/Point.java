package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

public class Point 
{
	public Point(double lat, double lon, double alt)
	{
		latitude = lat;
		longitude = lon;
		altitude = alt;
	}
	
	private double latitude;
	private double longitude;
	private double altitude;
	
	double get_latitude()
	{
		return latitude;
	}
	double get_longitude()
	{
		return longitude;
	}
	double get_attitude()
	{

		return altitude;
	}
	
}	
