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

package com.adobe.epubcheck.overlay;

import java.io.IOException;
import java.io.InputStream;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OverlayChecker implements ContentChecker, DocumentValidator {

	OCFPackage ocf;

	Report report;

	String path;

	XRefChecker xrefChecker = null;

	GenericResourceProvider resourceProvider;

	private OverlayHandler overlayHandler = null;

	EPUBVersion version;

	static XMLValidator mediaOverlayValidator_30_RNC = new XMLValidator(
			"schema/30/media-overlay-30.rnc");

	static XMLValidator mediaOverlayValidator_30_SCH = new XMLValidator(
			"schema/30/media-overlay-30.sch");

	public OverlayChecker(OCFPackage ocf, Report report, String path,
			XRefChecker xrefChecker, EPUBVersion version) {
		this.ocf = ocf;
		this.resourceProvider = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.version = version;
	}

	public OverlayChecker(String path,
			GenericResourceProvider resourceProvider, Report report) {
		this.resourceProvider = resourceProvider;
		this.report = report;
		this.path = path;
	}

	public void runChecks() {
		if (!ocf.hasEntry(path))
			report.error(null, 0, 0, "File " + path + " is missing");
		else if (!ocf.canDecrypt(path))
			report.error(null, 0, 0, "File " + path + " cannot be decrypted");
		else {
			validate();
		}
	}

	public boolean validate() {
		int errorsSoFar = report.getErrorCount();
		int warningsSoFar = report.getWarningCount();
		InputStream in = null;
		try {
			in = resourceProvider.getInputStream(path);
			XMLParser overlayParser = new XMLParser(
					in, path,
					"application/smil+xml", report, version);
			overlayHandler = new OverlayHandler(path, xrefChecker,
					overlayParser, report);
			overlayParser.addValidator(mediaOverlayValidator_30_RNC);
			overlayParser.addValidator(mediaOverlayValidator_30_SCH);
			overlayParser.addXMLHandler(overlayHandler);
			overlayParser.process();
		} catch (IOException e) {
			report.error(path, -1, -1,
					String.format(Messages.MISSING_FILE, path));
		}finally {
			try {
				in.close();
			} catch (Exception e2) {
				
			}
		}

		return errorsSoFar == report.getErrorCount()
				&& warningsSoFar == report.getWarningCount();
	}
}
