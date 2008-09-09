package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

import java.util.Hashtable;

class FindElementFunction extends AbstractPatternFunction {
  private final ValidatorPatternBuilder builder;
  private final Name name;
  private final Hashtable processed = new Hashtable();
  private int specificity = NameClass.SPECIFICITY_NONE;
  private Pattern pattern = null;

  static public Pattern findElement(ValidatorPatternBuilder builder, Name name, Pattern start) {
    FindElementFunction f = new FindElementFunction(builder, name);
    start.apply(f);
    if (f.pattern == null)
      return builder.makeNotAllowed();
    return f.pattern;
  }

  private FindElementFunction(ValidatorPatternBuilder builder, Name name) {
    this.builder = builder;
    this.name = name;
  }

  private boolean haveProcessed(Pattern p) {
    if (processed.get(p) != null)
      return true;
    processed.put(p, p);
    return false;
  }

  private Object caseBinary(BinaryPattern p) {
    if (!haveProcessed(p)) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
    }
    return null;

 }

  public Object caseGroup(GroupPattern p) {
    return caseBinary(p);
  }

  public Object caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public Object caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    if (!haveProcessed(p))
      p.getOperand().apply(this);
    return null;
  }

  public Object caseElement(ElementPattern p) {
    if (!haveProcessed(p)) {
      int s = p.getNameClass().containsSpecificity(name);
      if (s > specificity) {
        specificity = s;
        pattern = p.getContent();
      }
      else if (s == specificity && s != NameClass.SPECIFICITY_NONE)
        pattern = builder.makeChoice(pattern, p.getContent());
      p.getContent().apply(this);
    }
    return null;
  }

  public Object caseOther(Pattern p) {
    return null;
  }
}
