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

package com.adobe.epubcheck.ocf;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.encryption.AdobeFontManglingFilter;
import com.adobe.epubcheck.ocf.encryption.IDPFFontManglingFilter;
import com.adobe.epubcheck.ocf.encryption.UnsupportedEncryptionFilter;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Strings;

import io.mola.galimatias.URL;

class OCFEncryptionFileHandler extends XMLHandler
{

  private final OCFCheckerState state;

  public OCFEncryptionFileHandler(ValidationContext context, OCFCheckerState state)
  {
    super(context, state.getContainer().getRootURL());
    this.state = state;
  }

  @Override
  public void startElement()
  {
    // if the element is <CipherReference>, then the element name
    // is stripped of rootBase, and URLDecoded, and finally put into
    // encryptedItemsSet.
    XMLElement e = currentElement();
    if (e.getName().equals("CipherReference"))
    {
      String algorithm = null;
      XMLElement parent = e.getParent();
      if (parent != null)
      {
        parent = parent.getParent();
        if (parent != null && parent.getName().equals("EncryptedData"))
        {
          algorithm = (String) parent.getPrivateData();
        }
      }
      // FIXME 2022 what if the URI attribute was not found?
      String urlString = e.getAttribute("URI");
      URL url = checkURL(urlString);
      if (url != null)
      {
        if (!state.getContainer().contains(url))
        {
          context.report.message(MessageId.RSC_007, location(), urlString);
          return;
        }

        switch (Strings.nullToEmpty(algorithm))
        {

        case "http://www.idpf.org/2008/embedding":
          state.addEncryptedResource(url, new IDPFFontManglingFilter(null));
          state.addObfuscatedResource(url, location());
          break;

        case "http://ns.adobe.com/pdf/enc#RC":
          state.addEncryptedResource(url, new AdobeFontManglingFilter(null));
          break;

        default:
          state.addEncryptedResource(url, new UnsupportedEncryptionFilter());
          break;
        }
      }
    }
    else if (e.getName().equals("EncryptionMethod"))
    {
      String algorithm = e.getAttribute("Algorithm");
      if (algorithm != null)
      {
        XMLElement parent = e.getParent();
        if (parent != null)
        {
          String comp = parent.getAttributeNS("http://ns.adobe.com/digitaleditions/enc",
              "compression");
          if (comp == null)
          {
            parent.setPrivateData(algorithm);
          }
        }
      }
    }
  }

}
