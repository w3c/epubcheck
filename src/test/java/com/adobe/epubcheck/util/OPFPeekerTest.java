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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.OPFPeeker;
import com.google.common.collect.Sets;

public class OPFPeekerTest
{

  private List<MessageId> expectedErrors;
  private List<MessageId> expectedWarnings;

  private final GenericResourceProvider provider = new GenericResourceProvider()
  {
    private static final String basepath = "/30/single/opf/peekData/";

    @Override
    public InputStream getInputStream(String path)
      throws IOException
    {
      return this.getClass().getResourceAsStream(basepath+path);
    }
  };
  /*
   * TEST DEBUG FUNCTION
   */
  public OPFData retrieveData(String fileName, List<MessageId> errors,
      List<MessageId> warnings)
  {
    return retrieveData(fileName, errors, warnings, new ArrayList<MessageId>(), false);
  }

  public OPFData retrieveData(String fileName, List<MessageId> errors,
      List<MessageId> warnings, List<MessageId> fatalErrors)
  {
    return retrieveData(fileName, errors, warnings, fatalErrors, false);
  }

  public OPFData retrieveData(String fileName, List<MessageId> errors,
      List<MessageId> warnings, List<MessageId> fatalErrors, boolean verbose)
  {
    OPFData result = null;
    ValidationReport testReport = new ValidationReport(fileName,
        Messages.get("opv_version_test"));
    try
    {
      OPFPeeker peeker = new OPFPeeker(fileName, testReport, provider);
      result = peeker.peek();
    } catch (InvalidVersionException e)
    {
      testReport.message(MessageId.RSC_005, new MessageLocation(fileName, -1,
          -1), e.getMessage());
    } catch (IOException e) {
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

    return result;
  }

  @Before
  public void setup()
  {
    expectedErrors = new ArrayList<MessageId>();
    expectedWarnings = new ArrayList<MessageId>();
  }

  @Test
  public void testRetrieveVersionValidVersion()
  {
    retrieveData("validVersion.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoPackageElement()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("noPackageElement.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoVersionAttribute()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("noVersion.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoEqualSign()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    retrieveData("noEqual.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionValueWithoutQuotes()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    retrieveData("valueWithoutQuotes.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionSpacesBetweenQuotes()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("spacesBetweenQuotes.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionSpacesInValue()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("spacesInValue.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionVersion123323()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("version123.323.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNoPointInValue()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("noPointInValue.opf", expectedErrors, expectedWarnings);
  }

  @Test
  public void testRetrieveVersionNegativeVersion()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("negativeVersion.opf", expectedErrors, expectedWarnings);
  }
  
  @Test
  public void testRetrieveType()
  {
    OPFData data = retrieveData("singleDCType.opf",expectedErrors,expectedWarnings);
    assertEquals(Sets.newHashSet("foo"), data.getTypes());
  }
  
  @Test
  public void testRetrieveMultipleTypes()
  {
    OPFData data = retrieveData("multipleDCType.opf",expectedErrors,expectedWarnings);
    assertEquals(Sets.newHashSet("foo","bar"), data.getTypes());
  }
  
  @Test
  public void testRetrieveOnlyTopLevelTypes()
  {
    OPFData data = retrieveData("collectionDCType.opf",expectedErrors,expectedWarnings);
    assertEquals(Sets.newHashSet("foo"), data.getTypes());
  }
  
  @Test
  public void testRetrieveTypeWithWhiteSpace()
  {
    OPFData data = retrieveData("whitespaceInDCType.opf",expectedErrors,expectedWarnings);
    assertEquals(Sets.newHashSet("foo bar"), data.getTypes());
  }

}
