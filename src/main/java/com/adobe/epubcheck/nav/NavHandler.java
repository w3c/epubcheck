package com.adobe.epubcheck.nav;

import java.util.Set;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.ops.OPSHandler30;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.StructureVocab.EPUB_TYPES;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;

public class NavHandler extends OPSHandler30
{

  private boolean inToc = false;

  NavHandler(ValidationContext context, XMLParser parser)
  {
    super(context, parser);
  }

  @Override
  public void startElement()
  {
    super.startElement();
    XMLElement e = parser.getCurrentElement();
    String name = e.getName();
    if (inToc && "a".equals(name))
    {
      context.featureReport.report(FeatureEnum.TOC_LINKS, EPUBLocation.create(path));
    }
  }

  @Override
  public void endElement()
  {
    super.endElement();
    XMLElement e = parser.getCurrentElement();
    String name = e.getName();
    if (inToc && "nav".equals(name))
    {
      inToc = false;
    }
  }

  protected void checkTypes(Set<EPUB_TYPES> types)
  {
    super.checkTypes(types);
    if (types.contains(EPUB_TYPES.PAGE_LIST))
    {
      context.featureReport.report(FeatureEnum.PAGE_LIST, EPUBLocation.create(path));
    }
    if (types.contains(EPUB_TYPES.TOC))
    {
      inToc = true;
    }
  }

}
