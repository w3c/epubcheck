package com.adobe.epubcheck.util;

import java.util.HashSet;

public class EpubTypeAttributes {

	public static HashSet<String> EpubTypeSet;

	static {
		HashSet<String> set = new HashSet<String>();
		set.add("cover");
		set.add("frontmatter");
		set.add("bodymatter");
		set.add("backmatter");
		set.add("volume");
		set.add("part");
		set.add("chapter");
		set.add("subchapter");
		set.add("division");
		set.add("epigraph");
		set.add("conclusion");
		set.add("afterword");
		set.add("warning");
		set.add("epilogue");
		set.add("foreword");
		set.add("introduction");
		set.add("prologue");
		set.add("preface");
		set.add("preamble");
		set.add("notice");
		set.add("landmarks");
		set.add("lot");
		set.add("index");
		set.add("colophon");
		set.add("appendix");
		set.add("loi");
		set.add("toc");
		set.add("glossary");
		set.add("glossterm");
		set.add("glossdef");
		set.add("biblioentry");
		set.add("bibliography");
		set.add("imprint");
		set.add("errata");
		set.add("copyright-page");
		set.add("acknowledgments");
		set.add("other-credits");
		set.add("titlepage");
		set.add("imprimatur");
		set.add("contributors");
		set.add("halftitlepage");
		set.add("dedication");
		set.add("help");
		set.add("sidebar");
		set.add("annotation");
		set.add("marginalia");
		set.add("practice");
		set.add("note");
		set.add("footnote");
		set.add("rearnote");
		set.add("footnotes");
		set.add("rearnotes");
		set.add("bridgehead");
		set.add("title");
		set.add("halftitle");
		set.add("fulltitle");
		set.add("subtitle");
		set.add("covertitle");
		set.add("concluding-sentence");
		set.add("keyword");
		set.add("topic-sentence");
		set.add("annoref");
		set.add("noteref");
		set.add("page-list");
		set.add("pagebreak");
		set.add("table");
		set.add("table-row");
		set.add("table-cell");
		set.add("list");
		set.add("list-item");
		EpubTypeSet = set;
	}

}
