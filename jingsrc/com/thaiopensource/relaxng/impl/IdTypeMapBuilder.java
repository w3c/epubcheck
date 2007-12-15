package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.thaiopensource.xml.util.Name;

public class IdTypeMapBuilder {
  private boolean hadError;
  private final ErrorHandler eh;
  private final PatternFunction idTypeFunction = new IdTypeFunction();
  private final IdTypeMapImpl idTypeMap = new IdTypeMapImpl();
  private final Hashtable elementProcessed = new Hashtable();
  private final Vector possibleConflicts = new Vector();

  private void notePossibleConflict(NameClass elementNameClass, NameClass attributeNameClass, Locator loc) {
    possibleConflicts.addElement(new PossibleConflict(elementNameClass, attributeNameClass, loc));
  }

  private static class WrappedSAXException extends RuntimeException {
    private final SAXException cause;
    WrappedSAXException(SAXException cause) {
      this.cause = cause;
    }
  }

  private static class PossibleConflict {
    private final NameClass elementNameClass;
    private final NameClass attributeNameClass;
    private final Locator locator;

    private PossibleConflict(NameClass elementNameClass, NameClass attributeNameClass, Locator locator) {
      this.elementNameClass = elementNameClass;
      this.attributeNameClass = attributeNameClass;
      this.locator = locator;
    }
  }

  private static class ScopedName {
    private final Name elementName;
    private final Name attributeName;

    private ScopedName(Name elementName, Name attributeName) {
      this.elementName = elementName;
      this.attributeName = attributeName;
    }

    public int hashCode() {
      return elementName.hashCode() ^ attributeName.hashCode();
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof ScopedName))
        return false;
      ScopedName other = (ScopedName)obj;
      return elementName.equals(other.elementName) && attributeName.equals(other.attributeName);
    }
  }

  private static class IdTypeMapImpl implements IdTypeMap {
    private final Hashtable table = new Hashtable();
    public int getIdType(Name elementName, Name attributeName) {
      Integer n = (Integer)table.get(new ScopedName(elementName, attributeName));
      if (n == null)
        return Datatype.ID_TYPE_NULL;
      return n.intValue();
    }
    private void add(Name elementName, Name attributeName, int idType) {
      table.put(new ScopedName(elementName, attributeName), new Integer(idType));
    }
  }

  private class IdTypeFunction extends AbstractPatternFunction {
    public Object caseOther(Pattern p) {
      return new Integer(Datatype.ID_TYPE_NULL);
    }

    public Object caseData(DataPattern p) {
      return new Integer(p.getDatatype().getIdType());
    }

    public Object caseDataExcept(DataExceptPattern p) {
      return new Integer(p.getDatatype().getIdType());
    }

    public Object caseValue(ValuePattern p) {
      return new Integer(p.getDatatype().getIdType());
    }
  }

  private class BuildFunction extends AbstractPatternFunction {
    private final NameClass elementNameClass;
    private final Locator locator;
    private final boolean attributeIsParent;

    BuildFunction(NameClass elementNameClass, Locator locator) {
      this.elementNameClass = elementNameClass;
      this.locator = locator;
      this.attributeIsParent = false;
    }

   BuildFunction(NameClass elementNameClass, Locator locator, boolean attributeIsParent) {
      this.elementNameClass = elementNameClass;
      this.locator = locator;
      this.attributeIsParent = attributeIsParent;
    }

    private BuildFunction down() {
      if (!attributeIsParent)
        return this;
      return new BuildFunction(elementNameClass, locator, false);
    }

    public Object caseChoice(ChoicePattern p) {
      BuildFunction f = down();
      p.getOperand1().apply(f);
      p.getOperand2().apply(f);
      return null;
    }

    public Object caseInterleave(InterleavePattern p) {
      BuildFunction f = down();
      p.getOperand1().apply(f);
      p.getOperand2().apply(f);
      return null;
    }

    public Object caseGroup(GroupPattern p) {
      BuildFunction f = down();
      p.getOperand1().apply(f);
      p.getOperand2().apply(f);
      return null;
    }

    public Object caseOneOrMore(OneOrMorePattern p) {
      p.getOperand().apply(down());
      return null;
    }

    public Object caseElement(ElementPattern p) {
      if (elementProcessed.get(p) != null)
        return null;
      elementProcessed.put(p, p);
      p.getContent().apply(new BuildFunction(p.getNameClass(), p.getLocator()));
      return null;
    }

    public Object caseAttribute(AttributePattern p) {
      int idType = ((Integer)p.getContent().apply(idTypeFunction)).intValue();
      if (idType != Datatype.ID_TYPE_NULL) {
        NameClass attributeNameClass = p.getNameClass();
        if (!(attributeNameClass instanceof SimpleNameClass)) {
          error("id_attribute_name_class", p.getLocator());
          return null;
        }
        elementNameClass.accept(new ElementNameClassVisitor(((SimpleNameClass)attributeNameClass).getName(),
                                                            locator,
                                                            idType));
      }
      else
        notePossibleConflict(elementNameClass, p.getNameClass(), locator);
      p.getContent().apply(new BuildFunction(null, p.getLocator(), true));
      return null;
    }

    private void datatype(Datatype dt) {
      if (dt.getIdType() != Datatype.ID_TYPE_NULL && !attributeIsParent)
        error("id_parent", locator);
    }

    public Object caseData(DataPattern p) {
      datatype(p.getDatatype());
      return null;
    }

    public Object caseDataExcept(DataExceptPattern p) {
      datatype(p.getDatatype());
      p.getExcept().apply(down());
      return null;
    }

    public Object caseValue(ValuePattern p) {
      datatype(p.getDatatype());
      return null;
    }

    public Object caseList(ListPattern p) {
      p.getOperand().apply(down());
      return null;
    }

    public Object caseOther(Pattern p) {
      return null;
    }
  }

  private class ElementNameClassVisitor implements NameClassVisitor {
    private final Name attributeName;
    private final Locator locator;
    private final int idType;

    ElementNameClassVisitor(Name attributeName, Locator locator, int idType) {
      this.attributeName = attributeName;
      this.locator = locator;
      this.idType = idType;
    }

    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }

    public void visitName(Name elementName) {
      int tem = idTypeMap.getIdType(elementName, attributeName);
      if (tem !=  Datatype.ID_TYPE_NULL && tem != idType)
        error("id_type_conflict", elementName, attributeName, locator);
      idTypeMap.add(elementName, attributeName, idType);
    }

    public void visitNsName(String ns) {
      visitOther();
    }

    public void visitNsNameExcept(String ns, NameClass nc) {
      visitOther();
    }

    public void visitAnyName() {
      visitOther();
    }

    public void visitAnyNameExcept(NameClass nc) {
      visitOther();
    }

    public void visitNull() {
    }

    public void visitError() {
    }

    private void visitOther() {
      error("id_element_name_class", locator);
    }
  }

  private void error(String key, Locator locator) {
    hadError = true;
    if (eh != null)
      try {
        eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key), locator));
      }
      catch (SAXException e) {
        throw new WrappedSAXException(e);
      }
  }

  private void error(String key, Name arg1, Name arg2, Locator locator) {
   hadError = true;
   if (eh != null)
     try {
       eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key, NameFormatter.format(arg1), NameFormatter.format(arg2)),
                                      locator));
     }
     catch (SAXException e) {
       throw new WrappedSAXException(e);
     }
  }

  public IdTypeMapBuilder(ErrorHandler eh, Pattern pattern) throws SAXException {
    this.eh = eh;
    try {
      pattern.apply(new BuildFunction(null, null));
      for (Enumeration e = possibleConflicts.elements();
           e.hasMoreElements();) {
        PossibleConflict pc = (PossibleConflict)e.nextElement();
        if (pc.elementNameClass instanceof SimpleNameClass
            && pc.attributeNameClass instanceof SimpleNameClass) {
          Name elementName = ((SimpleNameClass)pc.elementNameClass).getName();
          Name attributeName = ((SimpleNameClass)pc.attributeNameClass).getName();
          int idType = idTypeMap.getIdType(elementName,
                                           attributeName);
          if (idType != Datatype.ID_TYPE_NULL)
            error("id_type_conflict", elementName, attributeName, pc.locator);
        }
        else {
          for (Enumeration f = idTypeMap.table.keys(); f.hasMoreElements();) {
            ScopedName sn = (ScopedName)f.nextElement();
            if (pc.elementNameClass.contains(sn.elementName)
                && pc.attributeNameClass.contains(sn.attributeName)) {
              error("id_type_conflict", sn.elementName, sn.attributeName, pc.locator);
              break;
            }
          }
        }
      }
    }
    catch (WrappedSAXException e) {
      throw e.cause;
    }
  }

  public IdTypeMap getIdTypeMap() {
    if (hadError)
      return null;
    return idTypeMap;
  }
}
