package com.thaiopensource.validate.nrl;

import org.xml.sax.Attributes;

class FilteredAttributes implements Attributes {
  private final Attributes attributes;
  private final IntSet indexSet;
  private int[] reverseIndexMap;

  public FilteredAttributes(IntSet indexSet, Attributes attributes) {
    this.indexSet = indexSet;
    this.attributes = attributes;
  }

  private int reverseIndex(int k) {
    if (reverseIndexMap == null) {
      reverseIndexMap = new int[attributes.getLength()];
      for (int i = 0, len = indexSet.size(); i < len; i++)
        reverseIndexMap[indexSet.get(i)] = i + 1;
    }
    return reverseIndexMap[k] - 1;
  }

  public int getLength() {
    return indexSet.size();
  }

  public String getURI(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getURI(indexSet.get(index));
  }

  public String getLocalName(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getLocalName(indexSet.get(index));
  }

  public String getQName(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getQName(indexSet.get(index));
  }

  public String getType(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getType(indexSet.get(index));
  }

  public String getValue(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getValue(indexSet.get(index));
  }

  public int getIndex(String uri, String localName) {
    int n = attributes.getIndex(uri, localName);
    if (n < 0)
      return n;
    return reverseIndex(n);
  }

  public int getIndex(String qName) {
    int n = attributes.getIndex(qName);
    if (n < 0)
      return n;
    return reverseIndex(n);
  }

  private int getRealIndex(String uri, String localName) {
    int index = attributes.getIndex(uri, localName);
    if (index < 0 || reverseIndex(index) < 0)
      return -1;
    return index;
  }

  private int getRealIndex(String qName) {
    int index = attributes.getIndex(qName);
    if (index < 0 || reverseIndex(index) < 0)
      return -1;
    return index;
  }

  public String getType(String uri, String localName) {
    return attributes.getType(getRealIndex(uri, localName));
  }

  public String getValue(String uri, String localName) {
    return attributes.getValue(getRealIndex(uri, localName));
  }

  public String getType(String qName) {
    return attributes.getType(getRealIndex(qName));
  }

  public String getValue(String qName) {
    return attributes.getValue(getRealIndex(qName));
  }

}
