package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

public class ParserException extends Exception
{
	private String message;
	
	public ParserException(String msg)
	{
		super(msg);
	}

}