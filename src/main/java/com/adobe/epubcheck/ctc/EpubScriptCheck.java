package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.ScriptTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubScriptCheck implements DocumentValidator
{
  private final ZipFile zip;
  private final Report report;
  private final EpubPackage epack;

  public EpubScriptCheck(EpubPackage epack, Report report)
  {
    this.zip = epack.getZip();
    this.report = report;
    this.epack = epack;
  }

  @Override
  public boolean validate()
  {
    boolean result = false;
    SearchDictionary vtsd = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);
    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem mi = epack.getManifest().getItem(i);
      if (vtsd.isValidMediaType(mi.getMediaType()))
      {
        XMLContentDocParser parser = new XMLContentDocParser(this.zip, report);
        ScriptTagHandler sh = new ScriptTagHandler(this.report);
        String fileToParse = epack.getManifestItemFileName(mi);
        ZipEntry entry = this.zip.getEntry(fileToParse);
        if (entry == null)
        {
          report.message(MessageId.RSC_001, new MessageLocation(this.epack.getFileName(), -1, -1), fileToParse);
          continue;
        }
        sh.setFileName(fileToParse);
        sh.setVersion(epack.getVersion());
        parser.parseDoc(fileToParse, sh);
        if (sh.getScriptElementCount() > 0 || sh.getInlineScriptCount() > 0)
        {
          if (epack.getVersion() == EPUBVersion.VERSION_2)
          {
            report.message(MessageId.SCP_004, new MessageLocation(fileToParse, -1, -1));
          }
          else
          {
            if (sh.getInlineScriptCount() > 0)
            {
              report.info(fileToParse, FeatureEnum.SCRIPT, "inline");
            }
            if (mi.getProperties() == null || !mi.getProperties().contains("scripted"))
            {
              report.message(MessageId.SCP_005, new MessageLocation(fileToParse, -1, -1));
            }
          }
        }
      }

      checkJavascript(mi);
    }
    return result;
  }

  void checkJavascript(ManifestItem mi)
  {
    InputStream is = null;
    BufferedReader reader = null;

    if (mi.getMediaType().equalsIgnoreCase("text/javascript"))
    {
      String fileToParse = epack.getManifestItemFileName(mi);
      ZipEntry entry = this.zip.getEntry(fileToParse);
      try
      {
        is = zip.getInputStream(entry);
        reader = new BufferedReader(new InputStreamReader(is));
        int lineNumber = 0;
        while (reader.ready())
        {
          String line = reader.readLine();
          ++lineNumber;
          CheckForInner(fileToParse, lineNumber, line);
        }
        reader.close();
        is.close();
      }
      catch (IOException ex)
      {
        report.message(MessageId.RSC_001, new MessageLocation(fileToParse, -1, -1));
      }
      finally
      {
        if (reader != null)
        {
          try
          {
            reader.close();
          }
          catch (IOException e)
          {
          }
        }
        if (is != null)
        {
          try
          {
            is.close();
          }
          catch (IOException e)
          {
          }
        }
      }
    }
  }

  public void CheckForInner(String fileName, int line, String script)
  {
    String lower = script.toLowerCase();
    int column = lower.indexOf("innerhtml");
    if (column >= 0)
    {
      report.message(MessageId.SCP_007, new MessageLocation(fileName, line, column, trimContext(script, column)));
    }

    column = lower.indexOf("innertext");
    if (column >= 0)
    {
      report.message(MessageId.SCP_008, new MessageLocation(fileName, line, column, trimContext(script, column)));
    }

    Matcher m = ScriptTagHandler.evalPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_001, new MessageLocation(fileName, line, m.start(0), trimContext(script, m.start())));
    }
    m = ScriptTagHandler.localStoragePattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_003, new MessageLocation(fileName, line, m.start(0), trimContext(script, m.start())));
    }
    m = ScriptTagHandler.sessionStoragePattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_003, new MessageLocation(fileName, line, m.start(0), trimContext(script, m.start())));
    }
    m = ScriptTagHandler.xmlHttpRequestPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_002, new MessageLocation(fileName, line, m.start(0), trimContext(script, m.start())));
    }
    m = ScriptTagHandler.microsoftXmlHttpRequestPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_002, new MessageLocation(fileName, line, m.start(0), trimContext(script, m.start())));
    }
  }
  String trimContext(String context, int start)
  {
    String trimmed = context.substring(start).trim();
    int end = context.indexOf("\n");
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
