package com.thaiopensource.datatype.xsd;

import java.math.BigDecimal;

class PrecisionRestrictDatatype extends ValueRestrictDatatype {
  private final int precision;

  PrecisionRestrictDatatype(DatatypeBase base, int precision) {
    super(base);
    this.precision = precision;
  }

  boolean satisfiesRestriction(Object obj) {
    return getPrecision((BigDecimal)obj) <= precision;
  }

  static int getPrecision(BigDecimal n) {
    return n.movePointRight(n.scale()).abs().toString().length();
  }
}
