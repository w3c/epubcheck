package com.adobe.epubcheck.vocab;

/**
 * Vocabularies which are known but not validated in EpubCheck.
 */
public final class ForeignVocabs
{

  public static final String DCTERMS_PREFIX = "dcterms";
  public static final String DCTERMS_URI = "http://purl.org/dc/terms/";
  public static final Vocab DCTERMS_VOCAB = new UncheckedVocab(DCTERMS_URI, DCTERMS_PREFIX);

  public static final String MARC_PREFIX = "marc";
  public static final String MARC_URI = "http://id.loc.gov/vocabulary/";
  public static final Vocab MARC_VOCAB = new UncheckedVocab(MARC_URI, MARC_PREFIX);

  public static final String ONIX_PREFIX = "onix";
  public static final String ONIX_URI = "http://www.editeur.org/ONIX/book/codelists/current.html#";
  public static final Vocab ONIX_VOCAB = new UncheckedVocab(ONIX_URI, ONIX_PREFIX);

  public static final String SCHEMA_PREFIX = "schema";
  public static final String SCHEMA_URI = "http://schema.org/";
  public static final Vocab SCHEMA_VOCAB = new UncheckedVocab(SCHEMA_URI, SCHEMA_PREFIX);

  public static final String XSD_PREFIX = "xsd";
  public static final String XSD_URI = "http://www.w3.org/2001/XMLSchema#";
  public static final Vocab XSD_VOCAB = new UncheckedVocab(XSD_URI, XSD_PREFIX);

  private ForeignVocabs()
  {
  }

}
