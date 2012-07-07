/*
 * @(#) ActionPointQuery.java
 *
 * This code is part of the JNavigator project.
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
package at.uni_salzburg.cs.ckgroup.cscpp.viewer.json;

import java.io.IOException;

import at.uni_salzburg.cs.ckgroup.cscpp.utils.HttpQueryUtils;
import at.uni_salzburg.cs.ckgroup.cscpp.utils.IServletConfig;
import at.uni_salzburg.cs.ckgroup.cscpp.viewer.IMapperProxy;

public class ActionPointQuery implements IJsonQuery {
	
//	private IMapperProxy mapperProxy;

	public ActionPointQuery(IMapperProxy mapperProxy) {
//		this.mapperProxy = mapperProxy;
	}
	
	@Override
	public String execute(IServletConfig config, String[] parameters)
			throws IOException {
		
		StringBuilder b = new StringBuilder();
		
		for (int k=3; k < parameters.length; ++k) {
			if (k == 4) {
				b.append("//");
			} else if (k != 3) {
				b.append("/");
			}
			b.append(parameters[k].replace("%3A", ":"));
		}
		
		return HttpQueryUtils.simpleQuery(b.toString());
	}

}
