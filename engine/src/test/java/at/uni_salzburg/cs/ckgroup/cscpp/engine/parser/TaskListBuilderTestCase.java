package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.AbstractAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.AirPressure;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.IAction;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Picture;
import at.uni_salzburg.cs.ckgroup.cscpp.engine.actions.Temperature;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.FileUtils;

public class TaskListBuilderTestCase {
	
	private static final String PREFIX = "at/uni_salzburg/cs/ckgroup/cscpp/engine/parser/";
	private static final String TEST_01_PROGRAM = PREFIX + "ScannerTest01.txt";
	private static final String TEST_02_PROGRAM = PREFIX + "ScannerTest02.txt";
	
	private File dataDir;

	@Before
	public void setUp() throws Exception {
		dataDir = File.createTempFile("tlbtc", "dir", new File("/tmp"));
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.removeRecursively(dataDir);
	}
	
	@Test
	public void testCase01() throws IOException, ParseException {
		
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(TEST_01_PROGRAM);
		assertNotNull(inStream);
		
		Scanner scanner = new Scanner(inStream);
		
		TaskListBuilder builder = new TaskListBuilder(dataDir);
		
		List<Task> taskList = builder.build(scanner);
		assertEquals(3, taskList.size());
		
		Task task = taskList.get(0);
		assertEquals(123.45, task.getPosition().getLatitude(), 1E-9);
		assertEquals(2134234.345, task.getPosition().getLongitude(), 1E-9);
		assertEquals(34, task.getPosition().getAltitude(), 1E-9);
		assertEquals(12.3, task.getTolerance(), 1E-9);
		
		assertEquals(2, task.getActionList().size());
		
		IAction action = (IAction)task.getActionList().get(0);
		assertFalse(action.isComplete());
		assertTrue(action instanceof Picture);
		assertNull(((Picture)action).getFilename());
		
		action = (IAction)task.getActionList().get(1);
		assertFalse(action.isComplete());
		assertTrue(action instanceof Temperature);
		assertNull(((Temperature)action).getTemperature());
		
		
		task = taskList.get(1);
		assertEquals(1, task.getPosition().getLatitude(), 1E-9);
		assertEquals(1, task.getPosition().getLongitude(), 1E-9);
		assertEquals(1, task.getPosition().getAltitude(), 1E-9);
		assertEquals(100, task.getTolerance(), 1E-9);
		
		assertEquals(1, task.getActionList().size());
		
		action = (IAction)task.getActionList().get(0);
		assertFalse(action.isComplete());
		assertTrue(action instanceof Temperature);
		assertNull(((Temperature)action).getTemperature());
		
		
		task = taskList.get(2);
		assertEquals(2, task.getPosition().getLatitude(), 1E-9);
		assertEquals(2, task.getPosition().getLongitude(), 1E-9);
		assertEquals(2, task.getPosition().getAltitude(), 1E-9);
		assertEquals(1.2, task.getTolerance(), 1E-9);
		
		assertEquals(1, task.getActionList().size());
		
		action = (IAction)task.getActionList().get(0);
		assertFalse(action.isComplete());
		assertTrue(action instanceof Picture);
		assertNull(((Picture)action).getFilename());
	}


	@Test
	public void testCase02() throws IOException, ParseException {
		
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(TEST_02_PROGRAM);
		assertNotNull(inStream);
		
		Scanner scanner = new Scanner(inStream);
		
		TaskListBuilder builder = new TaskListBuilder(dataDir);
		
		List<Task> taskList = builder.build(scanner);
		assertEquals(3, taskList.size());
		
		// first task
		Task task = taskList.get(0);
		assertEquals(47.69292705, task.getPosition().getLatitude(), 1E-9);
		assertEquals(13.38507593, task.getPosition().getLongitude(), 1E-9);
		assertEquals(-20.000, task.getPosition().getAltitude(), 1E-9);
		assertEquals(5.0, task.getTolerance(), 1E-9);
		
		assertEquals(3, task.getActionList().size());
		
		AbstractAction action = (AbstractAction)task.getActionList().get(0);
		assertTrue(action.isComplete());
		assertEquals(1342217901294L, action.getTimestamp());
		assertTrue(action instanceof Picture);
		assertEquals("img1241582018627917693.png", ((Picture)action).getFilename());
		
		action = (AbstractAction)task.getActionList().get(1);
		assertTrue(action.isComplete());
		assertEquals(1342217901297L, action.getTimestamp());
		assertTrue(action instanceof Temperature);
		assertEquals(-12.3, ((Temperature)action).getTemperature(), 1E-9);
		
		action = (AbstractAction)task.getActionList().get(2);
		assertTrue(action.isComplete());
		assertEquals(1342217901300L, action.getTimestamp());
		assertTrue(action instanceof AirPressure);
		assertEquals(1094.8, ((AirPressure)action).getAirPressure(), 1E-9);
		
		
		// second task
		task = taskList.get(1);
		assertEquals(-47.69291983, task.getPosition().getLatitude(), 1E-9);
		assertEquals(13.38562846, task.getPosition().getLongitude(), 1E-9);
		assertEquals(20.000, task.getPosition().getAltitude(), 1E-9);
		assertEquals(5.0, task.getTolerance(), 1E-9);
		
		assertEquals(3, task.getActionList().size());
		
		action = (AbstractAction)task.getActionList().get(0);
		assertTrue(action.isComplete());
		assertEquals(1342217919834L, action.getTimestamp());
		assertTrue(action instanceof Picture);
		assertEquals("img1668021337803956345.png", ((Picture)action).getFilename());
		
		action = (AbstractAction)task.getActionList().get(1);
		assertTrue(action.isComplete());
		assertEquals(1342217919836L, action.getTimestamp());
		assertTrue(action instanceof Temperature);
		assertEquals(7.8, ((Temperature)action).getTemperature(), 1E-9);
		
		action = (AbstractAction)task.getActionList().get(2);
		assertTrue(action.isComplete());
		assertEquals(1342217919839L, action.getTimestamp());
		assertTrue(action instanceof AirPressure);
		assertEquals(1084.7, ((AirPressure)action).getAirPressure(), 1E-9);
		
		
		// third task
		task = taskList.get(2);
		assertEquals(47.69294511, task.getPosition().getLatitude(), 1E-9);
		assertEquals(-13.38582999, task.getPosition().getLongitude(), 1E-9);
		assertEquals(20.000, task.getPosition().getAltitude(), 1E-9);
		assertEquals(5.0, task.getTolerance(), 1E-9);
		
		assertEquals(3, task.getActionList().size());
		
		action = (AbstractAction)task.getActionList().get(0);
		assertTrue(action.isComplete());
		assertEquals(1342217933771L, action.getTimestamp());
		assertTrue(action instanceof Picture);
		assertEquals("img963661589640754782.png", ((Picture)action).getFilename());
		
		action = (AbstractAction)task.getActionList().get(1);
		assertTrue(action.isComplete());
		assertEquals(1342217933774L, action.getTimestamp());
		assertTrue(action instanceof Temperature);
		assertEquals(30.0, ((Temperature)action).getTemperature(), 1E-9);
		
		action = (AbstractAction)task.getActionList().get(2);
		assertTrue(action.isComplete());
		assertEquals(1342217933777L, action.getTimestamp());
		assertTrue(action instanceof AirPressure);
		assertEquals(1094.0, ((AirPressure)action).getAirPressure(), 1E-9);
	}
	
}
