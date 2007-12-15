package com.thaiopensource.util;

public class SinglePropertyMap implements PropertyMap {
  private final PropertyId pid;
  private final Object value;

  public SinglePropertyMap(PropertyId pid, Object value) {
    if (!(pid.getValueClass().isInstance(value))) {
      if (value == null)
        throw new NullPointerException();
      throw new ClassCastException();
    }
    this.pid = pid;
    this.value = value;
  }

  public Object get(PropertyId pid) {
    if (pid != this.pid)
      return null;
    return value;
  }

  public boolean contains(PropertyId pid) {
    return pid == this.pid;
  }

  public int size() {
    return 1;
  }

  public PropertyId getKey(int i) {
    if (i != 0)
      throw new IndexOutOfBoundsException();
    return pid;
  }
}
