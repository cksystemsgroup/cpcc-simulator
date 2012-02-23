package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ParserTestCase {
	
	public static final String[] expectedResult_01 = {
		"Point 123.45000000 2134234.34500000 34.000 tolerance 12.3\n" +
		"Picture\n"+
		"Temperature\n", 

		"Point 1.00000000 1.00000000 1.000 tolerance 100.0\n" +
		"Temperature\n",

		"Point 2.00000000 2.00000000 2.000 tolerance 1.2\n" +
		"Picture\n"
	};
	

	@Test
	public void testCase01() throws FileNotFoundException, ParserException {
		
//		String cmdPathName = ParserTestCase.class.getResource("cmd.txt").getPath();
		String cmdPathName = Thread.currentThread().getContextClassLoader().getResource("at/uni_salzburg/cs/ckgroup/cscpp/engine/parser/cmd.txt").getPath();
		
		Scanner sc = new Scanner(cmdPathName);
		Assert.assertNotNull(sc);
		
		Parser pa = new Parser(null);
		Assert.assertNotNull(pa);
		
		List<Command> lst = pa.run(sc);
		Assert.assertNotNull(lst);
		
		Assert.assertEquals("Number of commands", 3, lst.size());

		for (int k=0; k < lst.size(); ++ k) {
			System.out.println(lst.get(k).toString());
			Assert.assertEquals("Command #"+k, expectedResult_01[k], lst.get(k).toString());
		}
		
	}

}
