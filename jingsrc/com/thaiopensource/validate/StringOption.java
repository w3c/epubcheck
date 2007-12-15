package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;

public class StringOption implements Option {
  private final StringPropertyId pid;

  public StringOption(StringPropertyId pid) {
    this.pid = pid;
  }

  public PropertyId getPropertyId() {
    return pid;
  }

  public Object valueOf(String arg) throws OptionArgumentException {
    if (arg == null)
      return defaultValue();
    return normalize(arg);
  }

  public String defaultValue() throws OptionArgumentPresenceException {
    throw new OptionArgumentPresenceException();
  }

  public String normalize(String value) throws OptionArgumentFormatException {
    return value;
  }

  public Object combine(Object[] values) {
    return null;
  }
}
