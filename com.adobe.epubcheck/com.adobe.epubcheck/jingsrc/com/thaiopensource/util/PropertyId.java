package com.thaiopensource.util;

public class PropertyId {
  private final String name;
  private final Class valueClass;

  public PropertyId(String name, Class valueClass) {
    if (name == null || valueClass == null)
      throw new NullPointerException();
    this.name = name;
    this.valueClass = valueClass;
  }

  public Class getValueClass() {
    return valueClass;
  }

  public final int hashCode() {
    return super.hashCode();
  }

  public final boolean equals(Object obj) {
    return super.equals(obj);
  }

  public String toString() {
    return name;
  }
}
