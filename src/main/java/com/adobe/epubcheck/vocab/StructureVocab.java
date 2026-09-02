package com.adobe.epubcheck.vocab;

import static com.adobe.epubcheck.vocab.PropertyStatus.ALLOWED;
import static com.adobe.epubcheck.vocab.PropertyStatus.DEPRECATED;
import static com.adobe.epubcheck.vocab.PropertyStatus.DISALLOWED_IN_XHTML;

import com.google.common.base.Preconditions;;

public final class StructureVocab
{

  public static final String URI = "http://www.idpf.org/epub/vocab/structure/#";
  public static final EnumVocab<EPUB_TYPES> VOCAB = new EnumVocab<EPUB_TYPES>(EPUB_TYPES.class,
      URI);
  public static final Vocab UNCHECKED_VOCAB = new UncheckedVocab(URI, "");

  public static enum EPUB_TYPES implements PropertyStatus.Holder
  {
    ABSTRACT,
    ACKNOWLEDGMENTS,
    AFTERWORD,
    ANNOREF(DEPRECATED),
    ANNOTATION(DEPRECATED),
    APPENDIX,
    ASIDE(DISALLOWED_IN_XHTML),
    ASSESSMENT,
    BACKLINK,
    BACKMATTER,
    BIBLIOENTRY(DEPRECATED),
    BIBLIOGRAPHY,
    BIBLIOREF,
    BODYMATTER,
    BRIDGEHEAD(DEPRECATED),
    CHAPTER,
    COLOPHON,
    CONCLUDING_SENTENCE,
    CONCLUSION,
    CONTRIBUTORS,
    COPYRIGHT_PAGE,
    COVER,
    COVERTITLE,
    CREDIT,
    CREDITS,
    DEDICATION,
    DIVISION,
    ENDNOTE(DEPRECATED),
    ENDNOTES,
    EPIGRAPH,
    EPILOGUE,
    ERRATA,
    FIGURE(DISALLOWED_IN_XHTML),
    FOOTNOTE,
    FOOTNOTES,
    FOREWORD,
    FRONTMATTER,
    FULLTITLE,
    GLOSSARY,
    GLOSSDEF,
    GLOSSREF,
    GLOSSTERM,
    HALFTITLE,
    HALFTITLEPAGE,
    HELP(DEPRECATED),
    IMPRIMATUR,
    IMPRINT,
    INDEX,
    INTRODUCTION,
    KEYWORD,
    LANDMARKS,
    LEARNING_OBJECTIVE,
    LEARNING_RESOURCE,
    LIST(DISALLOWED_IN_XHTML),
    LIST_ITEM(DISALLOWED_IN_XHTML),
    LOA,
    LOI,
    LOT,
    LOV,
    MARGINALIA(DEPRECATED),
    NOTE(DEPRECATED),
    NOTEREF,
    NOTICE,
    OTHER_CREDITS,
    PAGEBREAK,
    PAGE_LIST,
    PART,
    PREAMBLE,
    PREFACE,
    PROLOGUE,
    PULLQUOTE,
    QNA,
    REARNOTE(DEPRECATED),
    REARNOTES(DEPRECATED),
    REVISION_HISTORY,
    SIDEBAR(DEPRECATED),
    SUBCHAPTER(DEPRECATED),
    SUBTITLE,
    TABLE(DISALLOWED_IN_XHTML),
    TABLE_CELL(DISALLOWED_IN_XHTML),
    TABLE_ROW(DISALLOWED_IN_XHTML),
    TIP,
    TITLE,
    TITLEPAGE,
    TOC,
    TOPIC_SENTENCE,
    VOLUME,
    WARNING(DEPRECATED);

    private final PropertyStatus status;

    private EPUB_TYPES()
    {
      this(ALLOWED);
    }

    private EPUB_TYPES(PropertyStatus status)
    {
      this.status = Preconditions.checkNotNull(status);
    }

    @Override
    public PropertyStatus getStatus()
    {
      return status;
    }
  }

  private StructureVocab()
  {
  }
}
