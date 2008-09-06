package com.thaiopensource.relaxng.impl;

class EndAttributesFunction extends AbstractPatternFunction {
  private final ValidatorPatternBuilder builder;

  EndAttributesFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public Object caseOther(Pattern p) {
    return p;
  }

  public Object caseGroup(GroupPattern p) {
    Pattern p1 = p.getOperand1();
    Pattern p2 = p.getOperand2();
    Pattern q1 = memoApply(p1);
    Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeGroup(q1, q2);
  }

  public Object caseInterleave(InterleavePattern p) {
    Pattern p1 = p.getOperand1();
    Pattern p2 = p.getOperand2();
    Pattern q1 = memoApply(p1);
    Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeInterleave(q1, q2);
  }

  public Object caseChoice(ChoicePattern p) {
    Pattern p1 = p.getOperand1();
    Pattern p2 = p.getOperand2();
    Pattern q1 = memoApply(p1);
    Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeChoice(q1, q2);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    Pattern p1 = p.getOperand();
    Pattern q1 = memoApply(p1);
    if (p1 == q1)
      return p;
    return builder.makeOneOrMore(p1);
  }

  public Object caseAfter(AfterPattern p) {
    Pattern p1 = p.getOperand1();
    Pattern q1 = memoApply(p1);
    if (p1 == q1)
      return p;
    return builder.makeAfter(q1, p.getOperand2());
  }

  public Object caseAttribute(AttributePattern p) {
    return builder.makeNotAllowed();
  }

  final Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.endAttributes(this);
  }

  ValidatorPatternBuilder getPatternBuilder() {
    return builder;
  }
}
