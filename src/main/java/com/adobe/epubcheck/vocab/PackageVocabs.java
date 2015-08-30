package com.adobe.epubcheck.vocab;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class PackageVocabs
{

  public static final String PACKAGE_VOCAB_URI = "http://idpf.org/epub/vocab/package/#";

  public static EnumVocab<META_PROPERTIES> META_VOCAB = new EnumVocab<META_PROPERTIES>(
      META_PROPERTIES.class, PACKAGE_VOCAB_URI);

  public static enum META_PROPERTIES
  {
    ALTERNATE_SCRIPT,
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
    TITLE_TYPE
  }

  public static EnumVocab<ITEM_PROPERTIES> ITEM_VOCAB = new EnumVocab<ITEM_PROPERTIES>(
      ITEM_PROPERTIES.class, PACKAGE_VOCAB_URI);

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
      ITEMREF_PROPERTIES.class, PACKAGE_VOCAB_URI);

  public static enum ITEMREF_PROPERTIES
  {
    PAGE_SPREAD_RIGHT,
    PAGE_SPREAD_LEFT
  }

  public static final String LINKREL_VOCAB_URI = "http://idpf.org/epub/vocab/package/link/#";

  public static EnumVocab<LINKREL_PROPERTIES> LINKREL_VOCAB = new EnumVocab<LINKREL_PROPERTIES>(
      LINKREL_PROPERTIES.class, LINKREL_VOCAB_URI);

  public static enum LINKREL_PROPERTIES
  {
    ACQUIRE,
    MARC21XML_RECORD,
    MODS_RECORD,
    ONIX_RECORD,
    RECORD,
    XML_SIGNATURE,
    XMP_RECORD
  }

  private PackageVocabs()
  {
  }
}