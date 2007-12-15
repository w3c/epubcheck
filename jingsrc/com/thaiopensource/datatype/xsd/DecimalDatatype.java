package com.thaiopensource.datatype.xsd;

import java.math.BigDecimal;

import org.relaxng.datatype.ValidationContext;

class DecimalDatatype extends DatatypeBase implements OrderRelation {

  boolean lexicallyAllows(String str) {
    int len = str.length();
    if (len == 0)
      return false;
    int i = 0;
    switch (str.charAt(i)) {
    case '+':
    case '-':
      if (++i == len)
	return false;
    }
    boolean hadDecimalPoint = false;
    if (str.charAt(i) == '.') {
      hadDecimalPoint = true;
      if (++i == len)
	return false;
    }
    do {
      switch (str.charAt(i)) {
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
	break;
      case '.':
	if (hadDecimalPoint)
	  return false;
	hadDecimalPoint = true;
	break;
      default:
	return false;
      }
    } while (++i < len);
    return true;
  }

  Object getValue(String str, ValidationContext vc) {
    if (str.charAt(0) == '+')
      str = str.substring(1);	// JDK 1.1 doesn't handle leading +
    return new BigDecimal(str);
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  public boolean isLessThan(Object obj1, Object obj2) {
    return ((BigDecimal)obj1).compareTo((BigDecimal)obj2) < 0;
  }

  /**
   * BigDecimal.equals considers objects distinct if they have the
   * different scales but the same mathematical value. Similarly
   * for hashCode.
   */

  public boolean sameValue(Object value1, Object value2) {
    return ((BigDecimal)value1).compareTo((BigDecimal)value2) == 0;
  }

  public int valueHashCode(Object value) {
    return ((BigDecimal)value).toBigInteger().hashCode();
  }

}
