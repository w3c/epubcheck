package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

public interface NameClassVisitor {
  void visitChoice(NameClass nc1, NameClass nc2);
  void visitNsName(String ns);
  void visitNsNameExcept(String ns, NameClass nc);
  void visitAnyName();
  void visitAnyNameExcept(NameClass nc);
  void visitName(Name name);
  void visitNull();
  void visitError();
}
