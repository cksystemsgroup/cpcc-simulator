package org.cpcc.interpreter.testing;

import org.cpcc.interpreter.JSInterpreter;
import org.cpcc.interpreter.runtime.TransientFunctions;
import org.cpcc.interpreter.runtime.TransientState;

public class JSFunctionsTransientMock extends TransientFunctions {

	private static final long serialVersionUID = 3409273822118853711L;

	public JSFunctionsTransientMock(TransientState transientState) {
		super(transientState);
	}
	
	public void printDummy() {
		JSInterpreter.getConsoleProvider().print(((TransientStateMock)transientState).test+"\n");
	}

}
