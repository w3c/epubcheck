package com.adobe.epubcheck.util;

import org.w3c.dom.Element;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.xml.XMLParser;

public class HandlerUtil
{

  public static void checkXMLVersion(XMLParser parser)
  {
    String version = parser.getXMLVersion();

    //I don't think it is possible for this to be null.  A null version would cause a SAX parser error.
    if (version == null)
    {
      parser.getReport().message(MessageId.HTM_002, EPUBLocation.create(parser.getResourceName(), parser.getLineNumber(), parser.getColumnNumber()));
    }
    else if (!"1.0".equals(version))
    {
      parser.getReport().message(MessageId.HTM_001, EPUBLocation.create(parser.getResourceName(), parser.getLineNumber(), parser.getColumnNumber()), version);
    }
  }

  public static int getElementLineNumber(Element e)
  {
    return getElementIntAttribute( e, EpubConstants.ElementLineNumberAttribute);
  }

  public static int getElementColumnNumber(Element e)
  {
    return getElementIntAttribute( e, EpubConstants.ElementColumnNumberAttribute);

  }

  static int getElementIntAttribute(Element e, String whichAttribute)
  {
    int val = -1;
    String number = e.getAttribute(whichAttribute);
    if (number != null)
    {
      try
      {
        val = Integer.parseInt(number.trim());
      }
      catch (NumberFormatException ex)
      {
        val = -1;
      }
    }
    return val;
  }

}
