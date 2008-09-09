package com.thaiopensource.relaxng.impl;


class AfterPattern extends BinaryPattern {
  AfterPattern(Pattern p1, Pattern p2) {
    super(false,
	  combineHashCode(AFTER_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }

  boolean isNotAllowed() {
    return p1.isNotAllowed();
  }

  Object apply(PatternFunction f) {
    return f.caseAfter(this);
  }
  void accept(PatternVisitor visitor) {
    // XXX visitor.visitAfter(p1, p2);
  }
}
