package com.adobe.epubcheck.ctc;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubTextContentCheck implements Checker
{
  private final Report report;
  private final EpubPackage epack;
  private final List<TextSearch> search;

  public EpubTextContentCheck(Report report, EpubPackage epack)
  {
    this.epack = epack;
    this.search = new ArrayList<TextSearch>();
    this.search.add(new EntitySearch(epack.getVersion(), epack.getZip(), report));
    this.search.add(new FileLinkSearch(epack.getVersion(), epack.getZip(), report));
    this.report = report;
  }

  public void check()
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

        for(TextSearch ts : this.search)
        {
          ts.Search(fileToParse);
        }
      }
    }
  }

}


