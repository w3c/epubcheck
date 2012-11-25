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

package com.adobe.epubcheck.ops;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;

public class OPSCheckerTest {

	public void testValidateDocument(String fileName, String mimeType,
			int errors, int warnings, EPUBVersion version) {
		testValidateDocument(fileName, mimeType, errors, warnings, version,false);

	}

	public void testValidateDocument(String fileName, String mimeType,
			int errors, int warnings, EPUBVersion version, boolean verbose) {
		ValidationReport testReport = new ValidationReport(fileName, String.format(
				Messages.SINGLE_FILE, mimeType, version));
		String basepath = null;
		if (version == EPUBVersion.VERSION_2)
			basepath = "/20/single/";
		else if (version == EPUBVersion.VERSION_3)
			basepath = "/30/single/";
		
		GenericResourceProvider resourceProvider = null;
		if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
			resourceProvider = new URLResourceProvider(fileName);
		} else {
			URL fileURL = this.getClass().getResource(basepath+fileName);
			String filePath = fileURL!=null?fileURL.getPath():basepath+fileName;
			resourceProvider = new FileResourceProvider(filePath);
		}

		OPSChecker opsChecker = new OPSChecker(basepath + fileName, mimeType,
				resourceProvider, testReport, version);

		opsChecker.validate();

		if (verbose) {
			verbose = false;
			System.out.println(testReport);
		}

		assertEquals(errors, testReport.getErrorCount());
		assertEquals(warnings, testReport.getWarningCount());
	}

	@Test
	public void testValidateSVGRectInvalid() {
		testValidateDocument("svg/invalid/rect.svg", "image/svg+xml", 4, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateSVGRectValid() {
		testValidateDocument("svg/valid/rect.svg", "image/svg+xml", 0, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLEdits001() {
		testValidateDocument("xhtml/valid/edits-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLEmbed001() {
		testValidateDocument("xhtml/valid/embed-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLForms001() {
		testValidateDocument("xhtml/valid/forms-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLGlobalAttrs001() {
		testValidateDocument("xhtml/valid/global-attrs-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLOps001() {
		testValidateDocument("xhtml/valid/ops-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLOPSMATHML001() {
		testValidateDocument("xhtml/valid/ops-mathml-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLLINK() {
		testValidateDocument("xhtml/valid/link.xhtml", "application/xhtml+xml",
				0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLLINKInvalid() {
		testValidateDocument("xhtml/invalid/link.xhtml",
				"application/xhtml+xml", 2, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLXml11() {
		testValidateDocument("xhtml/invalid/xml11.xhtml",
				"application/xhtml+xml", 1, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTMLOPSMATHML002() {
		testValidateDocument("xhtml/valid/ops-mathml-002.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLOPSSVG001() {
		testValidateDocument("xhtml/valid/ops-svg-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLRuby001() {
		testValidateDocument("xhtml/valid/ruby-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLCanvas() {
		testValidateDocument("xhtml/valid/canvas.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLInvalidCanvasFallback() {
		testValidateDocument("xhtml/invalid/canvas-fallback.xhtml",
				"application/xhtml+xml", 1, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLSCH001() {
		testValidateDocument("xhtml/valid/sch-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLSections001() {
		testValidateDocument("xhtml/valid/sections-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLSSML() {
		testValidateDocument("xhtml/valid/ssml.xhtml", "application/xhtml+xml",
				0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLStyle001() {
		testValidateDocument("xhtml/valid/style-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLStyle002() {
		testValidateDocument("xhtml/invalid/style-001.xhtml",
				"application/xhtml+xml", 0, 1, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTMLSwitch001() {
		testValidateDocument("xhtml/valid/switch-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLTables001() {
		testValidateDocument("xhtml/valid/tables-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLText001() {
		testValidateDocument("xhtml/valid/text-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLTrigger() {
		testValidateDocument("xhtml/valid/trigger.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLData() {
		testValidateDocument("xhtml/valid/data.xhtml", "application/xhtml+xml",
				0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLPrefixes001() {
		testValidateDocument("xhtml/valid/prefixes-001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLInvalidPrefixes001() {
		testValidateDocument("xhtml/invalid/prefixes-001.xhtml",
				"application/xhtml+xml", 2, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLVideo() {
		testValidateDocument("xhtml/valid/video.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTML_OPSMATHML001() {
		testValidateDocument("xhtml/invalid/ops-mathml-001.xhtml",
				"application/xhtml+xml", 4, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTML_OPSMATHML002() {
		testValidateDocument("xhtml/invalid/ops-mathml-002.xhtml",
				"application/xhtml+xml", 7, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTML_SCH001() {
		testValidateDocument("xhtml/invalid/sch-001.xhtml",
				"application/xhtml+xml", 48, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTML_SVG001() {
		testValidateDocument("xhtml/invalid/svg-001.xhtml",
				"application/xhtml+xml", 2, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTML_Switch001() {
		testValidateDocument("xhtml/invalid/switch-001.xhtml",
				"application/xhtml+xml", 9, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTML_Trigger() {
		testValidateDocument("xhtml/invalid/trigger.xhtml",
				"application/xhtml+xml", 2, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_UnresolvedDTD() {
		testValidateDocument("ops/invalid/unresolved-entity.xhtml",
				"application/xhtml+xml", 1, 1, EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testValidateXHTML_DupeID() {
		testValidateDocument("ops/invalid/dupe-id.xhtml",
				"application/xhtml+xml", 2, 0, EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testValidateXHTML_httpequiv1() {
		testValidateDocument("xhtml/invalid/http-equiv-1.xhtml",
				"application/xhtml+xml", 1, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_httpequiv2() {
		testValidateDocument("xhtml/valid/http-equiv-1.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_SSMLemptyPh() {
		testValidateDocument("xhtml/invalid/ssml-empty-ph.xhtml",
				"application/xhtml+xml", 0, 2, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_issue153_valid() {
		testValidateDocument("xhtml/valid/issue153.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_issue153_invalid() {
		testValidateDocument("xhtml/invalid/issue153.xhtml",
				"application/xhtml+xml", 1, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_issue166_valid() {
		testValidateDocument("ops/valid/svg-foreignObject.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testValidateXHTML_doctype1_obsolete() {
		testValidateDocument("xhtml/invalid/doctype-1.xhtml",
				"application/xhtml+xml", 1, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_doctype1() {
		//<!DOCTYPE html>
		testValidateDocument("xhtml/valid/doctype-1.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTML_doctype2() {
		//<!DOCTYPE html SYSTEM "about:legacy-compat">
		testValidateDocument("xhtml/valid/doctype-2.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateXHTMLIssue204() { 
		testValidateDocument("xhtml/valid/issue204.xhtml",
				"application/xhtml+xml", 1, 1, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTMLStyleAttr001() { 
		testValidateDocument("xhtml/valid/styleAttr001.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTMLStyleAttr002() { 
		testValidateDocument("xhtml/invalid/styleAttr001.xhtml",
				"application/xhtml+xml", 0, 1, EPUBVersion.VERSION_3);
	}
	
	// this test should validate, see issue 173, need to wait for schema update.
//	@Test
//	public void testValidateXHTMLSVGwithRDF() { 
//		testValidateDocument("xhtml/valid/svg-rdf-001.xhtml",
//				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_3, true);
//	}
	
	@Test
	public void testValidateSVGIssue196() { 
		testValidateDocument("ops/valid/svg-font-face.svg",
				"image/svg+xml", 0, 0, EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testValidateXHTMLIssue215() { 
		testValidateDocument("ops/valid/issue215.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testValidateSVGIssue219() { 
		testValidateDocument("svg/valid/issue219.svg",
				"image/svg+xml", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTMLIssue222_223_20() {
		//foreignObject allowed outside switch, and <body> allowed inside
		testValidateDocument("ops/valid/issue222.xhtml",
				"application/xhtml+xml", 0, 0, EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testValidateXHTMLIssue222_223_30() {
		//in 3.0 foreignObject content must be flow as per 
		//http://idpf.org/epub/30/spec/epub30-contentdocs.html#confreq-svg-foreignObject
		//so the document gives 1 error
		testValidateDocument("svg/valid/issue222.xhtml",
				"application/xhtml+xml", 1, 0, EPUBVersion.VERSION_3);
	}
}
