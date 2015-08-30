package com.adobe.epubcheck.vocab;

public class DictVocab
{
  public static final String URI = "http://www.idpf.org/epub/vocab/structure/#";
  public static final EnumVocab<EPUB_TYPES> VOCAB = new EnumVocab<EPUB_TYPES>(EPUB_TYPES.class,
      URI);

  public static enum EPUB_TYPES
  {
    ANTONYM_GROUP,
    CONDENSED_ENTRY,
    DEF,
    DICTENTRY,
    DICTIONARY,
    ETYMOLOGY,
    EXAMPLE,
    GRAM_INFO,
    IDIOM,
    PART_OF_SPEECH,
    PART_OF_SPEECH_GROUP,
    PART_OF_SPEECH_LIST,
    PHONETIC_TRANSCRIPTION,
    PHRASE_GROUP,
    PHRASE_LIST,
    SENSE_GROUP,
    SENSE_LIST,
    SYNONYM_GROUP,
    TRAN,
    TRAN_INFO;
  }
}
