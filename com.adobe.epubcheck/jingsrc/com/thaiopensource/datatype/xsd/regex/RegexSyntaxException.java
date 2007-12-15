package com.thaiopensource.datatype.xsd.regex;

/**
 * Thrown when an syntactically incorrect regular expression is detected.
 */
public class RegexSyntaxException extends Exception {
  private final int position;

  /**
   * Represents an unknown position within a string containing a regular expression.
   */
  static public final int UNKNOWN_POSITION = -1;

  public RegexSyntaxException(String detail) {
    this(detail, UNKNOWN_POSITION);
  }

  public RegexSyntaxException(String detail, int position) {
    super(detail);
    this.position = position;
  }

  /**
   * Returns the index into the regular expression where the error was detected
   * or <code>UNKNOWN_POSITION</code> if this is unknown.
   *
   * @return the index into the regular expression where the error was detected,
   * or <code>UNKNOWNN_POSITION</code> if this is unknown
   */
  public int getPosition() {
    return position;
  }
}
