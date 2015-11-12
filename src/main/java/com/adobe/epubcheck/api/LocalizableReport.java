package com.adobe.epubcheck.api;

import java.util.Locale;

/**
 * Extends the {@link Report} interface with a method to configure the locale
 * used to report messages.
 */
public interface LocalizableReport extends Report
{

  /**
   * Sets the locale to use in the report's messages
   */
  public void setLocale(Locale locale);
  
  /**
   * Gets the locale to use in the report's messages
   */
  public Locale getLocale();
}
