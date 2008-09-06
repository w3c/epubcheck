package com.thaiopensource.datatype.xsd;

abstract class BinaryDatatype extends DatatypeBase implements Measure {
  BinaryDatatype() {
    // whiteSpace is actually collapse, but we handle it ourselves for efficiency
    super(WHITE_SPACE_PRESERVE);
  }

  public int valueHashCode(Object value) {
    byte[] v = (byte[])value;
    int hc = 0;
    for (int i = 0, len = v.length; i < len; i++)
      hc = (hc * 33) ^ (v[i] & 0xFF);
    return hc;
  }

  public boolean sameValue(Object value1, Object value2) {
    byte[] v1 = (byte[])value1;
    byte[] v2 = (byte[])value2;
    if (v1.length != v2.length)
      return false;
    for (int i = 0, len = v1.length; i < len; i++)
      if (v1[i] != v2[i])
        return false;
    return true;
  }

  public int getLength(Object obj) {
    return ((byte[])obj).length;
  }

  Measure getMeasure() {
    return this;
  }
}
