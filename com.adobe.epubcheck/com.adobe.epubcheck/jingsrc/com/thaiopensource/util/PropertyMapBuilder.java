package com.thaiopensource.util;

import java.util.Hashtable;
import java.util.Enumeration;

public class PropertyMapBuilder {
  private Hashtable map;
  private PropertyId[] keys;

  private static class PropertyMapImpl implements PropertyMap {
    private final Hashtable map;
    private final PropertyId[] keys;

    private PropertyMapImpl(Hashtable map, PropertyId[] keys) {
      this.map = map;
      this.keys = keys;
    }

    public Object get(PropertyId pid) {
      return map.get(pid);
    }

    public int size() {
      return keys.length;
    }

    public boolean contains(PropertyId pid) {
      return map.get(pid) != null;
    }

    public PropertyId getKey(int i) {
      return keys[i];
    }
  }

  public PropertyMapBuilder() {
    this.map = new Hashtable();
  }

  public PropertyMapBuilder(PropertyMap pm) {
    if (pm instanceof PropertyMapImpl) {
      PropertyMapImpl pmi = (PropertyMapImpl)pm;
      this.map = pmi.map;
      this.keys = pmi.keys;
    }
    else {
      this.map = new Hashtable();
      for (int i = 0, len = pm.size(); i < len; i++) {
        PropertyId pid = pm.getKey(i);
        put(pid, pm.get(pid));
      }
    }
  }

  private void lock() {
    if (keys != null)
      return;
    keys = new PropertyId[map.size()];
    int i = 0;
    for (Enumeration e = map.keys(); e.hasMoreElements();)
      keys[i++] = (PropertyId)e.nextElement();
  }

  private void copyIfLocked() {
    if (keys == null)
      return;
    Hashtable newMap = new Hashtable();
    for (int i = 0; i < keys.length; i++)
      newMap.put(keys[i], map.get(keys[i]));
    map = newMap;
    keys = null;
  }

  public PropertyMap toPropertyMap() {
    lock();
    return new PropertyMapImpl(map, keys);
  }

  public Object put(PropertyId id, Object value) {
    copyIfLocked();
    if (value == null)
      return map.remove(id);
    if (!id.getValueClass().isInstance(value))
      throw new ClassCastException();
    return map.put(id, value);
  }

  public Object get(PropertyId pid) {
    return map.get(pid);
  }

  public boolean contains(PropertyId pid) {
    return map.get(pid) != null;
  }
}
