package com.thaiopensource.util;

import java.util.ResourceBundle;
import java.text.MessageFormat;

public class Localizer {
  private final Class cls;
  private ResourceBundle bundle;

  public Localizer(Class cls) {
    this.cls = cls;
  }

  public String message(String key) {
    return MessageFormat.format(getBundle().getString(key), new Object[]{});
  }

  public String message(String key, Object arg) {
    return MessageFormat.format(getBundle().getString(key),
				new Object[]{arg});
  }

  public String message(String key, Object arg1, Object arg2) {
    return MessageFormat.format(getBundle().getString(key),
				new Object[]{arg1, arg2});
  }

  public String message(String key, Object[] args) {
    return MessageFormat.format(getBundle().getString(key), args);
  }

  private ResourceBundle getBundle() {
    if (bundle == null){
      String s = cls.getName();
      int i = s.lastIndexOf('.');
      if (i > 0)
	s = s.substring(0, i + 1);
      else
	s = "";
      bundle = ResourceBundle.getBundle(s + "resources.Messages");
    }
    return bundle;
  }
}
