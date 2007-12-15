package com.thaiopensource.validate.nrl;

class ElementsOrAttributes {
  private static final int ELEMENTS_FLAG = 01;
  private static final int ATTRIBUTES_FLAG = 02;

  static final ElementsOrAttributes NEITHER = new ElementsOrAttributes(0);
  static final ElementsOrAttributes ELEMENTS = new ElementsOrAttributes(ELEMENTS_FLAG);
  static final ElementsOrAttributes ATTRIBUTES = new ElementsOrAttributes(ATTRIBUTES_FLAG);
  static final ElementsOrAttributes BOTH = new ElementsOrAttributes(ELEMENTS_FLAG|ATTRIBUTES_FLAG);

  private static final ElementsOrAttributes values[] = {
    NEITHER,
    ELEMENTS,
    ATTRIBUTES,
    BOTH
  };

  private int flags = 0;

  private ElementsOrAttributes(int flags) {
    this.flags = flags;
  }

  ElementsOrAttributes addElements() {
    return values[flags | ELEMENTS_FLAG];
  }

  ElementsOrAttributes addAttributes() {
    return values[flags | ATTRIBUTES_FLAG];
  }

  boolean containsAttributes() {
    return (flags & ATTRIBUTES_FLAG) != 0;
  }

  boolean containsElements() {
    return (flags & ELEMENTS_FLAG) != 0;
  }

}
