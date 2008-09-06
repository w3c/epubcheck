package com.thaiopensource.validate.mns;

import org.xml.sax.Attributes;

class NamespaceFilteredAttributes implements Attributes {
  private final String ns;
  private final boolean keepLocal;
  private final Attributes attributes;
  private final int[] indexMap;
  private final int[] reverseIndexMap;

  public NamespaceFilteredAttributes(String ns, boolean keepLocal, Attributes attributes) {
    this.ns = ns;
    this.keepLocal = keepLocal;
    this.attributes = attributes;
    int n = 0;
    for (int i = 0, len = attributes.getLength(); i < len; i++)
      if (keepAttribute(attributes.getURI(i)))
        n++;
    indexMap = new int[n];
    reverseIndexMap = new int[attributes.getLength()];
    n = 0;
    for (int i = 0, len = attributes.getLength(); i < len; i++) {
      if (keepAttribute(attributes.getURI(i))) {
        reverseIndexMap[i] = n;
        indexMap[n++] = i;
      }
      else
        reverseIndexMap[i] = -1;
    }
  }

  private boolean keepAttribute(String uri) {
    return uri.equals(ns) || (keepLocal && uri.equals(""));
  }

  public int getLength() {
    return indexMap.length;
  }

  public String getURI(int index) {
    if (index < 0 || index >= indexMap.length)
      return null;
    return attributes.getURI(indexMap[index]);
  }

  public String getLocalName(int index) {
    if (index < 0 || index >= indexMap.length)
      return null;
    return attributes.getLocalName(indexMap[index]);
  }

  public String getQName(int index) {
    if (index < 0 || index >= indexMap.length)
      return null;
    return attributes.getQName(indexMap[index]);
  }

  public String getType(int index) {
    if (index < 0 || index >= indexMap.length)
      return null;
    return attributes.getType(indexMap[index]);
  }

  public String getValue(int index) {
    if (index < 0 || index >= indexMap.length)
      return null;
    return attributes.getValue(indexMap[index]);
  }

  public int getIndex(String uri, String localName) {
    int n = attributes.getIndex(uri, localName);
    if (n < 0)
      return n;
    return reverseIndexMap[n];
  }

  public int getIndex(String qName) {
    int n = attributes.getIndex(qName);
    if (n < 0)
      return n;
    return reverseIndexMap[n];
  }

  public String getType(String uri, String localName) {
    if (keepAttribute(uri))
      return attributes.getType(uri, localName);
    return null;
  }

  public String getValue(String uri, String localName) {
    if (keepAttribute(uri))
      return attributes.getValue(uri, localName);
    return null;
  }

  public String getType(String qName) {
    int i = getIndex(qName);
    if (i < 0)
      return null;
    return getType(i);
  }

  public String getValue(String qName) {
    int i = getIndex(qName);
    if (i < 0)
      return null;
    return getValue(i);
  }
}
