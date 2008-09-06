package com.thaiopensource.util;

public abstract class Utf16 {
  // 110110XX XXXXXX 110111XX XXXXXX
  static public boolean isSurrogate(char c) {
    return (c & 0xF800) == 0xD800;
  }
  static public boolean isSurrogate1(char c) {
    return (c & 0xFC00) == 0xD800;
  }
  static public boolean isSurrogate2(char c) {
    return (c & 0xFC00) == 0xDC00;
  }
  static public int scalarValue(char c1, char c2) {
    return (((c1 & 0x3FF) << 10) | (c2 & 0x3FF)) + 0x10000;
  }
  static public char surrogate1(int c) {
    return (char)(((c - 0x10000) >> 10) | 0xD800);
  }
  static public char surrogate2(int c) {
    return (char)(((c - 0x10000) & 0x3FF) | 0xDC00);
  }
}

