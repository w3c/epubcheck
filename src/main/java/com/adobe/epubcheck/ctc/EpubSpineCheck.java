package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.MetadataElement;
import com.adobe.epubcheck.ctc.epubpackage.SpineItem;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.outWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubSpineCheck implements DocumentValidator
{
  private static final int MAX_SPINE_ITEM_THRESHOLD = 100;
  private final EpubPackage epubPack;
  private final Document doc;
  private final String pathRootFile;
  private final Report report;

  public EpubSpineCheck(EpubPackage epubPack, Report report)
  {
    this.epubPack = epubPack;
    this.doc = epubPack.getPackDoc();
    pathRootFile = epubPack.getPackageMainFile();
    this.report = report;
  }

  public boolean validate()
  {
    boolean resExists = isSpineDefined(doc, pathRootFile);
    boolean resElements = tooManySpineElements(doc, pathRootFile, MAX_SPINE_ITEM_THRESHOLD);

    return (resExists && resElements);
  }

  private boolean isSpineDefined(Document doc, String pathRootFile)
  {
    NodeList spines = doc.getElementsByTagNameNS(EpubConstants.OpfNamespaceUri, "spine");
    Node spine = spines.getLength() > 0 ? spines.item(0) : null;
    if (spine == null)
    {
      report.message(MessageId.OPF_019, EPUBLocation.create(pathRootFile));
      outWriter.println("Spine element not found within package root document " + pathRootFile);
      return false;
    }
    return true;
  }

  private boolean tooManySpineElements(Document doc, String pathRootFile, int maxSpineElements)
  {
    boolean isFixedFormat = false;
    for (SpineItem si : epubPack.getSpine().getItems())
    {
      String val = si.getProperties();
      if (val != null && val.equalsIgnoreCase("rendition:layout-pre-paginated"))
      {
        isFixedFormat = true;
        break;
      }
    }

    if (!isFixedFormat)
    {
      for (MetadataElement e : epubPack.getMetadata().getMetaElements())
      {
        if (e.getName().equals("meta"))
        {
          String prop = e.getAttribute("property");
          if (prop != null && prop.equalsIgnoreCase("rendition:layout"))
          {
            // #727 NPE guard
            String val = e.getValue();
            if (val != null && val.equalsIgnoreCase("pre-paginated"))
            {
              isFixedFormat = true;
              break;
            }
          }
        }
      }
    }

    if (!isFixedFormat)
    {
      NodeList spine = doc.getElementsByTagName("spine");
      if (spine.getLength() > 0)
      {
        NodeList spineElements = spine.item(0).getChildNodes();
        if (spineElements.getLength() > maxSpineElements)
        {
          report.message(MessageId.OPF_020, EPUBLocation.create(pathRootFile));
          return false;
        }
      }
    }
    return true;
  }
}
