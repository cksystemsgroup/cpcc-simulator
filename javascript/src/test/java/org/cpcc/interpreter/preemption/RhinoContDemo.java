package org.cpcc.interpreter.preemption;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;
 
public class RhinoContDemo {
 
    public static class ContClass implements Serializable {
 
        private static final long serialVersionUID = 5293591614442332806L;
 
        public void f() {
            Context ctx = Context.enter();
            try {
                ContinuationPending pending = ctx.captureContinuation();
                throw pending;
            } finally {
                Context.exit();
            }
        }
 
        public void log(String s) {
            System.out.println(s);
        }
 
    }
 
    public static class MyDebugger implements Debugger {
        public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, StringBuffer source) {
        }
 
        public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
            return new MyDebugFrame(fnOrScript);
        }
 
        @Override
        public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
 
        }
    }
 
    public static class MyDebugFrame implements DebugFrame {
        DebuggableScript fnOrScript;
 
        int continuationline = -1;
 
        MyDebugFrame(DebuggableScript fnOrScript) {
            this.fnOrScript = fnOrScript;
        }
 
        public void onEnter(Context cx, Scriptable activation, Scriptable thisObj, Object[] args) {
            System.out.println("Frame entered");
        }
 
        public void onLineChange(Context cx, int lineNumber) {
            continuationline = lineNumber;
            if (isBreakpoint(lineNumber)) {
                System.out.println("Breakpoint hit: " + fnOrScript.getSourceName() + ":" + lineNumber);
            }
        }
 
        public void onExceptionThrown(Context cx, Throwable ex) {
        }
 
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
            System.out.println(cx.getDebuggerContextData());
            // TODO Auto-generated method stub
 
        }
    }
 
    public static void main(String[] args) throws IOException {
        Context ctx = Context.enter();
        ctx.setDebugger(new MyDebugger(), null);
 
        ctx.setOptimizationLevel(-1);
        Scriptable scope = ctx.initStandardObjects();
        scope.put("cont", scope, Context.javaToJS(new ContClass(), scope));
        String js =
            "var a = 1;\n" +
            "cont.log(a);\n" +
            "var b = 2;\n" +
            "cont.log(b);\n" +
            "var c =cont.f();\n" +
            "cont.log(a);\n" +
            "cont.log(b);\n" +
            "cont.log(c)";
        Script script = ctx.compileReader(new StringReader(js), "test cont", 1, null);
        try {
            ctx.executeScriptWithContinuations(script, scope);
        } catch (ContinuationPending pending) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input = br.readLine();
            ctx.resumeContinuation(pending.getContinuation(), scope, input);
        }
 
    }
}
