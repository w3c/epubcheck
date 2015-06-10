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

package com.adobe.epubcheck.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;

public abstract class AbstractEpubCheckTest
{

  private String basepath;
  List<MessageId> expectedWarnings = new LinkedList<MessageId>();
  List<MessageId> expectedErrors = new LinkedList<MessageId>();
  List<MessageId> expectedFatals = new LinkedList<MessageId>();

  protected AbstractEpubCheckTest(String basepath)
  {
    this.basepath = basepath;
  }

  public void testValidateDocument(String fileName)
  {
    testValidateDocument(fileName, false);
  }

  public void testValidateDocument(String fileName, boolean verbose)
  {
    testValidateDocument(fileName, null, null, verbose);
  }

  public void testValidateDocument(String fileName, EPUBProfile profile)
  {
    testValidateDocument(fileName, profile, false);
  }

  public void testValidateDocument(String fileName, EPUBProfile profile, boolean verbose)
  {
    testValidateDocument(fileName, null, profile, verbose);
  }

  public void testValidateDocument(String fileName, String resultFile)
  {
    testValidateDocument(fileName, resultFile, null, false);
  }

  public void testValidateDocument(String fileName, String resultFile, boolean verbose)
  {
    testValidateDocument(fileName, resultFile, EPUBProfile.DEFAULT, verbose);
  }

  public void testValidateDocument(String fileName, String resultFile, EPUBProfile profile,
      boolean verbose)
  {
    EPUBProfile validationProfile = profile == null ? EPUBProfile.DEFAULT : profile;
    DocumentValidator epubCheck;
    ValidationReport testReport;
    if (fileName.startsWith("http://") || fileName.startsWith("https://"))
    {
      GenericResourceProvider resourceProvider = new URLResourceProvider(fileName);
      try
      {
        testReport = new ValidationReport(fileName);
        epubCheck = new EpubCheck(resourceProvider.getInputStream(null), testReport, fileName,
            validationProfile);
      } catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    else
    {
      File testFile;
      try
      {
        URL url = this.getClass().getResource(basepath + fileName);
        URI uri = url.toURI();
        testFile = new File(uri);
      } catch (URISyntaxException e)
      {
        throw new IllegalStateException("Cannot find test file", e);
      }
      if (testFile.isDirectory())
      {
        Archive epub = new Archive(testFile.getPath());
        testReport = new ValidationReport(epub.getEpubName());
        epub.createArchive();
        epubCheck = new EpubCheck(epub.getEpubFile(), testReport, validationProfile);
      }
      else
      {
        testReport = new ValidationReport(fileName);
        epubCheck = new EpubCheck(new File(testFile.getPath()), testReport, validationProfile);
      }
    }

    epubCheck.validate();

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", IdsToListOfString(expectedErrors),
        IdsToListOfString(testReport.getErrorIds()));
    assertEquals("The warning results do not match", IdsToListOfString(expectedWarnings),
        IdsToListOfString(testReport.getWarningIds()));
    assertEquals("The fatal error results do not match", IdsToListOfString(expectedFatals),
        IdsToListOfString(testReport.getFatalErrorIds()));

    if (resultFile != null)
    {
      URL fileURL = this.getClass().getResource(basepath + resultFile);
      File f;
      try
      {
        f = new File(fileURL.toURI());
      } catch (URISyntaxException e)
      {
        throw new IllegalStateException("Cannot find test file", e);
      }
      assertTrue(f.getAbsolutePath() + " doesn't exist", f.exists());
      BufferedReader in = null;
      try
      {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
        String line;
        while ((line = in.readLine()) != null)
        {
          if (line.trim().length() != 0 && !line.startsWith("#"))
          { // allow comments
            assertTrue(line + " not found", testReport.hasInfoMessage(line));
          }
        }
      } catch (IOException e)
      { /* IGNORE */
      } finally
      {
        if (in != null)
        {
          try
          {
            in.close();
          } catch (IOException e)
          { /* IGNORE */
          }
        }
      }
    }
  }

  private final static String messageName = MessageId.class.getSimpleName();

  private static List<String> IdsToListOfString(List<MessageId> ids)
  {
    if (ids != null && ids.size() > 0)
    {
      List<String> list = new ArrayList<String>(ids.size());
      for (MessageId id : ids)
      {
        String s = String.format("%1$s.%2$s", messageName, id.name());
        list.add(s);
      }
      return list;
    }
    return new ArrayList<String>(0);
  }

  @Before
  public void setup()
  {
    expectedErrors.clear();
    expectedWarnings.clear();
    expectedFatals.clear();
  }
}
