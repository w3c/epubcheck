package com.thaiopensource.datatype.xsd.regex;

/**
 * A regular expression that can be matched against a string.
 * @see RegexEngine
 */
public interface Regex {
  /**
   * Tests whether this regular expression matches a string.
   *
   * @param str the String to be tested
   * @return <code>true</code> if <code>str</code> matches this regular expression,
   * <code>false</code> otherwise
   */
  boolean matches(String str);
}
