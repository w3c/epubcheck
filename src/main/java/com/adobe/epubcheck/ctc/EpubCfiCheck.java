package com.adobe.epubcheck.ctc;

import java.io.File;
import java.util.Vector;
import java.util.zip.ZipEntry;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.AnchorTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubCfiCheck implements Checker
{
  private final Report report;
  private final EpubPackage epack;

  public EpubCfiCheck(EpubPackage epack, Report report)
  {
    this.epack = epack;
    this.report = report;
  }


  public void check()
  {
    SearchDictionary validTypes = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);

    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem itemEntry = epack.getManifest().getItem(i);
      if (validTypes.isValidMediaType(itemEntry.getMediaType()))
      {
        String fileToParse = epack.getManifestItemFileName(itemEntry);
        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(epack.getFileName()), fileToParse);
          continue;
        }

        XMLContentDocParser parser = new XMLContentDocParser(epack.getZip(), report);
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
        report.message(MessageId.HTM_012, EPUBLocation.create(file, entry.getLine(), entry.getColumn(), entry.getValue()));
      }
      else
      {
        report.message(MessageId.HTM_013, EPUBLocation.create(file, entry.getLine(), entry.getColumn(), entry.getValue()));
      }
    }
  }
}
