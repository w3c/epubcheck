package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

import java.util.zip.ZipEntry;

public class EpubTextContentCheck implements DocumentValidator
{
  private final Report report;
  private final EpubPackage epack;
  private final DictionarySearch search;

  public EpubTextContentCheck(Report report, EpubPackage epack)
  {
    this.epack = epack;
    this.search = new DictionarySearch(epack.getZip(), report);
    this.report = report;
  }

  public boolean validate()
  {
    SearchDictionary tsd = new SearchDictionary(DictionaryType.SEARCH);
    SearchDictionary validScriptTypes = new SearchDictionary(DictionaryType.SCRIPT_TYPES);

    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem itemEntry = epack.getManifest().getItem(i);
      if (validScriptTypes.isValidMediaType(itemEntry.getMediaType()))
      {
        String fileToParse = epack.getManifestItemFileName(itemEntry);

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          report.message(MessageId.RSC_001, new MessageLocation(this.epack.getFileName(), -1, -1), fileToParse);
          continue;
        }

        this.search.find(fileToParse, tsd);
        /*XMLContentDocParser parser;
        try
        {
          parser = new XMLContentDocParser(epubPackage.getZip(), report);
          TagsTextSearchHandler handler = new TagsTextSearchHandler();
          parser.parseDoc(fileToParse, handler);
          HashMap<String, Integer[]> tagRanges = handler.getRanges();
          String[] keys = tagRanges.keySet().toArray(new String[0]);
          for (String key : keys)
          {
            Integer[] tagRange = tagRanges.get(key);

            find(fileToParse, tsd, tagRange[0], tagRange[1], tagRange[2], tagRange[3]);
          }
        }
        catch (ParserConfigurationException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch (SAXException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } */
      }
    }
    return true;
  }
}


