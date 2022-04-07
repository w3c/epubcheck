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

import org.junit.Test;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;

public class OCFFilenameCheckerTest
{

  private boolean verbose = false;
  private final Messages messages = Messages.getInstance();

  /*
   * TEST DEBUG FUNCTION
   */
  public void testValidateDocument(String fileName, String expected, EPUBVersion version,
      boolean verbose)
  {
    if (verbose)
    {
      this.verbose = verbose;
    }
    testValidateDocument(fileName, expected, version);

  }

  public void testValidateDocument(String fileName, String expected, EPUBVersion version)
  {
    Report testReport = new ValidationReport(fileName,
        String.format(messages.get("single_file"), "opf", version.toString(), EPUBProfile.DEFAULT));

    ValidationContext testContext = ValidationContext.test().report(testReport).version(version)
        .build();

    OCFFilenameChecker checker = new OCFFilenameChecker(testContext);

    String result = checker.checkCompatiblyEscaped(fileName);

    if (verbose)
    {
      verbose = false;
      outWriter.println(testReport);
      outWriter.println("Test result: " + result + " \nExpected: " + expected);
    }

    assertEquals(expected, result);
  }

  @Test
  public void testValidateDocumentTest001()
  {
    testValidateDocument("abc\u3053abc", "\u3053", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTest002()
  {
    testValidateDocument("www.google.ro.", ".", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTest003()
  {
    testValidateDocument("go gle/ro", " ", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTest004()
  {
    testValidateDocument("/foo/b>ar/quux", ">", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTest005()
  {
    testValidateDocument("/foo/b>ar/quu\uE000x", "\uE000>", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateDocumentTest006()
  {
    testValidateDocument("http://www% .google.ro", "", EPUBVersion.VERSION_2);
  }
}
