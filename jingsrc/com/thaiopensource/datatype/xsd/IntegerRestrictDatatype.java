package com.thaiopensource.datatype.xsd;

class IntegerRestrictDatatype extends ScaleRestrictDatatype {
  IntegerRestrictDatatype(DatatypeBase base) {
    super(base, 0);
  }

  boolean lexicallyAllows(String str) {
    return super.lexicallyAllows(str) && str.charAt(str.length() - 1) != '.';
  }
}
