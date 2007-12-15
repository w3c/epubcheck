package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

abstract class ValueRestrictDatatype extends RestrictDatatype {
  ValueRestrictDatatype(DatatypeBase base) {
    super(base);
  }

  boolean allowsValue(String str, ValidationContext vc) {
    return getValue(str, vc) != null;
  }

  Object getValue(String str, ValidationContext vc) {
    Object obj = base.getValue(str, vc);
    if (obj == null || !satisfiesRestriction(obj))
      return null;
    return obj;
  }

  abstract boolean satisfiesRestriction(Object value);
}
