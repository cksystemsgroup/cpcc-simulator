/*
 * @(#) SandboxNativeJavaObject.java
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

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

public class SandboxNativeJavaObject extends NativeJavaObject {

	private static final long serialVersionUID = 6171062703545916867L;

	public SandboxNativeJavaObject(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType);
	}
	
	@Override
	public Object get(String propertyName, Scriptable obj) {
		if (propertyName.equals("getClass")) {
			return NOT_FOUND;
		}
		return super.get(propertyName, obj);
	}
}
