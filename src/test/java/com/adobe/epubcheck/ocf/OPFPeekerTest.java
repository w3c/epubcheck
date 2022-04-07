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

package com.adobe.epubcheck.ocf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.PublicationType;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public class OPFPeekerTest
{
  private List<MessageId> expectedErrors = new LinkedList<MessageId>();
  private List<MessageId> expectedWarnings = new LinkedList<MessageId>();
  private List<MessageId> expectedFatals = new LinkedList<MessageId>();

  private final GenericResourceProvider provider = new GenericResourceProvider()
  {

    @Override
    public InputStream openStream(URL url)
      throws IOException
    {
      return this.getClass().getResourceAsStream(url.path());
    }
  };

  /*
   * TEST DEBUG FUNCTION
   */
  public OCFCheckerState retrieveData(String fileName)
  {
    return retrieveData(fileName, false);
  }

  private OCFCheckerState retrieveData(String fileName, boolean verbose)
  {
    URL fileURL = toURL(fileName);
    ValidationReport testReport = new ValidationReport(fileName,
        Messages.getInstance().get("opv_version_test"));
    ValidationContext context = new ValidationContextBuilder().url(fileURL)
        .report(testReport).resourceProvider(provider).build();
    OCFCheckerState state = new OCFCheckerState(context);
    PackageDocumentPeeker peeker = new PackageDocumentPeeker(context, state);
    peeker.peek();
    if (!state.getError().isEmpty())
    {
      testReport.message(MessageId.RSC_005, EPUBLocation.of(context),
          state.getError());
    }
    else if (!state.getPublicationVersion().isPresent())
    {
      testReport.message(MessageId.RSC_005, EPUBLocation.of(context),
          InvalidVersionException.VERSION_NOT_FOUND);
    }

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());

    return state;
  }

  private URL toURL(String fileName)
  {
    try
    {
      return URL.parse("file:/opf-peeker/" + fileName);
    } catch (GalimatiasParseException e)
    {
      throw new IllegalArgumentException("Could not create URL for file " + fileName);
    }
  }

  @Before
  public void setup()
  {
    expectedErrors.clear();
    expectedWarnings.clear();
    expectedFatals.clear();
  }

  @Test
  public void testRetrieveVersionValidVersion()
  {
    retrieveData("validVersion.opf");
  }

  @Test
  public void testRetrieveVersionNoPackageElement()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("noPackageElement.opf");
  }

  @Test
  public void testRetrieveVersionNoVersionAttribute()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("noVersion.opf");
  }

  @Test
  public void testRetrieveVersionNoEqualSign()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    retrieveData("noEqual.opf");
  }

  @Test
  public void testRetrieveVersionValueWithoutQuotes()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    retrieveData("valueWithoutQuotes.opf");
  }

  @Test
  public void testRetrieveVersionSpacesBetweenQuotes()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("spacesBetweenQuotes.opf");
  }

  @Test
  public void testRetrieveVersionSpacesInValue()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("spacesInValue.opf");
  }

  @Test
  public void testRetrieveVersionVersion123323()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("version123.323.opf");
  }

  @Test
  public void testRetrieveVersionNoPointInValue()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("noPointInValue.opf");
  }

  @Test
  public void testRetrieveVersionNegativeVersion()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    retrieveData("negativeVersion.opf");
  }

  @Test
  public void testRetrieveType()
  {
    OCFCheckerState data = retrieveData("singleDCType.opf");
    assertEquals(EnumSet.of(PublicationType.EDUPUB), data.getPublicationTypes());
  }

  @Test
  public void testRetrieveMultipleTypes()
  {
    OCFCheckerState data = retrieveData("multipleDCType.opf");
    assertEquals(EnumSet.of(PublicationType.EDUPUB, PublicationType.INDEX),
        data.getPublicationTypes());
  }

  @Test
  public void testRetrieveOnlyTopLevelTypes()
  {
    OCFCheckerState data = retrieveData("collectionDCType.opf");
    assertEquals(EnumSet.of(PublicationType.EDUPUB), data.getPublicationTypes());
  }

  @Test
  public void testRetrieveTypeWithWhiteSpace()
  {
    OCFCheckerState data = retrieveData("whitespaceInDCType.opf");
    assertEquals(EnumSet.of(PublicationType.EDUPUB), data.getPublicationTypes());
  }

  @Test
  public void testRetrieveID()
  {
    OCFCheckerState data = retrieveData("uniqueId.opf");
    assertTrue(data.getPublicationID().isPresent());
    assertEquals("foo", data.getPublicationID().get());
  }

  @Test
  public void testEmptyID()
  {
    OCFCheckerState data = retrieveData("emptyId.opf");
    assertFalse(data.getPublicationID().isPresent());
  }

  @Test
  public void tesMissingID()
  {
    OCFCheckerState data = retrieveData("missingId.opf");
    assertFalse(data.getPublicationID().isPresent());
  }

  @Test
  public void testMultipleIDs()
  {
    OCFCheckerState data = retrieveData("multipleIds.opf");
    assertTrue(data.getPublicationID().isPresent());
    assertEquals("foo", data.getPublicationID().get());
  }

  @Test
  public void testIDWithWhiteSpace()
  {
    OCFCheckerState data = retrieveData("whitespaceInDCIdentifier.opf");
    assertTrue(data.getPublicationID().isPresent());
    assertEquals("foo", data.getPublicationID().get());
  }

}
