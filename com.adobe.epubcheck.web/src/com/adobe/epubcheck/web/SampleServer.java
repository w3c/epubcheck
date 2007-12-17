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
			cx.addServlet("Check", "/Check", EPubCheckServlet.class
					.getName());

			svr.addContext(cx);
			svr.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
