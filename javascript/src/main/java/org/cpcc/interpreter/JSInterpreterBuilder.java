/*
 * @(#) JSInterpreterBuilder.java
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
package org.cpcc.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cpcc.interpreter.runtime.base.BuiltinFunctions;
import org.cpcc.interpreter.runtime.base.NullPositionProvider;
import org.cpcc.interpreter.runtime.base.SystemConsoleProviderImpl;
import org.cpcc.interpreter.runtime.types.LatLngAlt;
import org.cpcc.interpreter.runtime.types.SensorValue;
import org.cpcc.interpreter.runtime.types.VirtualVehicleActionPoint;
import org.cpcc.interpreter.runtime.types.VirtualVehicleFile;
import org.cpcc.interpreter.runtime.types.VirtualVehiclePrintWriter;
import org.cpcc.interpreter.sandbox.SandboxContextFactory;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSInterpreterBuilder {

	private String[] builtinFns = {
		"getpos", "println", "migrate", "sleep", "random", "flyTo"
	};

	@SuppressWarnings("serial")
	private static final Set<Class<? extends Scriptable>> builtinTypes = new HashSet<Class<? extends Scriptable>>() {{
		add(LatLngAlt.class);
		add(VirtualVehicleFile.class);
		add(VirtualVehiclePrintWriter.class);
		add(SensorValue.class);
		add(VirtualVehicleActionPoint.class);
	}};
	
	
	private Map<String,Object> providedPackages = new HashMap<String, Object>();

	private Set<Class<? extends Scriptable>> providedTypes = new HashSet<Class<? extends Scriptable>>();
	
	private Map<String,InputStream> codeFiles = new HashMap<String, InputStream>();
	
	public void addProvidedPackage(String packageScope, Object o) {
		providedPackages.put(packageScope, o);
	}
	
	public void addProvidedTypes(Class<? extends Scriptable> type) {
		providedTypes.add(type);
	}
	
	public void addCodefile(String name, InputStream inputStream) {
		codeFiles.put(name, inputStream);
	}
	
	public JSInterpreter build () throws IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
		
		ContextFactory contextFactory = new SandboxContextFactory();
		
		if(!ContextFactory.hasExplicitGlobal()) {
			ContextFactory.initGlobal(contextFactory);
		}
		Context context = ContextFactory.getGlobal().enterContext();

		ScriptableObject contextScope = context.initStandardObjects();
		JSInterpreter.setContextScope(contextScope);
		
		for (Entry<String, Object> e : providedPackages.entrySet()) {
			Object wrappedOut = Context.javaToJS(e.getValue(), contextScope);
			ScriptableObject.putProperty(contextScope, e.getKey(), wrappedOut);
		}

		context.setClassShutter(new MyClassShutter(providedPackages.keySet()));

		context.setOptimizationLevel(-1); 
		
		contextScope.defineFunctionProperties(builtinFns, BuiltinFunctions.class, ScriptableObject.DONTENUM);
		
		for (Class<? extends Scriptable> bt : builtinTypes){
			ScriptableObject.defineClass(contextScope, bt, true);
		}
		
		for (Class<? extends Scriptable> bt : providedTypes){
			ScriptableObject.defineClass(contextScope, bt, true);
		}
		
		for (Entry<String, InputStream> e : codeFiles.entrySet()) { 
			context.evaluateReader(contextScope, new InputStreamReader(e.getValue()), e.getKey(), 1, null);
		}
		
		JSInterpreter jsi = new JSInterpreter();
		
		jsi.setConsoleProvider(new SystemConsoleProviderImpl());
		jsi.setPositionProvider(new NullPositionProvider());
		
		return jsi;
	}
	
	
	private class MyClassShutter implements ClassShutter {
		
		private Set<String> allowedPackages;

		public MyClassShutter(Set<String> allowedPackages) {
			this.allowedPackages = allowedPackages;
		}
		
		@Override
		public boolean visibleToScripts(String className) {
			for(String allowed : allowedPackages) {
				if(allowed.startsWith(className)) {
					return true;
				}
			}
			return false;
		}
	}
	
}
