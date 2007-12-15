package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class HexBinaryDatatype extends BinaryDatatype {
  static private final int INVALID = -1;
  static private final int WHITESPACE = -2;

  boolean lexicallyAllows(String str) {
    int len = str.length();
    int i = 0;
    while (i < len && weight(str.charAt(i)) == WHITESPACE)
      i++;
    while (i + 1 < len && weight(str.charAt(i)) >= 0 && weight(str.charAt(i + 1)) >= 0)
      i += 2;
    while (i < len && weight(str.charAt(i)) == WHITESPACE)
      i++;
    return i == len;
  }

  Object getValue(String str, ValidationContext vc) {
    int len = str.length();
    int start = 0;
    while (start < len && weight(str.charAt(start)) == WHITESPACE)
      start++;
    int end = len;
    while (end > start && weight(str.charAt(end - 1)) == WHITESPACE)
      end--;
    byte[] value = new byte[(end - start) >> 1];
    int j = 0;
    for (int i = start; i < end; i += 2, j++)
      value[j] = (byte)((weight(str.charAt(i)) << 4) | weight(str.charAt(i + 1)));
    return value;
  }

  static private int weight(char c) {
    switch (c) {
    case '0': case '1': case '2': case '3': case '4':
    case '5': case '6': case '7': case '8': case '9':
      return c - '0';
    case 'A': case 'B': case 'C':
    case 'D': case 'E': case 'F':
      return c + (10 - 'A');
    case 'a': case 'b': case 'c':
    case 'd': case 'e': case 'f':
      return c + (10 - 'a');
    case ' ': case '\n': case '\r': case '\t':
      return WHITESPACE;
    }
    return INVALID;
  }

}
