package org.w3c.epubcheck.util.infra;

/**
 * Code point ranges defined in Infra and other standards.
 * 
 * @see https://infra.spec.whatwg.org
 */
public final class CodePoints
{

  private CodePoints()
  {
    // static utility class
  }

  /**
   * Determines if the given code point is an ASCII whitespace.
   * 
   * @see https://infra.spec.whatwg.org/#ascii-whitespace
   */
  public static boolean isASCIIWhitespace(int c)
  {
    return c == 0x0020 // SPACE
        || c == 0x0009 // TAB
        || c == 0x000A // LF
        || c == 0x000C // FF
        || c == 0x000D // CR
    ;
  }

  /**
   * Determines if the given code point is an ASCII digit.
   * 
   * @see https://infra.spec.whatwg.org/#ascii-digit
   */
  public static boolean isASCIIDigit(int c)
  {
    return c >= 0x0030 && c <= 0x0039;
  }

  /**
   * Determines if the given code point is an ASCII alpha.
   * 
   * @see https://infra.spec.whatwg.org/#ascii-alpha
   * 
   */
  public static boolean isASCIIAlpha(int c)
  {
    return c >= 0x0041 && c <= 0x005A // A to Z
        || c >= 0x0061 && c <= 0x007A // a to z
    ;
  }

  /**
   * Determines if the given code point is an ASCII alphanumeric.
   * 
   * @see https://infra.spec.whatwg.org/#ascii-alphanumeric
   * 
   */
  public static boolean isASCIIAlphanum(int c)
  {
    return isASCIIAlpha(c) || isASCIIDigit(c);
  }

  /**
   * Determines if the given code point is an HTTP token code point.
   * 
   * @see https://mimesniff.spec.whatwg.org/#http-token-code-point
   * 
   */
  public static boolean isHTTPTokenCodePoint(int c)
  {
    return isASCIIAlphanum(c)
        || c == 0x0021 // (!)
        || c == 0x0023 // (#)
        || c == 0x0024 // ($)
        || c == 0x0025 // (%)
        || c == 0x0026 // (&)
        || c == 0x0027 // (')
        || c == 0x002A // (*)
        || c == 0x002B // (+)
        || c == 0x002D // (-)
        || c == 0x002E // (.)
        || c == 0x005E // (^)
        || c == 0x005F // (_)
        || c == 0x0060 // (`)
        || c == 0x007C // (|)
        || c == 0x007E // (~)
    ;
  }

  /**
   * Determines if the given code point is an HTTP quoted-string token code
   * point.
   * 
   * @see https://mimesniff.spec.whatwg.org/#http-quoted-string-token-code-point
   * 
   */
  public static boolean isHTTPQuotedStringTokenCodePoint(int c)
  {
    return c == 0x009 // TAB
        || c >= 0x0020 && c <= 0x007E // SPACE to (~)
        || c >= 0x0080 && c <= 0x00FF // U+0080 to (ÿ)
    ;
  }

  /**
   * Determines if the given code point is an HTTP whitespace.
   * 
   * @see https://fetch.spec.whatwg.org/#http-whitespace
   * 
   */
  public static boolean isHTTPWhitespace(int c)
  {
    return c == 0x0020 // SPACE
        || c == 0x0009 // TAB
        || c == 0x000A // LF
        || c == 0x000D // CR
    ;
  }
}
