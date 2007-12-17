package com.adobe.epubcheck.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.adobe.epubcheck.tool.Checker;

public class EPubCheckServlet extends HttpServlet {

	static final long serialVersionUID = 0;

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		if (!ServletFileUpload.isMultipartContent(req)) {
			out
					.println("<html><body><h1>Invalid request type</h1></body></html>");
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
			Checker.check(bookFile, out);
			book.delete();
		} catch (Exception e) {
			out.println("Internal Server Error");
			e.printStackTrace(out);
		}
	}

}
