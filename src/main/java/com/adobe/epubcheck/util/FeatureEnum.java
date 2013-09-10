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
  TOOL_NAME("tool name"),
  TOOL_VERSION("tool version"),
    TOOL_DATE("tool date"),
  EXEC_MODE("execution mode"),
  FORMAT_NAME("format name"),
  FORMAT_VERSION("format version"),
  CREATION_DATE("creation date"),
  MODIFIED_DATE("modification date"),
  EPUB_RENDITIONS_COUNT("EPUB renditions count"),
  CHARS_COUNT("characters count"),
  PAGES_COUNT("pages count"),
  SECTIONS_COUNT("sections count"),
  ITEMS_COUNT("items count"),
  DECLARED_MIMETYPE("declared mimetype"),
  FONT_EMBEDDED("font embedded"),
  FONT_REFERENCE("font reference"),
  REFERENCE("reference"),
  DC_LANGUAGE("language"),
  DC_TITLE("title"),
  DC_CREATOR("creator"),
  DC_CONTRIBUTOR("contributor"),
  DC_PUBLISHER("publisher"),
  DC_DATE("date"),
  DC_RIGHTS("rights"),
  DC_SUBJECT("subject"),
  DC_DESCRIPTION("description"),
  UNIQUE_IDENT("unique identifier"),
  HAS_FIXED_LAYOUT("hasFixedLayout"),
  HAS_SCRIPTS("hasScripts"),
  HAS_SIGNATURES("hasSignatures"),
  HAS_ENCRYPTION("hasEncryption"),
  IS_SPINEITEM("is spine item"),
  HAS_NCX("Has ncx file"),
  IS_LINEAR("linear"),
  RESOURCE("resource"),
  SIZE("size"),
  COMPRESSED_SIZE("compressed size"),
  COMPRESSION_METHOD("compression method"),
  HAS_HTML4("html 4"),
  HAS_HTML5("html 5"),
  SCRIPT("script"),
  SHA_256("SHA-256"),
  RENDITION_LAYOUT("rendition:layout"),
  RENDITION_ORIENTATION("rendition:orientation"),
  RENDITION_SPREAD("rendition:spread"),
  NAVIGATION_ORDER("navigation order");


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
