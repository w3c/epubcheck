package com.adobe.epubcheck.ctc;

import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.ContentValidator.ValidationType;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.util.EPUBVersion;

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
      factory.newInstance(report, ValidationType.RENDITION, epack).validate();
      factory.newInstance(report, ValidationType.CFI, epack).validate();
      factory.newInstance(report, ValidationType.METADATA_V3, epack).validate();
      factory.newInstance(report, ValidationType.NAV, epack).validate();
    }
    else if (version != null && EPUBVersion.VERSION_2.equals(version))
    {
      factory.newInstance(report, ValidationType.EPUB3_STRUCTURE, epack).validate();
      factory.newInstance(report, ValidationType.METADATA_V2, epack).validate();
    }

    factory.newInstance(report, ValidationType.NCX, epack).validate();
    factory.newInstance(report, ValidationType.MULTIPLE_CSS, epack).validate();
    factory.newInstance(report, ValidationType.HTML_STRUCTURE, epack).validate();
    factory.newInstance(report, ValidationType.LINK, epack).validate();
    factory.newInstance(report, ValidationType.CSS_SEARCH, epack).validate();
    factory.newInstance(report, ValidationType.TOC, epack).validate();
    factory.newInstance(report, ValidationType.LANG, epack).validate();
    factory.newInstance(report, ValidationType.SPINE, epack).validate();
    factory.newInstance(report, ValidationType.TEXT, epack).validate();
    factory.newInstance(report, ValidationType.SCRIPT, epack).validate();
    factory.newInstance(report, ValidationType.SPAN, epack).validate();
    factory.newInstance(report, ValidationType.SVG, epack).validate();
  }
}
