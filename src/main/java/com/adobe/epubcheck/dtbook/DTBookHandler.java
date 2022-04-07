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
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.URISchemes;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;

import io.mola.galimatias.URL;

public class DTBookHandler extends XMLHandler
{
  private final XRefChecker xrefChecker;

  public DTBookHandler(ValidationContext context)
  {
    super(context);
    this.xrefChecker = context.xrefChecker.get();
  }

  @Override
  public void startElement()
  {
    XMLElement e = currentElement();
    String ns = e.getNamespace();
    String name = e.getName();
    if (ns.equals("http://www.daisy.org/z3986/2005/dtbook/"))
    {
      // Register IDs
      xrefChecker.registerID(e.getAttribute("id"), XRefChecker.Type.HYPERLINK, location());

      // Check cross-references (link@href | a@href | img@src)
      URL url = null;
      XRefChecker.Type type = XRefChecker.Type.GENERIC;
      /*
       * This section checks to see if the references used are registered
       * schema-types and whether they point to external resources. The
       * resources are only allowed to be external if the attribute "external"
       * is set to true.
       */
      if (name.equals("a"))
      {
        url = checkURL(e.getAttribute("href"));

        if (url != null && "true".equals(e.getAttribute("external")))
        {
          //FIXME 2022 check that external attribute is set for remote URLs
          if (context.isRemote(url)) {
            report.info(path, FeatureEnum.REFERENCE, url.toString());
            if (!URISchemes.contains(url.scheme()))
            {
              report.message(MessageId.OPF_021, location(), url.toHumanString());
            }
            url = null;
          }
        }
      }
      else if (name.equals("link"))
      {
        url = checkURL(e.getAttribute("href"));
      }
      else if (name.equals("img"))
      {
        url = checkURL(e.getAttribute("src"));
        type = XRefChecker.Type.IMAGE;
      }

      if (url != null)
      {
        xrefChecker.registerReference(url, type, location());
        if (context.isRemote(url))
        {
          report.info(path, FeatureEnum.REFERENCE, url.toString());
        }
      }
    }
  }
}
