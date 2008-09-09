package com.thaiopensource.xml.util;

public final class Name {
  final private String namespaceUri;
  final private String localName;
  final private int hc;
  
  public Name(String namespaceUri, String localName) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
    this.hc = namespaceUri.hashCode() ^ localName.hashCode();
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getLocalName() {
    return localName;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Name))
      return false;
    Name other = (Name)obj;
    return (this.hc == other.hc
	    && this.namespaceUri.equals(other.namespaceUri)
	    && this.localName.equals(other.localName));
  }

  public int hashCode() {
    return hc;
  }
}

