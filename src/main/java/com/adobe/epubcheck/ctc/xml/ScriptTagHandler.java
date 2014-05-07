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

import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptTagHandler extends DefaultHandler
{
  private Locator locator;
  private String fileName;
  private int inlineScriptCount = 0;
  private boolean inScript = false;
  private EPUBVersion version = EPUBVersion.Unknown;
  private final Report report;

  public static final Pattern xmlHttpRequestPattern = Pattern.compile("new[\\s]*XMLHttpRequest[\\s]*\\(");
  public static final Pattern microsoftXmlHttpRequestPattern = Pattern.compile("Microsoft.XMLHTTP");
  public static final Pattern evalPattern = Pattern.compile("((^eval[\\s]*\\()|([^a-zA-Z0-9]eval[\\s]*\\()|(.*[\\s]+eval[\\s]*\\().*)");
  public static final Pattern localStoragePattern = Pattern.compile("localStorage\\.");
  public static final Pattern sessionStoragePattern = Pattern.compile("sessionStorage\\.");


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

  public Vector<ScriptElement>  getScriptElements()
  {
    return scriptElements;
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
        String attrName = attributes.getLocalName(i).toLowerCase();
        if (scriptEvents.contains(attrName))
        {
          this.inlineScriptCount++;
          if (this.version != EPUBVersion.VERSION_2)
          {
            report.message(MessageId.SCP_006,
                new MessageLocation(this.fileName, locator.getLineNumber(), locator.getColumnNumber(), attrName));
            String attrValue = attributes.getValue(i);

            CheckForInner(attrValue);
          }
        }
        if (mouseEvents.contains(attrName))
        {
          report.message(MessageId.SCP_009,
              new MessageLocation(this.fileName, locator.getLineNumber(), locator.getColumnNumber(), attrName));
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
      String script = new String(ch, start, length);
      CheckForInner(script);
    }
  }

  public void CheckForInner(String script)
  {
    String lower = script.toLowerCase();
    int column = lower.indexOf("innerhtml");
    if (column >= 0)
    {
      report.message(MessageId.SCP_007, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber(), trimContext(script, column)));
    }
    column = lower.indexOf("innertext");
    if (column >= 0)
    {
      report.message(MessageId.SCP_008, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber(), trimContext(script, column)));
    }

    Matcher m = evalPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_001, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber(), trimContext(script, m.start())));
    }
    m = localStoragePattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_003, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber(), trimContext(script, m.start())));
    }
    m = sessionStoragePattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_003, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber(), trimContext(script, m.start())));
    }
    m = xmlHttpRequestPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_002, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber(), trimContext(script, m.start())));
    }
    m = microsoftXmlHttpRequestPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_002, new MessageLocation(fileName, locator.getLineNumber(), locator.getColumnNumber(), trimContext(script, m.start())));
    }
  }

  String trimContext(String context, int start)
  {
    String trimmed = context.substring(start).trim();
    int end = trimmed.indexOf("\n");
    if (end < 0)
    {
      return trimmed;
    }
    else
    {
      return trimmed.substring(0, end);
    }
  }
}

