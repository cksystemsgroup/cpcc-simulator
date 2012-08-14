/*
 * @(#) VariableReplacer.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.utils;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableReplacer
{
	private Properties properties;
	
	public VariableReplacer (Properties properties) {
		this.properties = properties;
	}
	
	public static String quoteList (String list)
	{
		String[] elements = list.trim ().split ("\\s*,\\s*");
		StringBuffer result = new StringBuffer ();
		for (int i = 0; i < elements.length; i++)
		{
			if (i != 0) result.append (",");
			result.append ("'");
			result.append (elements[i]);
			result.append ("'");
		}
		return result.toString ();
	}

	public String replace (String input)
	{
		Pattern p = Pattern.compile ("\\$\\{[^{}]+\\}");
		Matcher m = p.matcher (input);

		while (m.find ())
		{
			String var = input.substring (m.start () + 2, m.end () - 1);
			String prop = null;
			if (var.endsWith ("_quoted_list"))
			{
				prop = properties.getProperty (var.substring (0, var.length () - 12), "");
				prop = quoteList (prop);
			}
			else
			{
				prop = properties.getProperty (var);
			}
			String expr = input.substring (m.start (), m.end ()).replace ("$", "\\$").replace ("{", "\\{").replace ("}", "\\}");
			if (prop != null) {
				input = input.replaceAll (expr, prop);
			}
			m = p.matcher (input);
		}

		return input;
	}

}
