/*
 * @(#) TaskListBuilder.java
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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.uni_salzburg.cs.ckgroup.course.PolarCoordinate;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.AbstractAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.AirPressure;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Altitude;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Course;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Picture;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Random;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Sonar;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Speed;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Temperature;

public class TaskListBuilder {
	
	@SuppressWarnings("serial")
	private final static Set<Symbol> actionSymbols = new HashSet<Symbol>() {{
		add(Symbol.AIRPRESSURE);
		add(Symbol.ALTITUDE);
		add(Symbol.COURSE);
		add(Symbol.PICTURE);
		add(Symbol.RANDOM);
		add(Symbol.SONAR);
		add(Symbol.SPEED);
		add(Symbol.TEMPERATURE);
	}};
	
	private File dataDir;
	
	public TaskListBuilder(File dataDir) {
		this.dataDir = dataDir;
	}

	public List<Task> build (Scanner scanner) throws IOException, ParseException {
		
		List<Task> taskList = new ArrayList<Task>();
		
		Token token = scanner.next();
		while (token.getSymbol() != Symbol.END ) {
			taskList.add(parseTask (scanner, token));
		}
		
		return taskList;
	}

	private Task parseTask(Scanner scanner, Token token) throws IOException, ParseException {
		
		if (Symbol.POINT != token.getSymbol()) {
			throw new ParseException(Symbol.POINT, scanner, token);
		}
		
		Token latitude = parseSignedNumber(scanner);
		Token longitude = parseSignedNumber(scanner);
		Token altitude = parseSignedNumber(scanner);
		
		if (Symbol.TOLERANCE != token.copyFields(scanner.next()).getSymbol()) {
			throw new ParseException(Symbol.TOLERANCE, scanner, token);
		}
		
		Token tolerance = parseSignedNumber(scanner);
		
		List<IAction> actionList = new ArrayList<IAction>();
		
		token.copyFields(scanner.next());
		while (actionSymbols.contains(token.getSymbol())) {
			IAction action = parseAction(scanner, token);
			actionList.add(action);
		}
		
		Task task = new Task();
		task.setPosition(new PolarCoordinate(latitude.getNumber().doubleValue(), longitude.getNumber().doubleValue(), altitude.getNumber().doubleValue()));
		task.setTolerance(tolerance.getNumber().doubleValue());
		task.setActionList(actionList);
		return task;
	}

	private Token parseSignedNumber(Scanner scanner) throws IOException, ParseException {
		Token token = scanner.next();
		boolean negate = Symbol.MINUS == token.getSymbol();
		
		if (negate) {
			token = scanner.next();
		}
		
		if (Symbol.NUMBER != token.getSymbol()) {
			throw new ParseException(Symbol.NUMBER, scanner, token);
		}
		
		if (negate) {
			token.setNumber(token.getNumber().multiply(BigDecimal.valueOf(-1)));
		}
		return token;
	}
	
	private IAction parseAction(Scanner scanner, Token token) throws IOException, ParseException {
		
		Symbol sym = token.getSymbol();
		
		if (Symbol.LEFT_PAREN != token.copyFields(scanner.next()).getSymbol()) {
			return buildAction(sym, null, null);
		}
		
		Token timeStamp = parseSignedNumber(scanner);
		if (Symbol.NUMBER != timeStamp.getSymbol()) {
			throw new ParseException(Symbol.NUMBER, scanner, timeStamp);
		}
		
		Token value = scanner.next();
		if (Symbol.COMMA == value.getSymbol()) {
			value = scanner.next();
		}
		
		if (Symbol.MINUS == value.getSymbol()) {
			value = scanner.next();
			value.setNumber(value.getNumber().multiply(BigDecimal.valueOf(-1)));
			if (Symbol.NUMBER != value.getSymbol()) {
				throw new ParseException(Symbol.NUMBER, scanner, value);
			}
		} else if (Symbol.PLUS == value.getSymbol()) {
			value = scanner.next();
			if (Symbol.NUMBER != value.getSymbol()) {
				throw new ParseException(Symbol.NUMBER, scanner, value);
			}
		}
		
		IAction action = buildAction(sym, timeStamp, value);
		if (action == null) {
			throw new ParseException(Symbol.OTHER, scanner, value);
		}
		
		token.copyFields(scanner.next());
		if (Symbol.RIGHT_PAREN != token.getSymbol()) {
			throw new ParseException(Symbol.RIGHT_PAREN, scanner, token);
		}
		
		token.copyFields(scanner.next());
		return action;
	}

	private IAction buildAction(Symbol sym, Token timeStamp, Token value) {
		AbstractAction action = null;
		Symbol expected = Symbol.NUMBER;
		
		switch (sym) {
		case AIRPRESSURE:	action = new AirPressure(); break;
		case ALTITUDE:		action = new Altitude(); break;
		case COURSE:		action = new Course(); break;
		case PICTURE:		action = new Picture(); ((Picture)action).setDataDir(dataDir); expected = Symbol.LITERAL; break;
		case RANDOM:		action = new Random(); break;
		case SONAR:			action = new Sonar(); break;
		case SPEED:			action = new Speed(); break;
		case TEMPERATURE:	action = new Temperature(); break;
		}
		
		if (timeStamp == null) {
			return action;
		}
		
		if (value == null || expected != value.getSymbol()) {
			return null;
		}
		
		switch (sym) {
		case AIRPRESSURE:
			((AirPressure)action).setAirPressure(value.getNumber().doubleValue());
			break;
		case ALTITUDE:
			((Altitude)action).setAltitudeOverGround(value.getNumber().doubleValue());
			break;
		case COURSE:
			((Course)action).setCourseOverGround(value.getNumber().doubleValue());
			break;
		case PICTURE:
			((Picture)action).setFilename(value.getItemString());
			break;
		case RANDOM:
			((Random)action).setRandom(Integer.valueOf(value.getNumber().intValue()));
			break;
		case SONAR:
			((Sonar)action).setSonar(value.getNumber().doubleValue());
			break;
		case SPEED:
			((Speed)action).setSpeedOverGround(value.getNumber().doubleValue());
			break;
		case TEMPERATURE:
			((Temperature)action).setTemperature(value.getNumber().doubleValue());
			break;
		}
		
		if (action != null) {
			action.setTimestamp(timeStamp.getNumber().longValue());
		}
		
		return action;
	}
	
}
