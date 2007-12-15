package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;

/**
 * A PropertyId whose value is constrained to be an instance of
 * String.
 *
 * @see String
 */

public class StringPropertyId extends PropertyId {
   public StringPropertyId(String name) {
      super(name, String.class);
    }

  /**
   * Returns the value of the property.  This is a typesafe
   * version of <code>PropertyMap.get</code>.
   *
   * @param properties the PropertyMap to be used
   * @return the String to which the PropertyMap maps this PropertyId,
   * or <code>null</code> if this PropertyId is not in the PropertyMap
   * @see com.thaiopensource.util.PropertyMap#get
   */
  public String get(PropertyMap properties) {
    return (String)properties.get(this);
  }

  /**
   * Sets the value of the property. Modifies the PropertyMapBuilder
   * so that this PropertyId is mapped to the specified value. This
   * is a typesafe version of PropertyMapBuilder.put.
   *
   * @param builder the PropertyMapBuilder to be modified
   * @param value the String to which this PropertyId is to be mapped
   * @return the String to which this PropertyId was mapped before,
   * or <code>null</code> if it was not mapped
   *
   * @see com.thaiopensource.util.PropertyMapBuilder#put
   */
  public String put(PropertyMapBuilder builder, String value) {
    return (String)builder.put(this, value);
  }
}
