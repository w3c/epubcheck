package com.thaiopensource.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.adobe.epubcheck.messages.LocaleHolder;
import com.adobe.epubcheck.messages.LocalizedMessages;
import com.adobe.epubcheck.messages.ResourceResolver;

import java.text.MessageFormat;

/**
 * This is a monkey-patch for Jing's {@link Localizer} class to attempt at
 * making it slightly more locale-aware.
 * 
 * Whenever a message is localized, is uses a {@link ResourceBundle} for the
 * {@code Locale} held as a thread-local static variable in the
 * {@code LocaleHolder} class.
 *
 */
public class Localizer
{
  private final Class<?> cls;
  private final Map<Locale,ResourceBundle> bundles = new HashMap<>();

  public Localizer(Class<?> cls)
  {
    this.cls = cls;
  }

  public String message(String key)
  {
    return MessageFormat.format(getBundle().getString(key), new Object[]{});
  }

  public String message(String key, Object arg)
  {
    return MessageFormat.format(getBundle().getString(key), new Object[] { arg });
  }

  public String message(String key, Object arg1, Object arg2)
  {
    return MessageFormat.format(getBundle().getString(key), new Object[] { arg1, arg2 });
  }

  public String message(String key, Object[] args)
  {
    return MessageFormat.format(getBundle().getString(key), args);
  }

  private ResourceBundle getBundle()
  {
    Locale locale = LocaleHolder.get();
    if (!bundles.containsKey(locale))
    {
      String s = cls.getName();
      int i = s.lastIndexOf('.');
      if (i > 0) {
        s = s.substring(0, i + 1);
      }
      else
      {
        s = "";
      }
      try {
        bundles.put(locale,
                ResourceResolver.toResourceBundle(ResourceResolver.getInstance()
                        .resource2Url(s + "resources.Messages", LocaleHolder.get())));
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    return bundles.get(locale);
  }
}
