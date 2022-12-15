package org.w3c.epubcheck.core.references;

import com.adobe.epubcheck.api.EPUBLocation;
import com.google.common.base.Preconditions;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public final class Reference
{
  public static enum Type
  {
    // Linked resources
    LINK,
    // Publication resources
    GENERIC,
    STYLESHEET,
    MEDIA_OVERLAY,
    HYPERLINK,
    FONT,
    IMAGE,
    AUDIO,
    VIDEO,
    TRACK,
    CITE,
    // Others, used for internal checks
    SVG_PAINT,
    SVG_CLIP_PATH,
    SVG_SYMBOL,
    REGION_BASED_NAV,
    SEARCH_KEY,
    NAV_TOC_LINK,
    NAV_PAGELIST_LINK,
    OVERLAY_TEXT_LINK;

    public boolean isPublicationResourceReference()
    {
      switch (this)
      {
      case GENERIC:
      case STYLESHEET:
      case FONT:
      case IMAGE:
      case AUDIO:
      case VIDEO:
      case TRACK:
      case MEDIA_OVERLAY:
        return true;
      default:
        return false;
      }
    }
  }

  public final URL url;
  public final URL targetResource;
  public final EPUBLocation location;
  public final Type type;
  public final boolean hasIntrinsicFallback;

  public Reference(URL url, Type type, EPUBLocation location, boolean hasIntrinsicFallback)
  {
    Preconditions.checkArgument(url != null);
    Preconditions.checkArgument(type != null);
    Preconditions.checkArgument(location != null);
    try
    {
      this.url = url;
      this.type = type;
      this.location = location;
      this.targetResource = url.withFragment(null);
      this.hasIntrinsicFallback = hasIntrinsicFallback;
    } catch (GalimatiasParseException e)
    {
      throw new AssertionError(e);
    }
  }

  @Override
  public String toString()
  {
    return url.toString();
  }

}