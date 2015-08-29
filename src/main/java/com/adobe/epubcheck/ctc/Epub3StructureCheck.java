package com.adobe.epubcheck.ctc;

import java.util.zip.ZipEntry;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.Epub3StructureHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

public class Epub3StructureCheck implements DocumentValidator
{
  private final Report report;
  private final EpubPackage epack;

  public Epub3StructureCheck(EpubPackage epack, Report report)
  {
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
        XMLContentDocParser parser = new XMLContentDocParser(epack.getZip(), report);
        Epub3StructureHandler epub3StructureHandler = new Epub3StructureHandler();
        String fileToParse = epack.getManifestItemFileName(mi);

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(epack.getFileName()), fileToParse);
          continue;
        }

        epub3StructureHandler.setFileName(epack.getFileName());
        epub3StructureHandler.setReport(report);
        parser.parseDoc(fileToParse, epub3StructureHandler);

        if (epub3StructureHandler.getSpecificTagsCount() > 0)
        {
          result = true;
        }
      }
    }
    return result;
  }
}
