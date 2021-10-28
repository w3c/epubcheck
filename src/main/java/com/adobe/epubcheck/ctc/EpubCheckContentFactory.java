package com.adobe.epubcheck.ctc;

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
public class EpubCheckContentFactory implements ContentValidator
{
  static private final EpubCheckContentFactory instance = new EpubCheckContentFactory();

  static public EpubCheckContentFactory getInstance()
  {
    return instance;
  }

  @Override
  public Checker newInstance(Report report, ValidationType vt, EpubPackage epack)
  {
    if (vt.equals(ValidationType.METADATA_V3))
    {
      return new EpubMetaDataV3Check(epack, report);
    }
    if (vt.equals(ValidationType.METADATA_V2))
    {
      return new EpubMetaDataV2Check(epack, report);
    }
    if (vt.equals(ValidationType.TEXT))
    {
      return new EpubTextContentCheck(report, epack);
    }
    else if (vt.equals(ValidationType.NAV))
    {
      return new EpubNavCheck(epack, report);
    }
    else if (vt.equals(ValidationType.NCX))
    {
      return new EpubNCXCheck(epack, report);
    }
    else if (vt.equals(ValidationType.SPINE))
    {
      return new EpubSpineCheck(epack, report);
    }
    else if (vt.equals(ValidationType.SCRIPT))
    {
      return new EpubScriptCheck(epack, report);
    }
    else if (vt.equals(ValidationType.SPAN))
    {
      return new EpubSpanCheck(epack, report);
    }
    else if (vt.equals(ValidationType.LANG))
    {
      return new EpubLangCheck(epack, report);
    }
    else if (vt.equals(ValidationType.CSS_SEARCH))
    {
      return new EpubCSSCheck(epack, report);
    }
    else if (vt.equals(ValidationType.LINK))
    {
      return new EpubExtLinksCheck(epack, report);
    }
    else if (vt.equals(ValidationType.RENDITION))
    {
      return new EpubRenditionCheck(epack, report);
    }
    else if (vt.equals(ValidationType.CFI))
    {
      return new EpubCfiCheck(epack, report);
    }
    else if (vt.equals(ValidationType.HTML_STRUCTURE))
    {
      return new EpubHTML5StructureCheck(epack, report);
    }
    else if (vt.equals(ValidationType.MULTIPLE_CSS))
    {
      return new EpubStyleSheetsCheck(epack, report);
    }
    else if (vt.equals(ValidationType.EPUB3_STRUCTURE))
    {
      return new Epub3StructureCheck(epack, report);
    }
    else if (vt.equals(ValidationType.TOC))
    {
      return new EpubTocCheck(epack, report);
    }
    else if (vt.equals(ValidationType.SVG))
    {
      return new EpubSVGCheck(epack, report);
    }
    else
    {
      return null;
    }
  }
}
