package com.thaiopensource.validate;

/**
 * A class with a unique value, used as the value of properties whose
 * significance is purely whether or not they are present in the
 * PropertyMap.
 */
public class Flag {
  private Flag() { }
  /**
   * The unique value of this class.
   */
  public static final Flag PRESENT = new Flag();
}
