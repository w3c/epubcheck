package com.adobe.epubcheck.ctc;

import java.util.zip.ZipEntry;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

public class EpubTextContentCheck implements DocumentValidator
{
  private final Report report;
  private final EpubPackage epack;
  private final EntitySearch search;

  public EpubTextContentCheck(Report report, EpubPackage epack)
  {
    this.epack = epack;
    this.search = new EntitySearch(epack.getVersion(), epack.getZip(), report);
    this.report = report;
  }

  public boolean validate()
  {
    SearchDictionary validScriptTypes = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);

    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem itemEntry = epack.getManifest().getItem(i);
      if (validScriptTypes.isValidMediaType(itemEntry.getMediaType()))
      {
        String fileToParse = epack.getManifestItemFileName(itemEntry);

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(this.epack.getFileName()), fileToParse);
          continue;
        }
        this.search.Search(fileToParse);
      }
    }
    return true;
  }

}


