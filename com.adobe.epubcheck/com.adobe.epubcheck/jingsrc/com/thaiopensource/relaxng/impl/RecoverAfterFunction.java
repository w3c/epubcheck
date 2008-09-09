package com.thaiopensource.relaxng.impl;

class RecoverAfterFunction extends AbstractPatternFunction {
  private final ValidatorPatternBuilder builder;

  RecoverAfterFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public Object caseOther(Pattern p) {
    throw new RuntimeException("recover after botch");
  }

  public Object caseChoice(ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().applyForPattern(this),
			      p.getOperand2().applyForPattern(this));
  }

  public Object caseAfter(AfterPattern p) {
    return p.getOperand2();
  }
}
