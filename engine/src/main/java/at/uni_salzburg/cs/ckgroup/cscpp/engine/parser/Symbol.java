/*
 * @(#) Symbol.java
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

public enum Symbol {

	LEFT_PAREN	("("),				// left parenthesis symbol
	LEFT_BRACK	("["),				// left bracket symbol
	LEFT_BRACE	("{"),				// left brace symbol
	RIGHT_PAREN	(")"),				// right parenthesis symbol
	RIGHT_BRACK	("]"),				// right bracket symbol
	RIGHT_BRACE	("}"),				// right brace symbol
	PERIOD		("."),				// period symbol
	PLUS		("+"),				// positive sign / addition
	MINUS		("-"),				// negative sign / subtraction
	COLON		(":"),				// colon
	SEMICOLON	(";"),				// semicolon
	COMMA		(","),				// comma
	NUMBER_SIGN	("#"),				// number sign
	DIVIDE		("/"),				// arithmetic division
	
	COMMENT		(null),				// comment symbol
	NUMBER		(null),				// decimal number
	IDENT		(null),				// identifier symbol
	LITERAL		(null),				// string literal symbol
	END			(null),				// end
	OTHER		(null),				// other symbols

	POINT		("point"),			// action point position
	TOLERANCE	("tolerance"),		// action point tolerance specification

	AIRPRESSURE	("airPressure"),	// air pressure sensor
	ALTITUDE	("altitude"),		// altitude sensor
	COURSE		("course"),			// course over ground sensor
	PICTURE		("picture"),		// photo camera sensor
	RANDOM		("random"),			// random number sensor
	SONAR		("sonar"),			// sonar altitude over ground sensor
	SPEED		("speed"),			// speed sensor
	TEMPERATURE	("temperature");	// temperature sensor
	
	
	private String symbolString;
	
	Symbol (String symbolString) {
		this.symbolString = symbolString;
	}
	
	public String getSymbolString() {
		return symbolString;
	}
	
	public static Symbol getSymbol(String ss) {
		if (ss == null) {
			return null;
		}
		
		for (Symbol s : values()) {
			if (s != null && s.symbolString != null && s.symbolString.equalsIgnoreCase(ss)) {
				return s;
			}
		}
		
		return null;
	}
	
	public static Symbol getSymbol(char c) {
		
		for (Symbol s : values()) {
			if (s.symbolString != null && s.symbolString.length() == 1) {
				if (s.symbolString.charAt(0) == c) {
					return s;
				}
			}
		}
		
		return null;
	}
}
