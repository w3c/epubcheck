package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;


class SimpleNameClass implements NameClass {

  private final Name name;

  SimpleNameClass(Name name) {
    this.name = name;
  }

  public boolean contains(Name name) {
    return this.name.equals(name);
  }

  public int containsSpecificity(Name name) {
    return contains(name) ? SPECIFICITY_NAME : SPECIFICITY_NONE;
  }

  public int hashCode() {
    return name.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof SimpleNameClass))
      return false;
    SimpleNameClass other = (SimpleNameClass)obj;
    return name.equals(other.name);
  }

  Name getName() {
    return name;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitName(name);
  }

  public boolean isOpen() {
    return false;
  }
}
