package com.thaiopensource.validate.nrl;

import java.util.Hashtable;
import java.util.Enumeration;

class Hashset {
  private final Hashtable table = new Hashtable();

  boolean contains(Object key) {
    return table.get(key) != null;
  }

  void add(Object key) {
    table.put(key, key);
  }

  void addAll(Hashset set) {
    for (Enumeration enumList = set.table.keys(); enumList.hasMoreElements();)
      add(enumList.nextElement());
  }

  void clear() {
    table.clear();
  }

  Enumeration members() {
    return table.keys();
  }
}
