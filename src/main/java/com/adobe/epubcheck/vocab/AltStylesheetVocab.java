package com.adobe.epubcheck.vocab;

public final class AltStylesheetVocab
{
  public static final String PREFIX = "";
  public static final String URI = "";
  public static final EnumVocab<PROPERTIES> VOCAB = new EnumVocab<PROPERTIES>(PROPERTIES.class,
      URI, PREFIX);

  public static enum PROPERTIES
  {
    VERTICAL,
    HORIZONTAL,
    DAY,
    NIGHT;
  }

  private AltStylesheetVocab()
  {
  }

}
