package com.thaiopensource.relaxng.impl;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class ElementPattern extends Pattern {
  private Pattern p;
  private final NameClass origNameClass;
  private NameClass nameClass;
  private boolean expanded = false;
  private boolean checkedRestrictions = false;
  private final Locator loc;

  ElementPattern(NameClass nameClass, Pattern p, Locator loc) {
    super(false,
	  ELEMENT_CONTENT_TYPE,
	  combineHashCode(ELEMENT_HASH_CODE,
			  nameClass.hashCode(),
			  p.hashCode()));
    this.nameClass = nameClass;
    this.origNameClass = nameClass;
    this.p = p;
    this.loc = loc;
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    if (alpha != null)
      alpha.addElement(origNameClass);
    if (checkedRestrictions)
      return;
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_element");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_element");
    case ATTRIBUTE_CONTEXT:
      throw new RestrictionViolationException("attribute_contains_element");
    }
    checkedRestrictions = true;
    try {
      p.checkRestrictions(ELEMENT_CONTEXT, new DuplicateAttributeDetector(), null);
    }
    catch (RestrictionViolationException e) {
      checkedRestrictions = false;
      e.maybeSetLocator(loc);
      throw e;
    }
  }

  Pattern expand(SchemaPatternBuilder b) {
    if (!expanded) {
      expanded = true;
      p = p.expand(b);
      if (p.isNotAllowed())
	nameClass = new NullNameClass();
    }
    return this;
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof ElementPattern))
      return false;
    ElementPattern ep = (ElementPattern)other;
    return nameClass.equals(ep.nameClass) && p == ep.p;
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth + 1);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitElement(nameClass, p);
  }

  Object apply(PatternFunction f) {
    return f.caseElement(this);
  }

  void setContent(Pattern p) {
    this.p = p;
  }

  Pattern getContent() {
    return p;
  }

  NameClass getNameClass() {
    return nameClass;
  }

  Locator getLocator() {
    return loc;
  }
}
