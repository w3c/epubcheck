package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class FloatDatatype extends DoubleDatatype {

  Object getValue(String str, ValidationContext vc) {
    if (str.equals("INF"))
      return new Float(Float.POSITIVE_INFINITY);
    if (str.equals("-INF"))
      return new Float(Float.NEGATIVE_INFINITY);
    if (str.equals("NaN"))
      return new Float(Float.NaN);
    return new Float(str);
  }

  public boolean isLessThan(Object obj1, Object obj2) {
    return ((Float)obj1).floatValue() < ((Float)obj2).floatValue();
  }

  public boolean sameValue(Object value1, Object value2) {
    float f1 = ((Float)value1).floatValue();
    float f2 = ((Float)value2).floatValue();
    // NaN = NaN
    return f1 == f2 || (f1 != f1 && f2 != f2);
  }
}
