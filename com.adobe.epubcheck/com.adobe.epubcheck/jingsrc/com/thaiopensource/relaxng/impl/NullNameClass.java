package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

class NullNameClass implements NameClass {
  public boolean contains(Name name) {
    return false;
  }

  public int containsSpecificity(Name name) {
    return SPECIFICITY_NONE;
  }

  public int hashCode() {
    return NullNameClass.class.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NullNameClass))
      return false;
    return true;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitNull();
  }

  public boolean isOpen() {
    return false;
  }
}
