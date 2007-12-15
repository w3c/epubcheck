package com.thaiopensource.datatype.xsd.regex;

/**
 * A provider of a regular expression matching capability.
 */
public interface RegexEngine {
  /**
   * Compiles a string containing a regular expression into a <code>Regex</code> object.
   * The <code>Regex</code> object can be used to test whether a string matches the regular
   * expression.
   *
   * @param str a String containing a regular expression
   * @return a <code>Regex</code> for <code>str</code>
   * @throws RegexSyntaxException if <code>str</code> is not a valid regular expression
   */
  Regex compile(String str) throws RegexSyntaxException;
}
