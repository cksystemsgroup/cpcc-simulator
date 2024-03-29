/*
 * @(#) DefaultService.java
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;


public class DefaultService implements IService
{
	public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	public static final String MIME_TYPE_TEXT_HTML = "text/html";
	
	private boolean runTemplateEngine;
	
	private String[] welcomePages = { "index.html", "index.tpl" };
	
	private IServletConfig servletConfig;
	
	public DefaultService (IServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}
	
	public void service (ServletConfig config, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		String servicePath = request.getRequestURI();
		
		if (request.getRequestURI().startsWith(request.getContextPath())) {
			servicePath = request.getRequestURI().substring(request.getContextPath().length());
		}
		
		String realPathString = config.getServletContext ().getRealPath (servicePath);
		File realPath = new File(realPathString);
		
		if (servicePath.length() == 0 || "/".equals(servicePath) || realPath.isDirectory()) {
			for (String w : welcomePages) {
				File indexPath = new File(realPath,w);
				if (indexPath.exists()) {
//					servicePath = "/"+w;
//					realPath = indexPath;
					emit301(request, response, request.getContextPath() + "/"+w);
					break;
				}
			}
		}
		
		if (servicePath.matches ("/.*-INF.*")) {
			response.setContentType(MIME_TYPE_TEXT_PLAIN);
			PrintWriter out = response.getWriter();
			out.println ("Your are not at all welcome!\nNow bugger off!");
			return;
		}
		
		if (!realPath.exists()) {
			emit404 (request, response);
			return;
		}	
		
		if (realPath.isDirectory()) {
			response.setContentType(MIME_TYPE_TEXT_HTML);
			emitDirectoryListing (config, response.getWriter(), request.getContextPath(), servicePath, realPath.list());
			return;
		}
		
		response.setContentType(getContentType(realPath));
		
		emitPrefix(config, request, response);
		
		if (runTemplateEngine) {
			FileReader reader = new FileReader(realPath);
			PrintWriter out = response.getWriter();
			emitVelocityRenderedFile (config, out, reader, request.getContextPath(), servicePath, request.getParameterMap());
		} else {
			FileInputStream inStream = new FileInputStream(realPath);
			emitFile (response.getOutputStream(), inStream);
			inStream.close();
		}
	}

	protected void emitPrefix(ServletConfig config, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// intentionally empty
	}

	protected void emitFile (OutputStream out, InputStream in) throws IOException {
		int c;
		while ( (c = in.read()) >= 0 ) {
			out.write(c);
		}
	}
	
	private void emitDirectoryListing(ServletConfig config, PrintWriter out, String contextPath, String servicePath, String[] list) {
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityEngine ve = new VelocityEngine();
        ve.init(props);
        VelocityContext context = new VelocityContext();
        context.put("contextPath", contextPath);
        context.put("servicePath", servicePath);
        context.put("servletConfig", config);
        context.put("properties", servletConfig.getProperties());
        context.put("systemProperties", System.getProperties());
        Arrays.sort(list);
        context.put("directoryListing", list);
        
        Template template = ve.getTemplate("html/directoryListing.vm");
		template.merge(context, out);
	}

	protected void emitVelocityRenderedFile (ServletConfig config, PrintWriter out, Reader reader, String contextPath, String servicePath, @SuppressWarnings("rawtypes") Map parameters) throws IOException {
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityEngine ve = new VelocityEngine();
        ve.init(props);
        VelocityContext context = new VelocityContext();
        context.put("contextPath", contextPath);
        context.put("servicePath", servicePath);
        context.put("servletConfig", config);
        context.put("properties", servletConfig.getProperties());
        context.put("parameters", parameters);
        context.put("systemProperties", System.getProperties());
        
		ve.evaluate(context, out, "lala", reader);
	}
	
	protected String getContentType (File path) {
		String[] parts = path.getName().split ("\\.");
		String suffix = parts[parts.length-1];

		runTemplateEngine = false;
		if ("htm".equalsIgnoreCase (suffix) || "html".equalsIgnoreCase (suffix)) {
			return MIME_TYPE_TEXT_HTML;
		} else if ("xhtml".equalsIgnoreCase (suffix)) {
			return MIME_TYPE_TEXT_HTML;
		} else if ("css".equalsIgnoreCase (suffix)) {
			return "text/css";
		} else if ("jpeg".equalsIgnoreCase (suffix)) {
			return "image/jpeg";
		} else if ("jpg".equalsIgnoreCase (suffix)) {
			return "image/jpeg";
		} else if ("js".equalsIgnoreCase (suffix)) {
			return "text/javascript";
		} else if ("gif".equalsIgnoreCase (suffix)) {
			return "image/gif";
		} else if ("png".equalsIgnoreCase (suffix)) {
			return "image/png";
		} else if ("txt".equalsIgnoreCase (suffix)) {
			return MIME_TYPE_TEXT_PLAIN;
		} else if ("gz".equalsIgnoreCase (suffix)) {
			return "application/x-gzip";
		} else if ("tpl".equalsIgnoreCase (suffix)) {
			runTemplateEngine = true;
			return MIME_TYPE_TEXT_HTML;
		}
		
		return MIME_TYPE_TEXT_PLAIN;
	}
	
	protected void emit200 (HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(200);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType(MIME_TYPE_TEXT_HTML);
		response.getWriter().print(
			"<html><head><title>OK - " + request.getRequestURI() + "</title></head>" +
			"<body><h1>HTTP Status 200 - " + request.getRequestURI() + "</h1></body></html>"
		);
	}
	
	protected void emit301 (HttpServletRequest request, HttpServletResponse response, String location) throws IOException {
		response.setStatus(301);
		response.setHeader("Location", location);
		response.setContentType(MIME_TYPE_TEXT_HTML);
	}

	protected void emit400 (HttpServletRequest request, HttpServletResponse response, String cause) throws IOException {
		response.setStatus(400);
		response.setContentType(MIME_TYPE_TEXT_HTML);
		response.getWriter().print(
			"<html><head><title>Bad Request - " + cause + "</title></head>" +
			"<body><h1>HTTP Status 400 - " + cause + "</h1></body></html>"
		);
	}
	
	protected void emit404 (HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(404);
		response.setContentType(MIME_TYPE_TEXT_HTML);
		response.getWriter().print(
			"<html><head><title>File not found - " + request.getRequestURI() + "</title></head>" +
			"<body><h1>HTTP Status 404 - " + request.getRequestURI() + "</h1></body></html>"
		);
	}
	
	protected void emit422 (HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(422);
		response.setContentType(MIME_TYPE_TEXT_HTML);
		response.getWriter().print(
			"<html><head><title>File not found - " + request.getRequestURI() + "</title></head>" +
			"<body><h1>HTTP Status 422 - " + request.getRequestURI() + "</h1></body></html>"
		);
	}
	
	protected void emitPlainText (HttpServletResponse response, String text) throws IOException {
		response.setStatus(200);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType(MIME_TYPE_TEXT_PLAIN);
		if (text != null) {
			response.getWriter().print(text);
		}
	}
	
	protected void emitByteArray (HttpServletResponse response, String contentType, byte[] bytes) throws IOException {
		response.setStatus(200);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType(contentType);
		response.getOutputStream().write(bytes);
	}

	public IServletConfig getServletConfig() {
		return servletConfig;
	}
	
}
