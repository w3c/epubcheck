package org.w3c.epubcheck.constants;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
  XHTML("application/xhtml+xml"),
  OTHER("*/*");

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

  public boolean is(String string)
  {
    return string != null && this.toString().equals(string.toLowerCase(Locale.ROOT));
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

  public static MIMEType get(String name)
  {
    return (name != null) ? ENUM_MAP.getOrDefault(name.toLowerCase(Locale.ROOT), OTHER) : OTHER;
  }

  /**
   * Removes the (optional) parameters from a valid MIME type string, as defined
   * in the <a
   * href="https://mimesniff.spec.whatwg.org/#understanding-mime-types">MIME
   * Sniffing standard</a>. In other words, this returns the <b>essence</b>
   * (<code>"<var>type</var>/<var>subtype</var>"</code>) of
   * the MIME type described in the given string.
   * 
   * <p>
   * For instance, calling this method on MIME type string
   * {@code "audio/ogg; codecs=speex"} will return the string
   * {@code "audio/ogg"}.
   * </p>
   * 
   * <p>
   * Note: no validation is performed on the MIME type string itself; it
   * simply returns the substring before the first "<code>;</code>"
   * character.
   * </p>
   * 
   * @param typeString
   *        a MIME type string
   * @return a MIME type string with any parameter removed
   */
  public static String removeParams(String typeString)
  {
    if (typeString == null) return null;
    typeString = typeString.trim();
    int semicolon = typeString.indexOf(';');
    return (semicolon > 0) ? typeString.substring(0, semicolon) : typeString;
  }
}
