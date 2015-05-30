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
  public static final Vocab VOCAB = new EnumVocab(PROPERTIES.class, URI, PREFIX);
  public static final Map<String, Vocab> VOCAB_MAP = ImmutableMap.of(EpubCheckVocab.PREFIX,
      EpubCheckVocab.VOCAB);

  public static enum PROPERTIES
  {
    /**
     * Used to tell that a content item is non-linear.
     */
    NON_LINEAR;
  }

  private EpubCheckVocab()
  {
  }

}
