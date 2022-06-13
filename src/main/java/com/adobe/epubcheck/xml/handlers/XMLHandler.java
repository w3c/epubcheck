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
package com.adobe.epubcheck.xml.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.ext.Locator2;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.model.XMLAttribute;
import com.adobe.epubcheck.xml.model.XMLElement;

public abstract class XMLHandler extends BaseURLHandler
{
  private XMLElement currentElement = null;
  private Locator2 locator;
  protected final ValidationContext context;
  protected final Report report;
  protected final String path;

  public XMLHandler(ValidationContext context)
  {
    super(context);
    this.context = context;
    this.report = context.report;
    this.path = context.path;
  }

  @Override
  public final void setDocumentLocator(Locator locator)
  {
    this.locator = new DelegateLocator(locator);
  }

  @Override
  public final void startElement(String uri, String localName, String qName, Attributes attribs)
  {
    super.startElement(uri, localName, qName, attribs);

    // Check the XML version string when parsing the root element
    if (currentElement == null)
    {
      checkXMLVersion();
    }

    // Build the XML model
    int index = qName.indexOf(':');
    String prefix;
    String name;
    if (index < 0)
    {
      prefix = null;
      name = qName;
    }
    else
    {
      prefix = qName.substring(0, index);
      name = qName.substring(index + 1);
    }
    int count = attribs.getLength();
    XMLAttribute[] attributes = count == 0 ? null : new XMLAttribute[count];
    for (int i = 0; i < count; i++)
    {
      String attName = attribs.getLocalName(i);
      String attNamespace = attribs.getURI(i);
      String attQName = attribs.getQName(i);
      int attIndex = attQName.indexOf(':');
      String attPrefix;
      if (attIndex < 0)
      {
        attPrefix = null;
        attNamespace = null;
      }
      else
      {
        attPrefix = attQName.substring(0, attIndex);
      }
      String attValue = attribs.getValue(i);
      assert attributes != null;
      attributes[i] = new XMLAttribute(attNamespace, attPrefix, attName, attValue);
    }
    currentElement = new XMLElement(uri, prefix, name, attributes, currentElement);
    startElement();
  }

  protected void startElement()
  {
  }

  @Override
  public final void endElement(String uri, String localName, String qName)
  {
    endElement();
    currentElement = currentElement.getParent();
  }

  protected void endElement()
  {
  }

  /**
   * Return the currently parsed element, or <code>null</code> if the parser is
   * not yet parsing an element.
   * 
   * @return the currently parsed element, or <code>null</code>.
   */
  protected final XMLElement currentElement()
  {
    return currentElement;
  }

  private void checkXMLVersion()
  {
    String version = locator.getXMLVersion();
    if (version == null)
    {
      throw new AssertionError("XML version is null");
    }
    else if (!"1.0".equals(version))
    {
      report.message(MessageId.HTM_001, EPUBLocation.create(path), version);
    }
  }

}