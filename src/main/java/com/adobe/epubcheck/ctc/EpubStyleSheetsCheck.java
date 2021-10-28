package com.adobe.epubcheck.ctc;

import java.util.zip.ZipEntry;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.LinkTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubStyleSheetsCheck implements Checker
{
  private final Report report;
  private final EpubPackage epack;

  public EpubStyleSheetsCheck(EpubPackage epack, Report report)
  {
    this.epack = epack;
    this.report = report;
  }

  public void check()
  {
    SearchDictionary validTypes = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);

    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem mi = epack.getManifest().getItem(i);

      if (validTypes.isValidMediaType(mi.getMediaType()))
      {
        String fileToParse = epack.getManifestItemFileName(mi);

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(this.epack.getFileName()), fileToParse);
          continue;
        }

        XMLContentDocParser parser = new XMLContentDocParser(epack.getZip(), report);
        LinkTagHandler h = new LinkTagHandler(report);

        parser.parseDoc(fileToParse, h);
        h.checkForMultipleStyleSheets(fileToParse);
      }
    }
  }
}


