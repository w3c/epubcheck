package com.adobe.epubcheck.util;

import java.util.HashSet;

public class EpubTypeAttributes
{
  public static final HashSet<String> EpubTypeSet;
  static
  {
    HashSet<String> set = new HashSet<String>();
    set.add("acknowledgments");
    set.add("afterword");
    set.add("annoref");
    set.add("annotation");
    set.add("appendix");
    set.add("assessment");
    set.add("backmatter");
    set.add("biblioentry");
    set.add("bibliography");
    set.add("bodymatter");
    set.add("bridgehead");
    set.add("chapter");
    set.add("colophon");
    set.add("concluding-sentence");
    set.add("conclusion");
    set.add("contributors");
    set.add("copyright-page");
    set.add("cover");
    set.add("covertitle");
    set.add("dedication");
    set.add("division");
    set.add("epigraph");
    set.add("epilogue");
    set.add("errata");
    set.add("figure");
    set.add("footnote");
    set.add("footnotes");
    set.add("foreword");
    set.add("frontmatter");
    set.add("fulltitle");
    set.add("glossary");
    set.add("glossdef");
    set.add("glossterm");
    set.add("halftitle");
    set.add("halftitlepage");
    set.add("help");
    set.add("imprimatur");
    set.add("imprint");
    set.add("index");
    set.add("introduction");
    set.add("keyword");
    set.add("landmarks");
    set.add("learning-objective");
    set.add("learning-resource");
    set.add("list");
    set.add("list-item");
    set.add("loa");
    set.add("loi");
    set.add("lot");
    set.add("lov");
    set.add("marginalia");
    set.add("note");
    set.add("noteref");
    set.add("notice");
    set.add("other-credits");
    set.add("pagebreak");
    set.add("page-list");
    set.add("part");
    set.add("practice");
    set.add("preamble");
    set.add("preface");
    set.add("prologue");
    set.add("rearnote");
    set.add("rearnotes");
    set.add("revision-history");
    set.add("sidebar");
    set.add("subchapter");
    set.add("subtitle");
    set.add("table");
    set.add("table-cell");
    set.add("table-row");
    set.add("title");
    set.add("titlepage");
    set.add("toc");
    set.add("topic-sentence");
    set.add("volume");
    set.add("warning");
    set.add("qna");
        
    EpubTypeSet = set;
  }
}
