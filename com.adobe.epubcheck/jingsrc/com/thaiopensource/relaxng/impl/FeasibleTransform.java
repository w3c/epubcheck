package com.thaiopensource.relaxng.impl;

import java.util.Hashtable;

public class FeasibleTransform {
  private static class FeasiblePatternFunction extends AbstractPatternFunction {
    private final SchemaPatternBuilder spb;
    private final Hashtable elementTable = new Hashtable();

    FeasiblePatternFunction(SchemaPatternBuilder spb) {
      this.spb = spb;
    }

    public Object caseChoice(ChoicePattern p) {
      return spb.makeChoice(p.getOperand1().applyForPattern(this), p.getOperand2().applyForPattern(this));
    }

    public Object caseGroup(GroupPattern p) {
      return spb.makeGroup(p.getOperand1().applyForPattern(this), p.getOperand2().applyForPattern(this));
    }

    public Object caseInterleave(InterleavePattern p) {
      return spb.makeInterleave(p.getOperand1().applyForPattern(this), p.getOperand2().applyForPattern(this));
    }

    public Object caseOneOrMore(OneOrMorePattern p) {
      return spb.makeOneOrMore(p.getOperand().applyForPattern(this));
    }

    public Object caseElement(ElementPattern p) {
      if (elementTable.get(p) == null) {
        elementTable.put(p, p);
        p.setContent(p.getContent().applyForPattern(this));
      }
      return spb.makeOptional(p);
    }

    public Object caseOther(Pattern p) {
      return spb.makeOptional(p);
    }
  }

  public static Pattern transform(SchemaPatternBuilder spb, Pattern p) {
    return p.applyForPattern(new FeasiblePatternFunction(spb));
  }
}
