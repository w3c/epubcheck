package com.thaiopensource.relaxng.impl;

interface PatternFunction {
  Object caseEmpty(EmptyPattern p);
  Object caseNotAllowed(NotAllowedPattern p);
  Object caseError(ErrorPattern p);
  Object caseGroup(GroupPattern p);
  Object caseInterleave(InterleavePattern p);
  Object caseChoice(ChoicePattern p);
  Object caseOneOrMore(OneOrMorePattern p);
  Object caseElement(ElementPattern p);
  Object caseAttribute(AttributePattern p);
  Object caseData(DataPattern p);
  Object caseDataExcept(DataExceptPattern p);
  Object caseValue(ValuePattern p);
  Object caseText(TextPattern p);
  Object caseList(ListPattern p);
  Object caseRef(RefPattern p);
  Object caseAfter(AfterPattern p);
}
