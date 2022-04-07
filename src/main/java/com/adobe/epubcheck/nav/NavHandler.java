package com.adobe.epubcheck.nav;

import java.util.EnumSet;
import java.util.Set;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.ops.OPSHandler30;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.StructureVocab.EPUB_TYPES;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

import io.mola.galimatias.URL;

public class NavHandler extends OPSHandler30
{

  private NavType currentNavType = NavType.NONE;
  private boolean isNavTypes = false;

  private static enum NavType
  {
    NONE,
    TOC,
    LANDMARKS,
    PAGE_LIST,
    OTHER;

    private Converter<String, String> formatter = CaseFormat.UPPER_UNDERSCORE
        .converterTo(CaseFormat.LOWER_HYPHEN);

    @Override
    public String toString()
    {
      return formatter.convert(this.name());
    }

    public static EnumSet<NavType> TOC__PAGE_LIST = EnumSet.of(TOC, PAGE_LIST);
    public static EnumSet<NavType> TOC__PAGE_LIST__LANDMARKS = EnumSet.of(TOC, PAGE_LIST,
        LANDMARKS);
  }

  NavHandler(ValidationContext context)
  {
    super(context);
  }

  @Override
  public void startElement()
  {
    super.startElement();
    XMLElement e = currentElement();
    if (EpubConstants.HtmlNamespaceUri.equals(e.getNamespace()) && e.getName().equals("a"))
    {
      String href = e.getAttribute("href");
      URL url = checkURL(href);
      if (url != null)
      {
        // Feature reporting
        if (currentNavType == NavType.TOC)
        {
          context.featureReport.report(FeatureEnum.TOC_LINKS, location());
        }

        // For 'toc', 'landmarks', and 'page-list' nav:
        // the link MUST resolve to a Top-level Content Document
        // Note: links to out-of-spine in-container items are already reported
        // (RSC_011), so we only need to report links to remote resources
        if (NavType.TOC__PAGE_LIST__LANDMARKS.contains(currentNavType) && context.isRemote(url))
        {
          report.message(MessageId.NAV_010, location(), currentNavType, href);
        }
        // For 'toc' and 'page-list' nav, register special references to the
        // cross-reference checker, to be able to check that they are in reading
        // order
        // after all the Content Documents have been parsed
        else if ((NavType.TOC__PAGE_LIST.contains(currentNavType)) && xrefChecker.isPresent())
        {
          xrefChecker.get().registerReference(url,
              (currentNavType == NavType.TOC) ? XRefChecker.Type.NAV_TOC_LINK
                  : XRefChecker.Type.NAV_PAGELIST_LINK,
              location());
        }
      }
    }
  }

  @Override
  public void endElement()
  {
    super.endElement();
    XMLElement e = currentElement();
    if (EpubConstants.HtmlNamespaceUri.equals(e.getNamespace()) && e.getName().equals("nav"))
    {
      currentNavType = NavType.NONE;
    }
  }

  @Override
  protected void checkType(String type)
  {
    XMLElement e = currentElement();
    isNavTypes = (EpubConstants.HtmlNamespaceUri.equals(e.getNamespace())
        && e.getName().equals("nav"));
    super.checkType(type);
    isNavTypes = false;
  }

  @Override
  protected void checkTypes(Set<EPUB_TYPES> types)
  {
    super.checkTypes(types);
    if (isNavTypes)
    {
      if (types.contains(EPUB_TYPES.TOC))
      {
        currentNavType = NavType.TOC;
      }
      if (types.contains(EPUB_TYPES.PAGE_LIST))
      {
        currentNavType = NavType.PAGE_LIST;
        context.featureReport.report(FeatureEnum.PAGE_LIST, location());
      }
      if (types.contains(EPUB_TYPES.LANDMARKS))
      {
        currentNavType = NavType.LANDMARKS;
      }
      if (types.contains(EPUB_TYPES.LOI))
      {
        context.featureReport.report(FeatureEnum.LOI, location());
      }
      if (types.contains(EPUB_TYPES.LOT))
      {
        context.featureReport.report(FeatureEnum.LOT, location());
      }
      if (types.contains(EPUB_TYPES.LOA))
      {
        context.featureReport.report(FeatureEnum.LOA, location());
      }
      if (types.contains(EPUB_TYPES.LOV))
      {
        context.featureReport.report(FeatureEnum.LOV, location());
      }
    }
  }

}
