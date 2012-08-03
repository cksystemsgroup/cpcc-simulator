package org.cpcc.interpreter.preemption;

import org.junit.Test;

public class PreemptionTestCase {

	@Test
	public void testCase01() throws InterruptedException {
		
	    String js = "function VV() { " +
    		"println(\"hello world!\"); " +
    		"	for (var x=0; x < 10; ++x) { " +
    		"		println(\"Iteration: \" + x); " +
    		"		for (var t=0; t < 10000; ++t) { continue; } " +
    		"		sleep(1000); " +
    		"	}" +
    		"}";

		JScriptRunner runner = new JScriptRunner();
		runner.setJs(js);
		
		runner.start();
		
		try { Thread.sleep(5000); } catch (InterruptedException e) { }
		runner.interrupt();
		
		
		System.out.println("before preempt");
		runner.preempt();
		System.out.println("after preempt");
		
		runner.join();
	}

}
