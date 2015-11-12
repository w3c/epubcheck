package com.adobe.epubcheck.messages;

import java.util.Locale;

/**
 * This is a dictionary that maps the text of a message to a severity.
 */
public class LocalizedMessageDictionary implements MessageDictionary
{    
  private LocalizedMessages localizedMessages;
  private Locale locale;
  
  /**
   * Convenience constructor will use the default locale.
   */
  public LocalizedMessageDictionary()
  {
      this(null);
  }
  
  /**
   * Generate messages with an explicit locale. 
   * @param locale The locale to localize for. If the locale is not supported 
   * (or null), the default locale will be used instead.
   */
  public LocalizedMessageDictionary(Locale locale)
  {
      this.locale =  (locale != null) ? locale : Locale.getDefault();
      this.localizedMessages = LocalizedMessages.getInstance(locale);
  }
  
  /**
   * Returns the locale being used by this class for localization of the messages.
   * @return Locale in use.
   */
  public Locale getLocale()
  {
    return locale;
  }

  @Override
  public Message getMessage(MessageId id)
  {
    return localizedMessages.getMessage(id);
  }

  
}
