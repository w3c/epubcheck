package org.w3c.epubcheck.util.url;

import static io.mola.galimatias.URLUtils.UTF_8;
import static io.mola.galimatias.URLUtils.isURLCodePoint;
import static io.mola.galimatias.URLUtils.percentDecode;
import static io.mola.galimatias.URLUtils.percentEncode;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.google.common.base.Preconditions;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.ParseIssue;
import io.mola.galimatias.URL;
import io.mola.galimatias.canonicalize.DecodeUnreservedCanonicalizer;

//FIXME 2022 add unit tests
public final class URLUtils
{
  public static URL toURL(File file)
  {
    Preconditions.checkArgument(file != null, "file must not be null");
    return URL.fromJavaURI(file.toURI());
  }
  
  public static File toFile(URL url) {
    Preconditions.checkArgument(url != null, "file must not be null");
    Preconditions.checkArgument("file".equals(url.scheme()));
    try {
      return Paths.get(url.toJavaURI()).toFile();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
  
  public static String toFilePath(URL url) {
    Preconditions.checkArgument(url != null, "file must not be null");
    Preconditions.checkArgument("file".equals(url.scheme()));
    try {
      return Paths.get(url.toJavaURI()).toString();
    } catch (Exception e) {
      return decode(url.path());
    }
  }

  public static URL docURL(URL url)
  {
    Preconditions.checkArgument(url != null, "url must not be null");
    try
    {
      return url.withFragment(null);
    } catch (GalimatiasParseException impossible)
    {
      throw new AssertionError(impossible);
    }
  }

  // Note: "file" URL are treated like hierarchical URLs
  // When checking a single file as a file URL, EPUBCheck will then consider
  // relative URLs (resolved to file URLs themselves) as same origin as the
  // context URL.
  // Absolute file URL strings are reported by other checks anyways.
  public static boolean isSameOrigin(URL urlA, URL urlB)
  {
    if (urlA == null || urlB == null)
    {
      return urlA == null && urlB == null;
    }
    else if (urlA.equals(urlB))
    {
      return true;
    }
    else
    {
      switch (urlA.scheme())
      {
      case "ftp":
      case "http":
      case "https":
      case "ws":
      case "wss":
      case "file":
        return urlA.scheme().equals(urlB.scheme())
            && (urlA.host() == urlB.host() || urlA.host().equals(urlB.host()))
            && urlA.port() == urlB.port();
      default:
        return false;
      }
    }
  }

  /**
   * Test if a URL is "remote" compared to a another URL. A URL is considered
   * remote if it is not same origin as the test URL **and** it is not a `data`
   * URL.
   * 
   * Note that this relation is not defined in the URL standard, but is useful
   * in EPUB (to test for remote resources compared to container URLs).
   * 
   * @param test
   *        the URL to test
   * @param local
   *        the URL it is tested against
   * @return `true` if and only if `test` is remote compared to `local`.
   */
  public static boolean isRemote(URL test, URL local)
  {
    return (test == null || !test.scheme().equals("data")) && !isSameOrigin(test, local);
  }

  public static boolean isAbsoluteURLString(String string)
  {
    try
    {
      URL.parse(string);
    } catch (GalimatiasParseException e)
    {
      if (ParseIssue.MISSING_SCHEME.equals(e.getParseIssue()))
      {
        return false;
      }
    }
    return true;
  }

  public static boolean isPathRelativeSchemeLessURLString(String string)
  {
    // FIXME next implement isPathRelativeSchemeLessURLString
    // To check if path-relative-scheme-less-URL string
    // - use URLUtils.isRelative
    // - does not start with '/'
    // - has no fragment
    // - has no URL-query string
    return true;
  }

  // TODO replace this utility when Galimatias implements it
  // See https://github.com/smola/galimatias/issues/31
  public static String encodePath(String string)
  {
    if (string == null || string.isEmpty())
    {
      return string;
    }

    StringBuilder buffer = new StringBuilder(string.length() * 2);
    int index = 0;
    while (index < string.length())
    {
      int codepoint = string.codePointAt(index);
      if (!isURLCodePoint(codepoint) || codepoint == '?')
      {
        final byte[] bytes = new String(Character.toChars(codepoint)).getBytes(UTF_8);
        for (final byte b : bytes)
        {
          percentEncode(b, buffer);
        }
      }
      else
      {
        buffer.appendCodePoint(codepoint);
      }
      index += Character.charCount(codepoint);
    }
    return buffer.toString();
  }

  public static String decode(String string)
  {
    return percentDecode(string);
  }

  public static URL normalize(URL url)
  {
    URL normalized = url;
    if (url != null)
    {
      try
      {
        if (url.isHierarchical() && url.path() != null)
        {
          normalized = url.withPath(URLUtils.encodePath(URLUtils.decode(url.path())));
        }
        normalized = new DecodeUnreservedCanonicalizer().canonicalize(normalized);
      } catch (GalimatiasParseException unexpected)
      {
        throw new AssertionError(unexpected);
      }
    }
    return normalized;
  }

  /**
   * Returns the MIME type of a `data:` URL.
   * 
   * @param url
   *        a URL, can be `null`.
   * @return the MIME type declared in the data URL (can be an empty string), or
   *           `null` if `url` is not a data URL.
   */
  public static String getDataURLType(URL url)
  {
    if (!"data".equals(url.scheme()))
    {
      return null;
    }
    StringBuilder type = new StringBuilder();
    CharacterIterator characters = new StringCharacterIterator(url.schemeData());
    char c = characters.current();
    while (c != CharacterIterator.DONE && c != ',' && c != ';')
    {
      type.append(c);
      c = characters.next();
    }
    return type.toString();
  }
}
