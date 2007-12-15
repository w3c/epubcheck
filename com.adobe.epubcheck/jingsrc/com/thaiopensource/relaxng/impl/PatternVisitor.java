package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;

public interface PatternVisitor {
  void visitEmpty();
  void visitNotAllowed();
  void visitError();
  void visitGroup(Pattern p1, Pattern p2);
  void visitInterleave(Pattern p1, Pattern p2);
  void visitChoice(Pattern p1, Pattern p2);
  void visitOneOrMore(Pattern p);
  void visitElement(NameClass nc, Pattern content);
  void visitAttribute(NameClass ns, Pattern value);
  void visitData(Datatype dt);
  void visitDataExcept(Datatype dt, Pattern except);
  void visitValue(Datatype dt, Object obj);
  void visitText();
  void visitList(Pattern p);
}
