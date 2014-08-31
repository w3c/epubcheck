package com.adobe.epubcheck.vocab;

public final class MediaOverlaysVocab
{
  public static final String PREFIX = "media";
  public static final String URI = "http://www.idpf.org/epub/vocab/overlays/#";
  public static final Vocab VOCAB = new EnumVocab(PROPERTIES.class, URI, PREFIX);

  public static enum PROPERTIES
  {
    ACTIVE_CLASS,
    DURATION,
    NARRATOR,
    PLAYBACK_ACTIVE_CLASS;
  }

  private MediaOverlaysVocab()
  {
  }

}
