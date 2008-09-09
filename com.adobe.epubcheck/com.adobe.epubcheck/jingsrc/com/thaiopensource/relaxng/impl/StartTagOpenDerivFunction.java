package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

class StartTagOpenDerivFunction extends AbstractPatternFunction {
  private final Name name;
  private final ValidatorPatternBuilder builder;

  StartTagOpenDerivFunction(Name name, ValidatorPatternBuilder builder) {
    this.name = name;
    this.builder = builder;
  }

  public Object caseChoice(ChoicePattern p) {
    return builder.makeChoice(memoApply(p.getOperand1()),
			      memoApply(p.getOperand2()));
  }

  public Object caseGroup(GroupPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    Pattern tem = memoApply(p1).applyForPattern(
				      new ApplyAfterFunction(builder) {
					  Pattern apply(Pattern x) {
					    return builder.makeGroup(x, p2);
					  }
					});
    return p1.isNullable() ? builder.makeChoice(tem, memoApply(p2)) : tem;
  }

  public Object caseInterleave(InterleavePattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    return builder.makeChoice(
			      memoApply(p1).applyForPattern(new ApplyAfterFunction(builder) {
				  Pattern apply(Pattern x) {
				    return builder.makeInterleave(x, p2);
				  }
				}),
			      memoApply(p2).applyForPattern(new ApplyAfterFunction(builder) {
				  Pattern apply(Pattern x) {
				    return builder.makeInterleave(p1, x);
				  }
				}));
  }

  public Object caseAfter(AfterPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    return memoApply(p1).applyForPattern(
			       new ApplyAfterFunction(builder) {
				   Pattern apply(Pattern x) {
				     return builder.makeAfter(x, p2);
				   }
				 });
  }

  public Object caseOneOrMore(final OneOrMorePattern p) {
    final Pattern p1 = p.getOperand();
    return memoApply(p1).applyForPattern(
			       new ApplyAfterFunction(builder) {
				   Pattern apply(Pattern x) {
				     return builder.makeGroup(x,
							      builder.makeOptional(p));
				   }
				 });
  }


  public Object caseElement(ElementPattern p) {
    if (!p.getNameClass().contains(name))
      return builder.makeNotAllowed();
    return builder.makeAfter(p.getContent(), builder.makeEmpty());
  }

  public Object caseOther(Pattern p) {
    return builder.makeNotAllowed();
  }

  final Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.startTagOpenDeriv(this);
  }

  Name getName() {
    return name;
  }

  ValidatorPatternBuilder getPatternBuilder() {
    return builder;
  }
}
