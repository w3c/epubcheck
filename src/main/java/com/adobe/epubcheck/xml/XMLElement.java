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

package com.adobe.epubcheck.xml;

public class XMLElement extends XMLNode
{

  private final XMLAttribute[] attributes;

  private final XMLElement parent;

  private Object privateData;

  XMLElement(String namespace, String prefix, String name,
      XMLAttribute[] attributes, XMLElement parent)
  {
    super(namespace, prefix, name);
    this.attributes = attributes;
    this.parent = parent;
  }

  public int getAttributeCount()
  {
    if (attributes == null)
    {
      return 0;
    }
    return attributes.length;
  }

  public XMLAttribute getAttribute(int i)
  {
    return attributes[i];
  }

  public XMLElement getParent()
  {
    return parent;
  }

  public String getAttributeNS(String ns, String name)
  {
    if (attributes == null)
    {
      return null;
    }
    for (XMLAttribute attr : attributes)
    {
      String ans = attr.getNamespace();
      if (attr.getName().equals(name)
          && (ans == null ? ns == null : ns != null && ans.equals(ns)))
      {
        return attr.getValue();
      }
    }
    return null;
  }

  public String getAttribute(String attr)
  {
    return getAttributeNS(null, attr);
  }

  public Object getPrivateData()
  {
    return privateData;
  }

  public void setPrivateData(Object privateData)
  {
    this.privateData = privateData;
  }

}
