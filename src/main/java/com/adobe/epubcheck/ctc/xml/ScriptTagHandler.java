package com.adobe.epubcheck.ctc.xml;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ops.OPSHandler30;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class ScriptTagHandler extends DefaultHandler
{
  private Locator locator;
  private String fileName;
  private int inlineScriptCount = 0;
  private boolean inScript = false;
  private EPUBVersion version = EPUBVersion.Unknown;
  private final Report report;

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public ScriptTagHandler(Report report)
  {
    this.report = report;
  }

  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
  }

  public void setVersion(EPUBVersion version)
  {
    this.version = version;
  }

  private final Vector<ScriptElement> scriptElements = new Vector<ScriptElement>();

  public int getScriptElementCount()
  {
    return scriptElements.size();
  }

  public int getInlineScriptCount()
  {
    return inlineScriptCount;
  }

  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws
      SAXException
  {

    if (qName.compareToIgnoreCase("SCRIPT") == 0)
    {
      inScript = true;
      ScriptElement scriptElement = new ScriptElement();
      boolean isExternal = false;
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i);
        String attrValue = attributes.getValue(i);
        if (attrName.equalsIgnoreCase("src"))
        {
          isExternal = true;
        }
        scriptElement.addAttribute(attrName, attrValue);
      }
      if (isExternal)
      {
        report.info(this.fileName, FeatureEnum.SCRIPT, "external");
      }
      else
      {
        report.info(this.fileName, FeatureEnum.SCRIPT, "tag");
      }
      scriptElements.add(scriptElement);
    }
    else
    {
      HashSet<String> scriptEvents = OPSHandler30.getScriptEvents();
      HashSet<String> mouseEvents = OPSHandler30.getMouseEvents();
      for (int i = 0; i < attributes.getLength(); i++)
      {
        String attrName = attributes.getQName(i).toLowerCase();
        if (scriptEvents.contains(attrName))
        {
          this.inlineScriptCount++;
          if (this.version != EPUBVersion.VERSION_2)
          {
            report.message(MessageId.SCP_006,
                new MessageLocation(this.fileName, locator.getLineNumber(), locator.getColumnNumber(), attrName));
            String attrValue = attributes.getValue(i).toLowerCase();

            CheckForInner(attrValue);
          }
          if (mouseEvents.contains(attrName))
          {
            report.message(MessageId.SCP_009,
                new MessageLocation(this.fileName, locator.getLineNumber(), locator.getColumnNumber(), attrName));
          }
        }
      }
    }
  }

  public void endElement(String uri, String localName, String qName) throws
      SAXException
  {
    //outWriter.println("End Tag   -->:</" + qName+">");
    if (qName.compareToIgnoreCase("SCRIPT") == 0)
    {
      inScript = false;
    }
  }

  public void characters(char ch[], int start, int length) throws
      SAXException
  {
    //outWriter.println("-----Tag value----------->"+new String(ch, start, length));
    if (inScript)
    {
      String script = new String(ch, start, length).toLowerCase();

      CheckForInner(script);
    }
  }

  private void CheckForInner(String script)
  {
    if (script.contains("innerhtml"))
    {
      report.message(MessageId.SCP_007, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber()));
    }
    if (script.contains("innertext"))
    {
      report.message(MessageId.SCP_008, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber()));
    }
  }

  class ScriptElement
  {
    private final HashMap<String, String> attrs = new HashMap<String, String>();

    public void addAttribute(String name, String value)
    {
      attrs.put(name, value);
    }

    public String getAttribute(String name)
    {
      return attrs.get(name);
    }

/*  public Vector getAllAttributes()
  {
    Vector<String[]> attributes = new Vector<String[]>();
    Set keys = attrs.keySet();
    for (Object key1 : keys)
    {
      String[] attribute = new String[2];
      String key = (String) key1;
      String value = attrs.get(key);
      attribute[0] = key;
      attribute[1] = value;
      attributes.add(attribute);
    }
    return attributes;
  }
*/
  }
}