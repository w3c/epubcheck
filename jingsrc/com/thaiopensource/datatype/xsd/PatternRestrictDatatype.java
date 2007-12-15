package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.xsd.regex.Regex;

class PatternRestrictDatatype extends RestrictDatatype {
  private final Regex pattern;

  PatternRestrictDatatype(DatatypeBase base, Regex pattern) {
    super(base);
    this.pattern = pattern;
  }

  boolean lexicallyAllows(String str) {
    return pattern.matches(str) && super.lexicallyAllows(str);
  }
}
