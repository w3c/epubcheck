package com.thaiopensource.validate.mns;

import java.util.Hashtable;

class Hashset {
  private final Hashtable table = new Hashtable();

  boolean contains(Object key) {
    return table.get(key) != null;
  }

  void add(Object key) {
    table.put(key, key);
  }

  void clear() {
    table.clear();
  }
}
