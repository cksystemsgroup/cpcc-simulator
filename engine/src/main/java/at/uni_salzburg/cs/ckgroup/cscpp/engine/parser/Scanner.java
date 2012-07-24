/*
 * @(#) Scanner.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2012  Clemens Krainer
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
	
	@SuppressWarnings("serial")
	private static final Map<String,Token> charMap = new HashMap<String, Token>() {{
		put(Symbol.LEFT_PAREN.getSymbolString(),	new Token(Symbol.LEFT_PAREN, Symbol.LEFT_PAREN.getSymbolString(), null));
		put(Symbol.LEFT_BRACK.getSymbolString(),	new Token(Symbol.LEFT_BRACK, Symbol.LEFT_BRACK.getSymbolString(), null));
		put(Symbol.LEFT_BRACE.getSymbolString(),	new Token(Symbol.LEFT_BRACE, Symbol.LEFT_BRACE.getSymbolString(), null));
		put(Symbol.RIGHT_PAREN.getSymbolString(),	new Token(Symbol.RIGHT_PAREN, Symbol.RIGHT_PAREN.getSymbolString(), null));
		put(Symbol.RIGHT_BRACK.getSymbolString(),	new Token(Symbol.RIGHT_BRACK, Symbol.RIGHT_BRACK.getSymbolString(), null));
		put(Symbol.RIGHT_BRACE.getSymbolString(),	new Token(Symbol.RIGHT_BRACE, Symbol.RIGHT_BRACE.getSymbolString(), null));
		put(Symbol.PERIOD.getSymbolString(),		new Token(Symbol.PERIOD, Symbol.PERIOD.getSymbolString(), null));
		put(Symbol.PLUS.getSymbolString(),			new Token(Symbol.PLUS, Symbol.PLUS.getSymbolString(), null));
		put(Symbol.MINUS.getSymbolString(),			new Token(Symbol.MINUS, Symbol.MINUS.getSymbolString(), null));
		put(Symbol.COLON.getSymbolString(),			new Token(Symbol.COLON, Symbol.COLON.getSymbolString(), null));
		put(Symbol.SEMICOLON.getSymbolString(),		new Token(Symbol.SEMICOLON, Symbol.SEMICOLON.getSymbolString(), null));
		put(Symbol.COMMA.getSymbolString(),			new Token(Symbol.COMMA, Symbol.COMMA.getSymbolString(), null));
		put(Symbol.DIVIDE.getSymbolString(),		new Token(Symbol.DIVIDE, Symbol.DIVIDE.getSymbolString(), null));
	}};

	private static boolean isDigit(int c) {
		return c >= '0' && c <= '9';
	}
	
	private static boolean isLetter (int c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
	}
	
	private static boolean isSpace(int c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}
	
	private InputStreamReader reader;
	
	private int ch = -2;
	
	private int lineNumber = 1;
	private int columnNumber = 0;

	public Scanner (InputStream inStream) {
		reader = new InputStreamReader(inStream);
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getColumnNumber() {
		return columnNumber;
	}

	private int getChar() throws IOException {
		int x = reader.read();
		++columnNumber;
		if (x == '\n') {
			++lineNumber;
			columnNumber = 0;
		}
		return x;
	}
	
	public Token next() throws IOException {

		if (ch == -2) {
			ch = getChar();
		}
		
		while (isSpace(ch)) {
			ch = getChar();
		}
				
		if (ch == -1) {
			return new Token(Symbol.END, null, null);
		}
		
		// number
		if (isDigit(ch)) {
			StringBuilder b = new StringBuilder();
			boolean decimalSeparatorFound = false; 
			
			do {
				b.append((char)ch);
				ch = getChar();
				if (!decimalSeparatorFound && ch == '.') {
					b.append((char)ch);
					decimalSeparatorFound = true;
					ch = getChar();
				}
			} while (isDigit(ch));
			
			return new Token(Symbol.NUMBER, b.toString(), new BigDecimal(b.toString()));
		}
		
		// string literal
		if (ch == '"' || ch == '\'') {
			int delimiter = ch;
			
			StringBuilder b = new StringBuilder();
			while ( (ch = getChar()) >= 0 && ch != delimiter) {
				b.append((char)ch);
			}
			
			ch = getChar();
			
			return new Token(Symbol.LITERAL, b.toString(), null);
		}
		
		// comment
		if (String.valueOf(ch).equals(Symbol.NUMBER_SIGN.getSymbolString())) {
			StringBuilder b = new StringBuilder();
			
			while ( (ch = getChar()) >= 0 && ch != '\n' && ch != '\r') {
				b.append((char)ch);
			}
			
			return new Token(Symbol.COMMENT, b.toString(), null);
		}
		
		// identifier
		if (isLetter(ch)) {
			StringBuilder b = new StringBuilder();
			b.append((char)ch);
			
			while ( (ch = getChar()) >= 0 && (isLetter(ch) || isDigit(ch)) ) {
				b.append((char)ch);
			}
			
			String identifier = b.toString();
			
			Symbol sym = Symbol.getSymbol(identifier);
			
			if (sym != null) {
				return new Token(sym, identifier, null);
			}

			return new Token(Symbol.IDENT, identifier, null);
		}

		String cs = String.valueOf((char)ch);
		ch = getChar();

		Token token = charMap.get(cs);
		
		if (token != null) {
			return token;
		}
				
		return new Token(Symbol.OTHER, cs, null);
	}

}
