package com.thaiopensource.validate.nrl;

import com.thaiopensource.util.Equal;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

class ContextMap {
  private Object rootValue;
  private Object otherValue;
  private final Hashtable nameTable = new Hashtable();

  Object get(Vector context) {
    return get(context, context.size());
  }

  boolean put(boolean isRoot, Vector names, Object value) {
    return put(isRoot, names, names.size(), value);
  }

  private Object get(Vector context, int len) {
    if (len > 0) {
      ContextMap nestedMap = (ContextMap)nameTable.get(context.elementAt(len - 1));
      if (nestedMap != null) {
        Object value = nestedMap.get(context, len - 1);
        if (value != null)
          return value;
      }
    }
    if (rootValue != null && len == 0)
      return rootValue;
    return otherValue;
  }

  private boolean put(boolean isRoot, Vector names, int len, Object value) {
    if (len == 0) {
      if (isRoot) {
        if (rootValue != null)
          return false;
        rootValue = value;
      }
      else {
        if (otherValue != null)
          return false;
        otherValue = value;
      }
      return true;
    }
    else {
      Object name = names.elementAt(len - 1);
      ContextMap nestedMap = (ContextMap)nameTable.get(name);
      if (nestedMap == null) {
        nestedMap = new ContextMap();
        nameTable.put(name, nestedMap);
      }
      return nestedMap.put(isRoot, names, len - 1, value);
    }
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ContextMap))
      return false;
    ContextMap other = (ContextMap)obj;
    if (!Equal.equal(this.rootValue, other.rootValue)
        || !Equal.equal(this.otherValue, other.otherValue))
      return false;
    // We want jing to work with JDK 1.1 so we cannot use Hashtable.equals
    if (this.nameTable.size() != other.nameTable.size())
      return false;
    for (Enumeration e = nameTable.keys(); e.hasMoreElements();) {
      Object key = e.nextElement();
      if (!nameTable.get(key).equals(other.nameTable.get(key)))
        return false;
    }
    return true;
  }

  public int hashCode() {
    int hc = 0;
    if (rootValue != null)
      hc ^= rootValue.hashCode();
    if (otherValue != null)
      hc ^= otherValue.hashCode();
    for (Enumeration e = nameTable.keys(); e.hasMoreElements();) {
      Object key = e.nextElement();
      hc ^= key.hashCode();
      hc ^= nameTable.get(key).hashCode();
    }
    return hc;
  }

  static private class Enumerator implements Enumeration {
    private Object rootValue;
    private Object otherValue;
    private Enumeration subMapValues;
    private final Enumeration subMaps;

    private Enumerator(ContextMap map) {
      rootValue = map.rootValue;
      otherValue = map.otherValue;
      subMaps = map.nameTable.elements();
    }

    private void prep() {
      while ((subMapValues == null || !subMapValues.hasMoreElements()) && subMaps.hasMoreElements())
        subMapValues = ((ContextMap)subMaps.nextElement()).values();
    }

    public boolean hasMoreElements() {
      prep();
      return rootValue != null || otherValue != null || (subMapValues != null && subMapValues.hasMoreElements());
    }

    public Object nextElement() {
      if (rootValue != null) {
        Object tem = rootValue;
        rootValue = null;
        return tem;
      }
      if (otherValue != null) {
        Object tem = otherValue;
        otherValue = null;
        return tem;
      }
      prep();
      if (subMapValues == null)
        throw new NoSuchElementException();
      return subMapValues.nextElement();
    }
  }

  Enumeration values() {
    return new Enumerator(this);
  }
}
