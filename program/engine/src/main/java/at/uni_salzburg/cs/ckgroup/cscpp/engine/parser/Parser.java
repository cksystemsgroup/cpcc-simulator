/*
 * @(#) Parser.java
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.util.LinkedList;
import java.util.List;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;

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
		List<IAction> list_actions = null;
		
		if (sym==Scanner.SymPOINT)
		{
			next_sym();
			
			pos = read_position();
			
			list_actions = read_actions();
		}
		else
			throw_exception("error reading command - no PolarCoordinate specified");
	
		return new Command(pos, list_actions);
	}

	private Position read_position() throws ParserException			
	{
		
		PolarCoordinate pt = read_point();
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
	
	private PolarCoordinate read_point() throws ParserException
	{
		double lat=0.0, lon=0.0, alt=0.0;
		
		if ((sym==Scanner.SymNUMBER) || (sym==Scanner.SymDOUBLE))
		{
			lat = scanner.get_double_value();
			next_sym();
		}
		else
			throw_exception("error reading PolarCoordinate - need double for latitude");
		
		if ((sym==Scanner.SymNUMBER) || (sym==Scanner.SymDOUBLE))
		{
			lon = scanner.get_double_value();
			next_sym();
		}
		else
			throw_exception("error reading PolarCoordinate - need double for longitude");

		if ((sym==Scanner.SymNUMBER) || (sym==Scanner.SymDOUBLE))
		{
			alt = scanner.get_double_value();
			next_sym();
		}
		else
			throw_exception("error reading PolarCoordinate - need double for altitude");
		
		return new PolarCoordinate(lat, lon, alt);
	}
	
	private List<IAction> read_actions()
	{
		List<IAction> lst = new LinkedList<IAction>();
		
		boolean cont = true;
		
		while ((sym!=Scanner.SymEOF) && cont)
		{
			switch(sym)
			{
			case Scanner.SymTEMPERATURE: 
				lst.add(new ActionTemperature()); 
				break;
			case Scanner.SymPICTURE: 
				lst.add(new ActionPicture()); 	
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
