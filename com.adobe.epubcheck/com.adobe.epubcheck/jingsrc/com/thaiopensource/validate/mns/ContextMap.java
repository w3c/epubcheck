package com.thaiopensource.validate.mns;

import com.thaiopensource.xml.util.Name;

import java.util.Vector;
import java.util.Hashtable;

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
      Name name = (Name)names.elementAt(len - 1);
      ContextMap nestedMap = (ContextMap)nameTable.get(name);
      if (nestedMap == null) {
        nestedMap = new ContextMap();
        nameTable.put(name, nestedMap);
      }
      return nestedMap.put(isRoot, names, len - 1, value);
    }
  }
}
