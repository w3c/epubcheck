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

package com.adobe.epubcheck.ocf;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OCFChecker {

	ZipFile zip;

	Report report;

	static XMLValidator containerValidator = new XMLValidator("rng/container.rng");
	
	public OCFChecker(ZipFile zip, Report report) {
		this.zip = zip;
		this.report = report;
	}

	public void runChecks() {
		String containerEntry = "META-INF/container.xml";
		ZipEntry container = zip.getEntry("META-INF/container.xml");
		if (container == null)
			report.error(null, 0, "META-INF/container.xml is missing");
		else {
			XMLParser containerParser = new XMLParser(zip, containerEntry, report);
			OCFHandler containerHandler = new OCFHandler(containerParser);
			containerParser.addXMLHandler( containerHandler );
			containerParser.addValidator(containerValidator);
			containerParser.process();
			String rootPath = containerHandler.getRootPath();
			
			OPFChecker opfChecker = new OPFChecker(zip, report, rootPath);
			opfChecker.runChecks();
		}
	}
}
