package com.adobe.epubcheck.vocab;

import com.google.common.base.CaseFormat;

public final class AccessibilityVocab
{
  public static final String PREFIX = "a11y";
  public static final String URI = "http://www.idpf.org/epub/vocab/package/a11y/#";
  public static final EnumVocab<META_PROPERTIES> META_VOCAB = new EnumVocab<META_PROPERTIES>(
      META_PROPERTIES.class, CaseFormat.LOWER_CAMEL, URI, PREFIX);
  public static final EnumVocab<LINKREL_PROPERTIES> LINKREL_VOCAB = new EnumVocab<LINKREL_PROPERTIES>(
      LINKREL_PROPERTIES.class, CaseFormat.LOWER_CAMEL, URI, PREFIX);

  public static enum META_PROPERTIES
  {
    CERTIFIED_BY,
    CERTIFIER_CREDENTIAL,
  }

  public static enum LINKREL_PROPERTIES
  {
    CERTIFIER_CREDENTIAL,
    CERTIFIER_REPORT;
  }

  private AccessibilityVocab()
  {
  }
}
