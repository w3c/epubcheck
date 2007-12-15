package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class RestrictDatatype extends DatatypeBase {
  protected final DatatypeBase base;
  
  RestrictDatatype(DatatypeBase base) {
    this(base, base.getWhiteSpace());
  }

  RestrictDatatype(DatatypeBase base, int whiteSpace) {
    super(whiteSpace);
    this.base = base;
  }

  boolean lexicallyAllows(String str) {
    return base.lexicallyAllows(str);
  }

  boolean allowsValue(String str, ValidationContext vc) {
    return base.allowsValue(str, vc);
  }

  OrderRelation getOrderRelation() {
    return base.getOrderRelation();
  }

  Measure getMeasure() {
    return base.getMeasure();
  }

  Object getValue(String str, ValidationContext vc) {
    return base.getValue(str, vc);
  }

  DatatypeBase getPrimitive() {
    return base.getPrimitive();
  }

  public int getIdType() {
    return base.getIdType();
  }

  public boolean sameValue(Object value1, Object value2) {
    return base.sameValue(value1, value2);
  }

  public int valueHashCode(Object value) {
    return base.valueHashCode(value);
  }
}
