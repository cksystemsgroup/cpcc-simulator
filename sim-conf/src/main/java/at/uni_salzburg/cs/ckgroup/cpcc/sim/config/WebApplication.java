/*
 * @(#) WebApplication.java
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

public class WebApplication {
	
	private static final String PROP_ARTIFACT = "artifact";
	private static final String PROP_CONTEXT_TEMPLATE = "context.template";
	private static final String PROP_MASTER_CONTEXT_TEMPLATE = "master.context.template";
	private static final String PROP_SLAVE_CONTEXT_TEMPLATE = "slave.context.template";
//	private static final String PROP_TEMPLATE_FILES = "template.files";
	private static final String PROP_QUANTITY = "quantity";
	private static final String PROP_ZONE_ASSIGNMENT = "zone.assignment";
	private static final String PROP_CENTRAL_ENGINE = "is.a.central.engine";

	private String groupId;
	private String artifactId;
	private String version;
	private String type;
	private String classifier;
	
	private String contextTemplate;
	private String masterContextTemplate;
	private String slaveContextTemplate;
	private boolean multiple;
	private TemplateFiles templateFiles;
	private boolean zoneAssigned;
	private boolean centralEngine;

	
	public void setFromProperties (String prefix, Properties props, List<String[]> pars, Map<String, String> configErrors) {
		String artifactProperty = prefix + PROP_ARTIFACT;
		pars.add(new String[]{artifactProperty});
		String artifactString = props.getProperty(artifactProperty);
		if (artifactString != null) {
			String[] art = artifactString.trim().split(":"); 
			if (art.length == 4 || art.length == 5) {
				groupId = art[0];
				artifactId = art[1];
				version = art[2];
				type = art[3];
				classifier = art.length == 5 ? art[4] : null;
			} else {
				configErrors.put(artifactProperty, "# incorrect artifact");
			}
		} else {
			configErrors.put(artifactProperty, "# missing");
		}
		
		String contextTemplateProperty = prefix + PROP_CONTEXT_TEMPLATE;
		pars.add(new String[]{contextTemplateProperty});
		contextTemplate = props.getProperty(contextTemplateProperty);
		if (contextTemplate == null) {
			configErrors.put(contextTemplateProperty, "# missing");
		}

		String masterContextTemplateProperty = prefix + PROP_MASTER_CONTEXT_TEMPLATE;
		masterContextTemplate = props.getProperty(masterContextTemplateProperty);
		if (masterContextTemplate != null) {
			pars.add(new String[]{masterContextTemplate});
		}

		String slaveContextTemplateProperty = prefix + PROP_SLAVE_CONTEXT_TEMPLATE;
		slaveContextTemplate = props.getProperty(slaveContextTemplateProperty);
		if (slaveContextTemplate != null) {
			pars.add(new String[]{slaveContextTemplate});
		}

		templateFiles = new TemplateFiles();
		templateFiles.setFromProperties(prefix, props, pars, configErrors);
		
		String quantityProperty = prefix + PROP_QUANTITY;
		pars.add(new String[]{quantityProperty});
		String quantityString = props.getProperty(quantityProperty);
		if (quantityString == null) {
			quantityString = "multiple";
			props.setProperty(quantityProperty, quantityString);
		}
		multiple = quantityString.equals("multiple");
		
		String zoneAssignedProperty = prefix + PROP_ZONE_ASSIGNMENT;
		zoneAssigned = Boolean.parseBoolean(props.getProperty(zoneAssignedProperty, "false"));
		if (zoneAssigned) {
			pars.add(new String[]{zoneAssignedProperty});
		}
		
		String centralEngineString = prefix + PROP_CENTRAL_ENGINE;
		centralEngine = Boolean.parseBoolean(props.getProperty(centralEngineString, "false"));
		if (centralEngine) {
			pars.add(new String[]{centralEngineString});
		}
		
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getType() {
		return type;
	}
	
	public String getClassifier() {
		return classifier;
	}

	public String getContextTemplate() {
		return contextTemplate;
	}

	public String getMasterContextTemplate() {
		return masterContextTemplate;
	}

	public String getSlaveContextTemplate() {
		return slaveContextTemplate;
	}
	
	public TemplateFiles getTemplateFiles() {
		return templateFiles;
	}

	public boolean isMultiple() {
		return multiple;
	}
	
	public boolean isZoneAssigned() {
		return zoneAssigned;
	}
	
	public boolean isCentralEngine() {
		return centralEngine;
	}
	
}
