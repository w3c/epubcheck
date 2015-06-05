package com.adobe.epubcheck.ctc.xml;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.LocationImpl;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.Location;

import java.util.Vector;

public class LinkTagHandler extends DefaultHandler
{
  private final Report report;
  private Vector<LinkMarkup> linkTags = new Vector<LinkMarkup>();

  public LinkTagHandler(Report report)
  {
    this.report = report;
  }

  private Locator locator;


  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
  }

  private int styleSheetsCount = 0;


  public int getStyleSheetsCount()
  {
    return styleSheetsCount;
  }

  public void checkForMultipleStyleSheets(String fileName)
  {
    LinkMarkup firstOne = null;
    for (LinkMarkup linkTag : linkTags)
    {
      if (linkTag.relAttribute.compareToIgnoreCase("stylesheet") == 0)
      {
        if (++styleSheetsCount == 1)
        {
          firstOne = linkTag;
        }
        else if (styleSheetsCount > 1)
        {
          if (firstOne != null)
          {
            report.message(MessageId.CSS_012, EPUBLocation.create(fileName, firstOne.getLocation().getLineNumber(), firstOne.getLocation().getColumnNumber(), firstOne.getHrefAttribute()));
            firstOne = null;
          }
          report.message(MessageId.CSS_012, EPUBLocation.create(fileName, linkTag.getLocation().getLineNumber(), linkTag.getLocation().getColumnNumber(), linkTag.getHrefAttribute()));
        }
      }

      if (linkTag.relAttribute.compareToIgnoreCase("alternate stylesheet") == 0)
      {
        String title = linkTag.getTitleAttribute();
        if (title == null || title.trim().equals(""))
        {
          report.message(MessageId.CSS_015, EPUBLocation.create(fileName, linkTag.getLocation().getLineNumber(), linkTag.getLocation().getColumnNumber(), linkTag.getHrefAttribute()));
        }
        if (styleSheetsCount == 0)
        {
          report.message(MessageId.CSS_016, EPUBLocation.create(fileName, linkTag.getLocation().getLineNumber(), linkTag.getLocation().getColumnNumber(), linkTag.getHrefAttribute()));
        }
      }
    }
  }

  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws
      SAXException
  {
    if (qName.compareToIgnoreCase("link") == 0)
    {
      LinkMarkup la = new LinkMarkup();
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        String attrValue = attributes.getValue(i);
        if (attrName.compareToIgnoreCase("rel") == 0)
        {
          la.setRelAttribute(attrValue);
        }
        else if (attrName.compareToIgnoreCase("type") == 0)
        {
          la.setTypeAttribute(attrValue);
        }
        else if (attrName.compareToIgnoreCase("href") == 0)
        {
          la.setHrefAttribute(attrValue);
        }
        else if (attrName.compareToIgnoreCase("class") == 0)
        {
          la.setClassAttribute(attrValue);
        }
        else if (attrName.compareToIgnoreCase("title") == 0)
        {
          la.setTitleAttribute(attrValue);
        }

        la.setLocation(new LocationImpl(locator.getLineNumber(), locator.getColumnNumber(), -1, locator.getPublicId(), locator.getSystemId()));
      }
      linkTags.add(la);
    }
  }


  class LinkMarkup
  {
    String relAttribute = "";
    String typeAttribute = "";
    String hrefAttribute = "";
    String classAttribute = "";
    String titleAttribute = "";
    Location location;

    public void setRelAttribute(String relAttribute)
    {
      this.relAttribute = relAttribute;
    }

    public void setTypeAttribute(String typeAttribute)
    {
      this.typeAttribute = typeAttribute;
    }

    public String getHrefAttribute()
    {
      return hrefAttribute;
    }

    public void setHrefAttribute(String hrefAttribute)
    {
      this.hrefAttribute = hrefAttribute;
    }

    public void setClassAttribute(String classAttribute)
    {
      this.classAttribute = classAttribute;
    }

    public String getTitleAttribute()
    {
      return titleAttribute;
    }

    public void setTitleAttribute(String value)
    {
      titleAttribute = value;
    }

    public Location getLocation()
    {
      return location;
    }

    public void setLocation(Location location)
    {
      this.location = location;
    }
  }
}

