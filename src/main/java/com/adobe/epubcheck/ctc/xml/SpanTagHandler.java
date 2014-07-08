package com.adobe.epubcheck.ctc.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

public class SpanTagHandler extends DefaultHandler
{


  private Element currentElement = null;
  private Element topElement = null;
  private int generateMessage = 0;
  private long characterCount = 0;

  public Element getTopElement()
  {
    return topElement;
  }

  public int getGenerateMessage()
  {
    return generateMessage;
  }

  public void countNestedElements(Element e)
  {
    Queue<Element> elementQueue = new LinkedList<Element>();
    int divElementsCounter = 0;
    int spanElementsCounter = 0;
    elementQueue.add(e);

    while (!elementQueue.isEmpty())
    {
      e = elementQueue.remove();
      if (e != null && e.elementName != null)
      {
        if (e.elementName.compareToIgnoreCase("DIV") == 0)
        {
          divElementsCounter++;
        }
        else if (e.elementName.compareToIgnoreCase("SPAN") == 0)
        {
          spanElementsCounter++;
        }
        for (int i = 0; i < e.nestedElements.size(); i++)
        {
          Element childElement = e.nestedElements.get(i);
          elementQueue.add(childElement);
        }
      }
    }

    long numWords = 1 + (characterCount / 6);
    if (numWords > 50)
    {
      if (((double) divElementsCounter / numWords > .10) || ((double) spanElementsCounter / numWords > .1))
      {
        generateMessage++;
      }
    }
  }

  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws
      SAXException
  {

    Element newElement = new Element(qName);
    newElement.setParentElement(currentElement);
    if (currentElement != null)
    {
      currentElement.addNestedElement(newElement);
      currentElement = newElement;
    }
    else
    {
      currentElement = newElement;
      topElement = currentElement;
    }

  }

  public void endElement(String uri, String localName,
      String qName) throws
      SAXException
  {
    currentElement = currentElement.getParentElement();
  }

  public void characters(char ch[], int start, int length) throws
      SAXException
  {
    characterCount += (long) length;
    //outWriter.println("-----Tag value----------->" + new String(ch, start, length));
  }


  class Element
  {
    Element parentElement;
    final Vector<Element> nestedElements = new Vector<Element>();
    final String elementName;

    public Element(String name)
    {
      elementName = name;
    }

    public void setParentElement(Element e)
    {
      parentElement = e;
    }

    public Element getParentElement()
    {
      return parentElement;
    }

    public void addNestedElement(Element e)
    {
      nestedElements.add(e);
    }
  }
}