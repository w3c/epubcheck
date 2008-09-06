package com.thaiopensource.relaxng.impl;

class EmptyPattern extends Pattern {
  EmptyPattern() {
    super(true, EMPTY_CONTENT_TYPE, EMPTY_HASH_CODE);
  }
  boolean samePattern(Pattern other) {
    return other instanceof EmptyPattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitEmpty();
  }
  Object apply(PatternFunction f) {
    return f.caseEmpty(this);
  }
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_empty");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_empty");
    }
  }
}
