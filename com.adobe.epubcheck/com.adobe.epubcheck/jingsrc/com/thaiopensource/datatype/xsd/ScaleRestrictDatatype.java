package com.thaiopensource.datatype.xsd;

import java.math.BigDecimal;

class ScaleRestrictDatatype extends ValueRestrictDatatype {
  private final int scale;

  ScaleRestrictDatatype(DatatypeBase base, int scale) {
    super(base);
    this.scale = scale;
  }

  boolean satisfiesRestriction(Object obj) {
    return ((BigDecimal)obj).scale() <= scale;
  }
}
