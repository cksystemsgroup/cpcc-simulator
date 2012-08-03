package org.cpcc.interpreter.runtime.base;

public class SystemConsoleProviderImpl implements ConsoleProvider {
	@Override
	public void print(String message) {
		System.out.print(message);
	}
}
