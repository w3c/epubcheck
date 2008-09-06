package com.thaiopensource.relaxng.impl;

class TextOnlyFunction extends EndAttributesFunction {
  TextOnlyFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }
  public Object caseAttribute(AttributePattern p) {
    return p;
  }
  public Object caseElement(ElementPattern p) {
    return getPatternBuilder().makeNotAllowed();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.textOnly(this);
  }

}

