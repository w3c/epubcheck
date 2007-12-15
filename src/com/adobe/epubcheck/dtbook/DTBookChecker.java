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

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.Report;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class DTBookChecker implements ContentChecker {

	ZipFile zip;

	Report report;

	String path;

	XRefChecker xrefChecker;
	
	static XMLValidator dtbookValidator = new XMLValidator("rng/dtbook-2005-2.rng");
	
	public DTBookChecker(ZipFile zip, Report report, String path, XRefChecker xrefChecker) {
		this.zip = zip;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
	}
	
	public void runChecks() {
		ZipEntry opfEntry = zip.getEntry(path);
		if (opfEntry == null)
			report.error(null, 0, "DTBook file " + path + " is missing");
		else {
			XMLParser dtbookParser = new XMLParser(zip, path, report);
			dtbookParser.addValidator(dtbookValidator);
			DTBookHandler dtbookHandler = new DTBookHandler(dtbookParser, path, xrefChecker);
			dtbookParser.addXMLHandler(dtbookHandler);
			dtbookParser.process();
		}		
	}

}
