package com.thaiopensource.util;

public interface PropertyMap {
  public static final PropertyMap EMPTY = new PropertyMap() {
    public Object get(PropertyId pid) {
      return null;
    }

    public boolean contains(PropertyId pid) {
      return false;
    }

    public int size() {
      return 0;
    }

    public PropertyId getKey(int i) {
      throw new IndexOutOfBoundsException();
    }
  };
  Object get(PropertyId pid);
  boolean contains(PropertyId pid);
  int size();
  PropertyId getKey(int i);
}
