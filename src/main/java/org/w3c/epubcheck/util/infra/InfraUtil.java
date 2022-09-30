package org.w3c.epubcheck.util.infra;

public final class InfraUtil
{

  private InfraUtil()
  {
    // static utility class
  }

  /**
   * if a character is https://infra.spec.whatwg.org/#ascii-whitespace U+0009
   * TAB, U+000A LF, U+000C FF, U+000D CR, or U+0020 SPACE.
   */
  public static boolean isASCIIWhitespace(char c)
  {
    return c == ' ' || c == '\t' || c == '\f' || c == '\n' || c == '\r';
  }
}
