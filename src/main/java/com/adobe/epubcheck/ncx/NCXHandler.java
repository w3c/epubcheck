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

package com.adobe.epubcheck.ncx;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;

import io.mola.galimatias.URL;

public class NCXHandler extends XMLHandler
{
  private final XRefChecker xrefChecker;
  String uid;

  public NCXHandler(ValidationContext context)
  {
    super(context);
    this.xrefChecker = context.xrefChecker.get();
  }

  @Override
  public void characters(char[] chars, int start, int len)
  {

    XMLElement e = currentElement();
    String name = e.getName();
    String ns = e.getNamespace();
    boolean keepValue = ("http://www.daisy.org/z3986/2005/ncx/".equals(ns) && "text".equals(name));
    if (keepValue)
    {
      String val = (String) e.getPrivateData();
      String text = new String(chars, start, len);
      e.setPrivateData((val == null) ? text : val + text);
    }
  }

  @Override
  public void startElement()
  {
    XMLElement e = currentElement();
    String ns = e.getNamespace();
    String name = e.getName();
    if (ns.equals("http://www.daisy.org/z3986/2005/ncx/"))
    {
      if ("content".equals(name))
      {
        URL srcURL = checkURL(e.getAttribute("src"));
        if (srcURL != null)
        {
          if (context.isRemote(srcURL))
          {
            report.info(path, FeatureEnum.REFERENCE, srcURL.toString());
          }
          xrefChecker.registerReference(srcURL, XRefChecker.Type.HYPERLINK, location());
        }
      }
      else if ("meta".equals(name))
      {
        String metaName = e.getAttribute("name");
        if ("dtb:uid".equals(metaName))
        {
          uid = e.getAttribute("content");
        }
      }
    }
  }

  @Override
  public void endElement()
  {
    XMLElement e = currentElement();
    String ns = e.getNamespace();
    String name = e.getName();
    if (ns.equals("http://www.daisy.org/z3986/2005/ncx/"))
    {
      if ("text".equals(name))
      {
        String text = (String) e.getPrivateData();
        if (text == null || text.trim().isEmpty())
        {
          report.message(MessageId.NCX_006, location());
        }
      }
    }
  }

  /**
   * @return the uid
   */
  public String getUid()
  {
    return uid;
  }
}
