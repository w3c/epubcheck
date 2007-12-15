package com.thaiopensource.relaxng.parse.sax;

import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.relaxng.datatype.ValidationContext;

import java.util.Hashtable;

public abstract class DtdContext implements DTDHandler, ValidationContext {
  private final Hashtable notationTable;
  private final Hashtable unparsedEntityTable;

  public DtdContext() {
    notationTable = new Hashtable();
    unparsedEntityTable = new Hashtable();
  }

  public DtdContext(DtdContext dc) {
    notationTable = dc.notationTable;
    unparsedEntityTable = dc.unparsedEntityTable;
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId)
          throws SAXException {
    notationTable.put(name, name);
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName)
          throws SAXException {
    unparsedEntityTable.put(name, name);
  }

  public boolean isNotation(String notationName) {
    return notationTable.get(notationName) != null;
  }

  public boolean isUnparsedEntity(String entityName) {
    return unparsedEntityTable.get(entityName) != null;
  }

  public void clearDtdContext() {
    notationTable.clear();
    unparsedEntityTable.clear();
  }
}
