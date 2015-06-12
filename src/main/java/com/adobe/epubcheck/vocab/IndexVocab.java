package com.adobe.epubcheck.vocab;

public class IndexVocab
{
  public static final String URI = "http://www.idpf.org/epub/vocab/structure/#";
  public static final EnumVocab<EPUB_TYPES> VOCAB = new EnumVocab<EPUB_TYPES>(EPUB_TYPES.class, URI);

  public static enum EPUB_TYPES
  {
    INDEX,
    INDEX_EDITOR_NOTE,
    INDEX_ENTRY,
    INDEX_ENTRY_LIST,
    INDEX_GROUP,
    INDEX_HEADNOTES,
    INDEX_LEGEND,
    INDEX_LOCATOR,
    INDEX_LOCATOR_LIST,
    INDEX_LOCATOR_RANGE,
    INDEX_TERM,
    INDEX_TERM_CATEGORIES,
    INDEX_TERM_CATEGORY,
    INDEX_XREF_PREFERRED,
    INDEX_XREF_RELATED;
  }
}
