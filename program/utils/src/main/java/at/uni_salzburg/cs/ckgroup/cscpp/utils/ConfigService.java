/*
 * @(#) ConfigService.java
 *
 * This code is part of the JNavigator project.
 * Copyright (c) 2011  Clemens Krainer
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ConfigService extends DefaultService {
	
	Logger LOG = Logger.getLogger(ConfigService.class);

	public final static String ACTION_CONFIG_UPLOAD = "configUpload";
	
	public final static String PROP_WEB_APP_BASE_URL = "web.app.base.url";


	public ConfigService (IServletConfig configuraton) {
		super (configuraton);
	}
	
	@Override
	public void service(ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String servicePath = request.getRequestURI();
		if (request.getRequestURI().startsWith(request.getContextPath()))
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		
		String[] cmd = servicePath.trim().split("/+");
		if (cmd.length < 2) {
			emit404(request, response);
			return;
		}
		
		String action = cmd[2];
		MimeEntry uploadedFile = null;
		
		if (request.getContentType() != null) {
			MimeParser parser = new MimeParser(request.getContentType());
			List<MimeEntry> list = parser.parse(request.getInputStream());
			String name = null;
			String fileName = null;
			
			for (MimeEntry entry : list) {
				Map<String, String> headerMap = entry.getHeaders();
				String contentDisposition = headerMap.get(MimeEntry.CONTENT_DISPOSITION);
				
				if (contentDisposition.matches(".*\\sname=\"(\\S*)\"[\000-\377]*"))
					name = contentDisposition.replaceFirst(".*\\sname=\"(\\S*)\".*", "$1");
				
				if (contentDisposition.matches(".*\\sfilename=\"(\\S*)\"[\000-\377]*"))
					fileName = contentDisposition.replaceFirst(".*\\sfilename=\"(\\S*)\".*", "$1");
				
				LOG.debug("cdName=" + name + ", fileName=" + fileName);
				
				if (name != null && fileName != null && !"".equals(fileName)) {
					uploadedFile = entry;
					break;
				}
			}		
		}
		
		String nextPage;

		if (ACTION_CONFIG_UPLOAD.equals(action)) {
			File confFile = servletConfig.getConfigFile();
			if (uploadedFile != null) {
				MimeUtils.saveFile (uploadedFile, confFile);
				URI uri;
				try {
					uri = getWebAppContectUri(request);
				} catch (URISyntaxException e) {
					throw new ServletException(e);
				}
				Properties props = new Properties();
				props.load(new FileInputStream(confFile));
				props.setProperty(PROP_WEB_APP_BASE_URL, uri.toString());
				props.store(new FileOutputStream(confFile), "");

				LOG.info("Written: " + confFile);
				servletConfig.reloadConfigFile();
				LOG.info("Configuration uploaded.");
				nextPage = request.getContextPath() + "/config.tpl";
			} else {
				emit422(request, response);
				return;
			}
		} else{
			LOG.error("Can not handle: " + servicePath);
			emit404(request, response);
			return;
		}
		
		emit301 (request, response, nextPage);	
	}

	private URI getWebAppContectUri(HttpServletRequest request) throws URISyntaxException {
		String scheme = request.getScheme();
		String host = request.getServerName();
		int port = request.getServerPort();
		String path = request.getContextPath();
		return new URI(scheme, null, host, port, path, null, null);
	}
}
