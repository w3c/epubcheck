package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.epubpackage.SpineItem;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Hashtable;
import java.util.zip.ZipEntry;

public class EpubSVGCheck implements DocumentValidator
{
  private final XmlDocParser docParser;
  private final Report report;
  private final EpubPackage epack;
  private boolean isGlobalFixedFormat;


  public EpubSVGCheck(EpubPackage epack, Report report)
  {
    this.report = report;
    this.epack = epack;
    docParser = new XmlDocParser(epack.getZip(), report);
    this.isGlobalFixedFormat = EpubPackage.isGlobalFixed(epack);
  }

  @Override
  public boolean validate()
  {
    SearchDictionary validTypes = new SearchDictionary(SearchDictionary.DictionaryType.SVG_MEDIA_TYPES);

    boolean isGlobalFixed = EpubPackage.isGlobalFixed(this.epack);

    Hashtable<String, SpineItem> spineItems = new Hashtable<String, SpineItem>();
    for (int i = 0; i < epack.getSpine().itemsLength(); ++i)
    {
      SpineItem si = epack.getSpine().getItem(i);
      spineItems.put(si.getIdref(), si);
    }

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

        SpineItem si = spineItems.get(itemEntry.getId());
        boolean itemIsFixedFormat = isGlobalFixed;
        if (si != null)
        {

          String properties = si.getProperties();
          if (properties != null)
          {
            if (properties.length() != 0)
            {
              properties = properties.replaceAll("[\\s]+", " ");
              String propertyArray[] = properties.split(" ");
              for (String prop : propertyArray)
              {
                if (prop.equals("rendition:layout-pre-paginated"))
                {
                  itemIsFixedFormat = true;
                }
                else if (prop.equals("rendition:layout-reflowable"))
                {
                  itemIsFixedFormat = false;
                }
              }
            }
          }
        }
        if (itemIsFixedFormat)
        {
          checkSvgDoc(fileToParse);
        }
      }
    }
    return true;
  }

  private void checkSvgDoc(String svgDocEntry)
  {
    Document doc = docParser.parseDocument(svgDocEntry);
    if (doc != null)
    {
      String svgNS = "http://www.w3.org/2000/svg";
      NodeList n = doc.getElementsByTagNameNS(svgNS, "svg");
      for (int i = 0; i < n.getLength(); i++)
      {
        Element svgElement = (Element) n.item(i);
        String viewport = svgElement.getAttributeNS(svgNS, "viewBox");
        if (viewport == null || viewport.length() == 0)
        {
          report.message(MessageId.HTM_048, new MessageLocation(svgDocEntry, XmlDocParser.getElementLineNumber(svgElement), XmlDocParser.getElementColumnNumber(svgElement)));
        }
      }
    }
  }

  public boolean isGlobalFixedFormat()
  {
    return isGlobalFixedFormat;
  }
}
