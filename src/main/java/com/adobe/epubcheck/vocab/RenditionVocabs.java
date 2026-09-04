package com.adobe.epubcheck.vocab;

import static com.adobe.epubcheck.vocab.PropertyStatus.ALLOWED;
import static com.adobe.epubcheck.vocab.PropertyStatus.DEPRECATED;
import static com.adobe.epubcheck.vocab.PropertyStatus.OUTDATED;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public final class RenditionVocabs
{
  public static final String PREFIX = "rendition";
  public static final String URI = "http://www.idpf.org/vocab/rendition/#";

  public static final EnumVocab<META_PROPERTIES> META_VOCAB = new EnumVocab<META_PROPERTIES>(
      META_PROPERTIES.class, URI, PREFIX);

  public enum META_PROPERTIES implements PropertyStatus.Holder
  {
    LAYOUT,
    ORIENTATION(OUTDATED),
    SPREAD(OUTDATED),
    VIEWPORT(DEPRECATED),
    FLOW(OUTDATED);

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
    public PropertyStatus getStatus()
    {
      return status;
    }
  }

  public static final EnumVocab<ITEMREF_PROPERTIES> ITEMREF_VOCAB = new EnumVocab<ITEMREF_PROPERTIES>(
      ITEMREF_PROPERTIES.class, URI, PREFIX);

  public static Set<Property> SPREAD_PROPERTIES = ImmutableSet.of(
      ITEMREF_VOCAB.get(ITEMREF_PROPERTIES.PAGE_SPREAD_CENTER),
      ITEMREF_VOCAB.get(ITEMREF_PROPERTIES.PAGE_SPREAD_LEFT),
      ITEMREF_VOCAB.get(ITEMREF_PROPERTIES.PAGE_SPREAD_RIGHT),
      PackageVocabs.ITEMREF_VOCAB.get(PackageVocabs.ITEMREF_PROPERTIES.PAGE_SPREAD_LEFT),
      PackageVocabs.ITEMREF_VOCAB.get(PackageVocabs.ITEMREF_PROPERTIES.PAGE_SPREAD_RIGHT));

  public enum ITEMREF_PROPERTIES implements PropertyStatus.Holder
  {
    LAYOUT_PRE_PAGINATED,
    LAYOUT_REFLOWABLE,
    ORIENTATION_AUTO(OUTDATED),
    ORIENTATION_LANDSCAPE(OUTDATED),
    ORIENTATION_PORTRAIT(OUTDATED),
    SPREAD_AUTO(OUTDATED),
    SPREAD_BOTH(OUTDATED),
    SPREAD_LANDSCAPE(OUTDATED),
    SPREAD_NONE(OUTDATED),
    SPREAD_PORTRAIT(DEPRECATED),
    PAGE_SPREAD_CENTER,
    PAGE_SPREAD_LEFT,
    PAGE_SPREAD_RIGHT,
    FLOW_PAGINATED(OUTDATED),
    FLOW_SCROLLED_CONTINUOUS(OUTDATED),
    FLOW_SCROLLED_DOC(OUTDATED),
    FLOW_AUTO(OUTDATED),
    ALIGN_X_CENTER(DEPRECATED);

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
    public PropertyStatus getStatus()
    {
      return status;
    }
  }

  private RenditionVocabs()
  {
  }
}
