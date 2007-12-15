package com.thaiopensource.relaxng.impl;

class EndTagDerivFunction extends AbstractPatternFunction {
  private final ValidatorPatternBuilder builder;

  EndTagDerivFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public Object caseOther(Pattern p) {
    return builder.makeNotAllowed();
  }

  public Object caseChoice(ChoicePattern p) {
    return builder.makeChoice(memoApply(p.getOperand1()),
			      memoApply(p.getOperand2()));
  }

  public Object caseAfter(AfterPattern p) {
    if (p.getOperand1().isNullable())
      return p.getOperand2();
    else
      return builder.makeNotAllowed();
  }

  final private Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  private PatternMemo apply(PatternMemo memo) {
    return memo.endTagDeriv(this);
  }
}
