package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;
import com.thaiopensource.xml.util.StringSplitter;

class ListDatatype extends DatatypeBase implements Measure {
  private final DatatypeBase itemType;
  
  ListDatatype(DatatypeBase itemType) {
    this.itemType = itemType;
  }

  Object getValue(String str, ValidationContext vc) {
    String[] tokens = StringSplitter.split(str);
    Object[] items = new Object[tokens.length];
    for (int i = 0; i < items.length; i++) {
      items[i] = itemType.getValue(tokens[i], vc);
      if (items[i] == null)
	return null;
    }
    return items;
  }

  boolean lexicallyAllows(String str) {
    String[] tokens = StringSplitter.split(str);
    for (int i = 0; i < tokens.length; i++)
      if (!itemType.lexicallyAllows(tokens[i]))
	return false;
    return true;
  }

  boolean allowsValue(String str, ValidationContext vc) {
    String[] tokens = StringSplitter.split(str);
    for (int i = 0; i < tokens.length; i++)
      if (!itemType.allowsValue(tokens[i], vc))
	return false;
    return true;
  }

  Measure getMeasure() {
    return this;
  }

  public int getLength(Object obj) {
    return ((Object[])obj).length;
  }

  public boolean isContextDependent() {
    return itemType.isContextDependent();
  }

  public int getIdType() {
    if (itemType.getIdType() == ID_TYPE_IDREF)
      return ID_TYPE_IDREFS;
    else
      return ID_TYPE_NULL;
  }

  public int valueHashCode(Object obj) {
    Object[] items = (Object[])obj;
    int hc = 0;
    for (int i = 0; i < items.length; i++)
      hc ^= itemType.valueHashCode(items[i]);
    return hc;
  }

  public boolean sameValue(Object obj1, Object obj2) {
    Object[] items1 = (Object[])obj1;
    Object[] items2 = (Object[])obj2;
    if (items1.length != items2.length)
      return false;
    for (int i = 0; i < items1.length; i++)
      if (!itemType.sameValue(items1[i], items2[i]))
	return false;
    return true;
  }
}
