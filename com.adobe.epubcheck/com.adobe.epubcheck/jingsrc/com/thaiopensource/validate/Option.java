package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;

public interface Option {
  PropertyId getPropertyId();
  Object valueOf(String arg) throws OptionArgumentException;
  /**
   * Combines multiple values of an option into a single value.  A property
   * whose value is logically a sequence may be represented by multiple options
   * each representing a single member of the sequence.
   *
   * @param values an array of values to be combined
   * @return an Object representing the combination, or <code>null</code>
   * if they cannot be combined
   */
  Object combine(Object[] values);
}
