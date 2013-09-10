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
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

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
        String fileToParse;
        if (epack.getPackageMainPath() != null && epack.getPackageMainPath().length() > 0)
        {
          fileToParse = PathUtil.resolveRelativeReference(epack.getPackageMainFile(), mi.getHref(), null);
        }
        else
        {
          fileToParse = mi.getHref();
        }
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
    }
    return result;
  }
}
