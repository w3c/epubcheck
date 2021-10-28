package com.adobe.epubcheck.ctc;

import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.ContentValidator.ValidationType;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.util.EPUBVersion;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class CheckManager
{
  private final EpubPackage epack;
  private final EpubCheckContentFactory factory;
  private Report report;

  public CheckManager(ZipFile zip, Report report)
  {
    setReport(report);
    PackageReader pr = new PackageReader(zip, report);
    epack = pr.readPackageData();
    factory = EpubCheckContentFactory.getInstance();
  }

  private void setReport(Report report)
  {
    this.report = report;
  }

  public void checkPackage()
  {
    if (epack == null)
    {
      return;
    }

    EPUBVersion version = epack.getVersion();

    if (version != null && version.equals(EPUBVersion.VERSION_3))
    {
      factory.newInstance(report, ValidationType.RENDITION, epack).check();
      factory.newInstance(report, ValidationType.CFI, epack).check();
      factory.newInstance(report, ValidationType.METADATA_V3, epack).check();
      factory.newInstance(report, ValidationType.NAV, epack).check();
    }
    else if (version != null && EPUBVersion.VERSION_2.equals(version))
    {
      factory.newInstance(report, ValidationType.EPUB3_STRUCTURE, epack).check();
      factory.newInstance(report, ValidationType.METADATA_V2, epack).check();
    }

    factory.newInstance(report, ValidationType.NCX, epack).check();
    factory.newInstance(report, ValidationType.MULTIPLE_CSS, epack).check();
    factory.newInstance(report, ValidationType.HTML_STRUCTURE, epack).check();
    factory.newInstance(report, ValidationType.LINK, epack).check();
    factory.newInstance(report, ValidationType.CSS_SEARCH, epack).check();
    factory.newInstance(report, ValidationType.TOC, epack).check();
    factory.newInstance(report, ValidationType.LANG, epack).check();
    factory.newInstance(report, ValidationType.SPINE, epack).check();
    factory.newInstance(report, ValidationType.TEXT, epack).check();
    factory.newInstance(report, ValidationType.SCRIPT, epack).check();
    factory.newInstance(report, ValidationType.SPAN, epack).check();
    factory.newInstance(report, ValidationType.SVG, epack).check();
  }
}
