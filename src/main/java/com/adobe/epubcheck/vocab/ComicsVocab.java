package com.adobe.epubcheck.vocab;

public class ComicsVocab
{
  public static final String URI = "http://www.idpf.org/epub/vocab/structure/#";
  public static final EnumVocab<EPUB_TYPES> VOCAB = new EnumVocab<EPUB_TYPES>(EPUB_TYPES.class, URI);

  public static enum EPUB_TYPES
  {
      BALLOON,
      PANEL,
      PANEL_GROUP,
      TEXT_AREA,
      SOUND_AREA
  }
}
