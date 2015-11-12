package com.adobe.epubcheck.messages;

import java.util.Locale;

/**
 * Holds the "currently used" {@code Locale} in a static thread-local variable.
 * 
 * Pieces of code that set or change the locale used in the application runtime
 * should update the static locale stored in this class. See for instance how it
 * is done in the {@code MasterReport} implementation.
 *
 */
public final class LocaleHolder
{
  private static final ThreadLocal<Locale> current = new InheritableThreadLocal<Locale>();

  public static void set(final Locale locale)
  {
    current.set(locale);
  }

  public static Locale get()
  {
    Locale locale = current.get();
    if (locale == null)
    {
      locale = Locale.getDefault();
    }
    return locale;
  }

  private LocaleHolder()
  {
  }
}
