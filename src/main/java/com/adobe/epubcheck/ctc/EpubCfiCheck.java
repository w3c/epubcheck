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

import java.io.File;
import java.util.Vector;
import java.util.zip.ZipEntry;


public class EpubCfiCheck implements DocumentValidator
{
  private final Report report;
  private final EpubPackage epack;

  public EpubCfiCheck(EpubPackage epack, Report report)
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
        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          report.message(MessageId.RSC_001, new MessageLocation(epack.getFileName(), -1, -1), fileToParse);
          continue;
        }

        XMLContentDocParser parser;
        parser = new XMLContentDocParser(epack.getZip(), report);
        AnchorTagHandler h = new AnchorTagHandler();
        parser.parseDoc(fileToParse, h);
        Vector<AnchorTagHandler.DocTagContent> v = h.getHrefAttributesValues();

        for (int e = 0; e < v.size(); e++)
        {
          AnchorTagHandler.DocTagContent value = v.elementAt(e);
          searchInsideValue(value, fileToParse);
        }
      }
    }

    return true;
  }

  private void searchInsideValue(AnchorTagHandler.DocTagContent entry, String file)
  {
    String url = entry.getValue();
    int frag = entry.getValue().indexOf("#epubcfi");
    if (frag > -1)
    {
      String fileName = url.substring(0, frag);
      fileName = new File(fileName).getName();
      if (entry.getValue().contains(".epub") && fileName.compareTo(file) != 0)
      {
        report.message(MessageId.HTM_012, new MessageLocation(file, entry.getLine(), entry.getColumn(), entry.getValue()));
      }
      else
      {
        report.message(MessageId.HTM_013, new MessageLocation(file, entry.getLine(), entry.getColumn(), entry.getValue()));
      }
    }
  }
}
