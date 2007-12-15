package com.thaiopensource.relaxng.impl;

import org.xml.sax.SAXException;

class OneOrMorePattern extends Pattern {
  private final Pattern p;

  OneOrMorePattern(Pattern p) {
    super(p.isNullable(),
	  p.getContentType(),
	  combineHashCode(ONE_OR_MORE_HASH_CODE, p.hashCode()));
    this.p = p;
  }

  Pattern expand(SchemaPatternBuilder b) {
    Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeOneOrMore(ep);
    else
      return this;
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_one_or_more");
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_one_or_more");
    }
    
    p.checkRestrictions(context == ELEMENT_CONTEXT
			? ELEMENT_REPEAT_CONTEXT
			: context,
			dad,
			alpha);
    if (context != LIST_CONTEXT
	&& !contentTypeGroupable(p.getContentType(), p.getContentType()))
      throw new RestrictionViolationException("one_or_more_string");
  }

  boolean samePattern(Pattern other) {
    return (other instanceof OneOrMorePattern
	    && p == ((OneOrMorePattern)other).p);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitOneOrMore(p);
  }

  Object apply(PatternFunction f) {
    return f.caseOneOrMore(this);
  }

  Pattern getOperand() {
    return p;
  }
}
