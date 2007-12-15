package com.thaiopensource.relaxng.impl;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class ListPattern extends Pattern {
  private final Pattern p;
  private final Locator locator;

  ListPattern(Pattern p, Locator locator) {
    super(false,
	  DATA_CONTENT_TYPE,
	  combineHashCode(LIST_HASH_CODE, p.hashCode()));
    this.p = p;
    this.locator = locator;
  }

  Pattern expand(SchemaPatternBuilder b) {
    Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeList(ep, locator);
    else
      return this;
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  boolean samePattern(Pattern other) {
    return (other instanceof ListPattern
	    && p == ((ListPattern)other).p);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitList(p);
  }

  Object apply(PatternFunction f) {
    return f.caseList(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_list");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_list");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_list");
    }
    try {
      p.checkRestrictions(LIST_CONTEXT, dad, null);
    }
    catch (RestrictionViolationException e) {
      e.maybeSetLocator(locator);
      throw e;
    }
  }
  
  Pattern getOperand() {
    return p;
  }
}
