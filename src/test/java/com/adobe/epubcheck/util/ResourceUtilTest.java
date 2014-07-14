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

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.VersionRetriever;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ResourceUtilTest
{


  private static String basepath = "/30/single/opf/retrieveVersion/";


  /*
    * TEST DEBUG FUNCTION
    */
  public void testVersion(String fileName, List<MessageId> errors, List<MessageId> warnings)
  {
    testVersion(fileName, errors, warnings, new ArrayList<MessageId>(), false);
  }

  public void testVersion(String fileName, List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors)
  {
    testVersion(fileName, errors, warnings, fatalErrors, false);
  }

  public void testVersion(String fileName, List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors, boolean verbose)
  {

    ValidationReport testReport = new ValidationReport(fileName, Messages.get("opv_version_test"));

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

    try
    {
      new VersionRetriever(fileName, testReport)
          .retrieveOpfVersion(resourceProvider.getInputStream(basepath
              + fileName));
    }
    catch (InvalidVersionException e)
    {
      testReport.message(MessageId.RSC_005, new MessageLocation(fileName, -1, -1), e.getMessage());
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", errors, testReport.getErrorIds());
    assertEquals("The warning results do not match", warnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", fatalErrors, testReport.getFatalErrorIds());
  }

  @Test
  public void testRetrieveVersionValidVersion()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("validVersion.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoPackageElement()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("noPackageElement.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoVersionAttribute()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("noVersion.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoEqualSign()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("noEqual.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionValueWithoutQuotes()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("valueWithoutQuotes.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionSpacesBetweenQuotes()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("spacesBetweenQuotes.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionSpacesInValue()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("spacesInValue.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionVersion123323()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("version123.323.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoPointInValue()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("noPointInValue.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNegativeVersion()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testVersion("negativeVersion.opf", expectedErrors, expectedWarnings);
  }

}
