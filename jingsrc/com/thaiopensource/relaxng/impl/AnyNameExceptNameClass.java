package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

class AnyNameExceptNameClass implements NameClass {

  private final NameClass nameClass;

  AnyNameExceptNameClass(NameClass nameClass) {
    this.nameClass = nameClass;
  }

  public boolean contains(Name name) {
    return !nameClass.contains(name);
  }

  public int containsSpecificity(Name name) {
    return contains(name) ? SPECIFICITY_ANY_NAME : SPECIFICITY_NONE;
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof AnyNameExceptNameClass))
      return false;
    return nameClass.equals(((AnyNameExceptNameClass)obj).nameClass);
  }

  public int hashCode() {
    return ~nameClass.hashCode();
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitAnyNameExcept(nameClass);
  }

  public boolean isOpen() {
    return true;
  }
}
