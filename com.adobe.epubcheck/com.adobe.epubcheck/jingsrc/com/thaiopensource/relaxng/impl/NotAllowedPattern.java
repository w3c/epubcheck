package com.thaiopensource.relaxng.impl;

class NotAllowedPattern extends Pattern {
  NotAllowedPattern() {
    super(false, EMPTY_CONTENT_TYPE, NOT_ALLOWED_HASH_CODE);
  }
  boolean isNotAllowed() {
    return true;
  }
  boolean samePattern(Pattern other) {
    // needs to work for UnexpandedNotAllowedPattern
    return other.getClass() == this.getClass();
  }
  void accept(PatternVisitor visitor) {
    visitor.visitNotAllowed();
  }
  Object apply(PatternFunction f) {
    return f.caseNotAllowed(this);
  }
}
