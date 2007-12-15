package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import com.thaiopensource.xml.util.WellKnownNamespaces;

class CompatibilityDatatypeLibrary implements DatatypeLibrary {
  private final DatatypeLibraryFactory factory;
  private DatatypeLibrary xsdDatatypeLibrary = null;

  CompatibilityDatatypeLibrary(DatatypeLibraryFactory factory) {
    this.factory = factory;
  }

  public DatatypeBuilder createDatatypeBuilder(String type)
          throws DatatypeException {
    if (type.equals("ID") || type.equals("IDREF") || type.equals("IDREFS")) {
      if (xsdDatatypeLibrary == null) {
        xsdDatatypeLibrary = factory.createDatatypeLibrary(WellKnownNamespaces.XML_SCHEMA_DATATYPES);
        if (xsdDatatypeLibrary == null)
          throw new DatatypeException();
      }
      return xsdDatatypeLibrary.createDatatypeBuilder(type);
    }
    throw new DatatypeException();
  }

  public Datatype createDatatype(String type) throws DatatypeException {
    return createDatatypeBuilder(type).createDatatype();
  }
}
