import java.util.LinkedList;
import java.util.List;


class ParserException extends Exception
{
	private String message;
	
	public ParserException(String msg)
	{
		super(msg);
	}

};

public class Parser 
{
	private int sym;
	Scanner scanner;
	
	public Parser()
	{
		sym = 0;
	}
	
	private boolean is_double()
	{
		return ((sym==Scanner.SymDOUBLE) || (sym==Scanner.SymNUMBER));
	}
	
	private void next_sym()
	{
		sym = scanner.read_sym();
	}
	
	public List<Command> run(Scanner sc) throws ParserException
	{
		Command cmd;
		List<Command> lst_cmds = new LinkedList<Command>();
		
		scanner = sc;
		next_sym();
		
		while (sym!=Scanner.SymEOF)
		{
			cmd = read_command();
			
			lst_cmds.add(cmd);
		}
		
		
		return lst_cmds;
	}
	
	
	
	
	private Command read_command() throws ParserException
	{
		Position pos = null;
		List<Action> list_actions = null;
		
		if (sym==Scanner.SymPOINT)
		{;
			next_sym();
			
			pos = read_position();
			
			list_actions = read_actions();
			
		}
		else
			throw_exception("error reading command - no point specified");
	
		return new Command(pos, list_actions);
	}
	

	private Position read_position() throws ParserException			
	{
		
		Point pt = read_point();
		double tol = 0.0;
		
		if (sym==Scanner.SymTOLERANCE)
		{
			next_sym();
			
			if (is_double())
			{
				tol = scanner.get_double_value();
				next_sym();
			}
			else
				throw_exception("error reading tolerance - need double");
		}
		else
			throw_exception("error reading position - no tolerance specified");
			
		
		return new Position(pt, tol);
	}

	
	private Point read_point() throws ParserException
	{
		double lat=0.0, lon=0.0, alt=0.0;
		
		if ((sym==Scanner.SymNUMBER) || (sym==Scanner.SymDOUBLE))
		{
			lat = scanner.get_double_value();
			next_sym();
		}
		else
			throw_exception("error reading point - need double for latitude");
		
		if ((sym==Scanner.SymNUMBER) || (sym==Scanner.SymDOUBLE))
		{
			lon = scanner.get_double_value();
			next_sym();
		}
		else
			throw_exception("error reading point - need double for longitude");

		if ((sym==Scanner.SymNUMBER) || (sym==Scanner.SymDOUBLE))
		{
			alt = scanner.get_double_value();
			next_sym();
		}
		else
			throw_exception("error reading point - need double for altitude");

		
		return new Point(lat, lon, alt);
		
	}
	
	private List<Action> read_actions()
	{
		List<Action> lst = new LinkedList<Action>();
		
		boolean cont = true;
		
		while ((sym!=Scanner.SymEOF) && cont)
		{
			switch(sym)
			{
			case Scanner.SymTEMPERATURE: 
				lst.add(new Action(Action.ActionType.TEMPERATURE)); 
				break;
			case Scanner.SymPICTURE: 
				lst.add(new Action(Action.ActionType.PICTURE)); 	
				break;
				
			default:
				// maybe beginning of next command -> so no exception
				cont = false;
				break;
			
			}
			
			if (cont)
				next_sym();
		}
		
		return lst;
	
	}
	
	private void throw_exception(String msg) throws ParserException
	{
		msg = "line "+scanner.get_line() +": " + msg;
		
		throw new ParserException(msg);
	
	}
	
}