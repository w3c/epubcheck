package com.adobe.epubcheck.ctc.xml;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.NamespaceHelper;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Epub3StructureHandler extends DefaultHandler
{
  Locator locator;
  String fileName;
  Report report;
  NamespaceHelper namespaceHelper = new NamespaceHelper();
  final String[] HTMLEpub3SpecTags = new String[]{"audio", "nav", "video"};
  int specificTagsCount = 0;

  public int getSpecificTagsCount()
  {
    return specificTagsCount;
  }

  @Override
  public void startPrefixMapping (String prefix, String uri) throws SAXException
  {
    namespaceHelper.declareNamespace(prefix, uri, EPUBLocation.create(fileName, locator.getLineNumber(), locator.getColumnNumber(), prefix), report);
  }


  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    namespaceHelper.onStartElement(fileName, locator, uri, qName, attributes, report);

    //outWriter.println("Start Tag -->:<" +qName+">");
    for (String HTMLEpub3SpecTag : HTMLEpub3SpecTags)
    {
      if (qName.compareToIgnoreCase(HTMLEpub3SpecTag) == 0)
      {
        specificTagsCount++;
      }
    }
  }

  @Override
  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
  }

  @Override
  public void endElement (String uri, String localName, String qName)
  {
    namespaceHelper.onEndElement(report);
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public void setReport(Report report)
  {
    this.report = report;
  }
}