package com.thaiopensource.relaxng.impl;

abstract class ApplyAfterFunction extends AbstractPatternFunction {
  private final ValidatorPatternBuilder builder;

  ApplyAfterFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public Object caseAfter(AfterPattern p) {
    return builder.makeAfter(p.getOperand1(), apply(p.getOperand2()));
  }

  public Object caseChoice(ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().applyForPattern(this),
			      p.getOperand2().applyForPattern(this));
  }

  public Object caseNotAllowed(NotAllowedPattern p) {
    return p;
  }

  public Object caseOther(Pattern p) {
    throw new RuntimeException("apply after botch");
  }

  abstract Pattern apply(Pattern p);
}
