package com.adobe.epubcheck.vocab;

import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Preconditions;;

public final class StructureVocab
{

  public static final String URI = "http://www.idpf.org/epub/vocab/structure/#";
  public static final EnumVocab<EPUB_TYPES> VOCAB = new EnumVocab<EPUB_TYPES>(EPUB_TYPES.class,
      URI);
  public static final Vocab UNCHECKED_VOCAB = new UncheckedVocab(URI, "");

  public static enum EPUB_TYPES implements PropertyStatus
  {
    ABSTRACT,
    ACKNOWLEDGMENTS,
    AFTERWORD,
    ANNOREF(DEPRECATED),
    ANNOTATION(DEPRECATED),
    APPENDIX,
    ASIDE(DISALLOWED_ON_CONTENT_DOCS),
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
    FIGURE(DISALLOWED_ON_CONTENT_DOCS),
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
    LIST(DISALLOWED_ON_CONTENT_DOCS),
    LIST_ITEM(DISALLOWED_ON_CONTENT_DOCS),
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
    TABLE(DISALLOWED_ON_CONTENT_DOCS),
    TABLE_CELL(DISALLOWED_ON_CONTENT_DOCS),
    TABLE_ROW(DISALLOWED_ON_CONTENT_DOCS),
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
    public boolean isAllowed(ValidationContext context)
    {
      return status.isAllowed(context);
    }

    @Override
    public boolean isDeprecated()
    {
      return status.isDeprecated();
    }
  }

  private StructureVocab()
  {
  }
}
