package com.thaiopensource.util;

public class Equal {
  private Equal() { }

  static public boolean equal(Object obj1, Object obj2) {
    return obj1 == null ? obj2 == null : obj1.equals(obj2);
  }
}
