package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class BooleanDatatype extends DatatypeBase {
  boolean lexicallyAllows(String str) {
    return str.equals("true") || str.equals("false") || str.equals("1") || str.equals("0");
  }
  Object getValue(String str, ValidationContext vc) {
    switch (str.charAt(0)) {
    case 't':
    case '1':
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }
}
