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

import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class Messages {

  private static final String BUNDLE_NAME = "com.adobe.epubcheck.util.messages"; //$NON-NLS-1$
  private static final Map<String, Messages> localizedMessages = new HashMap<String, Messages>();
  
  private ResourceBundle bundle;
  private Locale locale;
  
  /**
   * Returns messages localized for the default (host) locale. 
   * @return Messages localized for the default locale.
   */
  public static Messages getInstance()
  {
    return getInstance(null);
  }
  
  /**
   * Get a Messages instance that has been localized for the given locale, or
   * the default locale if locale is null. Note that passing an unknown locale
   * returns the default messages.
   * 
   * @param locale The locale to use for localization of the messages.
   * @return The localized messages or default.
   */
  public static Messages getInstance(Locale locale) 
  {
    Messages instance = null;
    
    if(locale == null) 
    {
      locale = Locale.getDefault();
    }
    
    String localeKey = locale.getLanguage();
    if(localizedMessages.containsKey(localeKey)) 
    {
      instance = localizedMessages.get(localeKey);
    } 
    else 
    {
      synchronized (Messages.class) 
      {
        if (instance == null) 
        {
            instance = new Messages(locale);
            localizedMessages.put(localeKey, instance);
        }
      }
    }
    
    return instance;
 
  }
  
  protected Messages()
  {
      this(null);
  }
  
  protected Messages(Locale locale)
  {
      this.locale = (locale != null) ? locale : Locale.getDefault();
      bundle = ResourceBundle.getBundle(BUNDLE_NAME, this.locale, new UTF8Control());
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

  private class UTF8Control extends Control
  {
    @Override
    public ResourceBundle newBundle
        (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
        throws
        IllegalAccessException,
        InstantiationException,
        IOException
    {
      // The below is a copy of the default implementation.
      String bundleName = toBundleName(baseName, locale);
      String resourceName = toResourceName(bundleName, "properties"); //$NON-NLS-1$
      ResourceBundle bundle = null;
      InputStream stream = null;
      if (reload)
      {
        URL url = loader.getResource(resourceName);
        if (url != null)
        {
          URLConnection connection = url.openConnection();
          if (connection != null)
          {
            connection.setUseCaches(false);
            stream = connection.getInputStream();
          }
        }
      }
      else
      {
        stream = loader.getResourceAsStream(resourceName);
      }
      if (stream != null)
      {
        try
        {
          // Only this line is changed to make it to read properties files as UTF-8.
          bundle = new PropertyResourceBundle(
              new BufferedReader(
                  new InputStreamReader(stream, Charsets.UTF_8)));
        }
        finally
        {
          stream.close();
        }
      }
      return bundle;
    }
  }

}
