package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.*;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import org.w3c.dom.*;

import java.util.HashSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubNavCheck implements DocumentValidator
{

  private final XmlDocParser docParser;
  private final Document packageMainDocument;
  private final EpubPackage epack;
  private final Report report;

  public EpubNavCheck(EpubPackage epack, Report report)
  {
    ZipFile zip = epack.getZip();
    this.packageMainDocument = epack.getPackDoc();
    this.epack = epack;
    docParser = new XmlDocParser(zip, report);
    this.report = report;
  }


  @Override
  public boolean validate()
  {
    boolean result = false;
    Vector<String> navDocPath = getNAVDocuments(packageMainDocument);

    if (navDocPath != null && navDocPath.size() > 0)
    {
      for (String navDoc : navDocPath)
      {
        if (navDoc != null)
        {
          String fileToParse;
          if (epack.getPackageMainPath() != null && epack.getPackageMainPath().length() > 0)
          {
            fileToParse = PathUtil.resolveRelativeReference(epack.getPackageMainFile(), navDoc, null);
          }
          else
          {
            fileToParse = navDoc;
          }

          ZipEntry entry = epack.getZip().getEntry(fileToParse);
          if (entry == null)
          {
            report.message(MessageId.RSC_001, new MessageLocation(epack.getFileName(), -1, -1), fileToParse);
            continue;
          }

          checkNavDoc(fileToParse);
        }
      }
    }

    return result;
  }

  private Vector<String> getNAVDocuments(Document doc)
  {
    Vector<String> navItems = new Vector<String>();

    NodeList manifestList = doc.getElementsByTagName("manifest");
    for (int m = 0; m < manifestList.getLength(); m++)
    {
      Node manifestNode = manifestList.item(m);
      NodeList itemNodes = manifestNode.getChildNodes();

      for (int it = 0; it < itemNodes.getLength(); it++)
      {
        NamedNodeMap itemNodeAttributes = itemNodes.item(it).getAttributes();
        if (itemNodeAttributes != null && itemNodeAttributes.getNamedItem("properties") != null)
        {
          String nodePropertiesAttr = itemNodeAttributes.getNamedItem("properties").getNodeValue();
          if (nodePropertiesAttr != null && nodePropertiesAttr.compareToIgnoreCase("nav") == 0)
          {
            String hrefValue = null;
            if (itemNodeAttributes.getNamedItem("href").getNodeValue() != null)
            {
              hrefValue = itemNodeAttributes.getNamedItem("href").getNodeValue();
            }

            navItems.add(hrefValue);
          }
        }
      }
    }
    return navItems;
  }

  private boolean checkNavDoc(String navDocEntry)
  {
    HashSet<String> tocLinkSet = new HashSet<String>();
    boolean containsNavElements = false;
    Document doc = docParser.parseDocument(navDocEntry);
    if (doc == null)
    {
      // no need to report an error here because it was already reported inside of the docParser.
      return false;
    }
    NodeList n = doc.getElementsByTagName("nav");

    for (int i = 0; i < n.getLength(); i++)
    {
      Element navElement = (Element) n.item(i);
      String type = navElement.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type");
      if (type != null && type.equals("toc"))
      {
        containsNavElements = true;
        NodeList links = navElement.getElementsByTagName("a");
        int navOrder = 1;
        for (int j = 0; j < links.getLength(); j++)
        {
          Element link = (Element) links.item(j);
          String href = link.getAttribute("href");
          String path = href;
          int hash = href.indexOf("#");
          if (hash >= 0)
          {
            path = href.substring(0, hash);
          }
          path = PathUtil.resolveRelativeReference(navDocEntry, path, null);

          if (!path.equals("") && !tocLinkSet.contains(path))
          {
            report.info(path, FeatureEnum.NAVIGATION_ORDER, String.valueOf(navOrder));
            navOrder++;
            tocLinkSet.add(path);
          }
        }
      }
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
        String path = mi.getHref();
        path = PathUtil.resolveRelativeReference(epack.getPackageMainFile(), path,  null);

        if (path != null && !path.equals(tocFileName) && !path.equals(navDocEntry) && !tocLinkSet.contains(path))
        {
          report.message(MessageId.OPF_058, new MessageLocation(navDocEntry, -1, -1, path));
        }
      }
    }
    return containsNavElements;
  }
}
