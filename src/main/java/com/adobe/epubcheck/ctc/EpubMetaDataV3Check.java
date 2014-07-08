package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.EpubConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class EpubMetaDataV3Check implements DocumentValidator
{

  private final Document doc;
  private final String pathRootFile;
  private final Report report;

  public EpubMetaDataV3Check(EpubPackage epack, Report report)
  {
    this.doc = epack.getPackDoc();
    this.pathRootFile = epack.getPackageMainFile();
    this.report = report;
  }

  @Override
  public boolean validate()
  {
    return isMetaDataValid(doc, pathRootFile);
  }

  private boolean isMetaDataValid(Document doc, String pathRootFile)
  {
    boolean isMetadataValid = false;
    NodeList metadataList = doc.getDocumentElement().getElementsByTagName("metadata");
    if (metadataList.getLength() == 0)
    {
      metadataList = doc.getDocumentElement().getElementsByTagNameNS(EpubConstants.OpfNamespaceUri, "metadata");
    }
    if (metadataList.getLength() != 1)
    {
      isMetadataValid = false;
      report.message(MessageId.OPF_023, new MessageLocation(pathRootFile, -1, -1), metadataList.getLength());
    }
    return isMetadataValid;
  }
}
