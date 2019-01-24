package com.adobe.epubcheck.ctc;

import java.util.HashSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.epubpackage.PackageManifest;
import com.adobe.epubcheck.ctc.epubpackage.PackageSpine;
import com.adobe.epubcheck.ctc.epubpackage.SpineItem;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.reporting.CheckingReport;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubNCXCheck implements DocumentValidator
{
  private final XmlDocParser docParser;
  private final Document doc;
  private final String pathRootFile;
  private final Report report;
  private final EpubPackage epack;
  private String ncxDoc;


  public EpubNCXCheck(EpubPackage epack, Report report)
  {
    this.doc = epack.getPackDoc();
    this.report = report;
    this.pathRootFile = epack.getPackageMainFile();
    this.epack = epack;
    docParser = new XmlDocParser(epack.getZip(), report);
  }

  @Override
  public boolean validate()
  {
    boolean result = isNCXDefined(doc);
    if (result)
    {
      String fileToParse = epack.getManifestItemFileName(ncxDoc);
      checkNcxDoc(fileToParse);
    }

    if (!result && epack.getVersion() != EPUBVersion.VERSION_2)
    {
      if (report.getClass() == CheckingReport.class)
      {
        report.message(MessageId.NCX_003, EPUBLocation.create(pathRootFile));
      }
      else
      {
        report.info(pathRootFile, FeatureEnum.HAS_NCX, "false");
      }
    }

    return result;
  }

  private boolean isNCXDefined(Document doc)
  {
    boolean isNCXdefined = false;
    NodeList spineList = doc.getElementsByTagName("spine");
    if (spineList.getLength() > 0)
    {
      for (int i = 0; i < spineList.getLength(); i++)
      {
        NamedNodeMap attrs = spineList.item(i).getAttributes();
        Node n = attrs.getNamedItem("toc");
        if (n != null)
        {
          String tocID = n.getNodeValue();
          NodeList manifestList = doc.getElementsByTagName("manifest");
          for (int m = 0; m < manifestList.getLength(); m++)
          {
            Node manifestNode = manifestList.item(m);
            NodeList itemNodes = manifestNode.getChildNodes();

            for (int it = 0; it < itemNodes.getLength(); it++)
            {
              NamedNodeMap itemNodeAttributes = itemNodes.item(it).getAttributes();
              if (itemNodeAttributes != null)
              {
                String manifestNodeID = itemNodeAttributes.getNamedItem("id").getNodeValue();
                if (manifestNodeID != null && manifestNodeID.compareToIgnoreCase(tocID) == 0 && itemNodeAttributes.getNamedItem("href").getNodeValue() != null)
                {
                  isNCXdefined = true;
                  this.ncxDoc = itemNodeAttributes.getNamedItem("href").getNodeValue();
                }
              }
            }
          }
        }
      }
    }

    return isNCXdefined;
  }

  private void checkNcxDoc(String navDocEntry)
  {
    Document doc = docParser.parseDocument(navDocEntry);

    if (doc != null)
    {
      HashSet<String> tocLinkSet = new HashSet<String>();
      String ncxNS = "http://www.daisy.org/z3986/2005/ncx/";
      NodeList n = doc.getElementsByTagNameNS(ncxNS, "navPoint");
      for (int i = 0; i < n.getLength(); i++)
      {
        Element navElement = (Element) n.item(i);
        String playOrder = navElement.getAttributeNS(ncxNS, "playOrder");
        NodeList contentNodes = navElement.getElementsByTagNameNS(ncxNS, "content");
        if (contentNodes.getLength() > 0)
        {
          Element content = (Element) contentNodes.item(0);
          String path = content.getAttributeNS(ncxNS, "src");
          int hash = path.indexOf("#");
          if (hash >= 0)
          {
            path = path.substring(0, hash);
          }
          try
          {
            path = PathUtil.resolveRelativeReference(navDocEntry, path);
          }
          catch (IllegalArgumentException ex)
          {
            // safe to ignore, was already reported as RSC_005 in NCXHandler
          }
          if (!path.equals(""))
          {
            tocLinkSet.add(path);
            playOrder = playOrder != null ? playOrder.trim() : playOrder;
            if (validateInt(playOrder))
            {
              report.info(path, FeatureEnum.NAVIGATION_ORDER, playOrder);
            }
          }
        }
      }
      n = doc.getElementsByTagNameNS(ncxNS, "pageList");
      if (n.getLength() > 0)
      {
        Element pageList = (Element) n.item(0);
        report.message(MessageId.NCX_005, EPUBLocation.create(navDocEntry, getElementLineNumber(pageList), getElementColumnNumber(pageList), pageList.getTagName()));
      }

      PackageManifest manifest = epack.getManifest();
      PackageSpine spine = epack.getSpine();

      if (spine != null)
      {
        String tocFileName = spine.getToc();
        for (int i = 0; i < spine.itemsLength(); ++i)
        {
          SpineItem si = spine.getItem(i);
          ManifestItem mi = manifest.getItem(si.getIdref());
          if (mi != null)
          {
            String path = mi.getHref();
            path = PathUtil.resolveRelativeReference(epack.getPackageMainFile(), path);

            if (path != null && !path.equals(tocFileName) && !path.equals(navDocEntry) && !tocLinkSet.contains(path))
            {
              report.message(MessageId.OPF_059, EPUBLocation.create(navDocEntry, path), si.getIdref());
            }
          }
          else
          {
            // id not found in manifest
            report.message(MessageId.OPF_049, EPUBLocation.create(navDocEntry, epack.getPackageMainPath()), si.getIdref());
          }
        }
      }
    }
  }

  private boolean validateInt(String number)
  {
    if (number == null || number.length() == 0)
    {
      return false;
    }
    try
    {
      Integer.parseInt(number);
      return true;
    }
    catch (NumberFormatException ex)
    {
      return false;
    }
  }

  public static int getElementLineNumber(Element e)
  {
     return getElementIntAttribute( e, EpubConstants.ElementLineNumberAttribute);
  }

  public static int getElementColumnNumber(Element e)
  {
    return getElementIntAttribute( e, EpubConstants.ElementColumnNumberAttribute);

  }

  static int getElementIntAttribute(Element e, String whichAttribute)
  {
    int val = -1;
    String number = e.getAttribute(whichAttribute);
    if (number != null)
    {
      try
      {
        val = Integer.parseInt(number.trim());
      }
      catch (NumberFormatException ex)
      {
       val = -1;
      }
    }
    return val;
  }

}
