package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.AnchorTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;
import com.adobe.epubcheck.util.TextSearchDictionaryEntry;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;


public class EpubExtLinksCheck implements DocumentValidator
{
  private final Report report;
  private final EpubPackage epack;

  public EpubExtLinksCheck(EpubPackage epack, Report report)
  {
    this.epack = epack;
    this.report = report;
  }

  public boolean validate()
  {
    SearchDictionary tsd = new SearchDictionary(DictionaryType.LINK_VALUES);
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
        parser = new XMLContentDocParser(epack.getZip(), report);
        AnchorTagHandler h = new AnchorTagHandler();

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          report.message(MessageId.RSC_001, new MessageLocation(epack.getFileName(), -1, -1), fileToParse);
          continue;
        }

        parser.parseDoc(fileToParse, h);
        Vector<AnchorTagHandler.DocTagContent> v = h.getHrefAttributesValues();

        for (int e = 0; e < v.size(); e++)
        {
          AnchorTagHandler.DocTagContent value = v.elementAt(e);
          searchInsideValue(value, tsd, fileToParse);
        }
      }
    }
    return true;
  }

  private void searchInsideValue(AnchorTagHandler.DocTagContent entry, SearchDictionary tds, String file)
  {
    for (int s = 0; s < tds.getDictEntries().size(); s++)
    {
      TextSearchDictionaryEntry de = tds.getDictEntries().get(s);
      String regExValue = de.getRegexExp();
      MessageId messageCode = de.getErrorCode();

      Pattern p = Pattern.compile(regExValue);
      Matcher matcher = p.matcher(entry.getValue());
      int position = 0;
      while (matcher.find(position))
      {
        position = matcher.end();
        report.message(messageCode, new MessageLocation(file, entry.getLine(), entry.getColumn(), entry.getValue()));
      }
    }
  }
}


