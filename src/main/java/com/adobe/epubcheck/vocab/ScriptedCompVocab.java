package com.adobe.epubcheck.vocab;

public final class ScriptedCompVocab
{
  public static final String PREFIX = "epubsc";
  public static final String URI = "http://idpf.org/epub/vocab/sc/#";
  public static final EnumVocab<PROPERTIES> VOCAB = new EnumVocab<PROPERTIES>(PROPERTIES.class,
      URI, PREFIX);

  public static enum PROPERTIES
  {
    NETWORK_ACCESS_REQUIRED,
    REQUIRED_PARAMS,
    STORAGE_REQUIRED,
    VERSION;
  }

  private ScriptedCompVocab()
  {
  }

}
