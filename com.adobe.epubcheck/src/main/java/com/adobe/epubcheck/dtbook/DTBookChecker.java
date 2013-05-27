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

package com.adobe.epubcheck.dtbook;

import java.io.IOException;
import java.io.InputStream;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class DTBookChecker implements ContentChecker {

	OCFPackage ocf;

	Report report;

	String path;

	XRefChecker xrefChecker;

	EPUBVersion version;

	static XMLValidator dtbookValidator = new XMLValidator(
			"schema/20/rng/dtbook-2005-2.rng");

	public DTBookChecker(OCFPackage ocf, Report report, String path,
			XRefChecker xrefChecker, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.version = version;
	}

	public void runChecks() {
		if (!ocf.hasEntry(path))
			report.error(null, 0, 0, "DTBook file " + path + " is missing");
		else if (!ocf.canDecrypt(path))
			report.error(null, 0, 0, "DTBook file " + path
					+ " cannot be decrypted");
		else {
			XMLParser dtbookParser = null;
			InputStream in = null;
			try {
				in = ocf.getInputStream(path);
				dtbookParser = new XMLParser(in, path,
						"application/x-dtbook+xml", report, version);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}finally {
				try{
					in.close();
				}catch (Exception e) {

				}
			}
			dtbookParser.addValidator(dtbookValidator);
			DTBookHandler dtbookHandler = new DTBookHandler(dtbookParser, path,
					xrefChecker);
			dtbookParser.addXMLHandler(dtbookHandler);
			dtbookParser.process();
		}
	}
}
