package com.adobe.epubcheck.ctc;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.AnchorTagHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;
import com.adobe.epubcheck.util.TextSearchDictionaryEntry;


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
        String fileToParse = epack.getManifestItemFileName(itemEntry);

        XMLContentDocParser parser;
        parser = new XMLContentDocParser(epack.getZip(), report);
        AnchorTagHandler h = new AnchorTagHandler();

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(epack.getFileName()), fileToParse);
          continue;
        }

        parser.parseDoc(fileToParse, h);
        Vector<AnchorTagHandler.DocTagContent> v = h.getHrefAttributesValues();

        for (int e = 0; e < v.size(); e++)
        {
          AnchorTagHandler.DocTagContent value = v.elementAt(e);
          searchInsideValue(value, tsd, fileToParse);
          String type = value.getType();
          if ("img".compareToIgnoreCase(type) == 0 || "altimg".compareToIgnoreCase(type) == 0)
          {
            // ensure that this image is in the manifest
            String imageFile = value.getValue();
            if (imageFile.matches("^[^:/?#]+:.*"))
            {
              // Already reported in OPFHandler
//              report.message(MessageId.RSC_006, new EPUBLocation(fileToParse, value.getLine(), value.getColumn(), value.getContext()), value.getValue());
              continue;
            }

            imageFile = PathUtil.resolveRelativeReference(fileToParse, imageFile, null);
            int index = imageFile.lastIndexOf("#");
            if (index > 0)
            {
              imageFile = imageFile.substring(0, index);
            }

            ZipEntry imgentry = epack.getZip().getEntry(imageFile);
            if (imgentry == null && "altimg".equalsIgnoreCase(type))
            {
              // missing "img" already reported in XRefChecker
              // MessageId id = "img".compareToIgnoreCase(type) == 0 ? MessageId.RSC_001 : MessageId.RSC_018;
               report.message(MessageId.RSC_018, EPUBLocation.create(fileToParse, value.getLine(), value.getColumn(), value.getContext()), value.getValue());
            }
          }
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
      MessageId messageCode = de.getErrorCode();

      Pattern p = de.getPattern();
      Matcher matcher = p.matcher(entry.getValue());
      int position = 0;
      while (matcher.find(position))
      {
        position = matcher.end();
        report.message(messageCode, EPUBLocation.create(file, entry.getLine(), entry.getColumn(), entry.getValue()));
      }
    }
  }
}


