package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.FeatureEnum;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EpubRenditionCheck implements DocumentValidator
{
  private final Document doc;
  private final EpubPackage epack;
  private final Report report;

  public EpubRenditionCheck(EpubPackage epack, Report report)
  {
    this.epack = epack;
    this.doc = epack.getPackDoc();
    this.report = report;
  }

  @Override
  public boolean validate()
  {
    return isRenditionDefined(doc);
  }

  private boolean isRenditionDefined(Document doc)
  {
    boolean result = false;
    NodeList nList = doc.getElementsByTagName("metadata");
    if (nList.getLength() > 0)
    {
      Node metadata = nList.item(0);
      NodeList metaNodes = metadata.getChildNodes();

      for (int i = 0; i < metaNodes.getLength(); i++)
      {
        if (metaNodes.item(i).getNodeName().compareToIgnoreCase("meta") == 0)
        {
          Node n = metaNodes.item(i);
          String nodeValue;
          if (n.hasChildNodes())
          {
            nodeValue = n.getFirstChild().getNodeValue();
          }
          else
          {
            nodeValue = n.getNodeValue();
          }

          NamedNodeMap attrs = n.getAttributes();
          Node p = attrs.getNamedItem("property");
          if (p != null)
          {
            if (p.getNodeValue().contains("rendition:layout"))
            {
              report.info(null, FeatureEnum.RENDITION_LAYOUT, nodeValue);
              result = true;
            }
            if (p.getNodeValue().contains("rendition:orientation"))
            {
              report.info(null, FeatureEnum.RENDITION_ORIENTATION, nodeValue);
              result = true;
            }
            if (p.getNodeValue().contains("rendition:spread"))
            {
              report.info(null, FeatureEnum.RENDITION_SPREAD, nodeValue);
              result = true;
            }
          }
        }
      }
    }

    NodeList mList = doc.getElementsByTagName("spine");
    if (mList.getLength() > 0)
    {
      Node manifest = mList.item(0);
      NodeList itemNodes = manifest.getChildNodes();
      String renditionLayout = "rendition:layout-";
      String renditionOrientation = "rendition:orientation-";
      String renditionSpread = "rendition:spread-";

      for (int i = 0; i < itemNodes.getLength(); i++)
      {
        if (itemNodes.item(i).getNodeName().compareToIgnoreCase("itemref") == 0)
        {
          Node n = itemNodes.item(i);

          String nodeValue;
          NamedNodeMap attrs = n.getAttributes();
          Node p = attrs.getNamedItem("properties");
          Node idrefNode = attrs.getNamedItem("idref");
          String idref;
          ManifestItem manifestItem;
          String href = "";
          if (idrefNode != null)
          {
            idref = idrefNode.getNodeValue();
            manifestItem = epack.getManifest().getItem(idref);
            if (manifestItem != null)
            {
              href = epack.getManifestItemFileName(manifestItem);
            }
          }

          if (p != null)
          {  //<itemref idref="page001" properties="rendition:layout-pre-paginated" />
            if (p.hasChildNodes())
            {
              nodeValue = p.getFirstChild().getNodeValue();
            }
            else
            {
              nodeValue = p.getNodeValue();
            }

            if (p.getNodeValue().contains(renditionLayout))
            {
              report.info(href, FeatureEnum.RENDITION_LAYOUT, nodeValue.substring(renditionLayout.length()));
              result = true;
            }
            if (p.getNodeValue().contains(renditionOrientation))
            {
              report.info(href, FeatureEnum.RENDITION_ORIENTATION, nodeValue.substring(renditionOrientation.length()));
              result = true;
            }
            if (p.getNodeValue().contains(renditionSpread))
            {
              report.info(href, FeatureEnum.RENDITION_SPREAD, nodeValue.substring(renditionSpread.length()));
              result = true;
            }
          }
        }
      }
    }
    return result;
  }
}
