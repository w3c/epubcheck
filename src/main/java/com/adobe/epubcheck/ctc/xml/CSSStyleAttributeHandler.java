package com.adobe.epubcheck.ctc.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssSource;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.css.EpubCSSCheckCSSHandler;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.LocationImpl;

public class CSSStyleAttributeHandler extends DefaultHandler
{
  String fileName;
  Stack<String> tagStack = new Stack<String>();
  private Locator locator;
  private boolean inStyleTag = false;
  private boolean isGlobalFixedFormat = false;
  private boolean documentIsFixedFormat = false;
  private CSSStyleAttributeHandler.StyleAttribute currentStyleTag = null;
  private final HashMap<String, StyleAttribute> styleAttributesValues = new LinkedHashMap<String, StyleAttribute>();
  private final Stack<HashMap<String, EpubCSSCheckCSSHandler.ClassUsage>> localStyles = new Stack<HashMap<String, EpubCSSCheckCSSHandler.ClassUsage>>();
  private final Stack<Integer> styleLevels = new Stack<Integer>();
  private EpubCSSCheckCSSHandler cssHandler;
  private Report report;

  public CSSStyleAttributeHandler(boolean isGlobalFixedFormat, boolean  documentIsFixedFormat)
  {
    this.isGlobalFixedFormat = isGlobalFixedFormat;
    this.documentIsFixedFormat = documentIsFixedFormat;
  }
  public Report getReport()
  {
    return report;
  }

  public void setReport(Report report)
  {
    this.report = report;
  }

  public EpubCSSCheckCSSHandler getCssHandler()
  {
    return cssHandler;
  }

  public void setCssHandler(EpubCSSCheckCSSHandler value)
  {
    cssHandler = value;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public Collection<CSSStyleAttributeHandler.StyleAttribute> getStyleAttributesValues()
  {
    return styleAttributesValues.values();
  }

  private final Vector<CSSStyleAttributeHandler.StyleAttribute> styleTagValues = new Vector<CSSStyleAttributeHandler.StyleAttribute>();

  public Vector<CSSStyleAttributeHandler.StyleAttribute> getStyleTagValues()
  {
    return styleTagValues;
  }

  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
  }

  public void startDocument()
  {
    localStyles.clear();
    styleLevels.clear();
  }

  public void endDocument()
  {
    while (!localStyles.isEmpty())
    {
      HashMap<String, EpubCSSCheckCSSHandler.ClassUsage> localStyleMap = localStyles.pop();
      for (String key : localStyleMap.keySet())
      {
        EpubCSSCheckCSSHandler.ClassUsage cu = localStyleMap.get(key);
        if (cu.Count == 0)
        {

          assert (cu.Name != null && !cu.Name.isEmpty());
          report.message(MessageId.CSS_024, EPUBLocation.create(getFileName(), cu.Location.getLineNumber(), cu.Location.getColumnNumber(), key));
        }
      }
    }
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws
      SAXException
  {
    tagStack.push(qName.toLowerCase(Locale.ROOT));
    if (qName.compareToIgnoreCase("style") == 0)
    {
      HashMap<String, EpubCSSCheckCSSHandler.ClassUsage> localStyleMap = new LinkedHashMap<String, EpubCSSCheckCSSHandler.ClassUsage>();
      localStyles.push(localStyleMap);
      this.styleLevels.push(tagStack.size() - 1); // we are pushing the depth of the style's PARENT node here, not the depth of the STYLE node

      inStyleTag = true;
      currentStyleTag = new CSSStyleAttributeHandler.StyleAttribute();
      currentStyleTag.setLine(locator.getLineNumber());
      currentStyleTag.setColumn(locator.getColumnNumber());
      currentStyleTag.setValue("");

    }
    else
    {
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        if (attrName.compareToIgnoreCase("style") == 0)
        {
          String attrValue = attributes.getValue(i);
          StyleAttribute sa = new StyleAttribute();
          sa.setValue(attrValue);
          sa.setLine(locator.getLineNumber());
          sa.setColumn(locator.getColumnNumber());
          styleAttributesValues.put(attrValue, sa);
        }
        else if (attrName.compareToIgnoreCase("class") == 0)
        {
          String attrValue = attributes.getValue(i);
          if (attrValue != null && attrValue.length() > 0)
          {
            String[] attrValues = attrValue.split("\\s+");
            for (String value : attrValues)
            {
              if (value.length() > 0)
              {
                String styleName = "." + value;
                if (!IncrementLocalCssClassCount(styleName) && !IncrementGlobalCssClassCount(styleName))
                {
                  report.message(MessageId.CSS_025, EPUBLocation.create(getFileName(), locator.getLineNumber(), locator.getColumnNumber(), styleName));
                }
              }
            }
          }
        }
      }
    }
  }

  private boolean IncrementGlobalCssClassCount(String attrValue)
  {
    EpubCSSCheckCSSHandler handler = getCssHandler();
    return handler != null && handler.IncrementGlobalCssClassCount(attrValue);
  }

  private boolean IncrementLocalCssClassCount(String className)
  {
    for (int i = localStyles.size() - 1; i >= 0; --i)
    {
      HashMap<String, EpubCSSCheckCSSHandler.ClassUsage> h = localStyles.get(i);
      if (h != null)
      {
        EpubCSSCheckCSSHandler.ClassUsage cu = h.get(className);
        if (cu != null)
        {
          ++cu.Count;
          return true;
        }
      }
    }
    return false;
  }

  public void endElement(String uri, String localName,
      String qName) throws
      SAXException
  {
    tagStack.pop();
    if (inStyleTag && "style".compareToIgnoreCase(qName) == 0)
    {
      parseCurrentStyleTag(currentStyleTag);
      styleTagValues.add(currentStyleTag);
      inStyleTag = false;
    }

    if (styleLevels.size() > 0 && tagStack.size() < styleLevels.peek())
    {
      HashMap<String, EpubCSSCheckCSSHandler.ClassUsage> localStyleMap;
      if (!tagStack.empty() && "head".compareToIgnoreCase(qName) != 0)
      {
        styleLevels.pop();
        localStyleMap = localStyles.pop();
      }
      else
      {
        localStyleMap = localStyles.peek();
      }

      // don't look for unused ones when we are only closing the head tag, because they are "in scope" for the whole doc
      if ("head".compareToIgnoreCase(qName) != 0)
      {
        for (String key : localStyleMap.keySet())
        {
          EpubCSSCheckCSSHandler.ClassUsage cu = localStyleMap.get(key);
          if (cu != null && cu.Count == 0)
          {
            EPUBLocation location = EPUBLocation.create(cu.FileName, cu.Location.getLineNumber(), cu.Location.getColumnNumber(), key);
            if (cu != null)
            {
              assert (cu.Name != null && !cu.Name.isEmpty());
            }
            assert (key != null && !key.isEmpty());
            report.message(MessageId.CSS_024, location);
          }
        }
      }
    }
  }

  private void parseCurrentStyleTag(StyleAttribute currentStyleTag)
  {
    EpubCSSCheckCSSHandler handler = new EpubCSSCheckCSSHandler(report, currentStyleTag.getLine(), currentStyleTag.getColumn(), isGlobalFixedFormat, documentIsFixedFormat);
    try
    {
      String s = currentStyleTag.getValue();
      InputStream inputStream = new ByteArrayInputStream(s.getBytes("UTF-8"));
      CssSource source = new CssSource(this.getFileName(), inputStream);
      CssParser parser = new CssParser();
      handler.setPath(this.getFileName());

      HashMap<String, EpubCSSCheckCSSHandler.ClassUsage> localStyleMap = localStyles.peek();
      parser.parse(source, handler, handler);
      HashMap<String, EpubCSSCheckCSSHandler.ClassUsage> map = handler.getClassMap();
      for (String key : map.keySet())
      {
        EpubCSSCheckCSSHandler.ClassUsage cu = map.get(key);
        int line = cu.Location.getLineNumber();
        int column = cu.Location.getColumnNumber();
        cu.Location = new LocationImpl(line, column, cu.Location.getCharacterOffset(), cu.FileName, cu.FileName);
        cu.Count = 0;
        cu.Name = key;
        localStyleMap.put(cu.Name, cu);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  public void characters(char ch[], int start, int length) throws
      SAXException
  {
    if (inStyleTag)
    {
      currentStyleTag.setValue(currentStyleTag.getValue() + new String(ch, start, length));
    }
  }

  public class StyleAttribute
  {
    int line;
    int column;
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
