package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;
import com.thaiopensource.xml.util.Naming;

class QNameDatatype extends DatatypeBase {
  public boolean lexicallyAllows(String str) {
    return Naming.isQname(str);
  }

  static class QName {
    private final String namespaceURI;
    private final String localName;
    QName(String namespaceURI, String localName) {
      this.namespaceURI = namespaceURI;
      this.localName = localName;
    }
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof QName))
	return false;
      QName other = (QName)obj;
      return namespaceURI.equals(other.namespaceURI) && localName.equals(other.localName);
    }
    public int hashCode() {
      return localName.hashCode() ^ namespaceURI.hashCode();
    }
  }

  Object getValue(String str, ValidationContext vc) {
    int i = str.indexOf(':');
    if (i < 0) {
      String ns = vc.resolveNamespacePrefix("");
      if (ns == null)
	ns = "";
      return new QName(ns, str);
    }
    else {
      String prefix = str.substring(0, i);
      String ns = vc.resolveNamespacePrefix(prefix);
      if (ns == null)
	return null;
      return new QName(ns, str.substring(i + 1));
    }
  }

  boolean allowsValue(String str, ValidationContext vc) {
    int i = str.indexOf(':');
    return i < 0 || vc.resolveNamespacePrefix(str.substring(0, i)) != null;
  }

  public boolean isContextDependent() {
    return true;
  }
}
