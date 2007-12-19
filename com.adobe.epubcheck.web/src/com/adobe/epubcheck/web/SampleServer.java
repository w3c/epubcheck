/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.adobe.epubcheck.web;

import java.io.File;

import org.mortbay.http.HttpServer;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHttpContext;
import org.mortbay.util.InetAddrPort;

public class SampleServer {

	public static void main(String[] args) {
		// Sample HTTP server using jetty
		try {
			HttpServer svr = new HttpServer();
			svr.addListener(new InetAddrPort(80));
			ResourceHandler resourceHandler = new ResourceHandler();
			File root = new File("http_root");
			ServletHttpContext cx = new ServletHttpContext();
			cx.setContextPath("");
			cx.addHandler(resourceHandler);
			cx.setResourceBase(root.getAbsolutePath());

			// common servlets
			cx.addServlet("Check", "/Check", EpubCheckServlet.class
					.getName());

			svr.addContext(cx);
			svr.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
