package org.cpcc.interpreter.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import junit.framework.Assert;

import org.cpcc.interpreter.JSInterpreter;
import org.cpcc.interpreter.JSInterpreterBuilder;
import org.cpcc.interpreter.runtime.base.InstructionCountObserver;
import org.junit.Test;

public class InstructionCounterTest {

	private int icnt;
	
	private StringConsoleProvider consoleProvider;
	
	private class TestObserver implements InstructionCountObserver {
		@Override
		public void observeInstructioncound(int count) {
			icnt += count;
		}
	}
	
    private JSInterpreter createJSI(String resourceName) throws IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
		InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
		JSInterpreterBuilder b = new JSInterpreterBuilder();
		b.addCodefile(resourceName, fis);
//		b.addProvidedPackage(packageScope, o);
//		b.addProvidedTypes(type);
		JSInterpreter jsi = b.build();
		consoleProvider = new StringConsoleProvider();
		jsi.setConsoleProvider(consoleProvider);
		return jsi; 
    }
    
	@Test
	public void testIntructonCounterCallback() throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
		JSInterpreter jsi = createJSI("test-js/instruction-cnt.js");
		Assert.assertNotNull(jsi);
		
		jsi.addInstructionCountObserver(new TestObserver());
		jsi.setInstructionObserverThreshold(200);
		
		try {
			jsi.start();
			assertTrue(icnt > 200000);
			assertEquals("0.0\n", consoleProvider.result.toString());
		} finally {
			jsi.close();
		}
	}
}
