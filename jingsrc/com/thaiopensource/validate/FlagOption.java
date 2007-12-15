package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;

public class FlagOption implements Option {
  private final FlagPropertyId pid;
  public FlagOption(FlagPropertyId pid) {
    this.pid = pid;
  }

  public PropertyId getPropertyId() {
    return pid;
  }

  public Object valueOf(String arg) throws OptionArgumentException {
    if (arg != null)
      throw new OptionArgumentPresenceException();
    return Flag.PRESENT;
  }

  public Object combine(Object[] values) {
    return null;
  }
}
