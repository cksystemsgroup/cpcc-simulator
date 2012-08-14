/*
 * @(#) TemplateFiles.java
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
package at.uni_salzburg.cs.ckgroup.cpcc.sim.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TemplateFiles {

	private static final String PROP_TEMPLATE_FILES = "template.files";

	private Map<String, String> templateFiles;

	public void setFromProperties (String prefix, Properties props, List<String[]> pars, Map<String, String> configErrors) {
		String templateFilesProperty = prefix + PROP_TEMPLATE_FILES;
		String filesString = props.getProperty(templateFilesProperty);
		templateFiles = new HashMap<String, String>();
		if (filesString != null && !"".equals(filesString.trim())) {
			pars.add(new String[]{templateFilesProperty});			
			String[] fs = filesString.trim().split("\\s+");
			for (String f : fs) {
				String[] parts = f.split(":");
				String source = parts[0];
				String target = parts.length == 2 ? parts[1] : parts[0];
				if (target.endsWith(".vm")) {
					target = target.replaceAll("\\.vm$", "");
				}
				templateFiles.put(source, target);
			}
		}
	}
	
	public Map<String, String> getTemplateFiles() {
		return templateFiles;
	}

}
