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

package com.adobe.epubcheck.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.idpf.epubcheck.util.css.CssExceptions;
import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssSource;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;

public class CSSChecker implements ContentChecker
{
  private final OCFPackage ocf;
  private final Report report;
  private final String path; //css file path when Mode.FILE, host path when Mode.STRING
  private final XRefChecker xrefChecker;
  private final EPUBVersion version;
  private final EPUBProfile profile;
  private final Mode mode;

  //Below only used when checking css strings
  private final String value; //css string
  private int line;  //where css string occurs in host
  private final boolean isStyleAttribute;

  private enum Mode
  {
    FILE, STRING
  }

  /**
   * Constructor for CSS files.
   */
  public CSSChecker(OCFPackage ocf, Report report, String path,
      XRefChecker xrefChecker, EPUBVersion version, EPUBProfile profile)
  {
    this(ocf, report, null, false, path, -1, xrefChecker, version, profile, Mode.FILE);
  }


  public CSSChecker(OCFPackage ocf, Report report, String value, boolean isStyleAttribute, String path, int line,
      XRefChecker xrefChecker, EPUBVersion version, EPUBProfile profile) {
    this(ocf, report, value, isStyleAttribute, path, line, xrefChecker, version, profile, Mode.STRING);
  }
  /**
   * Constructor for CSS strings (html style attributes and elements) .
   */
  private CSSChecker(OCFPackage ocf, Report report, String value, boolean isStyleAttribute, String path, int line,
      XRefChecker xrefChecker, EPUBVersion version, EPUBProfile profile, Mode mode)
  {
    this.ocf = ocf;
    this.report = report;
    this.path = path;
    this.xrefChecker = xrefChecker;
    this.version = version;
    this.profile = profile==null?EPUBProfile.DEFAULT:profile;
    this.value = value;
    this.line = line;
    this.isStyleAttribute = isStyleAttribute;
    this.mode = mode;
  }

  public void runChecks()
  {
    CssSource source = null;

    try
    {
      if (this.mode == Mode.FILE && !ocf.hasEntry(path))
      {
        report.message(MessageId.RSC_001, new MessageLocation(this.ocf.getName(), -1, -1), path);
        return;
      }

      CSSHandler handler = new CSSHandler(path, xrefChecker, report, version);
      if (this.mode == Mode.STRING && this.line > -1)
      {
        handler.setStartingLineNumber(this.line);
      }

      source = getCssSource();
      parseItem(source, handler);
      handler.setStartingLineNumber(-1);
      this.line = -1;
    }
    catch (Exception e)
    {
      report.message(MessageId.PKG_008, new MessageLocation(path, -1, -1), e.getMessage());
    }
    finally
    {
      if (source != null)
      {
        try
        {
          InputStream iStream = source.getInputStream();
          if (iStream != null)
          {
            iStream.close();
          }
        }
        catch (IOException ignored)
        {
          // eat it
        }
      }
    }
  }

  CssSource getCssSource() throws
      IOException
  {
    CssSource source = null;
    if (this.mode == Mode.FILE)
    {
      source = new CssSource(this.path, ocf.getInputStream(this.path));
      String charset;
      if (source.getInputStream().getBomCharset().isPresent())
      {
        charset = source.getInputStream().getBomCharset().get().toLowerCase();
        if (!charset.equals("utf-8") && !charset.startsWith("utf-16"))
        {
          report.message(MessageId.CSS_004, new MessageLocation(path, -1, -1, ""), charset);
        }
      }
      if (source.getInputStream().getCssCharset().isPresent())
      {
        charset = source.getInputStream().getCssCharset().get().toLowerCase();
        if (!charset.equals("utf-8") && !charset.startsWith("utf-16"))
        {
          report.message(MessageId.CSS_003, new MessageLocation(path, 0, 0, ""), charset);
        }
      }
    }
    return source;
  }

  void parseItem(CssSource source, CSSHandler handler) throws IOException, CssExceptions.CssException
  {
    if (!isStyleAttribute)
    {
      if (this.mode == Mode.FILE)
      {
        new CssParser().parse(source, handler, handler);
      }
      else
      {
        new CssParser().parse(new StringReader(this.value), this.path, handler, handler);
      }
    }
    else
    {
      new CssParser().parseStyleAttribute(new StringReader(this.value), this.path, handler, handler);
    }
  }
}
