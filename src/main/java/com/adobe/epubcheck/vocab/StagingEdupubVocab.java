package com.adobe.epubcheck.vocab;

public final class StagingEdupubVocab
{
  public static final String URI = "http://www.idpf.org/epub/vocab/structure/#";
  public static final EnumVocab<EPUB_TYPES> VOCAB = new EnumVocab<EPUB_TYPES>(EPUB_TYPES.class, URI);

  public static enum EPUB_TYPES
  {
    ABSTRACT,
    ANSWER,
    ANSWERS,
    ASSESSMENTS,
    BIBLIOREF,
    CASE_STUDY,
    CREDIT,
    CREDITS,
    FEEDBACK,
    FILL_IN_THE_BLANK_PROBLEM,
    GENERAL_PROBLEM,
    GLOSSREF,
    KEYWORD,
    KEYWORDS,
    LABEL,
    LEARNING_OBJECTIVES,
    LEARNING_OUTCOME,
    LEARNING_OUTCOMES,
    LEARNING_RESOURCES,
    LEARNING_STANDARD,
    LEARNING_STANDARDS,
    MATCH_PROBLEM,
    MULTIPLE_CHOICE_PROBLEM,
    ORDINAL,
    PRACTICE,
    PRACTICES,
    PULLQUOTE,
    QUESTION,
    REFERRER,
    SERIESPAGE,
    TOC_BRIEF,
    TRUE_FALSE_PROBLEM;
  }
  
  private StagingEdupubVocab() {}
}
