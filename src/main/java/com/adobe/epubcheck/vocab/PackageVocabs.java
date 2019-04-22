package com.adobe.epubcheck.vocab;

import java.util.Set;

import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public final class PackageVocabs
{

  public static final String ITEM_VOCAB_URI = "http://idpf.org/epub/vocab/package/item/#";
  public static final String ITEMREF_VOCAB_URI = "http://idpf.org/epub/vocab/package/itemref/#";
  public static final String LINK_VOCAB_URI = "http://idpf.org/epub/vocab/package/link/#";
  public static final String META_VOCAB_URI = "http://idpf.org/epub/vocab/package/meta/#";

  public static EnumVocab<META_PROPERTIES> META_VOCAB = new EnumVocab<META_PROPERTIES>(
      META_PROPERTIES.class, META_VOCAB_URI);

  public static enum META_PROPERTIES
  {
    ALTERNATE_SCRIPT,
    AUTHORITY,
    BELONGS_TO_COLLECTION,
    COLLECTION_TYPE,
    DISPLAY_SEQ,
    DICTIONARY_TYPE, // DICT
    FILE_AS,
    GROUP_POSITION,
    IDENTIFIER_TYPE,
    META_AUTH,
    ROLE,
    SOURCE_LANGUAGE, // DICT
    SOURCE_OF,
    TARGET_LANGUAGE, // DICT
    TERM,
    TITLE_TYPE
  }

  public static EnumVocab<ITEM_PROPERTIES> ITEM_VOCAB = new EnumVocab<ITEM_PROPERTIES>(
      ITEM_PROPERTIES.class, ITEM_VOCAB_URI);

  public static enum ITEM_PROPERTIES
  {
    COVER_IMAGE("image/gif", "image/jpeg", "image/png", "image/svg+xml"),
    DATA_NAV("application/xhtml+xml"),
    DICTIONARY("application/vnd.epub.search-key-map+xml"),
    GLOSSARY("application/vnd.epub.search-key-map+xml", "application/xhtml+xml"),
    INDEX("application/xhtml+xml"),
    MATHML("application/xhtml+xml", "image/svg+xml"),
    NAV("application/xhtml+xml"),
    REMOTE_RESOURCES("application/xhtml+xml", "application/smil+xml", "image/svg+xml", "text/css"),
    SCRIPTED("application/xhtml+xml", "image/svg+xml"),
    SEARCH_KEY_MAP("application/vnd.epub.search-key-map+xml"),
    SVG("application/xhtml+xml"),
    SWITCH("application/xhtml+xml", "image/svg+xml");

    private final Set<String> types;

    private ITEM_PROPERTIES(String... types)
    {
      this.types = new ImmutableSet.Builder<String>().add(types).build();
    }

    public Set<String> allowedOnTypes()
    {
      return types;
    }
  }

  public static EnumVocab<ITEMREF_PROPERTIES> ITEMREF_VOCAB = new EnumVocab<ITEMREF_PROPERTIES>(
      ITEMREF_PROPERTIES.class, ITEMREF_VOCAB_URI);

  public static enum ITEMREF_PROPERTIES
  {
    PAGE_SPREAD_RIGHT,
    PAGE_SPREAD_LEFT
  }

  public static EnumVocab<LINKREL_PROPERTIES> LINKREL_VOCAB = new EnumVocab<LINKREL_PROPERTIES>(
      LINKREL_PROPERTIES.class, LINK_VOCAB_URI);

  public static enum LINKREL_PROPERTIES implements PropertyStatus
  {
    ACQUIRE,
    ALTERNATE,
    MARC21XML_RECORD(DEPRECATED),
    MODS_RECORD(DEPRECATED),
    ONIX_RECORD(DEPRECATED),
    RECORD,
    VOICING,
    XML_SIGNATURE(DEPRECATED),
    XMP_RECORD(DEPRECATED);

    private final PropertyStatus status;

    private LINKREL_PROPERTIES()
    {
      this(ALLOWED);
    }

    private LINKREL_PROPERTIES(PropertyStatus status)
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

  public static EnumVocab<LINK_PROPERTIES> LINK_VOCAB = new EnumVocab<LINK_PROPERTIES>(
      LINK_PROPERTIES.class, LINK_VOCAB_URI);

  public static enum LINK_PROPERTIES
  {
    ONIX,
    XMP;
  }

  private PackageVocabs()
  {
  }
}