package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class ScannerTestCase {
	
	private static final String PREFIX = "at/uni_salzburg/cs/ckgroup/cscpp/engine/parser/";
	private static final String TEST_01_PROGRAM = PREFIX + "ScannerTest01.txt";
	private static final String TEST_02_PROGRAM = PREFIX + "ScannerTest02.txt";
	private static final String TEST_04_PROGRAM = PREFIX + "ScannerTest04.txt";
	
	
	private void checkResult(Scanner scanner, Object[][] expected) throws IOException {

		for (Object[] o : expected) {
			Token token = scanner.next();
			assertNotNull(token);
			String exp = (String)o[0];
			Symbol sym = (Symbol)o[1];
			String str = (String)o[2];

			assertEquals(exp, sym, token.getSymbol());
			assertEquals(exp, str, token.getItemString());
			
			if (sym == Symbol.NUMBER) {
				assertEquals(Double.valueOf(str), token.getNumber().doubleValue(), 1E-9);
			}
		}
	}

	private static final Object[][] expected_01 = {
		{"01 01", Symbol.POINT, "Point"},
		{"01 02", Symbol.NUMBER, "123.45"},
		{"01 03", Symbol.NUMBER, "2134234.345"},
		{"01 04", Symbol.NUMBER, "34"},
		{"01 05", Symbol.TOLERANCE, "tolerance"},
		{"01 06", Symbol.NUMBER, "12.3"},
		{"01 07", Symbol.PICTURE, "Picture"},
		{"01 08", Symbol.TEMPERATURE, "Temperature"},
		
		{"01 09", Symbol.POINT, "Point"},
		{"01 10", Symbol.NUMBER, "1"},
		{"01 11", Symbol.NUMBER, "1"},
		{"01 12", Symbol.NUMBER, "1"},
		{"01 13", Symbol.TOLERANCE, "tolerance"},
		{"01 14", Symbol.NUMBER, "100"},
		{"01 15", Symbol.TEMPERATURE, "Temperature"},
		
		{"01 16", Symbol.POINT, "Point"},
		{"01 17", Symbol.NUMBER, "2"},
		{"01 18", Symbol.NUMBER, "2"},
		{"01 19", Symbol.NUMBER, "2"},
		{"01 20", Symbol.TOLERANCE, "tolerance"},
		{"01 21", Symbol.NUMBER, "1.2"},
		{"01 22", Symbol.PICTURE, "Picture"},

		{"01 23", Symbol.END, null}
	};
	
	@Test
	public void testCase01() throws IOException {
		
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(TEST_01_PROGRAM);
		assertNotNull(inStream);
		
		Scanner scanner = new Scanner(inStream);
		checkResult(scanner, expected_01);
	}

	private static final Object[][] expected_02 = {
		{"02 01", Symbol.POINT, "Point"},
		{"02 02", Symbol.NUMBER, "47.69292705"},
		{"02 03", Symbol.NUMBER, "13.38507593"},
		{"02 04", Symbol.MINUS, "-"},
		{"02 05", Symbol.NUMBER, "20.000"},
		{"02 06", Symbol.TOLERANCE, "tolerance"},
		{"02 07", Symbol.NUMBER, "5.0"},
		{"02 08", Symbol.PICTURE, "Picture"},
		{"02 09", Symbol.LEFT_PAREN, "("},
		{"02 10", Symbol.NUMBER, "1342217901294"},
		{"02 11", Symbol.LITERAL, "img1241582018627917693.png"},
		{"02 12", Symbol.RIGHT_PAREN, ")"},
		{"02 13", Symbol.TEMPERATURE, "Temperature"},
		{"02 14", Symbol.LEFT_PAREN, "("},
		{"02 15", Symbol.NUMBER, "1342217901297"},
		{"02 16", Symbol.MINUS, "-"},
		{"02 17", Symbol.NUMBER, "12.3"},
		{"02 18", Symbol.RIGHT_PAREN, ")"},
		{"02 19", Symbol.AIRPRESSURE, "AirPressure"},
		{"02 20", Symbol.LEFT_PAREN, "("},
		{"02 21", Symbol.NUMBER, "1342217901300"},
		{"02 22", Symbol.NUMBER, "1094.8"},
		{"02 23", Symbol.RIGHT_PAREN, ")"},
		
		{"02 24", Symbol.POINT, "Point"},
		{"02 25", Symbol.MINUS, "-"},
		{"02 26", Symbol.NUMBER, "47.69291983"},
		{"02 27", Symbol.NUMBER, "13.38562846"},
		{"02 28", Symbol.NUMBER, "20.000"},
		{"02 29", Symbol.TOLERANCE, "tolerance"},
		{"02 30", Symbol.NUMBER, "5.0"},
		{"02 31", Symbol.PICTURE, "Picture"},
		{"02 32", Symbol.LEFT_PAREN, "("},
		{"02 33", Symbol.NUMBER, "1342217919834"},
		{"02 34", Symbol.LITERAL, "img1668021337803956345.png"},
		{"02 35", Symbol.RIGHT_PAREN, ")"},
		{"02 36", Symbol.TEMPERATURE, "Temperature"},
		{"02 37", Symbol.LEFT_PAREN, "("},
		{"02 38", Symbol.NUMBER, "1342217919836"},
		{"02 39", Symbol.NUMBER, "7.8"},
		{"02 40", Symbol.RIGHT_PAREN, ")"},
		{"02 41", Symbol.AIRPRESSURE, "AirPressure"},
		{"02 42", Symbol.LEFT_PAREN, "("},
		{"02 43", Symbol.NUMBER, "1342217919839"},
		{"02 44", Symbol.NUMBER, "1084.7"},
		{"02 45", Symbol.RIGHT_PAREN, ")"},

		{"02 46", Symbol.POINT, "Point"},
		{"02 47", Symbol.NUMBER, "47.69294511"},
		{"02 48", Symbol.MINUS, "-"},
		{"02 49", Symbol.NUMBER, "13.38582999"},
		{"02 50", Symbol.NUMBER, "20.000"},
		{"02 51", Symbol.TOLERANCE, "tolerance"},
		{"02 52", Symbol.NUMBER, "5.0"},
		{"02 53", Symbol.PICTURE, "Picture"},
		{"02 54", Symbol.LEFT_PAREN, "("},
		{"02 55", Symbol.NUMBER, "1342217933771"},
		{"02 56", Symbol.LITERAL, "img963661589640754782.png"},
		{"02 57", Symbol.RIGHT_PAREN, ")"},
		{"02 58", Symbol.TEMPERATURE, "Temperature"},
		{"02 59", Symbol.LEFT_PAREN, "("},
		{"02 60", Symbol.NUMBER, "1342217933774"},
		{"02 61", Symbol.NUMBER, "30.0"},
		{"02 62", Symbol.RIGHT_PAREN, ")"},
		{"02 63", Symbol.AIRPRESSURE, "AirPressure"},
		{"02 64", Symbol.LEFT_PAREN, "("},
		{"02 65", Symbol.NUMBER, "1342217933777"},
		{"02 66", Symbol.NUMBER, "1094.0"},
		{"02 67", Symbol.RIGHT_PAREN, ")"},
		
		{"02 68", Symbol.END, null}
	};
	
	@Test
	public void testCase02() throws IOException {
		
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(TEST_02_PROGRAM);
		assertNotNull(inStream);
		
		Scanner scanner = new Scanner(inStream);
		checkResult(scanner, expected_02);
	}
	
	private static final Object[][] expected_04 = {
		{"02 01", Symbol.POINT, "Point"},
		{"02 02", Symbol.NUMBER, "47.69292705"},
		{"02 03", Symbol.NUMBER, "13.38507593"},
		{"02 04", Symbol.MINUS, "-"},
		{"02 05", Symbol.NUMBER, "20.000"},
		{"02 06", Symbol.TOLERANCE, "tolerance"},
		{"02 07", Symbol.NUMBER, "5.0"},
		{"02 07", Symbol.ARRIVAL, "Arrival"},
		{"02 07", Symbol.NUMBER, "1342217901293"},
		{"02 08", Symbol.PICTURE, "Picture"},
		{"02 09", Symbol.LEFT_PAREN, "("},
		{"02 10", Symbol.NUMBER, "1342217901294"},
		{"02 11", Symbol.LITERAL, "img1241582018627917693.png"},
		{"02 12", Symbol.RIGHT_PAREN, ")"},
		{"02 13", Symbol.TEMPERATURE, "Temperature"},
		{"02 14", Symbol.LEFT_PAREN, "("},
		{"02 15", Symbol.NUMBER, "1342217901297"},
		{"02 16", Symbol.MINUS, "-"},
		{"02 17", Symbol.NUMBER, "12.3"},
		{"02 18", Symbol.RIGHT_PAREN, ")"},
		{"02 19", Symbol.AIRPRESSURE, "AirPressure"},
		{"02 20", Symbol.LEFT_PAREN, "("},
		{"02 21", Symbol.NUMBER, "1342217901300"},
		{"02 22", Symbol.NUMBER, "1094.8"},
		{"02 23", Symbol.RIGHT_PAREN, ")"},

		{"02 24", Symbol.PROCESS, "Process"},
		{"02 25", Symbol.MINUS, "-"},
		{"02 26", Symbol.NUMBER, "47.69291983"},
		{"02 27", Symbol.NUMBER, "13.38562846"},
		{"02 28", Symbol.NUMBER, "20.000"},
		{"02 29", Symbol.TOLERANCE, "tolerance"},
		{"02 30", Symbol.NUMBER, "5.0"},
		{"02 07", Symbol.ARRIVAL, "Arrival"},
		{"02 07", Symbol.NUMBER, "1342217919833"},
		{"02 07", Symbol.DELAY, "Delay"},
		{"02 30", Symbol.NUMBER, "3000"},
		
		{"02 46", Symbol.POINT, "Point"},
		{"02 47", Symbol.NUMBER, "47.69294511"},
		{"02 48", Symbol.MINUS, "-"},
		{"02 49", Symbol.NUMBER, "13.38582999"},
		{"02 50", Symbol.NUMBER, "20.000"},
		{"02 51", Symbol.TOLERANCE, "tolerance"},
		{"02 52", Symbol.NUMBER, "5.0"},
		{"02 07", Symbol.ARRIVAL, "Arrival"},
		{"02 07", Symbol.NUMBER, "1342217933770"},
		{"02 07", Symbol.DELAY, "Delay"},
		{"02 30", Symbol.NUMBER, "4000"},
		{"02 07", Symbol.LIFETIME, "LifeTime"},
		{"02 30", Symbol.NUMBER, "86400"},
		
		{"02 53", Symbol.PICTURE, "Picture"},
		{"02 54", Symbol.LEFT_PAREN, "("},
		{"02 55", Symbol.NUMBER, "1342217933771"},
		{"02 56", Symbol.LITERAL, "img963661589640754782.png"},
		{"02 57", Symbol.RIGHT_PAREN, ")"},
		{"02 58", Symbol.TEMPERATURE, "Temperature"},
		{"02 59", Symbol.LEFT_PAREN, "("},
		{"02 60", Symbol.NUMBER, "1342217933774"},
		{"02 61", Symbol.NUMBER, "30.0"},
		{"02 62", Symbol.RIGHT_PAREN, ")"},
		{"02 63", Symbol.AIRPRESSURE, "AirPressure"},
		{"02 64", Symbol.LEFT_PAREN, "("},
		{"02 65", Symbol.NUMBER, "1342217933777"},
		{"02 66", Symbol.NUMBER, "1094.0"},
		{"02 67", Symbol.RIGHT_PAREN, ")"},
		
		{"02 68", Symbol.END, null}
	};
	
	
	@Test
	public void testCase04() throws IOException {
		
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(TEST_04_PROGRAM);
		assertNotNull(inStream);
		
		Scanner scanner = new Scanner(inStream);
		checkResult(scanner, expected_04);
	}
}
