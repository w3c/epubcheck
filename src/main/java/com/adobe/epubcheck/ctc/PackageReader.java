package com.adobe.epubcheck.ctc;

import java.util.Vector;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.epubpackage.MetadataElement;
import com.adobe.epubcheck.ctc.epubpackage.PackageManifest;
import com.adobe.epubcheck.ctc.epubpackage.PackageSpine;
import com.adobe.epubcheck.ctc.epubpackage.SpineItem;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;

class PackageReader
{
  private static final String containerEntry = "META-INF/container.xml";
  private ZipFile zip;
  private Report report;
  private String version;
  private EpubPackage epack;
  private String mainPackageFile;

  public PackageReader(ZipFile zip, Report report)
  {
    this.zip = zip;
    this.report = report;
  }

  public EpubPackage readPackageData()
  {
    Vector<String> pathToRootFile = getPathToRootFile();

    XmlDocParser p = new XmlDocParser(zip, report);
    for (String path : pathToRootFile)
    {
      setMainPackageFile(path);
      Document doc = p.parseDocument(path);
      if (doc != null)
      {
        epack = new EpubPackage(path, zip, doc);
        epack.setPackageMainFile(getMainPackageFile());
        epack.setVersion(getEpubVersion(doc));
        getMetadata(doc, epack);
        getManifest(doc, epack);
        getSpine(doc, epack);
      }
    }
    return epack;
  }

  String getMainPackageFile()
  {
    return mainPackageFile;
  }

  void setMainPackageFile(String mainPackageFile)
  {
    this.mainPackageFile = mainPackageFile;
  }

  private Vector<String> getPathToRootFile()
  {
    Vector<String> rootFiles = new Vector<String>();

    XmlDocParser p = new XmlDocParser(zip, report);
    Document doc = p.parseDocument(containerEntry);
    if (doc != null)
    {
      NodeList nList = doc.getElementsByTagName("rootfiles");

      for (int i = 0; i < nList.getLength(); i++)
      {
        Node n = nList.item(i);
        if (n.getNodeName().compareToIgnoreCase("rootfiles") == 0)
        {
          NodeList cn = n.getChildNodes();
          for (int j = 0; j < cn.getLength(); j++)
          {
            Node currentNode = cn.item(j);
            if (currentNode.getNodeName().compareToIgnoreCase("rootfile") == 0)
            {
              NamedNodeMap attr = currentNode.getAttributes();
              Node path = attr.getNamedItem("full-path");
              if (path != null && !path.getNodeValue().isEmpty())
              {
                String nodeValue = path.getNodeValue();
                rootFiles.add(nodeValue);
              }
            }
          }
        }
      }
    }
    return rootFiles;
  }

  private void getMetadata(Document doc, EpubPackage epack)
  {
    NodeList nList = doc.getElementsByTagNameNS(EpubConstants.OpfNamespaceUri, "metadata");
    if (nList.getLength() > 0)
    {
      Node metadata = nList.item(0);
      NodeList metaNodes = metadata.getChildNodes();

      for (int i = 0; i < metaNodes.getLength(); i++)
      {
        String nodeName = metaNodes.item(i).getLocalName();
        if (nodeName != null && !nodeName.startsWith("#"))
        {
          MetadataElement meta = new MetadataElement();

          Node n = metaNodes.item(i);
          meta.setName(n.getNodeName());

          if (n.hasChildNodes())
          {
            //outWriter.println("Dodaje element o nazwie "+metaNodes.item(i).getNodeName()+" z wartoscia "+n.getFirstChild().getNodeValue());
            meta.setValue(n.getFirstChild().getNodeValue());
          }
          else
          {
            //outWriter.println("Dodaje element o nazwie "+metaNodes.item(i).getNodeName()+" z wartoscia "+n.getNodeValue());
            meta.setValue(n.getNodeValue());
          }

          NamedNodeMap attrs = metaNodes.item(i).getAttributes();
          for (int a = 0; a < attrs.getLength(); a++)
          {
            //outWriter.println("	Dodaje attrybut "+attrs.item(a).getNodeName()+" z wartoscia "+attrs.item(a).getNodeValue());
            if (attrs.item(a).getNodeName().compareToIgnoreCase("elementLineNumber") != 0 && attrs.item(a).getNodeName().compareToIgnoreCase("elementColumnNumber") != 0)
            {
              meta.addAttribute(attrs.item(a).getNodeName(), attrs.item(a).getNodeValue());
            }
          }
          epack.getMetadata().addMetaElement(meta);
        }
      }
    }
  }

  private void getManifest(Document doc, EpubPackage epack)
  {
    NodeList nList = doc.getElementsByTagNameNS(EpubConstants.OpfNamespaceUri, "manifest");

    for (int i = 0; i < nList.getLength(); i++)
    {
      Node n = nList.item(i);
      String ln = n.getLocalName();
      if (ln.compareToIgnoreCase("manifest") == 0)
      {
        PackageManifest manifest = new PackageManifest();
        epack.setManifest(manifest);
        NodeList cn = n.getChildNodes();
        for (int j = 0; j < cn.getLength(); j++)
        {
          Node currentNode = cn.item(j);
          String childName = currentNode.getLocalName();
          if (childName != null && childName.compareToIgnoreCase("item") == 0)
          {
            ManifestItem item = new ManifestItem();
            NamedNodeMap attr = currentNode.getAttributes();

            Node hrefNode = attr.getNamedItem("href");
            String hrefValue;
            if (hrefNode != null)
            {
              hrefValue = hrefNode.getNodeValue();
              item.setHref(hrefValue);
            }

            Node mediaTypeNode = attr.getNamedItem("media-type");
            String mediaTypeValue;

            if (mediaTypeNode != null)
            {
              mediaTypeValue = mediaTypeNode.getNodeValue();
              item.setMediaType(mediaTypeValue);
            }

            Node propertiesNode = attr.getNamedItem("properties");
            String propertiesValue;

            if (propertiesNode != null)
            {
              propertiesValue = propertiesNode.getNodeValue();
              item.setProperties(propertiesValue);
            }

            Node idNode = attr.getNamedItem("id");
            String idValue;

            if (idNode != null)
            {
              idValue = idNode.getNodeValue();
              item.setId(idValue);
            }

            manifest.addItem(item);
          }
        }
      }
    }
  }

  private void getSpine(Document doc, EpubPackage epack)
  {
    NodeList nList = doc.getElementsByTagNameNS(EpubConstants.OpfNamespaceUri, "spine");

    for (int i = 0; i < nList.getLength(); i++)
    {
      Node n = nList.item(i);
      String ln = n.getLocalName();
      if (ln.compareToIgnoreCase("spine") == 0)
      {
        PackageSpine spine = new PackageSpine();
        epack.setSpine(spine);
        NamedNodeMap spineAttrs = n.getAttributes();

        Node idNode = spineAttrs.getNamedItem("id");
        if (idNode != null)
        {
          spine.setId(idNode.getNodeValue());
        }

        Node tocNode = spineAttrs.getNamedItem("toc");
        if (tocNode != null)
        {
          spine.setToc(tocNode.getNodeValue());
        }

        Node pageProgressionDirectionNode = spineAttrs.getNamedItem("page-progression-direction");
        if (pageProgressionDirectionNode != null)
        {
          spine.setPageProgressionDirection(pageProgressionDirectionNode.getNodeValue());
        }

        NodeList cn = n.getChildNodes();
        for (int j = 0; j < cn.getLength(); j++)
        {
          Node currentNode = cn.item(j);
          String itemRefName = currentNode.getLocalName();
          if (itemRefName != null && "itemref".compareToIgnoreCase(itemRefName) == 0)
          {
            SpineItem item = new SpineItem();
            NamedNodeMap attr = currentNode.getAttributes();

            Node idrefNode = attr.getNamedItem("idref");
            if (idrefNode != null)
            {
              String idRef = idrefNode.getNodeValue();
              if (idRef != null && idRef.length() > 0)
              {
                item.setIdref(idrefNode.getNodeValue());
              }
              else
              {
                continue;
              }
            }

            Node idSpineNode = attr.getNamedItem("id");
            if (idSpineNode != null)
            {
              item.setId(idSpineNode.getNodeValue());
            }

            Node linearNode = attr.getNamedItem("linear");
            if (linearNode != null)
            {
              item.setLinear(linearNode.getNodeValue());
            }

            Node propertiesNode = attr.getNamedItem("properties");
            if (propertiesNode != null)
            {
              item.setProperties(propertiesNode.getNodeValue());
            }
            spine.addItem(item);
          }
        }
      }
    }
  }

  private EPUBVersion getEpubVersion(Document doc)
  {
    NodeList packageNode = doc.getElementsByTagNameNS(EpubConstants.OpfNamespaceUri, "package");
    if (packageNode == null)
    {
      return EPUBVersion.Unknown;
    }

    Node firstItem = packageNode.item(0);
    if (firstItem == null)
    {
      return EPUBVersion.Unknown;
    }

    NamedNodeMap packageNodeAttr = firstItem.getAttributes();
    if (packageNodeAttr == null)
    {
      return EPUBVersion.Unknown;
    }

    Node node = packageNodeAttr.getNamedItem("version");
    if (node != null)
    {
      version = node.getNodeValue();
    }
    if (version != null && version.startsWith("3"))
    {
      return EPUBVersion.VERSION_3;
    }
    else if (version != null && version.startsWith("2"))
    {
      return EPUBVersion.VERSION_2;
    }
    return EPUBVersion.Unknown;
  }
}
