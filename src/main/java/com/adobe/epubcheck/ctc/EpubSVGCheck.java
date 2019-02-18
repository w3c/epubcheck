package com.adobe.epubcheck.ctc;

import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.epubpackage.SpineItem;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.SearchDictionary;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubSVGCheck implements DocumentValidator
{
  private static final String svgNS = "http://www.w3.org/2000/svg";
  private static final String xlinkNS = "http://www.w3.org/1999/xlink";
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
        String fileToParse = epack.getManifestItemFileName(itemEntry);

        ZipEntry entry = epack.getZip().getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(epack.getFileName()), fileToParse);
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
          if (itemIsFixedFormat)
          {
            checkSvgDoc(fileToParse);
          }
        }
      }
    }
    return true;
  }

  void checkSvgDoc(String svgDocEntry)
  {
    Document doc = docParser.parseDocument(svgDocEntry);
    if (doc != null)
    {
      checkViewBox(svgDocEntry, doc);
      checkImageXlinkHrefInline(svgDocEntry, doc);
    }
  }

  // FIXME this RegEX is a bit too naïve for CSS syntax rules
  // e.g. escape values and comments are theoretically allowed too
  private static Pattern PIXEL_LENGTH_REGEX = Pattern.compile("\\d+(px)?");

  void checkViewBox(String svgDocEntry, Document doc)
  {
    NodeList n = doc.getElementsByTagNameNS(svgNS, "svg");
    if (n.getLength() > 0)
    {
      Element svgElement = (Element) n.item(0);
      String viewport = svgElement.getAttributeNS(svgNS, "viewBox");
      boolean viewportFound = (viewport != null && viewport.trim().length() > 0);
      String height = svgElement.getAttributeNS(svgNS, "height");
      boolean heightFound = (height != null && height.trim().length() > 0);
      boolean isHeightInPixel = heightFound && PIXEL_LENGTH_REGEX.matcher(height.trim()).matches();
      String width = svgElement.getAttributeNS(svgNS, "width");
      boolean widthFound = (width != null && width.trim().length() > 0);
      boolean isWidthInPixel = widthFound && PIXEL_LENGTH_REGEX.matcher(width.trim()).matches();
      if (!viewportFound)
      {
        if (!heightFound || !widthFound)
        {
          report.message(MessageId.HTM_048,
              EPUBLocation.create(svgDocEntry, XmlDocParser.getElementLineNumber(svgElement),
                  XmlDocParser.getElementColumnNumber(svgElement)));
        }
        else
        {
          report.message(MessageId.HTM_054,
              EPUBLocation.create(svgDocEntry, XmlDocParser.getElementLineNumber(svgElement),
                  XmlDocParser.getElementColumnNumber(svgElement)));
          if (!isHeightInPixel || !isWidthInPixel)
          {
            report.message(MessageId.HTM_055,
                EPUBLocation.create(svgDocEntry, XmlDocParser.getElementLineNumber(svgElement),
                    XmlDocParser.getElementColumnNumber(svgElement)));
          }
        }
      }
    }
  }

  void checkImageXlinkHrefInline(String svgDocEntry, Document doc)
  {
    NodeList n = doc.getElementsByTagNameNS(svgNS, "image");
    for (int i = 0; i < n.getLength(); i++)
    {
      Element svgElement = (Element) n.item(i);
      String href = svgElement.getAttributeNS(xlinkNS, "href");
      if (href != null && href.length() > 0)
      {
        if (!href.startsWith("data:image"))
        {
          report.message(MessageId.MED_006, EPUBLocation.create(svgDocEntry, XmlDocParser.getElementLineNumber(svgElement), XmlDocParser.getElementColumnNumber(svgElement)));
        }
      }
    }
  }

  public boolean isGlobalFixedFormat()
  {
    return isGlobalFixedFormat;
  }
}
