/*
 * @(#) TimeFormatter.java
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
package at.uni_salzburg.cs.ckgroup.cscpp.engine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeFormatter {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String simpleTimeStampFormat (long lime) {
		return sdf.format(new Date(lime));
	}
	
	public String formatCode (String code) {
        Pattern p = Pattern.compile ("\\(\\d+\\s");
        Matcher m = p.matcher (code);

        while (m.find ())
        {
                String time = code.substring (m.start ()+1, m.end ()-1);
                String newTimeString = simpleTimeStampFormat (Long.parseLong(time));
                String expr = code.substring (m.start (), m.end ()).replace ("(", "\\(");
                code = code.replaceAll (expr, "(" + newTimeString + ", ");
                m = p.matcher (code);
        }

        return code;
	}
}
