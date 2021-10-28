package org.w3c.epubcheck.constants;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Optional;

//FIXME 2021 Romain - document
public enum MIMEType
{
  CSS("text/css"),
  DTBOOK("application/x-dtbook+xml"),
  EPUB("application/epub+zip"),
  HTML("text/html"),
  IMAGE_JPEG("image/jpeg"),
  IMAGE_GIF("image/gif"),
  IMAGE_PNG("image/png"),
  OEBPS("text/x-oeb1-document"),
  PACKAGE_DOC("application/oebps-package+xml"),
  SEARCH_KEY_MAP("application/vnd.epub.search-key-map+xml"),
  SMIL("application/smil+xml"),
  SVG("image/svg+xml"),
  XHTML("application/xhtml+xml");

  private static final Map<String, MIMEType> ENUM_MAP;

  private final String definition;

  private MIMEType(final String definition)
  {
    this.definition = definition;
  }

  public String toString()
  {
    return definition;
  }

  static
  {
    Map<String, MIMEType> map = new ConcurrentHashMap<String, MIMEType>();
    for (MIMEType value : MIMEType.values())
    {
      map.put(value.toString(), value);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static Optional<MIMEType> get(String name)
  {
    return Optional.fromNullable(ENUM_MAP.get(name.toLowerCase(Locale.ENGLISH)));
  }
}
