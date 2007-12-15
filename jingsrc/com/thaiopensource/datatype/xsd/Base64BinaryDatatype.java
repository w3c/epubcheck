package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class Base64BinaryDatatype extends BinaryDatatype {
  static private final byte[] weightTable = makeWeightTable();
  static private final byte INVALID = (byte)-1;
  static private final byte WHITESPACE = (byte)-2;
  static private final byte PADDING = (byte)-3;

  // for efficiency, don't assume whitespace normalized
  boolean lexicallyAllows(String str) {
    return byteCount(str) >= 0;
  }

  private static int byteCount(String str) {
    int nChars = 0;
    int nPadding = 0;
    int lastCharWeight = -1;
    for (int i = 0, len = str.length(); i < len; i++) {
      char c = str.charAt(i);
      if (c >= 128)
        return -1;
      int w = weightTable[c];
      switch (w) {
      case WHITESPACE:
        break;
      case PADDING:
        if (++nPadding > 2)
          return -1;
        break;
      case INVALID:
        return -1;
      default:
        if (nPadding > 0)
          return -1;
        lastCharWeight = w;
        nChars++;
        break;
      }
    }
    if (((nChars + nPadding) & 0x3) != 0)
      return -1;
    switch (nPadding) {
    case 1:
      // 1 padding char; last quartet specifies 2 bytes = 16 bits = 6 + 6 + 4 bits
      // lastChar must have 6 - 4 = 2 unused bits
      if ((lastCharWeight & 0x3) != 0)
        return -1;
      break;
    case 2:
      // 2 padding chars; last quartet specifies 1 byte = 8 bits = 6 + 2 bits
      // lastChar must have 6 - 2 = 4 unused bits
      if ((lastCharWeight & 0xF) != 0)
        return -1;
      break;
    }
    return ((nChars + nPadding) >> 2)*3 - nPadding;
  }

  Object getValue(String str, ValidationContext vc) {
    int nBytes = byteCount(str);
    byte[] value = new byte[nBytes];
    int valueIndex = 0;
    int nBytesAccum = 0;
    int accum = 0;
    for (int i = 0, len = str.length(); i < len; i++) {
      int w = weightTable[str.charAt(i)];
      if (w != WHITESPACE) {
        accum <<= 6;
        if (w != PADDING)
          accum |= w;
        if (++nBytesAccum == 4) {
          for (int shift = 16; shift >= 0; shift -= 8) {
            if (valueIndex < nBytes)
              value[valueIndex++] = (byte)((accum >> shift) & 0xFF);
          }
          nBytesAccum = 0;
          accum = 0;
        }
      }
    }
    return value;
  }

  static private byte[] makeWeightTable() {
    byte[] w = new byte[128];
    byte n = INVALID;
    for (int i = 0; i < 128; i++)
      w[i] = n;
    n = 0;
    for (int i = 'A'; i <= 'Z'; i++, n++)
      w[i] = n;
    for (int i = 'a'; i <= 'z'; i++, n++)
      w[i] = n;
    for (int i = '0'; i <= '9'; i++, n++)
      w[i] = n;
    w['+'] = n++;
    w['/'] = n++;
    w[' '] = w['\t'] = w['\r'] = w['\n'] = WHITESPACE;
    w['='] = PADDING;
    return w;
  }

}
