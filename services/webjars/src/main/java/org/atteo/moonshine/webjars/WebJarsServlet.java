/*
 * Copyright 2014 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atteo.moonshine.webjars;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebJarsServlet extends HttpServlet {
	private static final int BUFSIZE = 4096;
	private final String destination;

	public WebJarsServlet(String destination) {
		this.destination = destination;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String path = request.getPathInfo();
		if (path == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String mimetype = request.getServletContext().getMimeType(path);
		if (mimetype == null) {
			mimetype = "application/octet-stream";
		}
		response.setContentType(mimetype);

		try (InputStream stream = WebJarsServlet.class.getResourceAsStream(destination + path);
				DataInputStream dataStream = new DataInputStream(stream);
				ServletOutputStream out = response.getOutputStream()) {
			if (stream == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			int length;
			byte[] byteBuffer = new byte[BUFSIZE];
			while ((length = dataStream.read(byteBuffer)) != -1) {
				out.write(byteBuffer, 0, length);
			}
		}
	}
}
