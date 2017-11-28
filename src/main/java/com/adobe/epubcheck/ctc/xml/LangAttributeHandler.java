package com.adobe.epubcheck.ctc.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class LangAttributeHandler extends DefaultHandler
{

  private String xmlLangAttr = null;
  private String langAttr = null;

  public String getXmlLangAttr()
  {
    return xmlLangAttr;
  }

  public String getLangAttr()
  {
    return langAttr;
  }

  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws
      SAXException
  {

    //outWriter.println("Start Tag -->:<" +qName+">");
    if (qName.compareToIgnoreCase("HTML") == 0)
    {
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        String attrValue = attributes.getValue(i);
        if (attrName.compareToIgnoreCase("xml:lang") == 0)
        {
          xmlLangAttr = attrValue;
        }
        if (attrName.compareToIgnoreCase("lang") == 0)
        {
          langAttr = attrValue;
        }
      }
    }
  }

  public void endElement(String uri, String localName,
      String qName) throws
      SAXException
  {

    //outWriter.println("End Tag   -->:</" + qName+">");

  }

  public void characters(char ch[], int start, int length) throws
      SAXException
  {

    //outWriter.println("-----Tag value----------->"+new String(ch, start, length));

  }
}
