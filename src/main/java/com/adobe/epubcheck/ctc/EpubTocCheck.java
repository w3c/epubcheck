package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.PackageSpine;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.EPUBVersion;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubTocCheck implements DocumentValidator
{
  private final String pathRootFile;
  private final EpubPackage epack;
  private final Report report;

  public EpubTocCheck(EpubPackage epack, Report report)
  {
    this.pathRootFile = epack.getPackageMainFile();
    this.epack = epack;
    this.report = report;
  }

  @Override
  public boolean validate()
  {
    boolean result = true;
    PackageSpine spine = epack.getSpine();
    if (spine == null || (spine.getToc() == null && epack.getVersion() == EPUBVersion.VERSION_2))
    {
      report.message(MessageId.NCX_002, EPUBLocation.create(pathRootFile));
      result = false;
    }
    return result;
  }
}
