/*
 * Copyright (c) 2007 Adobe Systems Incorporated
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

package com.adobe.epubcheck.dtbook;

import java.io.IOException;
import java.io.InputStream;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidators;

public class DTBookChecker implements ContentChecker
{

  private final OCFPackage ocf;

  private final Report report;

  private final String path;

  private final XRefChecker xrefChecker;

  private final EPUBVersion version;

  public DTBookChecker(OCFPackage ocf, Report report, String path,
      XRefChecker xrefChecker, EPUBVersion version)
  {
    this.ocf = ocf;
    this.report = report;
    this.path = path;
    this.xrefChecker = xrefChecker;
    this.version = version;
  }

  public void runChecks()
  {
    if (!ocf.hasEntry(path))
    {
      report.message(MessageId.RSC_001, new MessageLocation(this.ocf.getName(), -1, -1), path);
    }
    else if (!ocf.canDecrypt(path))
    {
      report.message(MessageId.RSC_004, new MessageLocation(this.ocf.getName(), 0, 0), path);
    }
    else
    {
      XMLParser dtbookParser;
      InputStream in = null;
      try
      {
        in = ocf.getInputStream(path);
        dtbookParser = new XMLParser(ocf, in, path,
            "application/x-dtbook+xml", report, version);
        dtbookParser.addValidator(XMLValidators.DTBOOK_RNG.get());
        DTBookHandler dtbookHandler = new DTBookHandler(dtbookParser, path,
            xrefChecker);
        dtbookParser.addXMLHandler(dtbookHandler);
        dtbookParser.process();
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
      finally
      {
        try
        {
          if (in != null)
          {
            in.close();
          }
        }
        catch (IOException ignored)
        {
          // eat it
        }
      }
    }
  }
}
