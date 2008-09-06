package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

class AnyNameClass implements NameClass {

  public boolean contains(Name name) {
    return true;
  }

  public int containsSpecificity(Name name) {
    return SPECIFICITY_ANY_NAME;
  }

  public boolean equals(Object obj) {
    return obj != null && obj instanceof AnyNameClass;
  }

  public int hashCode() {
    return AnyNameClass.class.hashCode();
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitAnyName();
  }

  public boolean isOpen() {
    return true;
  }
}
