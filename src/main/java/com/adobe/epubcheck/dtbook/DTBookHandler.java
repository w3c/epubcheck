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

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.ops.OPSHandler;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

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
      String uri = null;
      /*
          * This section checks to see if the references used are registered
          * schema-types and whether they point to external resources. The
          * resources are only allowed to be external if the attribute
          * "external" is set to true.
          */
      if (name.equals("a"))
      {
        uri = e.getAttribute("href");
        String external = e.getAttribute("external");
        if (uri != null && external.equals("true"))
        {
          if (OPSHandler.isRegisteredSchemeType(uri))
          {
            uri = null;
          }
          else if (uri.indexOf(':') > 0)
          {
            parser.getReport().message(MessageId.OPF_021,
                new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
                uri);
            uri = null;
          }
        }
      }
      else if (name.equals("link"))
      {
        uri = e.getAttribute("href");
      }
      else if (name.equals("img"))
      {
        uri = e.getAttribute("src");
      }
      if (uri != null)
      {
        //TODO check if dtbook uses xml:base of so set third param
        uri = PathUtil.resolveRelativeReference(path, uri, null);
        xrefChecker.registerReference(path, parser.getLineNumber(),
            parser.getColumnNumber(), uri,
            name.equals("img") ? XRefChecker.RT_IMAGE
                : XRefChecker.RT_HYPERLINK);
        if (uri.startsWith("http"))
        {
          parser.getReport().info(path, FeatureEnum.REFERENCE, uri);
        }
      }
      if (id != null)
      {
        xrefChecker.registerAnchor(path, parser.getLineNumber(),
            parser.getColumnNumber(), id, XRefChecker.RT_HYPERLINK);
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
}
