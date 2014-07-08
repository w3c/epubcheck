package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.EncryptionFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class XmlDocParser
{
  private final ZipFile zip;
  private final Hashtable<String, EncryptionFilter> enc;
  private final Report report;
  public static final String ElementLineNumberAttribute = "elementLineNumber";
  public static final String ElementColumnNumberAttribute = "elementColumnNumber";

  public XmlDocParser(ZipFile zip, Report report)
  {
    this.zip = zip;
    this.enc = new Hashtable<String, EncryptionFilter>();
    this.report = report;
  }

  public Document parseDocument(String fileEntry)
  {
    Document doc = null;
    InputStream is = null;

    try
    {
      is = getInputStream(fileEntry);
      if (is == null)
      {
        String fileName = new File(zip.getName()).getName();
        report.message(MessageId.RSC_001, new MessageLocation(fileName, -1, -1), fileEntry);
      }
      else
      {
        doc = readXML(is, ElementLineNumberAttribute, ElementColumnNumberAttribute);
      }
    }
    catch (IOException e)
    {
      report.message(MessageId.PKG_008, new MessageLocation(fileEntry, -1, -1), fileEntry);
    }
    catch (SAXException e)
    {
      report.message(MessageId.RSC_005, new MessageLocation(fileEntry, -1, -1), e.getMessage());
      doc = null;
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (Exception ignore)
        {
        }
      }
    }

    return doc;
  }

  InputStream getInputStream(String name) throws
      IOException
  {
    ZipEntry entry = zip.getEntry(name);
    if (entry == null)
    {
      return null;
    }
    InputStream in = zip.getInputStream(entry);
    EncryptionFilter filter = enc.get(name);
    if (filter == null)
    {
      return in;
    }
    if (filter.canDecrypt())
    {
      return filter.decrypt(in);
    }
    return null;
  }

  private static Document readXML(InputStream is, final String lineNumAttribName, final String columnNumAttribName) throws
      IOException,
      SAXException
  {
    final Document doc;
    SAXParser parser;
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature("http://xml.org/sax/features/namespaces", true); //default false
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);//default true
      parser = factory.newSAXParser();
      //tell parser about the lexical handler
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      doc = docBuilder.newDocument();
    }
    catch (ParserConfigurationException e)
    {
      throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
    }

    final Stack<Element> elementStack = new Stack<Element>();
    final StringBuilder textBuffer = new StringBuilder();
    DefaultHandler handler = new DefaultHandler()
    {
      private Locator locator;

      @Override
      public void setDocumentLocator(Locator locator)
      {
        this.locator = locator; //Save the locator, so that it can be used later for line tracking when traversing nodes.
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws
          SAXException
      {
        addTextIfNeeded();
        Element el = doc.createElementNS(uri, qName);
        for (int i = 0; i < attributes.getLength(); i++)
        {
          String attributeURI = attributes.getURI(i);
          if (attributeURI == null || attributeURI.equals(""))
          {
            attributeURI = uri;
          }
          el.setAttributeNS(attributeURI, attributes.getQName(i), attributes.getValue(i));
        }
        el.setAttribute(lineNumAttribName, String.valueOf(locator.getLineNumber()));
        el.setAttribute(columnNumAttribName, String.valueOf(locator.getColumnNumber()));
        elementStack.push(el);
      }

      @Override
      public void endElement(String uri, String localName, String qName)
      {
        addTextIfNeeded();
        Element closedEl = elementStack.pop();
        if (elementStack.isEmpty())
        { // Is this the root element?
          doc.appendChild(closedEl);
        }
        else
        {
          Element parentEl = elementStack.peek();
          parentEl.appendChild(closedEl);
        }
      }

      @Override
      public void characters(char ch[], int start, int length) throws
          SAXException
      {
        textBuffer.append(ch, start, length);
      }

      // Outputs text accumulated under the current node
      private void addTextIfNeeded()
      {
        if (textBuffer.length() > 0)
        {
          Element el = elementStack.peek();
          Node textNode = doc.createTextNode(textBuffer.toString());
          el.appendChild(textNode);
          textBuffer.delete(0, textBuffer.length());
        }
      }
    };
    parser.parse(is, handler);
    return doc;
  }

  static public int getElementLineNumber(Element element)
  {
    return getElementAttributeAsInt(element, ElementLineNumberAttribute);
  }

  static public int getElementColumnNumber(Element element)
  {
    return getElementAttributeAsInt(element, ElementColumnNumberAttribute);
  }

  static int getElementAttributeAsInt(Element whichElement, String whichAttribute)
  {
    int result = -1;
    String attr = whichElement.getAttribute(whichAttribute);
    if (attr != null && attr.length() > 0)
    {
      result = Integer.parseInt(attr);
    }
    return result;
  }
}
