import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;


public class Scanner 
{
	//String strFileName;
	//int State;
	
	private InputStreamReader input_file;
	private int c;
	
	private int int_value;
	private double double_value;
	private String identifier;
	
	private HashMap<String, Integer> map_keywords;
	
	
	public static final int SymDOUBLE = 1;
	public static final int SymNUMBER = 2;
	public static final int SymIDENTIFIER = 3;
	public static final int SymPOINT = 4;
	public static final int SymPICTURE = 5;
	public static final int SymTEMPERATURE = 6;
	public static final int SymEOF = 7;
	public static final int SymTOLERANCE = 8;
	
	int line;
	

	
	public Scanner(String strFile) throws FileNotFoundException
	{ 
		line = 1;
		c = 0;
		map_keywords = new HashMap();
		
		map_keywords.put(new String("POINT"), new Integer(SymPOINT));
		map_keywords.put(new String("PICTURE"), new Integer(SymPICTURE));
		map_keywords.put(new String("TEMPERATURE"), new Integer(SymTEMPERATURE));
		map_keywords.put(new String("TOLERANCE"), new Integer(SymTOLERANCE));

		
		FileInputStream input_stream = new FileInputStream(strFile);
		input_file = new InputStreamReader(input_stream);
	}
	
	public void next_char()
	{
		try 
		{
			c = input_file.read();
			
			if (c=='\n')
				line++;
		} 
		catch (IOException e) 
		{
			c = -1;
		}
	}
	
	int get_line()
	{
		return line;
	}
	
	public int read_sym() 
	{
		int sym = 0;
		while ((c!=-1) && (c<=' '))
		{
			next_char();
		}
		
		if (c==-1)
		{
			sym = SymEOF;
		}
		else if ((c>='0') && (c<='9'))
		{
			sym = read_number();
		}
		else if (((c>='a') && (c<='z')) || ((c>='A') && (c<='Z')))
		{
			sym = read_identifier();
		}
		
		
		return sym;
	}
	
	
	private int read_number()
	{
		int ret = 0;
		
		String strNumber = new String();
		
		
		do 
		{
			strNumber+=(char)c;
			next_char();
		}
		while ((c>='0') && (c<='9'));
		
		if (c=='.')
		{	
			do 
			{
				strNumber+=(char)c;
				next_char();
			}
			while ((c>='0') && (c<='9'));
			
			double_value = Double.parseDouble(strNumber);
			
			ret = SymDOUBLE;
		}
		else
		{
			int_value = Integer.parseInt(strNumber);
			double_value = Double.parseDouble(strNumber);
			
			ret = SymNUMBER;
		}
		
		return ret;
	}
	
	
	private int read_identifier()
	{
		identifier = new String();
		
		while (((c>='0') && (c<='9')) || 
				((c>='a') && (c<='z')) || 
				((c>='A') && (c<='Z')) || (c=='_'))
		{
			identifier+=(char)c;
			
			next_char();
		}
		
		
		return get_keyword();
	}
	
	private int get_keyword()
	{
		String str = identifier.toUpperCase();
		
		Integer sym = (Integer)map_keywords.get(str);
			
		return (sym==null ? SymIDENTIFIER : sym.intValue());
			
	}

	
	public int get_int_value()
	{
		return int_value;
	}
	
	public double get_double_value()
	{
		return double_value;
	}
	
	public String get_identifier()
	{
		return identifier;
	}
	
	
}




