/*
 * @(#) SandboxContextFactory.java
 *
 * This code is part of the CPCC project.
 * Copyright (c) 2012  Clemens Krainer, Michael Lippautz
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
package org.cpcc.interpreter.sandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cpcc.interpreter.runtime.base.InstructionCountObserver;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class SandboxContextFactory extends ContextFactory {
	
	private static Map<Object, List<InstructionCountObserver>> icos = 
			Collections.synchronizedMap(new HashMap<Object, List<InstructionCountObserver>>());
	
	@Override
	protected Context makeContext() {
		Context context = super.makeContext();
		context.setWrapFactory(new SandboxWrapFactory());
		return context;
	}
	
	@Override
	protected void observeInstructionCount(Context context, int instructionCount) {
		if(icos.containsKey(context)){
			for (InstructionCountObserver obs : icos.get(context)) {
				obs.observeInstructioncound(instructionCount);
			}
		}
		super.observeInstructionCount(context, instructionCount);
	}
	
	public static void addInstructionCountObserver(Context context, InstructionCountObserver obs) {
		if (!icos.containsKey(context)) {
			icos.put(context, new ArrayList<InstructionCountObserver>());
		}
		if(!icos.get(context).contains(obs)) {
			icos.get(context).add(obs);
		}
	}
	
	public static void removeInstructionCountObserver(Context context, InstructionCountObserver obs) {
		if (icos.containsKey(context) && icos.get(context).contains(obs)){
			icos.get(context).remove(obs);
		}
	}
}
