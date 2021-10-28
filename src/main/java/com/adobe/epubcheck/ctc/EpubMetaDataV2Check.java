package com.adobe.epubcheck.ctc;

import org.w3c.dom.Document;
import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubMetaDataV2Check implements Checker
{
  private final Document doc;
  private final String pathRootFile;
  @SuppressWarnings("unused")
  private final Report report;

  public EpubMetaDataV2Check(EpubPackage epack, Report report)
  {
    this.doc = epack.getPackDoc();
    this.pathRootFile = epack.getPackageMainFile();
    this.report = report;
  }

  @Override
  public void check()
  {
    isMetaDataValid(doc, pathRootFile);
  }

  private void isMetaDataValid(Document doc, String pathRootFile)
  {
    // no custom checks
  }
}
