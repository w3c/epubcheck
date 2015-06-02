package com.adobe.epubcheck.vocab;

public class DCMESVocab
{
  public static final String URI = "http://purl.org/dc/elements/1.1/";
  public static final EnumVocab<PROPERTIES> VOCAB = new EnumVocab<PROPERTIES>(PROPERTIES.class, URI, "dc");

  public static enum PROPERTIES
  {

    CONTRIBUTOR,
    COVERAGE,
    CREATOR,
    DATE,
    DESCRIPTION,
    FORMAT,
    IDENTIFIER,
    LANGUAGE,
    PUBLISHER,
    RELATION,
    RIGHTS,
    SOURCE,
    SUBJECT,
    TITLE,
    TYPE;
  }
}
