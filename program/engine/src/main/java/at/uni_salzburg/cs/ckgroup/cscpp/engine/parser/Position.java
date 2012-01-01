package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

public class Position 
{
	private Point pt;
	private double tolerance;
	
	public Position(Point p, double tol)
	{
		pt = p;
		tolerance = tol;
	}
}
