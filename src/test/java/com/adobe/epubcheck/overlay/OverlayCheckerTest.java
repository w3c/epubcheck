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

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.*;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OverlayCheckerTest
{

  private static String basepath = "/30/single/overlays/";

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings)
  {
    testValidateDocument(fileName, errors, warnings, new ArrayList<MessageId>(), false);
  }

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors)
  {
    testValidateDocument(fileName, errors, warnings, fatalErrors, false);
  }

  public void testValidateDocument(String fileName, List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(fileName, String.format(
        Messages.get("single_file"), "media overlay", "3.0"));

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

    OverlayChecker overlayChecker = new OverlayChecker(basepath + fileName, resourceProvider,
        testReport);

    overlayChecker.validate();

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", errors, testReport.getErrorIds());
    assertEquals("The warning results do not match", warnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", fatalErrors, testReport.getFatalErrorIds());
  }

  @Test
  public void testValidateDocumentValidOverlay001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/overlay-001.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentValidOverlay002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/overlay-002.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentValidOverlay003()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/overlay-003.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentInvalidOverlay001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/overlay-001.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentInvalidOverlay002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/overlay-002.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentInvalidOverlay003()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/overlay-003.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentInvalidOverlay004()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/overlay-004.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentInvalidOverlay005()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/overlay-005.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentInvalidOverlay006()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_027, MessageId.OPF_027, MessageId.OPF_028, MessageId.OPF_027, MessageId.OPF_027);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/overlay-006.smil", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateDocumentValidOverlay007()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/overlay-007.smil", expectedErrors, expectedWarnings);
  }
}
