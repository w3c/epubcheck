package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMapBuilder;

public abstract class AbstractSchema implements Schema {
  private final PropertyMap properties;

  public AbstractSchema() {
    this(PropertyMap.EMPTY);
  }

  public AbstractSchema(PropertyMap properties) {
    this.properties = properties;
  }

  public AbstractSchema(PropertyMap properties, PropertyId[] supportedPropertyIds) {
    this(filterProperties(properties, supportedPropertyIds));
  }

  public PropertyMap getProperties() {
    return properties;
  }

  static public PropertyMap filterProperties(PropertyMap properties, PropertyId[] supportedPropertyIds) {
    PropertyMapBuilder builder = new PropertyMapBuilder();
    for (int i = 0; i < supportedPropertyIds.length; i++) {
      Object value = properties.get(supportedPropertyIds[i]);
      if (value != null)
        builder.put(supportedPropertyIds[i], value);
    }
    return builder.toPropertyMap();
  }
}
