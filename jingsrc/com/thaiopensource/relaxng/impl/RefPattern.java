package com.thaiopensource.relaxng.impl;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class RefPattern extends Pattern {
  private Pattern p;
  private Locator refLoc;
  private final String name;
  private int checkRecursionDepth = -1;
  private boolean combineImplicit = false;
  private byte combineType = COMBINE_NONE;
  private byte replacementStatus = REPLACEMENT_KEEP;
  private boolean expanded = false;

  static final byte REPLACEMENT_KEEP = 0;
  static final byte REPLACEMENT_REQUIRE = 1;
  static final byte REPLACEMENT_IGNORE = 2;

  static final byte COMBINE_NONE = 0;
  static final byte COMBINE_CHOICE = 1;
  static final byte COMBINE_INTERLEAVE = 2;

  RefPattern(String name) {
    this.name = name;
  }

  Pattern getPattern() {
    return p;
  }
  
  void setPattern(Pattern p) {
    this.p = p;
  }

  Locator getRefLocator() {
    return refLoc;
  }
  
  void setRefLocator(Locator loc) {
    this.refLoc = loc;
  }
  
  void checkRecursion(int depth) throws SAXException {
    if (checkRecursionDepth == -1) {
      checkRecursionDepth = depth;
      p.checkRecursion(depth);
      checkRecursionDepth = -2;
    }
    else if (depth == checkRecursionDepth)
      // XXX try to recover from this?
      throw new SAXParseException(SchemaBuilderImpl.localizer.message("recursive_reference", name),
				  refLoc);
  }

  Pattern expand(SchemaPatternBuilder b) {
    if (!expanded) {
      p = p.expand(b);
      expanded = true;
    }
    return p;
  }

  boolean samePattern(Pattern other) {
    return false;
  }

  void accept(PatternVisitor visitor) {
    p.accept(visitor);
  }

  Object apply(PatternFunction f) {
    return f.caseRef(this);
  }

  byte getReplacementStatus() {
    return replacementStatus;
  }
  
  void setReplacementStatus(byte replacementStatus) {
    this.replacementStatus = replacementStatus;
  }

  boolean isCombineImplicit() {
    return combineImplicit;
  }

  void setCombineImplicit() {
    combineImplicit = true;
  }

  byte getCombineType() {
    return combineType;
  }

  void setCombineType(byte combineType) {
    this.combineType = combineType;
  }

  String getName() {
    return name;
  }
}

