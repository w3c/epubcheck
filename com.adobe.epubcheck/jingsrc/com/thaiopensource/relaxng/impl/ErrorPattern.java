package com.thaiopensource.relaxng.impl;

class ErrorPattern extends Pattern {
  ErrorPattern() {
    super(false, EMPTY_CONTENT_TYPE, ERROR_HASH_CODE);
  }
  boolean samePattern(Pattern other) {
    return other instanceof ErrorPattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitError();
  }
  Object apply(PatternFunction f) {
    return f.caseError(this);
  }
}
