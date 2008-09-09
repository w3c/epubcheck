package com.thaiopensource.relaxng.impl;

class IgnoreMissingAttributesFunction extends EndAttributesFunction {
  IgnoreMissingAttributesFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }

  public Object caseAttribute(AttributePattern p) {
    return getPatternBuilder().makeEmpty();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.ignoreMissingAttributes(this);
  }
}
