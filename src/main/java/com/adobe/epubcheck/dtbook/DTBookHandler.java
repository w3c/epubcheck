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

import java.net.URI;
import java.net.URISyntaxException;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.URISchemes;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.google.common.base.Preconditions;

public class DTBookHandler implements XMLHandler
{
  private final XMLParser parser;
  private final String path;
  private final XRefChecker xrefChecker;
  private boolean checkedUnsupportedXmlVersion = false;

  DTBookHandler(XMLParser parser, String path, XRefChecker xrefChecker)
  {
    this.parser = parser;
    this.path = path;
    this.xrefChecker = xrefChecker;
  }

  public void characters(char[] chars, int arg1, int arg2)
  {
    // do nothing
  }

  public void ignorableWhitespace(char[] chars, int arg1, int arg2)
  {
    // do nothing
  }

  public void startElement()
  {
    if (!checkedUnsupportedXmlVersion)
    {
      HandlerUtil.checkXMLVersion(parser);
      checkedUnsupportedXmlVersion = true;
    }

    XMLElement e = parser.getCurrentElement();
    String ns = e.getNamespace();
    String name = e.getName();
    String id = e.getAttribute("id");
    if (ns.equals("http://www.daisy.org/z3986/2005/dtbook/"))
    {
      // link@href, a@href, img@src
      String href = null;
      /*
       * This section checks to see if the references used are registered
       * schema-types and whether they point to external resources. The
       * resources are only allowed to be external if the attribute "external"
       * is set to true.
       */
      if (name.equals("a"))
      {
        href = e.getAttribute("href");
        String external = e.getAttribute("external");
        if (href != null && external.equals("true"))
        {
          URI uri = checkURI(href);
          if (uri != null && URISchemes.contains(uri.getScheme()))
          {
            href = null;
          }
          else if (uri.getScheme() != null)
          {
            parser.getReport().message(MessageId.OPF_021,
                EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()), href);
            href = null;
          }
        }
      }
      else if (name.equals("link"))
      {
        href = e.getAttribute("href");
      }
      else if (name.equals("img"))
      {
        href = e.getAttribute("src");
      }
      if (href != null)
      {
        // TODO check if dtbook uses xml:base of so set third param
        href = PathUtil.resolveRelativeReference(path, href, null);
        xrefChecker.registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), href,
            name.equals("img") ? XRefChecker.Type.IMAGE : XRefChecker.Type.HYPERLINK);
        URI uri = checkURI(href);
        if (uri != null && "http".equals(uri.getScheme()))
        {
          parser.getReport().info(path, FeatureEnum.REFERENCE, href);
        }
      }
      if (id != null)
      {
        xrefChecker.registerAnchor(path, parser.getLineNumber(), parser.getColumnNumber(), id,
            XRefChecker.Type.HYPERLINK);
      }

    }
  }

  public void endElement()
  {
    // do nothing
  }

  public void processingInstruction(String arg0, String arg1)
  {
    // do nothing
  }

  // TODO duplicated from OPSHandler
  // should be in a URI utils class
  private URI checkURI(String uri)
  {
    try
    {
      return new URI(Preconditions.checkNotNull(uri).trim());
    } catch (URISyntaxException e)
    {
      parser.getReport().message(MessageId.RSC_020, parser.getLocation(), uri);
      return null;
    }
  }
}
