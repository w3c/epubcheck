package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class DoubleDatatype extends DatatypeBase implements OrderRelation {

  boolean lexicallyAllows(String str) {
    if (str.equals("INF") || str.equals("-INF") || str.equals("NaN"))
      return true;
    int len = str.length();
    boolean hadSign = false;
    boolean hadDecimalPoint = false;
    boolean hadDigit = false;
    boolean hadE = false;
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '+':
      case '-':
	if (hadDigit || hadDecimalPoint || hadSign)
	  return false;
	hadSign = true;
	break;
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
	hadDigit = true;
	break;
      case 'e':
      case 'E':
	if (hadE || !hadDigit)
	  return false;
	hadDigit = false;
	hadE = true;
	hadSign = false;
	hadDecimalPoint = false;
	break;
      case '.':
	if (hadDecimalPoint || hadE)
	  return false;
	hadDecimalPoint = true;
	break;
      default:
	return false;
      }
    }
    return hadDigit;
  }

  Object getValue(String str, ValidationContext vc) {
    if (str.equals("INF"))
      return new Double(Double.POSITIVE_INFINITY);
    if (str.equals("-INF"))
      return new Double(Double.NEGATIVE_INFINITY);
    if (str.equals("NaN"))
      return new Double(Double.NaN);
    return new Double(str);
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  public boolean isLessThan(Object obj1, Object obj2) {
    return ((Double)obj1).doubleValue() < ((Double)obj2).doubleValue();
  }

  public boolean sameValue(Object value1, Object value2) {
    double d1 = ((Double)value1).doubleValue();
    double d2 = ((Double)value2).doubleValue();
    // NaN = NaN
    return d1 == d2 || (d1 != d1 && d2 != d2);
  }
}
