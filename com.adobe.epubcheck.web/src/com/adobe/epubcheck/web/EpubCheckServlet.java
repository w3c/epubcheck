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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.adobe.epubcheck.api.EpubCheck;

public class EpubCheckServlet extends HttpServlet {

	static final long serialVersionUID = 0;

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		if (!ServletFileUpload.isMultipartContent(req)) {
			out
					.println("Invalid request type");
			return;
		}
		try {
			DiskFileItemFactory itemFac = new DiskFileItemFactory();
			// itemFac.setSizeThreshold(20000000); // bytes
			File repositoryPath = new File("upload");
			repositoryPath.mkdir();
			itemFac.setRepository(repositoryPath);
			ServletFileUpload servletFileUpload = new ServletFileUpload(itemFac);
			List fileItemList = servletFileUpload.parseRequest(req);
			Iterator list = fileItemList.iterator();
			FileItem book = null;
			while (list.hasNext()) {
				FileItem item = (FileItem) list.next();
				String paramName = item.getFieldName();
				if (paramName.equals("file"))
					book = item;
			}
			if (book == null) {
				out.println("Invalid request: no epub uploaded");
				return;
			}
			File bookFile = File.createTempFile("work", "epub");
			book.write(bookFile);
			EpubCheck epubCheck = new EpubCheck(bookFile, out);
			if( epubCheck.validate() )
				out.println("No errors or warnings detected");
			book.delete();
		} catch (Exception e) {
			out.println("Internal Server Error");
			e.printStackTrace(out);
		}
	}

}
