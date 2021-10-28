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
public interface ContentValidator
{
  public enum ValidationType
  {
    TEXT, CONTENT, SPINE, NCX, NAV, SCRIPT, SPAN, STYLE, METADATA_V3, METADATA_V2, LANG, CSS_SEARCH, LINK, RENDITION, CFI, HTML_STRUCTURE, MULTIPLE_CSS, EPUB3_STRUCTURE, TOC, SVG
  }

  public Checker newInstance(Report report, ValidationType vt, EpubPackage epack);
}
