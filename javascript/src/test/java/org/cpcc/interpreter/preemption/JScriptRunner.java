package org.cpcc.interpreter.preemption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.cpcc.interpreter.JSInterpreter;
import org.cpcc.interpreter.JSInterpreterBuilder;
import org.cpcc.interpreter.runtime.base.InstructionCountObserver;
import org.cpcc.interpreter.runtime.base.NullPositionProvider;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

public class JScriptRunner extends Thread {
	
	
//	private Context ctx = null;
//	
//	private ScriptableObject scope = null;
//
//	private Script script = null;
//
	private String js = null;
	
	private JSInterpreter jsi = null;
	
	private byte[] snapShot = null;
//
//	private MyDebugger debugger = null;

	private MyIObs iobs;

	
	@Override
	public void run() {
		try {
			iobs = new MyIObs();
//			debugger = new MyDebugger();
			
			JSInterpreterBuilder b = new JSInterpreterBuilder();
			b.addCodefile("program.js", new ByteArrayInputStream(js.getBytes()));
			jsi = b.build();
			jsi.addInstructionCountObserver(iobs);
			jsi.setInstructionObserverThreshold(10000);
//			jsi.getContext().setGeneratingDebug(true);
//			jsi.getContext().setDebugger(debugger, null);
			jsi.setPositionProvider(new NullPositionProvider());
			snapShot = jsi.start();
			System.out.println("continue script.");
			snapShot = jsi.start(snapShot);
			System.out.println("script ended." + (snapShot == null ? "ok" : "unfinished"));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//	    ctx = Context.enter();
//	    ctx.setOptimizationLevel(-1);
//	    scope = ctx.initStandardObjects();
//		try {
//			script = ctx.compileReader(new StringReader(js), "test cont", 1, null);
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//		
//	    try {
//	        ctx.executeScriptWithContinuations(script, scope);
//	    } catch (ContinuationPending pending) {
//	        ctx.resumeContinuation(pending.getContinuation(), scope, null);
//	    }
	}
	
	public void setJs(String js) {
		this.js = js;
	}
	
	public void preempt() {
//		while (ctx == null) {
//			try { Thread.sleep(100); } catch (InterruptedException e) { }
//		}
//		ctx.setDebugger(new MyDebugger(), null);
		
//		if (jsi == null) {
//			return;
//		}
//		Context context = jsi.getContext();
//		context.setDebugger(new MyDebugger(), null);
		
//		ContinuationPending cp = jsi.getContext().captureContinuation();
		
//		jsi.getContext().
		
		
//		jsi.addInstructioncountObserver(new MyIObs());
//		jsi.setInstructionObserverThreshold(1);
		
		iobs.preempt();
	}
	
	
	private class MyIObs implements InstructionCountObserver {
		
		private long invocations = 0;
		
//		private long lastInvocation = 0;
		
		private boolean preemption = false;
		
		public void preempt() {
			System.out.println("preempt!");
			preemption = true;
		}
		
		@Override
		public void observeInstructioncound(int count) {
			invocations += count;
			System.out.println("oic " + preemption + " invocations=" + invocations);
			if (preemption) {
				preemption = false;
				System.out.println("boing!");
//				Context.enter(jsi.getContext());
				Context.enter();
				try {
//					throw jsi.getContext().captureContinuation();
//					Context.throwAsScriptRuntimeEx(new Exception("lala"));
					throw new RuntimeException("Terminated because of extensive resource utilization.");
				} finally {
					Context.exit();
				}
			}
			
			
//			long x = System.currentTimeMillis();
//			double diff = x - lastInvocation;
//			lastInvocation = x;
//			double rate = count / diff;
//			System.out.println ("rate = " + rate + " " + count + " " + diff);
//			if (Double.isInfinite(rate)) {
//				System.out.println ("Slowing down a bit");
//				try { Thread.sleep(1000); } catch (InterruptedException e) { }
//			}
		}
	}
	
	
    public static class MyDebugger implements Debugger {
    	
        public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
        	System.out.println("get Frame");
            return new MyDebugFrame(fnOrScript);
        }
 
        @Override
        public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
        	System.out.println("handleCompilationDone 2 " + fnOrScript.getFunctionName());
        }
    }
 
    public static class MyDebugFrame implements DebugFrame {
        DebuggableScript fnOrScript;
 
        int continuationline = -1;
 
        MyDebugFrame(DebuggableScript fnOrScript) {
        	System.out.println("MyDebugFrame()");
            this.fnOrScript = fnOrScript;
        }
        
        @Override
        public void onEnter(Context cx, Scriptable activation, Scriptable thisObj, Object[] args) {
            System.out.println("onEnter");
        }

        @Override
        public void onLineChange(Context cx, int lineNumber) {
        	System.out.println("onLineChange " + lineNumber);
            continuationline = lineNumber;
            if (isBreakpoint(lineNumber)) {
                System.out.println("Breakpoint hit: " + fnOrScript.getSourceName() + ":" + lineNumber);
            }
        }
 
        @Override
        public void onExceptionThrown(Context cx, Throwable ex) {
        	System.out.println("onExceptionThrown");
        }
 
        @Override
        public void onExit(Context cx, boolean byThrow, Object resultOrException) {
            System.out.println("Frame exit, result=" + resultOrException);
            if (resultOrException instanceof ContinuationPending) {
                System.out.println("**found it! " + continuationline);
            }
        }
        
        private boolean isBreakpoint(int lineNumber) {
            // ...
            return true;
        }
 
        @Override
        public void onDebuggerStatement(Context cx) {
            System.out.println("onDebuggerStatement:" + cx.getDebuggerContextData());
            // TODO Auto-generated method stub
 
        }
    }
}
