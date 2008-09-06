package com.thaiopensource.relaxng.impl;

import java.util.Hashtable;

public class ValidatorPatternBuilder extends PatternBuilder {
  private final Hashtable patternMemoMap = new Hashtable();
  private final PatternFunction endAttributesFunction;
  private final PatternFunction ignoreMissingAttributesFunction;
  private final PatternFunction endTagDerivFunction;
  private final PatternFunction mixedTextDerivFunction;
  private final PatternFunction textOnlyFunction;
  private final PatternFunction recoverAfterFunction;
  private final PatternFunction dataDerivTypeFunction;

  private final Hashtable choiceMap = new Hashtable();
  private final PatternFunction removeChoicesFunction = new RemoveChoicesFunction();
  private final PatternFunction noteChoicesFunction = new NoteChoicesFunction();

  private class NoteChoicesFunction extends AbstractPatternFunction {
    public Object caseOther(Pattern p) {
      choiceMap.put(p, p);
      return null;
    }

    public Object caseChoice(ChoicePattern p) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      return null;
    }
  }

  private class RemoveChoicesFunction extends AbstractPatternFunction {
    public Object caseOther(Pattern p) {
      if (choiceMap.get(p) != null)
        return notAllowed;
      return p;
    }

    public Object caseChoice(ChoicePattern p) {
      Pattern p1 = p.getOperand1().applyForPattern(this);
      Pattern p2 = p.getOperand2().applyForPattern(this);
      if (p1 == p.getOperand1() && p2 == p.getOperand2())
        return p;
      if (p1 == notAllowed)
        return p2;
      if (p2 == notAllowed)
        return p1;
      Pattern p3 = new ChoicePattern(p1, p2);
      return interner.intern(p3);
    }
  }

  public ValidatorPatternBuilder(PatternBuilder builder) {
    super(builder);
    endAttributesFunction = new EndAttributesFunction(this);
    ignoreMissingAttributesFunction = new IgnoreMissingAttributesFunction(this);
    endTagDerivFunction = new EndTagDerivFunction(this);
    mixedTextDerivFunction = new MixedTextDerivFunction(this);
    textOnlyFunction = new TextOnlyFunction(this);
    recoverAfterFunction = new RecoverAfterFunction(this);
    dataDerivTypeFunction = new DataDerivTypeFunction(this);
  }

  PatternMemo getPatternMemo(Pattern p) {
    PatternMemo memo = (PatternMemo)patternMemoMap.get(p);
    if (memo == null) {
      memo = new PatternMemo(p, this);
      patternMemoMap.put(p, memo);
    }
    return memo;
  }

  PatternFunction getEndAttributesFunction() {
    return endAttributesFunction;
  }

  PatternFunction getIgnoreMissingAttributesFunction() {
    return ignoreMissingAttributesFunction;
  }

  PatternFunction getEndTagDerivFunction() {
    return endTagDerivFunction;
  }

  PatternFunction getMixedTextDerivFunction() {
    return mixedTextDerivFunction;
  }

  PatternFunction getTextOnlyFunction() {
    return textOnlyFunction;
  }

  PatternFunction getRecoverAfterFunction() {
    return recoverAfterFunction;
  }

  PatternFunction getDataDerivTypeFunction() {
    return dataDerivTypeFunction;
  }

  Pattern makeAfter(Pattern p1, Pattern p2) {
    Pattern p = new AfterPattern(p1, p2);
    return interner.intern(p);
  }

  Pattern makeChoice(Pattern p1, Pattern p2) {
    if (p1 == p2)
      return p1;
    if (p1 == notAllowed)
      return p2;
    if (p2 == notAllowed)
      return p1;
    if (!(p1 instanceof ChoicePattern)) {
      if (p2.containsChoice(p1))
        return p2;
    }
    else if (!(p2 instanceof ChoicePattern)) {
      if (p1.containsChoice(p2))
        return p1;
    }
    else {
      p1.apply(noteChoicesFunction);
      p2 = p2.applyForPattern(removeChoicesFunction);
      if (choiceMap.size() > 0)
        choiceMap.clear();
    }
    if (p1 instanceof AfterPattern && p2 instanceof AfterPattern) {
      AfterPattern ap1 = (AfterPattern)p1;
      AfterPattern ap2 = (AfterPattern)p2;
      if (ap1.getOperand1() == ap2.getOperand1())
        return makeAfter(ap1.getOperand1(), makeChoice(ap1.getOperand2(), ap2.getOperand2()));
      if (ap1.getOperand1() == notAllowed)
        return ap2;
      if (ap2.getOperand1() == notAllowed)
        return ap1;
      if (ap1.getOperand2() == ap2.getOperand2())
        return makeAfter(makeChoice(ap1.getOperand1(), ap2.getOperand1()), ap1.getOperand2());
    }
    return super.makeChoice(p1, p2);
  }
}
