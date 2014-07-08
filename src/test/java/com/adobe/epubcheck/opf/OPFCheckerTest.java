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

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.*;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OPFCheckerTest
{

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings,
                                   EPUBVersion version)
  {
    testValidateDocument(fileName, errors, warnings, new ArrayList<MessageId>(), version, false);

  }

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors,
                                   EPUBVersion version)
  {
    testValidateDocument(fileName, errors, warnings, fatalErrors, version, false);

  }

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors,
                                   EPUBVersion version, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(fileName, String.format(
        Messages.get("single_file"), "opf", version.toString()));

    GenericResourceProvider resourceProvider;
    if (fileName.startsWith("http://") || fileName.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(fileName);
    }
    else
    {
      String basepath = null;
      if (version == EPUBVersion.VERSION_2)
      {
        basepath = "/20/single/opf/";
      }
      else if (version == EPUBVersion.VERSION_3)
      {
        basepath = "/30/single/opf/";
      }
      URL fileURL = this.getClass().getResource(basepath + fileName);
      String filePath = fileURL != null ? fileURL.getPath() : basepath + fileName;
      resourceProvider = new FileResourceProvider(filePath);
    }

    OPFChecker opfChecker = null;
    if (version == EPUBVersion.VERSION_2)
    {
      opfChecker = new OPFChecker("test_single_opf", resourceProvider,
          testReport);
    }
    else if (version == EPUBVersion.VERSION_3)
    {
      opfChecker = new OPFChecker30("test_single_opf", resourceProvider,
          testReport);
    }

    assert opfChecker != null;
    opfChecker.validate();

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", errors, testReport.getErrorIds());
    assertEquals("The warning results do not match", warnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", fatalErrors, testReport.getFatalErrorIds());
  }

  @Test
  public void testValidateDocumentValidOPFBase001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/base-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFBindings001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/bindings-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFMediaOverlay001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/media-overlay-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFMediaOverlay002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/media-overlay-002.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFMinimal()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/minimal.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidOPFDcDate1()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/date-1.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidOPFDcDate2()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/date-2.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidOPFDcDate3()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/date-3.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testInvalidOPFDcDate1()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_053);
    testValidateDocument("invalid/date-1.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testInvalidOPFDcDate2()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_053);
    testValidateDocument("invalid/date-2.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testInvalidOPFNullIdentifier()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/null-identifier.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateDocumentValidOPFSvg()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem-svg.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFSvgFallback()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem-svg-fallback.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMalformed()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatalErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedFatalErrors, MessageId.RSC_016);
    testValidateDocument("invalid/malformed.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNoMetadataElement()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatalErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedFatalErrors, MessageId.RSC_016);
    testValidateDocument("invalid/noMetadataElement.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNoNav()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/noNav.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentInvalidMetaAbout()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/invalidMetaAbout.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNoDcNamespace()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatalErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedFatalErrors, MessageId.RSC_016);
    testValidateDocument("invalid/noDcNamespace.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentBindings001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/bindings-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentCoverImage()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_012, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/cover-image.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentFallback001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_040);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/fallback-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentFallback002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/fallback-002.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentIdUnique()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/id-unique.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentItemref001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_049, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/itemref-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMediaOverlay001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/media-overlay-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMediaOverlay002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/media-overlay-002.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMediaOverlayMeta001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/media-overlay-meta-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMinlegth()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.OPF_027, MessageId.RSC_005, MessageId.OPF_027, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/minlength.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentModifiedSyntax()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/modified-syntax.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentForeign()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_010);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/foreign.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentModified()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/modified.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNav001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNav002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNav003()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_012, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-003.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentOrder()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/order.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentRefinesRelative()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/refines-relative.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTocncx001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/tocncx-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTocncx002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_050, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/tocncx-002.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentUid001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/uid-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentUid002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/uid-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidPrefixes()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/prefixes.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_025);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/prefixes-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_027);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/prefixes-002.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes003()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_028);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/prefixes-003.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes004()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_011);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/prefixes-004.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentScheme()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/scheme-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentInvalidScheme001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_027);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/scheme-001.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentInvalidScheme002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_025, MessageId.OPF_027);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/scheme-002.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixDeclaration()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_004, MessageId.OPF_004, MessageId.OPF_006, MessageId.OPF_004, MessageId.OPF_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/prefix-declaration.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentItemProperties()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_012);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/item-properties.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateRedeclaredReservedPrefixes()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_007, MessageId.OPF_007);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    // should generate 2 errors (2 invalid redeclarations)
    testValidateDocument("invalid/prefixes-redeclare.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testBadOPFNamespace20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/bad-opf-namespace.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testBadOPFDupeID()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/dupe-id.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testOPF_Issue216()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue216.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_2);
  }

	@Test
	public void testFilenameInManifestContainsSpacesIssue239_EPUB2()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/filename_contains_spaces_issue239.opf", expectedErrors, expectedWarnings,
				EPUBVersion.VERSION_2);
	}
	
	@Test
	public void testFilenameInManifestContainsSpacesIssue239_EPUB3()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/filename_contains_spaces_issue239.opf", expectedErrors, expectedWarnings,
				EPUBVersion.VERSION_3);
	}
}
