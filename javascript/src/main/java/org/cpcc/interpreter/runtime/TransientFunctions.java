/*
 * @(#) TransientFunctions.java
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
package org.cpcc.interpreter.runtime;

import java.io.Serializable;


public class TransientFunctions implements Serializable {

	private static final long serialVersionUID = -4724181456829313751L;
	
	protected transient TransientState transientState;
	
	public TransientFunctions(TransientState transientState) {
		this.transientState = transientState;
	}

	public void setTransientState(TransientState transientState){
		this.transientState = transientState;
	}
}
