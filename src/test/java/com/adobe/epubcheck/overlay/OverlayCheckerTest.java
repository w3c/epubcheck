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

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;

public class OverlayCheckerTest {

	private static String basepath = "/30/single/overlays/";

	public void testValidateDocument(String fileName, int errors, int warnings) {
		testValidateDocument(fileName, errors, warnings,false);
	}

	public void testValidateDocument(String fileName, int errors, int warnings, boolean verbose) {
		ValidationReport testReport = new ValidationReport(fileName, String.format(
				Messages.SINGLE_FILE, "media overlay", "3.0"));

		GenericResourceProvider resourceProvider=null;
		if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
			resourceProvider = new URLResourceProvider(fileName);
		} else {
			URL fileURL = this.getClass().getResource(basepath+fileName);
			String filePath = fileURL!=null?fileURL.getPath():basepath+fileName;
			resourceProvider = new FileResourceProvider(filePath);
		}

		OverlayChecker overlayChecker = new OverlayChecker(basepath + fileName, resourceProvider,
				testReport);

		overlayChecker.validate();

		if (verbose) {
			verbose = false;
			System.out.println(testReport);
		}

		assertEquals(errors, testReport.getErrorCount());
		assertEquals(warnings, testReport.getWarningCount());
	}

	@Test
	public void testValidateDocumentValidOverlay001() {
		testValidateDocument("valid/overlay-001.smil", 0, 0);
	}

	@Test
	public void testValidateDocumentValidOverlay002() {
		testValidateDocument("valid/overlay-002.smil", 0, 0);
	}

	@Test
	public void testValidateDocumentValidOverlay003() {
		testValidateDocument("valid/overlay-003.smil", 0, 0);
	}

	@Test
	public void testValidateDocumentInvalidOverlay001() {
		testValidateDocument("invalid/overlay-001.smil", 1, 0);
	}

	@Test
	public void testValidateDocumentInvalidOverlay002() {
		testValidateDocument("invalid/overlay-002.smil", 1, 0);
	}

	@Test
	public void testValidateDocumentInvalidOverlay003() {
		testValidateDocument("invalid/overlay-003.smil", 2, 0);
	}

	@Test
	public void testValidateDocumentInvalidOverlay004() {
		testValidateDocument("invalid/overlay-004.smil", 2, 0);
	}

	@Test
	public void testValidateDocumentInvalidOverlay005() {
		testValidateDocument("invalid/overlay-005.smil", 7, 0);
	}

	@Test
	public void testValidateDocumentInvalidOverlay006() {
		testValidateDocument("invalid/overlay-006.smil", 5, 0);
	}
	
	@Test
	public void testValidateDocumentValidOverlay007() {
		testValidateDocument("valid/overlay-007.smil", 0, 0);
	}
}
