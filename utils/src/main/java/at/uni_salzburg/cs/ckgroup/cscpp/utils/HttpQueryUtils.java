/*
 * @(#) HttpQueryUtils.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpQueryUtils {
	
	public static String simpleQuery(String url) throws IOException {

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;

		response = httpclient.execute(httpget);
		ByteArrayOutputStream bo = new ByteArrayOutputStream();

		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			int l;
			byte[] tmp = new byte[2048];
			while ((l = instream.read(tmp)) != -1) {
				bo.write(tmp, 0, l);
			}
		}

		return bo.toString();
	}
	
	public static String[] fileUpload(String uploadUrl, String name, byte[] byteArray) throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(uploadUrl);
		String paramName = "vehicle";
		String paramValue = name;
		
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		entity.addPart( paramName, new InputStreamBody((new ByteArrayInputStream(byteArray)), "application/zip" ));
		entity.addPart( paramName, new StringBody( paramValue, "text/plain", Charset.forName( "UTF-8" )));
		httppost.setEntity( entity );
		HttpResponse httpResponse = httpclient.execute( httppost );
		StatusLine statusLine = httpResponse.getStatusLine();
		String reason = statusLine.getReasonPhrase();
		int rc = statusLine.getStatusCode();
		String response = EntityUtils.toString( httpResponse.getEntity(), "UTF-8" );
		httpclient.getConnectionManager().shutdown();
		return new String[] {String.valueOf(rc), reason, response};
	}
}
