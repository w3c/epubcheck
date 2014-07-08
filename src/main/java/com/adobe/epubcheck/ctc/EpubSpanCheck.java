package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.SpanTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubSpanCheck implements DocumentValidator
{
  private final ZipFile zip;
  private final Report report;
  private final EpubPackage epack;

  public EpubSpanCheck(EpubPackage epack, Report report)
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
        SpanTagHandler sh = new SpanTagHandler();
        String fileToParse = epack.getManifestItemFileName(mi);

        ZipEntry entry = this.zip.getEntry(fileToParse);
        if (entry == null)
        {
          report.message(MessageId.RSC_001, new MessageLocation(this.epack.getFileName(), -1, -1), fileToParse);
          continue;
        }

        parser.parseDoc(fileToParse, sh);
        sh.countNestedElements(sh.getTopElement());
        if (sh.getGenerateMessage() > 0)
        {
          report.message(MessageId.HTM_022, new MessageLocation(mi.getHref(), -1, -1));
        }
      }
    }
    return result;
  }
}
