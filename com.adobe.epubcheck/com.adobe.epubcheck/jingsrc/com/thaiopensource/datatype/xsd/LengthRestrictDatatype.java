package com.thaiopensource.datatype.xsd;

class LengthRestrictDatatype extends ValueRestrictDatatype {
  private final int length;
  private final Measure measure;

  LengthRestrictDatatype(DatatypeBase base, int length) {
    super(base);
    this.measure = base.getMeasure();
    this.length = length;
  }

  boolean satisfiesRestriction(Object obj) {
    return measure.getLength(obj) == length;
  }
}
