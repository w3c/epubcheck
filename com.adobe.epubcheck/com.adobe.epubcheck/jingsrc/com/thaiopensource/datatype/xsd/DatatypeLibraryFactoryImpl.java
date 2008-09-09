package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import com.thaiopensource.xml.util.WellKnownNamespaces;

public class DatatypeLibraryFactoryImpl implements DatatypeLibraryFactory {

  private DatatypeLibrary datatypeLibrary = null;

  public DatatypeLibrary createDatatypeLibrary(String uri) {
    if (!WellKnownNamespaces.XML_SCHEMA_DATATYPES.equals(uri))
      return null;
    synchronized (this) {
      if (datatypeLibrary == null)
        datatypeLibrary = new DatatypeLibraryImpl();
      return datatypeLibrary;
    }
  }
}
