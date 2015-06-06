package com.adobe.epubcheck.vocab;

public final class RenditionVocabs
{
  public static final String PREFIX = "rendition";
  public static final String URI = "http://www.idpf.org/vocab/rendition/#";

  public static final EnumVocab<META_PROPERTIES> META_VOCAB = new EnumVocab<META_PROPERTIES>(
      META_PROPERTIES.class, URI);

  public enum META_PROPERTIES
  {
    FLOW,
    LAYOUT,
    ORIENTATION,
    SPREAD,
    VIEWPORT
  }

  public static final EnumVocab<ITEMREF_PROPERTIES> ITEMREF_VOCAB = new EnumVocab<ITEMREF_PROPERTIES>(
      ITEMREF_PROPERTIES.class, URI);

  public enum ITEMREF_PROPERTIES
  {
    ALIGN_X_CENTER,
    FLOW_AUTO,
    FLOW_PAGINATED,
    FLOW_SCROLLED_CONTINUOUS,
    FLOW_SCROLLED_DOC,
    LAYOUT_PRE_PAGINATED,
    LAYOUT_REFLOWABLE,
    ORIENTATION_AUTO,
    ORIENTATION_LANDSCAPE,
    ORIENTATION_PORTRAIT,
    PAGE_SPREAD_CENTER,
    SPREAD_AUTO,
    SPREAD_BOTH,
    SPREAD_LANDSCAPE,
    SPREAD_NONE,
    SPREAD_PORTRAIT
  }

  private RenditionVocabs()
  {
  }
}
