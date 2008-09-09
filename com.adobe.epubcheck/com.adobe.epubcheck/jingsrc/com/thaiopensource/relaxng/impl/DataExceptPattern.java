package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;
import org.xml.sax.Locator;

class DataExceptPattern extends DataPattern {
  private final Pattern except;
  private final Locator loc;

  DataExceptPattern(Datatype dt, Pattern except, Locator loc) {
    super(dt);
    this.except = except;
    this.loc = loc;
  }

  boolean samePattern(Pattern other) {
    if (!super.samePattern(other))
      return false;
    return except.samePattern(((DataExceptPattern)other).except);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitDataExcept(getDatatype(), except);
  }

  Object apply(PatternFunction f) {
    return f.caseDataExcept(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    super.checkRestrictions(context, dad, alpha);
    try {
      except.checkRestrictions(DATA_EXCEPT_CONTEXT, null, null);
    }
    catch (RestrictionViolationException e) {
      e.maybeSetLocator(loc);
      throw e;
    }
  }

  Pattern getExcept() {
    return except;
  }
}
