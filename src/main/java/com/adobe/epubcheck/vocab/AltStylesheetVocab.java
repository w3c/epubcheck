package com.adobe.epubcheck.vocab;

public final class AltStylesheetVocab
{
  public static final String PREFIX = "";
  public static final String URI = "";
  public static final Vocab VOCAB = new EnumVocab(PROPERTIES.class, URI, PREFIX);

  public static enum PROPERTIES
  {
    VERTICAL, HORIZONTAL, DAY, NIGHT;
  }

  private AltStylesheetVocab()
  {
  }

}
