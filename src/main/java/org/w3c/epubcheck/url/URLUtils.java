package org.w3c.epubcheck.url;

import static io.mola.galimatias.URLUtils.UTF_8;
import static io.mola.galimatias.URLUtils.isURLCodePoint;
import static io.mola.galimatias.URLUtils.percentEncode;
import static io.mola.galimatias.URLUtils.percentDecode;

import java.io.File;
import java.net.URISyntaxException;

import com.google.common.base.Preconditions;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.ParseIssue;
import io.mola.galimatias.URL;

//FIXME 2022 add unit tests
public final class URLUtils
{
  public static URL toURL(File file)
  {
    Preconditions.checkArgument(file != null, "file must not be null");
    return URL.fromJavaURI(file.toURI());
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
      throw new IllegalArgumentException("Could not parse URL", e);
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
}
