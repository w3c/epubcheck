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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.OPFPeeker;

public class OPFPeekerTest
{

  private static String basepath = "/30/single/opf/retrieveVersion/";
  private List<MessageId> expectedErrors;
  private List<MessageId> expectedWarnings;
  private List<MessageId> expectedFatal;

  /*
   * TEST DEBUG FUNCTION
   */
  public void testVersion(String fileName, List<MessageId> errors,
      List<MessageId> warnings)
  {
    testVersion(fileName, errors, warnings, new ArrayList<MessageId>(), false);
  }

  public void testVersion(String fileName, List<MessageId> errors,
      List<MessageId> warnings, List<MessageId> fatalErrors)
  {
    testVersion(fileName, errors, warnings, fatalErrors, false);
  }

  public void testVersion(String fileName, List<MessageId> errors,
      List<MessageId> warnings, List<MessageId> fatalErrors, boolean verbose)
  {

    ValidationReport testReport = new ValidationReport(fileName,
        Messages.get("opv_version_test"));

    GenericResourceProvider resourceProvider;
    if (fileName.startsWith("http://") || fileName.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(fileName);
    } else
    {
      URL fileURL = this.getClass().getResource(basepath + fileName);
      String filePath = fileURL != null ? fileURL.getPath() : basepath
          + fileName;
      resourceProvider = new FileResourceProvider(filePath);
    }

    try
    {
      OPFPeeker peeker = new OPFPeeker(fileName, testReport);
      peeker.peek(resourceProvider.getInputStream(basepath + fileName));
    } catch (InvalidVersionException e)
    {
      testReport.message(MessageId.RSC_005, new MessageLocation(fileName, -1,
          -1), e.getMessage());
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", errors,
        testReport.getErrorIds());
    assertEquals("The warning results do not match", warnings,
        testReport.getWarningIds());
    assertEquals("The fatal error results do not match", fatalErrors,
        testReport.getFatalErrorIds());
  }

  @Before
  public void setup()
  {
    expectedErrors = new ArrayList<MessageId>();
    expectedWarnings = new ArrayList<MessageId>();
    expectedFatal = new ArrayList<MessageId>();
  }

  @Test
  public void testRetrieveVersionValidVersion()
  {
    testVersion("validVersion.opf", expectedErrors, expectedWarnings,expectedFatal,true);
  }

  @Test
  public void testRetrieveVersionNoPackageElement()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testVersion("noPackageElement.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoVersionAttribute()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testVersion("noVersion.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoEqualSign()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testVersion("noEqual.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionValueWithoutQuotes()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testVersion("valueWithoutQuotes.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionSpacesBetweenQuotes()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testVersion("spacesBetweenQuotes.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionSpacesInValue()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testVersion("spacesInValue.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionVersion123323()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testVersion("version123.323.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoPointInValue()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testVersion("noPointInValue.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNegativeVersion()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testVersion("negativeVersion.opf", expectedErrors, expectedWarnings);
  }

}
