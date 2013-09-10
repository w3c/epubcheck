package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.LinkTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

import java.util.zip.ZipEntry;


public class EpubStyleSheetsCheck implements DocumentValidator
{
  private final Report report;
  private final EpubPackage epack;

  public EpubStyleSheetsCheck(EpubPackage epack, Report report)
  {
    this.epack = epack;
    this.report = report;
  }

  public boolean validate()
  {
    SearchDictionary validTypes = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);

    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem itemEntry = epack.getManifest().getItem(i);

      if (validTypes.isValidMediaType(itemEntry.getMediaType()))
      {
        String fileToParse;
        if (epack.getPackageMainPath() != null && epack.getPackageMainPath().length() > 0)
        {
          fileToParse = PathUtil.resolveRelativeReference(epack.getPackageMainFile(), itemEntry.getHref(), null);
        }
        else
        {
          fileToParse = itemEntry.getHref();
        }

        XMLContentDocParser parser;
        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          report.message(MessageId.RSC_001, new MessageLocation(this.epack.getFileName(), -1, -1), fileToParse);
          continue;
        }

        parser = new XMLContentDocParser(epack.getZip(), report);
        LinkTagHandler h = new LinkTagHandler(report);

        parser.parseDoc(fileToParse, h);
        h.checkForMultipleStyleSheets(fileToParse);
      }
    }
    return true;
  }
}


