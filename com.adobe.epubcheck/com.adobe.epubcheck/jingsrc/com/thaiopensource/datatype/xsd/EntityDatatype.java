package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class EntityDatatype extends NCNameDatatype {
  boolean allowsValue(String str, ValidationContext vc) {
    return vc.isUnparsedEntity(str);
  }

  Object getValue(String str, ValidationContext vc) {
    if (!allowsValue(str, vc))
      return null;
    return super.getValue(str, vc);
  }
}
