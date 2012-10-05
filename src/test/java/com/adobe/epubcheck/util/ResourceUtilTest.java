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

package com.adobe.epubcheck.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import com.adobe.epubcheck.opf.VersionRetriever;

public class ResourceUtilTest {


	private static String basepath = "/30/single/opf/retrieveVersion/";


	/*
	 * TEST DEBUG FUNCTION
	 */
	public void testVersion(String fileName, int errors, int warnings) {
		testVersion(fileName, errors, warnings,false);
	}

	public void testVersion(String fileName, int errors, int warnings,boolean verbose) {

		ValidationReport testReport = new ValidationReport(fileName, Messages.OPV_VERSION_TEST);

		GenericResourceProvider resourceProvider=null;
		if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
			resourceProvider = new URLResourceProvider(fileName);
		}
		else {
			URL fileURL = this.getClass().getResource(basepath+fileName);
			String filePath = fileURL!=null?fileURL.getPath():basepath+fileName;
			resourceProvider = new FileResourceProvider(filePath);
		}

		try {
			new VersionRetriever(fileName, testReport)
					.retrieveOpfVersion(resourceProvider.getInputStream(basepath
							+ fileName));
		} catch (InvalidVersionException e) {
			testReport.error(fileName, -1, -1, e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (verbose) {
			verbose = false;
			System.out.println(testReport);
		}

		assertEquals(errors, testReport.getErrorCount());
		assertEquals(warnings, testReport.getWarningCount());
	}

	@Test
	public void testRetrieveVersionValidVersion() {
		testVersion("validVersion.opf", 0, 0);
	}

	@Test
	public void testRetrieveVersionNoPackageElement() {
		testVersion("noPackageElement.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionNoVersionAttribute() {
		testVersion("noVersion.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionNoEqualSign() {
		testVersion("noEqual.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionValueWithoutQuotes() {
		testVersion("valueWithoutQuotes.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionSpacesBetweenQuotes() {
		testVersion("spacesBetweenQuotes.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionSpacesInValue() {
		testVersion("spacesInValue.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionVersion123323() {
		testVersion("version123.323.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionNoPointInValue() {
		testVersion("noPointInValue.opf", 1, 0);
	}

	@Test
	public void testRetrieveVersionNegativeVersion() {
		testVersion("negativeVersion.opf", 1, 0);
	}

}
