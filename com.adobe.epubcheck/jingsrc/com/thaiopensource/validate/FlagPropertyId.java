package com.thaiopensource.validate;

import com.thaiopensource.validate.Flag;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMapBuilder;

/**
 * A PropertyId whose value is not significant. Its significance
 * rests purely on whether it is present in a PropertyMap.  The
 * value is constrained to be Flag.PRESENT.
 *
 * @see Flag
 */
public class FlagPropertyId extends PropertyId {
  public FlagPropertyId(String name) {
    super(name, Flag.class);
  }

  /**
   * Adds this property to a PropertyMapBuilder. Modifies
   * the PropertyMapBuilder so that this PropertyId is
   * mapped to Flag.PRESENT.
   *
   * @param builder the PropertyMapBuilder to be modified
   */
  public void add(PropertyMapBuilder builder) {
    builder.put(this, Flag.PRESENT);
  }
}
