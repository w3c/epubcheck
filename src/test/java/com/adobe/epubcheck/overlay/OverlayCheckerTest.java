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

public class OverlayCheckerTest
{

  private static String basepath = "/30/single/overlays/";
  private List<MessageId> expectedWarnings = new LinkedList<MessageId>();
  private List<MessageId> expectedErrors = new LinkedList<MessageId>();
  private List<MessageId> expectedFatals = new LinkedList<MessageId>();
  private final Messages messages = Messages.getInstance();

  public void testValidateDocument(String fileName)
  {
    testValidateDocument(fileName,false);
  }

  public void testValidateDocument(String fileName, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(fileName, String.format(
        messages.get("single_file"), "media overlay", EPUBVersion.VERSION_3, EPUBProfile.DEFAULT));

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

    OverlayChecker overlayChecker = new OverlayChecker(new ValidationContextBuilder()
        .mimetype("application/smil+xml").path(basepath + fileName)
        .resourceProvider(resourceProvider).report(testReport).build());

    overlayChecker.validate();

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

  @Test
  public void testValidateDocumentValidOverlay001()
  {
    testValidateDocument("valid/overlay-001.smil");
  }

  @Test
  public void testValidateDocumentValidOverlay002()
  {
    testValidateDocument("valid/overlay-002.smil");
  }

  @Test
  public void testValidateDocumentValidOverlay003()
  {
    testValidateDocument("valid/overlay-003.smil");
  }

  @Test
  public void testValidateDocumentInvalidOverlay001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/overlay-001.smil");
  }

  @Test
  public void testValidateDocumentInvalidOverlay002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/overlay-002.smil");
  }

  @Test
  public void testValidateDocumentInvalidOverlay003()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/overlay-003.smil");
  }

  @Test
  public void testValidateDocumentInvalidOverlay004()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/overlay-004.smil");
  }

  @Test
  public void testValidateDocumentInvalidOverlay005()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/overlay-005.smil");
  }

  @Test
  public void testValidateDocumentInvalidOverlay006()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_027, MessageId.OPF_027, MessageId.OPF_028,
        MessageId.OPF_027, MessageId.OPF_027);
    testValidateDocument("invalid/overlay-006.smil");
  }

  @Test
  public void testValidateDocumentValidOverlay007()
  {
    testValidateDocument("valid/overlay-007.smil");
  }

  @Test
  public void testValidateDocumentInvalidOverlay008_Issue568()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/overlay-008.smil");
  }
}
