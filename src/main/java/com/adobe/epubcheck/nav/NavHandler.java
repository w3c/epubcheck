package com.adobe.epubcheck.nav;

import java.util.Set;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.ops.OPSHandler30;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.StructureVocab.EPUB_TYPES;
import com.adobe.epubcheck.xml.XMLParser;

public class NavHandler extends OPSHandler30
{

  NavHandler(ValidationContext context, XMLParser parser)
  {
    super(context, parser);
  }

  protected void checkTypes(Set<EPUB_TYPES> types)
  {
    super.checkTypes(types);
    if (types.contains(EPUB_TYPES.PAGE_LIST))
    {
      context.featureReport.report(FeatureEnum.PAGE_LIST, path, null);
    }
  }

}
