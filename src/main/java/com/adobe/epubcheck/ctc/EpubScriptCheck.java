package com.adobe.epubcheck.ctc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.ScriptTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
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
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(this.epack.getFileName()), fileToParse);
          continue;
        }
        sh.setFileName(fileToParse);
        sh.setVersion(epack.getVersion());
        parser.parseDoc(fileToParse, sh);
        if (sh.getScriptElementCount() > 0 || sh.getInlineScriptCount() > 0)
        {
          if (sh.getInlineScriptCount() > 0)
          {
            report.info(fileToParse, FeatureEnum.SCRIPT, "inline");
          }
          if (sh.getScriptElementCount() > 0)
          {
            report.info(fileToParse, FeatureEnum.SCRIPT, "tag");
          }
          if (epack.getVersion() != EPUBVersion.VERSION_2)
          {
            report.message(MessageId.SCP_010, EPUBLocation.create(fileToParse));
            if (mi.getProperties() == null || !mi.getProperties().contains("scripted"))
            {
              report.message(MessageId.SCP_005, EPUBLocation.create(fileToParse));
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
    String mediaType = mi.getMediaType();
    if (mediaType != null && "text/javascript".equalsIgnoreCase(mediaType))
    {
      String fileToParse = epack.getManifestItemFileName(mi);
      ZipEntry entry = this.zip.getEntry(fileToParse);
      if (entry == null)
      {
        // already reported in core checkers
        // report.message(MessageId.RSC_001, EPUBLocation.create(fileToParse), fileToParse);
        return;
      }
      report.info(fileToParse, FeatureEnum.SCRIPT, "javascript");
      report.info(fileToParse, FeatureEnum.HAS_SCRIPTS, "");

      if (epack.getVersion() == EPUBVersion.VERSION_2)
      {
        report.message(MessageId.SCP_004, EPUBLocation.create(fileToParse));
      }
      else
      {
        report.message(MessageId.SCP_010, EPUBLocation.create(fileToParse));
      }

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
      catch (FileNotFoundException ex)
      {
        report.message(MessageId.RSC_001, EPUBLocation.create(fileToParse), fileToParse);
      }
      catch (IOException ex)
      {
        report.message(MessageId.PKG_008, EPUBLocation.create(fileToParse), fileToParse);
      }
      finally
      {
        if (reader != null)
        {
          try
          {
            reader.close();
          }
          catch (IOException ignored)
          {
          }
        }
        if (is != null)
        {
          try
          {
            is.close();
          }
          catch (IOException ignored)
          {
          }
        }
      }
    }
  }

  public void CheckForInner(String fileName, int line, String script)
  {
    String lower = script.toLowerCase(Locale.ROOT);
    int column = lower.indexOf("innerhtml");
    if (column >= 0)
    {
      report.message(MessageId.SCP_007, EPUBLocation.create(fileName, line, column, trimContext(script, column)));
    }

    column = lower.indexOf("innertext");
    if (column >= 0)
    {
      report.message(MessageId.SCP_008, EPUBLocation.create(fileName, line, column, trimContext(script, column)));
    }

    // the exact pattern is very complex and it slows down all script checking.
    //  what we can do here is use a blunt check (for the word "eval").  if it is not found, keep moving.
    //  If it is found, look closely using the exact pattern to see if the line truly matches the exact eval() function and report that.
    Matcher m = null;
    if (script.contains("eval"))
    {
      m = ScriptTagHandler.evalPattern.matcher(script);
      if (m.find())
      {
        report.message(MessageId.SCP_001, EPUBLocation.create(fileName, line, m.start(0), trimContext(script, m.start())));
      }
    }

    m = ScriptTagHandler.localStoragePattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_003, EPUBLocation.create(fileName, line, m.start(0), trimContext(script, m.start())));
    }
    m = ScriptTagHandler.sessionStoragePattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_003, EPUBLocation.create(fileName, line, m.start(0), trimContext(script, m.start())));
    }
    m = ScriptTagHandler.xmlHttpRequestPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_002, EPUBLocation.create(fileName, line, m.start(0), trimContext(script, m.start())));
    }
    m = ScriptTagHandler.microsoftXmlHttpRequestPattern.matcher(script);
    if (m.find())
    {
      report.message(MessageId.SCP_002, EPUBLocation.create(fileName, line, m.start(0), trimContext(script, m.start())));
    }
  }

  static public String trimContext(String context, int start)
  {
    String trimmed = context.substring(start).trim();
    int end = trimmed.indexOf("\n");
    if (end < 0 && trimmed.length() < 60)
    {
      return trimmed;
    }
    else
    {
      int newEnd = Math.min(60, (end > 0 ? end : trimmed.length()));
      return  trimmed.substring(0, newEnd);
    }
  }
}
