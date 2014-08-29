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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;

public class OPFCheckerTest
{

  private List<MessageId> expectedErrors;
  private List<MessageId> expectedWarnings;
  private List<MessageId> expectedFatalErrors;

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings,
      EPUBVersion version)
  {
    testValidateDocument(fileName, errors, warnings, new ArrayList<MessageId>(), version, false);

  }

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings,
      List<MessageId> fatalErrors, EPUBVersion version)
  {
    testValidateDocument(fileName, errors, warnings, fatalErrors, version, false);

  }

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings,
      List<MessageId> fatalErrors, EPUBVersion version, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(fileName, String.format(Messages.get("single_file"), "opf",
        version.toString()));

    GenericResourceProvider resourceProvider;
    if (fileName.startsWith("http://") || fileName.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(fileName);
    } else
    {
      String basepath = null;
      if (version == EPUBVersion.VERSION_2)
      {
        basepath = "/20/single/opf/";
      } else if (version == EPUBVersion.VERSION_3)
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
      opfChecker = new OPFChecker("test_single_opf", resourceProvider, testReport);
    } else if (version == EPUBVersion.VERSION_3)
    {
      opfChecker = new OPFChecker30("test_single_opf", resourceProvider, testReport);
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

  @Before
  public void setup()
  {
    expectedErrors = new ArrayList<MessageId>();
    expectedWarnings = new ArrayList<MessageId>();
    expectedFatalErrors = new ArrayList<MessageId>();
  }

  @Test
  public void testValidateDocumentValidOPFBase001()
  {
    testValidateDocument("valid/base-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFBindings001()
  {
    testValidateDocument("valid/bindings-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFMediaOverlay001()
  {
    testValidateDocument("valid/media-overlay-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFMediaOverlay002()
  {
    testValidateDocument("valid/media-overlay-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFMinimal()
  {
    testValidateDocument("valid/minimal.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidOPFDcDate1()
  {
    testValidateDocument("valid/date-1.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidOPFDcDate2()
  {
    testValidateDocument("valid/date-2.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidOPFDcDate3()
  {
    testValidateDocument("valid/date-3.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testInvalidOPFDcDate1()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_053);
    testValidateDocument("invalid/date-1.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testInvalidOPFDcDate2()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_053);
    testValidateDocument("invalid/date-2.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testInvalidOPFNullIdentifier()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/null-identifier.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateDocumentValidOPFSvg()
  {
    testValidateDocument("valid/lorem-svg.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidOPFSvgFallback()
  {
    testValidateDocument("valid/lorem-svg-fallback.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMalformed()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    Collections.addAll(expectedFatalErrors, MessageId.RSC_016);
    testValidateDocument("invalid/malformed.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNoMetadataElement()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    Collections.addAll(expectedFatalErrors, MessageId.RSC_016);
    testValidateDocument("invalid/noMetadataElement.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNoNav()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/noNav.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentInvalidMetaAbout()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/invalidMetaAbout.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNoDcNamespace()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    Collections.addAll(expectedFatalErrors, MessageId.RSC_016);
    testValidateDocument("invalid/noDcNamespace.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentBindings001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/bindings-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentCoverImage()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_012, MessageId.RSC_005);
    testValidateDocument("invalid/cover-image.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentFallback001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_040);
    testValidateDocument("invalid/fallback-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentFallback002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/fallback-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentIdUnique()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/id-unique.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentItemref001()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_049, MessageId.RSC_005);
    testValidateDocument("invalid/itemref-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMediaOverlay001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/media-overlay-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMediaOverlay002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/media-overlay-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMediaOverlayMeta001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/media-overlay-meta-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentMinlegth()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
         MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/minlength.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentModifiedSyntax()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/modified-syntax.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentForeign()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_010);
    testValidateDocument("invalid/foreign.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentModified()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/modified.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNav001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNav002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentNav003()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_012, MessageId.RSC_005);
    testValidateDocument("invalid/nav-003.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentOrder()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/order.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentRefinesRelative()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/refines-relative.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTocncx001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/tocncx-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTocncx002()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_050, MessageId.RSC_005);
    testValidateDocument("invalid/tocncx-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentUid001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/uid-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentUid002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/uid-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentValidPrefixes()
  {
    testValidateDocument("valid/prefixes.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_025);
    testValidateDocument("invalid/prefixes-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes002()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_027);
    testValidateDocument("invalid/prefixes-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes003()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_028);
    testValidateDocument("invalid/prefixes-003.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixes004()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_011);
    testValidateDocument("invalid/prefixes-004.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentScheme()
  {
    testValidateDocument("valid/scheme-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentInvalidScheme001()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_027);
    testValidateDocument("invalid/scheme-001.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentInvalidScheme002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_025);
    testValidateDocument("invalid/scheme-002.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentPrefixDeclaration()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_004c, MessageId.OPF_004c);
    testValidateDocument("invalid/prefix-declaration.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentItemProperties()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_012);
    testValidateDocument("invalid/item-properties.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateRedeclaredReservedPrefixes()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_007b);
    Collections.addAll(expectedWarnings, MessageId.OPF_007, MessageId.OPF_007);
    // should generate 2 warnings (redeclaration of reserved prefixes) and 1 error (redeclaration of default vocab)
    testValidateDocument("invalid/prefixes-redeclare.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testBadOPFNamespace20()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/bad-opf-namespace.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testBadOPFDupeID()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/dupe-id.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testOPF_Issue216()
  {
    testValidateDocument("valid/issue216.opf", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testFilenameInManifestContainsSpacesIssue239_EPUB2()
  {
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/filename_contains_spaces_issue239.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testFilenameInManifestContainsSpacesIssue239_EPUB3()
  {
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/filename_contains_spaces_issue239.opf", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMultipleDCTypes()
  {
    testValidateDocument("valid/dc-type.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMultipleDCSource()
  {
    testValidateDocument("valid/dc-source.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  

  @Test
  public void testMetaSourceOf()
  {
    testValidateDocument("valid/meta-source-of.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMetaSourceOfWrongValue()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/meta-source-of-wrongvalue.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMetaSourceOfNoRefines()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/meta-source-of-norefines.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMetaSourceWrongRefinesTarget()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/meta-source-of-wrongrefines.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testRecordLink()
  {
    testValidateDocument("valid/link-rel-record.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testRecordLinkNoMediaType()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/link-rel-record-no-mediatype.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testRecordLinkWithRefines()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/link-rel-record-refines.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMetaBelongsToCollection()
  {
    testValidateDocument("valid/meta-collection.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMetaBelongsToCollectionWrongRefines()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/meta-collection-refine-noncollection.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMetaCollectionTypeNoRefines()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/meta-collection-type-norefines.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMetaCollectionTypeWrongRefines()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/meta-collection-type-refines-noncollection.opf", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3);
  }
}
