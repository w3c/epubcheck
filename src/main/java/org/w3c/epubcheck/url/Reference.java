package org.w3c.epubcheck.url;

import com.adobe.epubcheck.api.EPUBLocation;

import io.mola.galimatias.URL;

public final class Reference
{

  public static enum Type
  {
    GENERIC,
    FONT,
    HYPERLINK,
    LINK,
    IMAGE,
    OBJECT,
    STYLESHEET,
    AUDIO,
    VIDEO,
    SVG_PAINT,
    SVG_CLIP_PATH,
    SVG_SYMBOL,
    REGION_BASED_NAV,
    SEARCH_KEY,
    NAV_TOC_LINK,
    NAV_PAGELIST_LINK,
    OVERLAY_TEXT_LINK,
    PICTURE_SOURCE,
    PICTURE_SOURCE_FOREIGN;
  };

  public final URL url;
  public final EPUBLocation location;
  public final Type type;

  public Reference(URL url, EPUBLocation location, Type type)
  {
    this.url = url;
    this.location = location;
    this.type = type;
  }

}
