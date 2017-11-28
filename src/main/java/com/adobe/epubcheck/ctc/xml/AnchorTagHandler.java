package com.adobe.epubcheck.ctc.xml;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Vector;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class AnchorTagHandler extends DefaultHandler
{

  public static final String MATH_ML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";
  private Vector<DocTagContent> tagsContent = new Vector<DocTagContent>();
  private DocTagContent currentScriptTag = null;
  private boolean scriptReading = false;
  private Locator locator;
  private HashMap<String, Integer> prefixes = new HashMap<String, Integer>();

  public Vector<DocTagContent> getHrefAttributesValues()
  {
    return tagsContent;
  }

  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
  }

  @Override
  public void endDocument()
  {
    prefixes.clear();
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    if (qName.compareToIgnoreCase("script") == 0)
    {
      scriptReading = true;
      currentScriptTag = new DocTagContent();
      currentScriptTag.setLine(locator.getLineNumber());
      currentScriptTag.setColumn(locator.getColumnNumber());
      currentScriptTag.setType("script");
      currentScriptTag.setContext("script");
      tagsContent.add(currentScriptTag);
    }

    if (qName.compareToIgnoreCase("html") == 0)
    {
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        String attrValue = attributes.getValue(i);
        if (attrName.startsWith("xmlns:") && attrValue.compareToIgnoreCase(MATH_ML_NAMESPACE) == 0)
        {
          String prefix = attrName.substring("xmlns:".length());
          startPrefixMapping (prefix, attrValue);
        }
      }
    }
    else if (qName.compareToIgnoreCase("A") == 0)
    {
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        String attrValue = attributes.getValue(i);
        if (attrName.compareToIgnoreCase("href") == 0)
        {
          DocTagContent sa = new DocTagContent();
          sa.setValue(attrValue);
          sa.setType("A");
          sa.setLine(locator.getLineNumber());
          sa.setColumn(locator.getColumnNumber());
          sa.setContext("href");
          tagsContent.add(sa);
        }
      }
    }
    else if (qName.compareToIgnoreCase("iframe") == 0)
    {
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        String attrValue = attributes.getValue(i);
        if (attrName.compareToIgnoreCase("src") == 0)
        {
          DocTagContent sa = new DocTagContent();
          sa.setValue(attrValue);
          sa.setType("iframe:src");
          sa.setLine(locator.getLineNumber());
          sa.setColumn(locator.getColumnNumber());
          sa.setContext("src");
          tagsContent.add(sa);
        }
      }
    }
    else if (qName.compareToIgnoreCase("img") == 0)
    {
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        String attrValue = attributes.getValue(i);
        if (attrName.compareToIgnoreCase("src") == 0)
        {
          DocTagContent sa = new DocTagContent();
          sa.setValue(attrValue);
          sa.setType("img");
          sa.setLine(locator.getLineNumber());
          sa.setColumn(locator.getColumnNumber());
          sa.setContext("img");
          tagsContent.add(sa);
        }
      }
    }
    else if (isMath(qName))
    {
      int imageIndex = attributes.getIndex("altimg");
      if (imageIndex >= 0)
      {
        String attrValue = attributes.getValue(imageIndex);
        DocTagContent sa = new DocTagContent();
        sa.setValue(attrValue);
        sa.setType("altimg");
        sa.setLine(locator.getLineNumber());
        sa.setColumn(locator.getColumnNumber());
        sa.setContext("altimg");
        tagsContent.add(sa);
      }
    }
  }

    private boolean isMath(String qName)
    {
        return prefixes.containsKey(qName);
    }

    public void startPrefixMapping (String prefix, String uri)
            throws SAXException
  {
      if (uri.compareToIgnoreCase(MATH_ML_NAMESPACE) == 0)
      {
          prefix = prefix + ":math";
          Integer count = prefixes.get(prefix);
          if (count == null)
          {
              count = 0;
          }
          prefixes.put(prefix, ++count);
      }
  }


  public void endPrefixMapping (String prefix)
            throws SAXException
  {
      prefix = prefix + ":math";
      Integer count = prefixes.get(prefix);
      if (count != null)
      {
          prefixes.put(prefix, --count);
      }
  }

  public void characters(char ch[], int start, int length) throws SAXException
  {
    if (scriptReading)
    {
      currentScriptTag.value = new String(ch, start, length);
      tagsContent.add(currentScriptTag);
      scriptReading = false;
    }
  }

  public class DocTagContent
  {
    String type;
    int line;
    int column;
    String context = "";
    String value = "";

    public int getLine()
    {
      return line;
    }

    public void setLine(int line)
    {
      this.line = line;
    }

    public int getColumn()
    {
      return column;
    }

    public void setColumn(int column)
    {
      this.column = column;
    }

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public void setContext(String context)
    {
      this.context = context;
    }

    public String getContext()
    {
      return context;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }
  }
}
