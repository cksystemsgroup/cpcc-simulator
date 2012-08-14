/*
 * @(#) TomcatInstance.java
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

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TomcatInstance {
	
	private static final String PROP_NAME_TEMPLATE = "name.template";
	private static final String PROP_WEB_APPS = "webapps";
	private static final String PROP_QUANTITY = "quantity";
	
	private String nameTemplate;
	private String[] webApps;
	private boolean multiple;
	
	public void setFromProperties (String prefix, Properties props, List<String[]> pars, Map<String, String> configErrors) {

		String nameTemplateProperty = prefix + PROP_NAME_TEMPLATE;
		pars.add(new String[]{nameTemplateProperty});
		nameTemplate = props.getProperty(nameTemplateProperty);
		if (nameTemplate == null) {
			configErrors.put(nameTemplateProperty, "# missing");
		}
		
		String webAppsProperty = prefix + PROP_WEB_APPS;
		pars.add(new String[]{webAppsProperty});
		String webAppsString = props.getProperty(webAppsProperty);
		if (webAppsString == null || "".equals(webAppsString.trim())) {
			configErrors.put(webAppsProperty, "# missing");
			webApps = null;
		} else {
			webApps = webAppsString.trim().split("\\s*,\\s*");
		}
		
		String quantityProperty = prefix + PROP_QUANTITY;
		pars.add(new String[]{quantityProperty});
		String quantityString = props.getProperty(quantityProperty);
		if (quantityString == null) {
			quantityString = "multiple";
			props.setProperty(quantityProperty, quantityString);
		}
		multiple = quantityString.equals("multiple");	
	}
	
	public String getNameTemplate() {
		return nameTemplate;
	}
	
	public String[] getWebApps() {
		return webApps;
	}

	public boolean isMultiple() {
		return multiple;
	}

}
