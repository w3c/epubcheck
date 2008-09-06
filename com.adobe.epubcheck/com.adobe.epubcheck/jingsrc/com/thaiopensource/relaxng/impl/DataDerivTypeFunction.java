package com.thaiopensource.relaxng.impl;

class DataDerivTypeFunction extends AbstractPatternFunction {
  private final ValidatorPatternBuilder builder;

  DataDerivTypeFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  static DataDerivType dataDerivType(ValidatorPatternBuilder builder, Pattern pattern) {
    return (DataDerivType)pattern.apply(builder.getDataDerivTypeFunction());
  }

  public Object caseOther(Pattern p) {
    return new SingleDataDerivType();
  }

  public Object caseAfter(AfterPattern p) {
    Pattern p1 = p.getOperand1();
    DataDerivType ddt = apply(p.getOperand1());
    if (!p1.isNullable())
      return ddt;
    return ddt.combine(new BlankDataDerivType());
  }

  private Object caseBinary(BinaryPattern p) {
    return apply(p.getOperand1()).combine(apply(p.getOperand2()));
  }

  public Object caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }

  public Object caseGroup(GroupPattern p) {
    return caseBinary(p);
  }

  public Object caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    return apply(p.getOperand());
  }

  public Object caseList(ListPattern p) {
    return InconsistentDataDerivType.getInstance();
  }

  public Object caseValue(ValuePattern p) {
    return new ValueDataDerivType(p.getDatatype());
  }

  public Object caseData(DataPattern p) {
    if (p.allowsAnyString())
      return new SingleDataDerivType();
    return new DataDataDerivType(p.getDatatype());
  }

  public Object caseDataExcept(DataExceptPattern p) {
    if (p.allowsAnyString())
      return apply(p.getExcept());
    return new DataDataDerivType(p.getDatatype()).combine(apply(p.getExcept()));
  }

  private DataDerivType apply(Pattern p) {
    return builder.getPatternMemo(p).dataDerivType();
  }
}
