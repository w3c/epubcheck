package com.adobe.epubcheck.vocab;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * A vocabulary used for storing (temporary) info into properties for
 * EpubCheck's internal use.
 *
 */
public final class EpubCheckVocab
{
  public static final String PREFIX = "epubcheck";
  public static final String URI = "http://www.idpf.org/epubcheck/#";
  public static final EnumVocab<PROPERTIES> VOCAB = new EnumVocab<PROPERTIES>(PROPERTIES.class, URI,
      PREFIX);
  public static final Map<String, Vocab> VOCAB_MAP = ImmutableMap
      .<String, Vocab> of(EpubCheckVocab.PREFIX, EpubCheckVocab.VOCAB);

  public static enum PROPERTIES
  {
    /**
     * Property of OPF items representing Fixed Layout Content Documents
     */
    FIXED_LAYOUT,
    /**
     * Property of OPF items referenced in 'index' collections
     */
    IN_INDEX_COLLECTION,
    /**
     * Property of OCF entries in Multiple Renditions
     */
    MULTIPLE_RENDITION,
    /**
     * Property of non-linear OPF items
     */
    NON_LINEAR,
    /**
     * Property used to identify the Rendition Mapping Document in the OCF
     * checker
     */
    RENDITION_MAPPING;

  }

  private EpubCheckVocab()
  {
  }

}
