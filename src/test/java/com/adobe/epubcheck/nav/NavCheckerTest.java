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

package com.adobe.epubcheck.nav;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;

public class NavCheckerTest {

	private static String basepath = "/30/single/nav/";
	
	public void testValidateDocument(String fileName, int errors, int warnings) {
		testValidateDocument(fileName, errors, warnings,false);

	}

	public void testValidateDocument(String fileName, int errors, int warnings,
			boolean verbose) {
		ValidationReport testReport = new ValidationReport(fileName, String.format(
				Messages.SINGLE_FILE, "nav", "3.0"));

		GenericResourceProvider resourceProvider;
		if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
			resourceProvider = new URLResourceProvider(fileName);
		} else {
			URL fileURL = this.getClass().getResource(basepath+fileName);
			String filePath = fileURL!=null?fileURL.getPath():basepath+fileName;
			resourceProvider = new FileResourceProvider(filePath);
		}

		NavChecker navChecker = new NavChecker(resourceProvider, testReport, basepath
				+ fileName, "application/xhtml+xml", EPUBVersion.VERSION_3);

		navChecker.validate();

		if (verbose) {
			verbose = false;
			System.out.println(testReport);
		}

		assertEquals(errors, testReport.getErrorCount());
		assertEquals(warnings, testReport.getWarningCount());
	}

	// XXX The mimeType of the nav document should be nav; this way it can be
	// tested as a nav file
	@Test
	public void testValidateDocumentValidMinimalNav() {
		testValidateDocument("valid/minimal.xhtml", 0, 0);
	}

	@Test
	public void testValidateDocumentValidNav001() {
		testValidateDocument("valid/nav001.xhtml", 0, 0);
	}

	@Test
	public void testValidateDocumentNoTocNav() {
		testValidateDocument("invalid/noTocNav.xhtml", 3, 0);
	}

//	@Test
//	public void testValidateDocumentNoTocNavFromURL() {
//		testValidateDocument("http://www.interq.ro/bgd/noTocNav.xhtml", 3, 0);
//	}

	@Test
	public void testValidateDocumentHText() {
		testValidateDocument("invalid/h-text.xhtml", 8, 0);
	}

	@Test
	public void testValidateDocumenNavLabels001() {
		testValidateDocument("invalid/nav-labels-001.xhtml", 1, 0);
	}

	@Test
	public void testValidateDocumentNavLabels002() {
		testValidateDocument("invalid/nav-labels-001.xhtml", 1, 0);
	}

	@Test
	public void testValidateDocumentNavLandmarks001() {
		testValidateDocument("invalid/nav-landmarks-001.xhtml", 1, 0);
	}

	@Test
	public void testValidateDocumentNavNoPagelist001() {
		testValidateDocument("invalid/nav-pagelist-001.xhtml", 1, 0);
	}

	@Test
	public void testValidateDocumentNavNoToc() {
		testValidateDocument("invalid/nav-no-toc.xhtml", 1, 0);
	}

	@Test
	public void testValidateDocumentNavReqHeading() {
		testValidateDocument("invalid/req-heading.xhtml", 1, 0);
	}

}
