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

import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class EncryptionHandler implements XMLHandler
{
  private final OCFPackage ocf;
  private final XMLParser parser;
  private boolean checkedUnsupportedXmlVersion = false;

  EncryptionHandler(OCFPackage ocf, XMLParser parser)
  {
    this.ocf = ocf;
    this.parser = parser;
  }

  public void startElement()
  {
    if (!checkedUnsupportedXmlVersion)
    {
      HandlerUtil.checkXMLVersion(parser);
      checkedUnsupportedXmlVersion = true;
    }

    // if the element is <CipherReference>, then the element name
    // is stripped of rootBase, and URLDecoded, and finally put into
    // encryptedItemsSet.
    XMLElement e = parser.getCurrentElement();
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
      }
      catch (UnsupportedEncodingException er)
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
          String comp = parent.getAttributeNS(
              "http://ns.adobe.com/digitaleditions/enc",
              "compression");
          if (comp == null)
          {
            parent.setPrivateData(algorithm);
          }
        }
      }
    }
  }

  public void endElement()
  {
  }

  public void ignorableWhitespace(char[] chars, int arg1, int arg2)
  {
  }

  public void characters(char[] chars, int arg1, int arg2)
  {
  }

  public void processingInstruction(String arg0, String arg1)
  {
  }
}
