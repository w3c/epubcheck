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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;

public class EncryptionHandler extends XMLHandler
{
  private final OCFPackage ocf;

  EncryptionHandler(ValidationContext context)
  {
    super(context);
    this.ocf = context.ocf.get();
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
      String entryName = e.getAttribute("URI");
      try
      {
        entryName = URLDecoder.decode(entryName, "UTF-8");
      } catch (UnsupportedEncodingException er)
      {
        // UTF-8 is guaranteed to be supported
        throw new InternalError(e.toString());
      }
      if (algorithm == null)
      {
        algorithm = "unknown";
      }
      if (algorithm.equals("http://www.idpf.org/2008/embedding"))
      {
        ocf.setEncryption(entryName, new IDPFFontManglingFilter(null));
        ocf.setObfuscated(entryName, location());
      }
      else if (algorithm.equals("http://ns.adobe.com/pdf/enc#RC"))
      {
        ocf.setEncryption(entryName, new AdobeFontManglingFilter(null));
      }
      else
      {
        ocf.setEncryption(entryName, new UnsupportedEncryptionFilter());
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
