package com.thaiopensource.relaxng.impl;

abstract class AbstractPatternFunction implements PatternFunction {
  public Object caseEmpty(EmptyPattern p) {
    return caseOther(p);
  }

  public Object caseNotAllowed(NotAllowedPattern p) {
    return caseOther(p);
  }

  public Object caseError(ErrorPattern p) {
    return caseOther(p);
  }

  public Object caseGroup(GroupPattern p) {
    return caseOther(p);
  }

  public Object caseInterleave(InterleavePattern p) {
    return caseOther(p);
  }

  public Object caseChoice(ChoicePattern p) {
    return caseOther(p);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    return caseOther(p);
  }

  public Object caseElement(ElementPattern p) {
    return caseOther(p);
  }

  public Object caseAttribute(AttributePattern p) {
    return caseOther(p);
  }

  public Object caseData(DataPattern p) {
    return caseOther(p);
  }

  public Object caseDataExcept(DataExceptPattern p) {
    return caseOther(p);
  }

  public Object caseValue(ValuePattern p) {
    return caseOther(p);
  }

  public Object caseText(TextPattern p) {
    return caseOther(p);
  }

  public Object caseList(ListPattern p) {
    return caseOther(p);
  }

  public Object caseAfter(AfterPattern p) {
    return caseOther(p);
  }

  public Object caseRef(RefPattern p) {
    return caseOther(p);
  }

  public abstract Object caseOther(Pattern p);
}
