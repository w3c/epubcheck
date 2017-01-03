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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;

public class NavCheckerTest
{

  private static String basepath = "/30/single/nav/";
  private List<MessageId> expectedWarnings = new LinkedList<MessageId>();
  private List<MessageId> expectedErrors = new LinkedList<MessageId>();
  private List<MessageId> expectedFatals = new LinkedList<MessageId>();

  public void testValidateDocument(String fileName)
  {
    testValidateDocument(fileName, false);

  }

  public void testValidateDocument(String fileName, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(fileName, String.format(
        Messages.get("single_file"), "nav", EPUBVersion.VERSION_3, EPUBProfile.DEFAULT));

    GenericResourceProvider resourceProvider;
    if (fileName.startsWith("http://") || fileName.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(fileName);
    }
    else
    {
      try {
        URL fileURL = this.getClass().getResource(basepath + fileName);
        String filePath = fileURL != null ? new File(fileURL.toURI()).getAbsolutePath() : basepath + fileName;
        resourceProvider = new FileResourceProvider(filePath);
      } catch (URISyntaxException e) {
        throw new IllegalStateException("Cannot find test file", e);
      }
    }

    NavChecker navChecker = new NavChecker(new ValidationContextBuilder().path(basepath + fileName)
        .resourceProvider(resourceProvider).report(testReport).mimetype("application/xhtml+xml")
        .version(EPUBVersion.VERSION_3).profile(EPUBProfile.DEFAULT).build());

    navChecker.validate();

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());
  }

  @Before
  public void setup()
  {
    expectedErrors.clear();
    expectedWarnings.clear();
    expectedFatals.clear();
  }

  // XXX The mimeType of the nav document should be nav; this way it can be
  // tested as a nav file
  @Test
  public void testValidateDocumentValidMinimalNav()
  {
    testValidateDocument("valid/minimal.xhtml");
  }

  @Test
  public void testValidateDocumentValidNav001()
  {
    testValidateDocument("valid/nav001.xhtml");
  }

  @Test
  public void testValidateDocumentNoTocNav()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("invalid/noTocNav.xhtml");
  }

  // @Test
  // public void testValidateDocumentNoTocNavFromURL() {
  // testValidateDocument("http://www.interq.ro/bgd/noTocNav.xhtml",
  // expectedErrors, expectedWarnings);
  // }

  @Test
  public void testValidateDocumentHText()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005);
    testValidateDocument("invalid/h-text.xhtml");
  }

  @Test
  public void testValidateDocumenNavLabels001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-labels-001.xhtml");
  }

  @Test
  public void testValidateDocumentNavLabels002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-labels-001.xhtml");
  }

  @Test
  public void testValidateDocumentNavLandmarks001()
  {
    // Missing epub:type attribute on anchor inside 'landmarks' nav element
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-landmarks-001.xhtml");
  }

  @Test
  public void testValidateDocumentNavLandmarks002()
  {
    // Multiple occurrences of the 'landmarks' nav element
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-landmarks-002.xhtml");
  }

  @Test
  public void testValidateDocumentNavLandmarksDuplicates()
  {
    // Multiple occurrences of the 'landmarks' nav element
    Collections.addAll(expectedWarnings, MessageId.RSC_017, MessageId.RSC_017);
    testValidateDocument("invalid/nav-landmarks-duplicates.xhtml");
  }

  @Test
  public void testValidateDocumentNavNoPagelist001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-pagelist-001.xhtml");
  }

  @Test
  public void testValidateDocumentNavNoToc()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-no-toc.xhtml");
  }

  @Test
  public void testValidateDocumentNavReqHeading()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("invalid/req-heading.xhtml");
  }
  
  @Test
  public void testValid_issuet538()
  {
    testValidateDocument("valid/issue538.xhtml");
  }

}
