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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
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

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings)
  {
    testValidateDocument(fileName, errors, warnings, new ArrayList<MessageId>(), false);

  }

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors,
                                   boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(fileName, String.format(
        Messages.get("single_file"), "nav", "3.0"));

    GenericResourceProvider resourceProvider;
    if (fileName.startsWith("http://") || fileName.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(fileName);
    }
    else
    {
      URL fileURL = this.getClass().getResource(basepath + fileName);
      String filePath = fileURL != null ? fileURL.getPath() : basepath + fileName;
      resourceProvider = new FileResourceProvider(filePath);
    }

    NavChecker navChecker = new NavChecker(resourceProvider, testReport, basepath
        + fileName, "application/xhtml+xml", EPUBVersion.VERSION_3);

    navChecker.validate();

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", errors, testReport.getErrorIds());
    assertEquals("The warning results do not match", warnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", fatalErrors, testReport.getFatalErrorIds());
  }

  // XXX The mimeType of the nav document should be nav; this way it can be
  // tested as a nav file
  @Test
  public void testValidateDocumentValidMinimalNav()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/minimal.xhtml", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentValidNav001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("valid/nav001.xhtml", expectedErrors, expectedWarnings,expectedFatals,false);
  }

  @Test
  public void testValidateDocumentNoTocNav()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/noTocNav.xhtml", expectedErrors, expectedWarnings);
  }

//	@Test
//	public void testValidateDocumentNoTocNavFromURL() {
//		testValidateDocument("http://www.interq.ro/bgd/noTocNav.xhtml", expectedErrors, expectedWarnings);
//	}

  @Test
  public void testValidateDocumentHText()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/h-text.xhtml", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumenNavLabels001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-labels-001.xhtml", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentNavLabels002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-labels-001.xhtml", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentNavLandmarks001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-landmarks-001.xhtml", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentNavNoPagelist001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-pagelist-001.xhtml", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentNavNoToc()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/nav-no-toc.xhtml", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentNavReqHeading()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/req-heading.xhtml", expectedErrors, expectedWarnings);
  }

}
