package com.adobe.epubcheck.vocab;

import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Preconditions;

public final class RenditionVocabs
{
  public static final String PREFIX = "rendition";
  public static final String URI = "http://www.idpf.org/vocab/rendition/#";

  public static final EnumVocab<META_PROPERTIES> META_VOCAB = new EnumVocab<META_PROPERTIES>(
      META_PROPERTIES.class, URI, PREFIX);

  public enum META_PROPERTIES implements PropertyStatus
  {
    LAYOUT,
    ORIENTATION,
    SPREAD,
    VIEWPORT(DEPRECATED),
    FLOW;

    private final PropertyStatus status;

    private META_PROPERTIES()
    {
      this(ALLOWED);
    }

    private META_PROPERTIES(PropertyStatus status)
    {
      this.status = Preconditions.checkNotNull(status);
    }

    @Override
    public boolean isAllowed(ValidationContext context)
    {
      return status.isAllowed(context);
    }

    @Override
    public boolean isDeprecated()
    {
      return status.isDeprecated();
    }
  }

  public static final EnumVocab<ITEMREF_PROPERTIES> ITEMREF_VOCAB = new EnumVocab<ITEMREF_PROPERTIES>(
      ITEMREF_PROPERTIES.class, URI);

  public enum ITEMREF_PROPERTIES implements PropertyStatus
  {
    LAYOUT_PRE_PAGINATED,
    LAYOUT_REFLOWABLE,
    ORIENTATION_AUTO,
    ORIENTATION_LANDSCAPE,
    ORIENTATION_PORTRAIT,
    SPREAD_AUTO,
    SPREAD_BOTH,
    SPREAD_LANDSCAPE,
    SPREAD_NONE,
    SPREAD_PORTRAIT(DEPRECATED),
    PAGE_SPREAD_CENTER,
    PAGE_SPREAD_LEFT,
    PAGE_SPREAD_RIGHT,
    FLOW_PAGINATED,
    FLOW_SCROLLED_CONTINUOUS,
    FLOW_SCROLLED_DOC,
    FLOW_AUTO,
    ALIGN_X_CENTER;

    private final PropertyStatus status;

    private ITEMREF_PROPERTIES()
    {
      this(ALLOWED);
    }

    private ITEMREF_PROPERTIES(PropertyStatus status)
    {
      this.status = Preconditions.checkNotNull(status);
    }

    @Override
    public boolean isAllowed(ValidationContext context)
    {
      return status.isAllowed(context);
    }

    @Override
    public boolean isDeprecated()
    {
      return status.isDeprecated();
    }
  }

  private RenditionVocabs()
  {
  }
}
