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

package com.adobe.epubcheck.opf;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;

public class OPFCheckerTest {

	public void testValidateDocument(String fileName, int errors, int warnings,
			EPUBVersion version) {
		testValidateDocument(fileName, errors, warnings, version,false);

	}

	public void testValidateDocument(String fileName, int errors, int warnings,
			EPUBVersion version, boolean verbose) {
		ValidationReport testReport = new ValidationReport(fileName, String.format(
				Messages.SINGLE_FILE, "opf", version.toString()));

		GenericResourceProvider resourceProvider;
		if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
			resourceProvider = new URLResourceProvider(fileName);
		} else {
			String basepath = null;
			if (version == EPUBVersion.VERSION_2)
				basepath = "/20/single/opf/";
			else if (version == EPUBVersion.VERSION_3)
				basepath = "/30/single/opf/";
			URL fileURL = this.getClass().getResource(basepath+fileName);
			String filePath = fileURL!=null?fileURL.getPath():basepath+fileName;
			resourceProvider = new FileResourceProvider(filePath);
		}

		OPFChecker opfChecker=null;
		if (version == EPUBVersion.VERSION_2)
			opfChecker = new OPFChecker("test_single_opf", resourceProvider,
					testReport);
		else if (version == EPUBVersion.VERSION_3)
			opfChecker = new OPFChecker30("test_single_opf", resourceProvider,
					testReport);

		opfChecker.validate();

		if (verbose) {
			System.out.println(testReport);
		}

		assertEquals(errors, testReport.getErrorCount());
		assertEquals(warnings, testReport.getWarningCount());
	}

	@Test
	public void testValidateDocumentValidOPFBase001() {
		testValidateDocument("valid/base-001.opf", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentValidOPFBindings001() {
		testValidateDocument("valid/bindings-001.opf", 0, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentValidOPFMediaOverlay001() {
		testValidateDocument("valid/media-overlay-001.opf", 0, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentValidOPFMediaOverlay002() {
		testValidateDocument("valid/media-overlay-002.opf", 0, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentValidOPFMinimal() {
		testValidateDocument("valid/minimal.opf", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidOPFDcDate1() {
		testValidateDocument("valid/date-1.opf", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidOPFDcDate2() {
		testValidateDocument("valid/date-2.opf", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidOPFDcDate3() {
		testValidateDocument("valid/date-3.opf", 0, 0, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testInvalidOPFDcDate1() {
		testValidateDocument("invalid/date-1.opf", 0, 1, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testInvalidOPFDcDate2() {
		testValidateDocument("invalid/date-2.opf", 0, 1, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testInvalidOPFNullIdentifier() {
		testValidateDocument("invalid/null-identifier.opf", 1, 0, EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testValidateDocumentValidOPFSvg() {
		testValidateDocument("valid/lorem-svg.opf", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentValidOPFSvgFallback() {
		testValidateDocument("valid/lorem-svg-fallback.opf", 0, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentMalformed() {
		testValidateDocument("invalid/malformed.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentNoMetadataElement() {
		testValidateDocument("invalid/noMetadataElement.opf", 3, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentNoNav() {
		testValidateDocument("invalid/noNav.opf", 2, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentInvalidMetaAbout() {
		testValidateDocument("invalid/invalidMetaAbout.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentNoDcNamespace() {
		testValidateDocument("invalid/noDcNamespace.opf", 3, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentBindings001() {
		testValidateDocument("invalid/bindings-001.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentCoverImage() {
		testValidateDocument("invalid/cover-image.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentFallback001() {
		testValidateDocument("invalid/fallback-001.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentFallback002() {
		testValidateDocument("invalid/fallback-002.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentIdUnique() {
		testValidateDocument("invalid/id-unique.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentItemref001() {
		testValidateDocument("invalid/itemref-001.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentMediaOverlay001() {
		testValidateDocument("invalid/media-overlay-001.opf", 4, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentMediaOverlay002() {
		testValidateDocument("invalid/media-overlay-002.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentMediaOverlayMeta001() {
		testValidateDocument("invalid/media-overlay-meta-001.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentMinlegth() {
		testValidateDocument("invalid/minlength.opf", 8, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentModifiedSyntax() {
		testValidateDocument("invalid/modified-syntax.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentForeign() {
		testValidateDocument("invalid/foreign.opf", 1, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentModified() {
		testValidateDocument("invalid/modified.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentNav001() {
		testValidateDocument("invalid/nav-001.opf", 1, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentNav002() {
		testValidateDocument("invalid/nav-002.opf", 1, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentNav003() {
		testValidateDocument("invalid/nav-003.opf", 2, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentOrder() {
		testValidateDocument("invalid/order.opf", 2, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentRefinesRelative() {
		testValidateDocument("invalid/refines-relative.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentTocncx001() {
		testValidateDocument("invalid/tocncx-001.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentTocncx002() {
		testValidateDocument("invalid/tocncx-002.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentUid001() {
		testValidateDocument("invalid/uid-001.opf", 1, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentUid002() {
		testValidateDocument("invalid/uid-002.opf", 1, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentValidPrefixes() {
		testValidateDocument("valid/prefixes.opf", 0, 0, EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentPrefixes001() {
		testValidateDocument("invalid/prefixes-001.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentPrefixes002() {
		testValidateDocument("invalid/prefixes-002.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentPrefixes003() {
		testValidateDocument("invalid/prefixes-003.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentPrefixes004() {
		testValidateDocument("invalid/prefixes-004.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentScheme() {
		testValidateDocument("valid/scheme-001.opf", 0, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentInvalidScheme001() {
		testValidateDocument("invalid/scheme-001.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentInvalidScheme002() {
		testValidateDocument("invalid/scheme-002.opf", 3, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentPrefixDeclaration() {
		testValidateDocument("invalid/prefix-declaration.opf", 5, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateDocumentItemProperties() {
		testValidateDocument("invalid/item-properties.opf", 1, 0,
				EPUBVersion.VERSION_3);
	}

	@Test
	public void testValidateRedeclaredReservedPrefixes() {
		// should generate 2 errors (2 invalid redeclarations)
		testValidateDocument("invalid/prefixes-redeclare.opf", 2, 0,
				EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testBadOPFNamespace20() {		
		testValidateDocument("invalid/bad-opf-namespace.opf", 6, 0,
				EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testBadOPFDupeID() {		
		testValidateDocument("invalid/dupe-id.opf", 2, 0,
				EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testOPF_Issue216() {
		testValidateDocument("valid/issue216.opf", 0, 0,
				EPUBVersion.VERSION_2);
	}
	
}
