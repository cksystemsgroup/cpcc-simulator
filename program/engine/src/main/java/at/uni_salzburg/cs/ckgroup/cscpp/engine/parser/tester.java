package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{

		System.out.println("test\n");
		
		try 
		{
			Scanner sc = new Scanner("/home/andreas/workspace/Flight_Commands/src/cmd.txt");
			Parser pa = new Parser();
			
	
			List<Command> lst = pa.run(sc);
		
		
		} 
		catch(ParserException e)
		{
			System.out.println(e.getMessage());
		}
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
