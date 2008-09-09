package com.thaiopensource.relaxng.impl;

import com.thaiopensource.datatype.Datatype2;
import org.relaxng.datatype.Datatype;

class DataPattern extends StringPattern {
  private final Datatype dt;

  DataPattern(Datatype dt) {
    super(combineHashCode(DATA_HASH_CODE, dt.hashCode()));
    this.dt = dt;
  }

  boolean samePattern(Pattern other) {
    if (other.getClass() != this.getClass())
      return false;
    return dt.equals(((DataPattern)other).dt);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitData(dt);
  }

  Object apply(PatternFunction f) {
    return f.caseData(this);
  }

  Datatype getDatatype() {
    return dt;
  }

  boolean allowsAnyString() {
    return dt instanceof Datatype2 && ((Datatype2)dt).alwaysValid();
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_data");
    }
  }
}
