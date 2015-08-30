/*
 * Copyright (c) 2011 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.util;

public enum FeatureEnum
{
  AUDIO("audio element"),
  CHARS_COUNT("characters count"),
  COMPRESSED_SIZE("compressed size"),
  COMPRESSION_METHOD("compression method"),
  CREATION_DATE("creation date"),
  DC_CONTRIBUTOR("contributor"),
  DC_CREATOR("creator"),
  DC_DATE("date"),
  DC_DESCRIPTION("description"),
  DC_LANGUAGE("language"),
  DC_PUBLISHER("publisher"),
  DC_RIGHTS("rights"),
  DC_SUBJECT("subject"),
  DC_TITLE("title"),
  DECLARED_MIMETYPE("declared mimetype"),
  DICTIONARY("dictiobary"),
  EPUB_RENDITIONS_COUNT("EPUB renditions count"),
  EXEC_MODE("execution mode"),
  FIGURE("figure element"),
  FONT_EMBEDDED("font embedded"),
  FONT_REFERENCE("font reference"),
  FORMAT_NAME("format name"),
  FORMAT_VERSION("format version"),
  HAS_ENCRYPTION("hasEncryption"),
  HAS_FIXED_LAYOUT("hasFixedLayout"),
  HAS_HTML4("html 4"),
  HAS_HTML5("html 5"),
  HAS_MICRODATA("microdata"),
  HAS_NCX("Has ncx file"),
  HAS_RDFA("RDFa"),
  HAS_SCRIPTS("hasScripts"),
  HAS_SIGNATURES("hasSignatures"),
  INDEX("index"),
  IS_LINEAR("linear"),
  IS_SPINEITEM("is spine item"),
  ITEMS_COUNT("items count"),
  LOA("list of audios"),
  LOI("list of illustrations"),
  LOT("list of tables"),
  LOV("list of videos"),
  MODIFIED_DATE("modification date"),
  NAVIGATION_ORDER("navigation order"),
  PAGE_BREAK("epub:page-break"),
  PAGE_LIST("epub:page-list"),
  PAGES_COUNT("pages count"),
  REFERENCE("reference"),
  RENDITION_LAYOUT("rendition:layout"),
  RENDITION_ORIENTATION("rendition:orientation"),
  RENDITION_SPREAD("rendition:spread"),
  RESOURCE("resource"),
  SCRIPT("script"),
  SECTIONS("sections"),
  SHA_256("SHA-256"),
  SIZE("size"),
  SPINE_INDEX("spine index"),
  TABLE("table element"),
  TOC_LINKS("ToC links"),
  TOOL_DATE("tool date"),
  TOOL_NAME("tool name"),
  TOOL_VERSION("tool version"),
  UNIQUE_IDENT("unique identifier"),
  VIDEO("video element");

  private final String feature;

  FeatureEnum(String feature)
  {
    this.feature = feature;
  }

  public String toString()
  {
    return feature;
  }
}
