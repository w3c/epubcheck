package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

class DataDerivFunction extends AbstractPatternFunction {
  private final ValidatorPatternBuilder builder;
  private final ValidationContext vc;
  private final String str;

  DataDerivFunction(String str, ValidationContext vc, ValidatorPatternBuilder builder) {
    this.str = str;
    this.vc = vc;
    this.builder = builder;
  }

  static boolean isBlank(String str) {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	break;
      default:
	return false;
      }
    }
    return true;
  }

  public Object caseText(TextPattern p) {
    return p;
  }

  public Object caseList(ListPattern p) {
    int len = str.length();
    int tokenStart = -1;
    PatternMemo memo = builder.getPatternMemo(p.getOperand());
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	if (tokenStart >= 0) {
	  memo = tokenDeriv(memo, tokenStart, i);
	  tokenStart = -1;
	}
	break;
      default:
	if (tokenStart < 0)
	  tokenStart = i;
	break;
      }
    }
    if (tokenStart >= 0)
      memo = tokenDeriv(memo, tokenStart, len);
    if (memo.getPattern().isNullable())
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  private PatternMemo tokenDeriv(PatternMemo p, int i, int j) {
    return p.dataDeriv(str.substring(i, j), vc);
  }

  public Object caseValue(ValuePattern p) {
    Datatype dt = p.getDatatype();
    Object value = dt.createValue(str, vc);
    if (value != null && dt.sameValue(p.getValue(), value))
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Object caseData(DataPattern p) {
    if (p.allowsAnyString())
      return builder.makeEmpty();
    if (p.getDatatype().isValid(str, vc))
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Object caseDataExcept(DataExceptPattern p) {
    Pattern tem = (Pattern)caseData(p);
    if (tem.isNullable() && memoApply(p.getExcept()).isNullable())
      return builder.makeNotAllowed();
    return tem;
  }

  public Object caseAfter(AfterPattern p) {
    Pattern p1 = p.getOperand1();
    if (memoApply(p1).isNullable() || (p1.isNullable() && isBlank(str)))
      return p.getOperand2();
    return builder.makeNotAllowed();
  }

  public Object caseChoice(ChoicePattern p) {
    return builder.makeChoice(memoApply(p.getOperand1()),
			      memoApply(p.getOperand2()));
  }
  
  public Object caseGroup(GroupPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    Pattern tem = builder.makeGroup(memoApply(p1), p2);
    if (!p1.isNullable())
      return tem;
    return builder.makeChoice(tem, memoApply(p2));
  }

  public Object caseInterleave(InterleavePattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    return builder.makeChoice(builder.makeInterleave(memoApply(p1), p2),
			      builder.makeInterleave(p1, memoApply(p2)));
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    return builder.makeGroup(memoApply(p.getOperand()),
			     builder.makeOptional(p));
  }

  public Object caseOther(Pattern p) {
    return builder.makeNotAllowed();
  }

  private Pattern memoApply(Pattern p) {
     return builder.getPatternMemo(p).dataDeriv(str, vc).getPattern();
   }
}
