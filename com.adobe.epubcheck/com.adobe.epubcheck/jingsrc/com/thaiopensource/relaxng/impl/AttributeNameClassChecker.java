package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.xml.util.Name;

class AttributeNameClassChecker implements NameClassVisitor {
  private String errorMessageId = null;
  
  public void visitChoice(NameClass nc1, NameClass nc2) {
    nc1.accept(this);
    nc2.accept(this);
  }

  public void visitNsName(String ns) {
    if (ns.equals(WellKnownNamespaces.XMLNS))
      errorMessageId = "xmlns_uri_attribute";
  }

  public void visitNsNameExcept(String ns, NameClass nc) {
    visitNsName(ns);
    nc.accept(this);
  }

  public void visitAnyName() { }

  public void visitAnyNameExcept(NameClass nc) {
    nc.accept(this);
  }

  public void visitName(Name name) {
    visitNsName(name.getNamespaceUri());
    if (name.equals(new Name("", "xmlns")))
      errorMessageId = "xmlns_attribute";
  }

  public void visitNull() { }

  public void visitError() { }

  String checkNameClass(NameClass nc) {
    errorMessageId = null;
    nc.accept(this);
    return errorMessageId;
  }
}
