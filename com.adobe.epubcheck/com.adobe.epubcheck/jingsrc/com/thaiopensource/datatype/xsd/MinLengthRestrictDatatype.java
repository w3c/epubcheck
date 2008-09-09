package com.thaiopensource.datatype.xsd;

class MinLengthRestrictDatatype extends ValueRestrictDatatype {
  private final int length;
  private final Measure measure;

  MinLengthRestrictDatatype(DatatypeBase base, int length) {
    super(base);
    this.measure = base.getMeasure();
    this.length = length;
  }

  boolean satisfiesRestriction(Object obj) {
    return measure.getLength(obj) >= length;
  }
}
