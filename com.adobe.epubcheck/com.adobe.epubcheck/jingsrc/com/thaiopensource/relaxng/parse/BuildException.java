package com.thaiopensource.relaxng.parse;

public class BuildException extends RuntimeException {
  private final Throwable cause;
  public BuildException(Throwable cause) {
    if (cause == null)
      throw new NullPointerException("null cause");
    this.cause = cause;
  }

  public Throwable getCause() {
    return cause;
  }
}
