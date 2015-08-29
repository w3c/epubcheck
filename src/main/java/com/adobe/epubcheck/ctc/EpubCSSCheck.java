package com.adobe.epubcheck.ctc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssSource;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.css.EpubCSSCheckCSSHandler;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.CSSStyleAttributeHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.EncryptionFilter;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;
import com.adobe.epubcheck.util.TextSearchDictionaryEntry;

public class EpubCSSCheck implements DocumentValidator
{
  final ZipFile zip;
  final Report report;
  final EpubPackage epack;
  final Hashtable<String, EncryptionFilter> enc;
  static final int EXCESSIVE_CSS_THRESHOLD = 10;
  final boolean isGlobalFixed;

  public EpubCSSCheck(EpubPackage epack, Report report)
  {
    this.epack = epack;
    this.zip = epack.getZip();
    this.enc = new Hashtable<String, EncryptionFilter>();
    this.report = report;
    this.isGlobalFixed = EpubPackage.isGlobalFixed(epack);
  }

  public boolean validate()
  {
    boolean hasFixedFormatItems = getHasFixedFormatItems(epack);
    SearchDictionary tsd = new SearchDictionary(DictionaryType.CSS_VALUES);
    SearchDictionary cssTypes = new SearchDictionary(DictionaryType.CSS_FILES);
    SearchDictionary validTypes = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);
    EpubCSSCheckCSSHandler handler = new EpubCSSCheckCSSHandler(report, isGlobalFixed, hasFixedFormatItems);
    int numCssFiles = 0;

    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem itemEntry = epack.getManifest().getItem(i);

      if (cssTypes.isValidMediaType(itemEntry.getMediaType()))
      {
        ++numCssFiles;
        String fileToParse = getEntryFileName(itemEntry, epack);

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(epack.getFileName()), fileToParse);
          continue;
        }

        try
        {
          InputStream inputStream = getInputStream(fileToParse);
          CssSource source = new CssSource(fileToParse, inputStream);
          CssParser parser = new CssParser();
          handler.setPath(fileToParse);

          parser.parse(source, handler, handler);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }

    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem itemEntry = epack.getManifest().getItem(i);
      if (validTypes.isValidMediaType(itemEntry.getMediaType()))
      {
        String fileToParse = getEntryFileName(itemEntry, epack);
        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(epack.getFileName()), fileToParse);
          continue;
        }

        XMLContentDocParser parser;
        String properties = itemEntry.getProperties();
        boolean itemIsFixedFormat = (properties != null && properties.contains("rendition:layout-pre-paginated"));

        parser = new XMLContentDocParser(epack.getZip(), report);
        CSSStyleAttributeHandler h = new CSSStyleAttributeHandler(isGlobalFixed, itemIsFixedFormat);
        h.setCssHandler(handler);
        h.setReport(report);
        h.setFileName(fileToParse);
        parser.parseDoc(fileToParse, h);
        Vector<CSSStyleAttributeHandler.StyleAttribute> styleTags = h.getStyleTagValues();

        for (int t = 0; t < styleTags.size(); t++)
        {
          CSSStyleAttributeHandler.StyleAttribute value = styleTags.elementAt(t);
          searchInsideValue(value, tsd, fileToParse);
        }

        Collection<CSSStyleAttributeHandler.StyleAttribute> styleAttributes = h.getStyleAttributesValues();
        for (CSSStyleAttributeHandler.StyleAttribute value : styleAttributes)
        {
          searchInsideValue(value, tsd, fileToParse);
          report.message(MessageId.ACC_013, EPUBLocation.create(fileToParse, value.getLine(), value.getColumn(), value.getValue()));
        }
      }
    }

    CheckUnusedCSSClassSelectors(handler, report);

    if (numCssFiles > EXCESSIVE_CSS_THRESHOLD)
    {
      report.message(MessageId.CSS_011, EPUBLocation.create(epack.getFileName()));
    }
    return true;
  }

  boolean getHasFixedFormatItems(EpubPackage epack)
  {
    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem itemEntry = epack.getManifest().getItem(i);
      String properties = itemEntry.getProperties();
      if (properties != null && properties.contains("rendition:layout-pre-paginated"))
      {
        return true;
      }
    }
    return false;
  }

  void CheckUnusedCSSClassSelectors(EpubCSSCheckCSSHandler handler, Report report)
  {
    if (handler != null)
    {
      handler.CheckUnusedCSSClassSelectors(report);
    }
  }

  static String getEntryFileName(ManifestItem itemEntry, EpubPackage epack)
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
    return fileToParse;
  }

  InputStream getInputStream(String name) throws
      IOException
  {
    ZipEntry entry = zip.getEntry(name);
    if (entry == null)
    {
      return null;
    }
    InputStream in = zip.getInputStream(entry);
    EncryptionFilter filter = enc.get(name);
    if (filter == null)
    {
      return in;
    }
    if (filter.canDecrypt())
    {
      return filter.decrypt(in);
    }
    return null;
  }

  void searchInsideValue(CSSStyleAttributeHandler.StyleAttribute entry, SearchDictionary tds, String file)
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
        report.message(messageCode, EPUBLocation.create(file, entry.getLine(), entry.getColumn(), entry.getValue().trim()));
      }
    }
  }
}


