/*
 * Copyright (c) 2011 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.util;

import com.adobe.epubcheck.messages.ResourceResolver;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages
{

  private static final String BUNDLE_NAME = "com.adobe.epubcheck.util.messages";
  private static final Table<String, Locale, Messages> messageTable = HashBasedTable.create();
  
  private ResourceBundle bundle;
  private Locale locale;
  
  /**
   * Returns messages localized for the default (host) locale. 
   * @return Messages localized for the default locale.
   */
  public static Messages getInstance()
  {
    return getInstance(null, null);
  }

  /**
   * Get a Messages instance that has been localized for the given locale, or the
   * default locale if locale is null. Note that passing an unknown locale returns
   * the default messages.
   * 
   * @param locale
   *          The locale to use for localization of the messages.
   * @return The localized messages or default.
   */
  public static Messages getInstance(Locale locale)
  {
    return getInstance(locale, null);
  }
  
  /**
   * Get a Messages instance that has been localized for the given locale, or
   * the default locale if locale is null. Note that passing an unknown locale
   * returns the default messages.
   * 
   * @param locale
   *          The locale to use for localization of the messages.
   * @return The localized messages or default.
   */
  public static Messages getInstance(Locale locale, Class<?> cls)
  {
    Messages instance = null;
    locale = (locale == null) ? Locale.getDefault() : locale;
    
    String bundleKey = (cls==null)? BUNDLE_NAME : getBundleName(cls);   
    if (messageTable.contains(bundleKey, locale)) {
      instance = messageTable.get(bundleKey, locale);
    } 
    else 
    {
      synchronized (Messages.class) 
      {
        if (instance == null) 
        {
          instance = new Messages(locale, bundleKey);
          messageTable.put(bundleKey, locale, instance);
        }
      }
    }
    
    return instance;
 
  }
  
  private static String getBundleName(Class<?> cls) {
    String className = cls.getName();
    int i = className.lastIndexOf('.');
    return ((i > 0) ? className.substring(0, i + 1) : "") + "messages";
  }

  protected Messages()
  {
      this(null);
  }
  
  protected Messages(Locale locale)
  {
    this(locale, BUNDLE_NAME);
  }

  protected Messages(Locale locale, String bundleName)
  {
    this.locale = (locale != null) ? locale : Locale.getDefault();
    try {
      this.bundle = ResourceResolver.toResourceBundle(
              ResourceResolver.getInstance().resource2Url(bundleName, this.locale));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public String get(String key)
  {
    try
    {
      return bundle.getString(key);
    }
    catch (MissingResourceException e)
    {
      return key;
    }
  }

  public String get(String key, Object... arguments)
  {
      return MessageFormat.format(get(key), arguments);
  }
  
  public Locale getLocale()
  {
    return locale;
  }

}
