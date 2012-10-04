/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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

package com.adobe.epubcheck.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.helpers.ParserFactory;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.Messages;

public class CSSChecker implements ContentChecker {

	private OCFPackage ocf;
	private Report report;
	private String path;
	private XRefChecker xrefChecker;
	private EPUBVersion version;
	private static final String SAC_PROPERTY = "org.w3c.css.sac.parser";
	
	public CSSChecker(OCFPackage ocf, Report report, String path,
			XRefChecker xrefChecker, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.version = version;
	}

	public void runChecks() {
		
		InputStream is = null;
		try {
			if (!ocf.hasEntry(path)) {
				report.error(null, 0, 0,
						String.format(Messages.MISSING_FILE, path));
				return;
			}
			
			String systemProp = System.getProperty(SAC_PROPERTY);
			if(systemProp == null || systemProp.length() < 1) {
				System.setProperty("org.w3c.css.sac.parser",
						"org.apache.batik.css.parser.Parser");
			}
			
			ParserFactory pf = new ParserFactory();
			
			Parser parser = pf.makeParser();

			// System.out.println("SACParser : " + parser.getClass().getName());
			
			CSSHandler ch = new CSSHandler(path, xrefChecker, report, version);
			parser.setDocumentHandler(ch);
			parser.setErrorHandler(ch);

			InputSource input = new InputSource();
			is = ocf.getInputStream(path);
			input.setByteStream(is);
			input.setURI(path);
						
			parser.parseStyleSheet(input);
			
		} catch (Exception e) {
			report.error(path, -1, 0, e.getMessage());
		} finally {			
			try{
				is.close();
			}catch (Exception e) {
				
			}
		}

	}

	class NullOutputStream extends OutputStream {
		@Override
		public void write(int arg0) throws IOException {

		}
	}

}
