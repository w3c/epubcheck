package com.adobe.epubcheck.test;

import com.adobe.epubcheck.util.Messages;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UtilMessage_LocalizationTest {
  
  private static final String NO_ERRORS_OR_WARNINGS = "no_errors__or_warnings";
  
  @Before
  public void setUp() {
      
  }
  
  @Test
  public void ensureDefault() {
    Messages messages = Messages.getInstance(); // should return the default localization
    Locale locale = messages.getLocale();
    Locale defaultLocale = Locale.getDefault();
    Assert.assertEquals( "Default constructor should use the (host) default locale.",
            locale, defaultLocale);
  }
  
  @Test
  public void ensureNullIsDefault() {
    Messages messages = Messages.getInstance(null); // should return the default localization
    Locale locale = messages.getLocale();
    Locale defaultLocale = Locale.getDefault();
    Assert.assertEquals( "Null locale should use (host) default locale.",
            locale, defaultLocale);
  }
  
  @Test
  public void ensureDefaultWithAlteredHostLocale() {
    
    Locale newLocale = new Locale("es");
    Locale previousLocale = Locale.getDefault();
    try {
      Locale.setDefault(newLocale);
      
      Messages messages = Messages.getInstance(); // should return the default localization
      Locale locale = messages.getLocale();
      Locale defaultLocale = Locale.getDefault();
      Assert.assertEquals( "After setting host locale, default constructor should use new host default locale.",
          locale, defaultLocale);
      
      Messages messagesWithExplicitLocale = Messages.getInstance(newLocale);
      
      // Messages should be the same for default locale and explicitly set locale
      Assert.assertEquals( "Messages should match if explicit and host locales are the same.",
          messagesWithExplicitLocale.get(NO_ERRORS_OR_WARNINGS), 
          messages.get(NO_ERRORS_OR_WARNINGS));
    } finally {
      // Reset the global host JVM locale
      Locale.setDefault(previousLocale);
    }
  }
  
  @Test
  public void ensureMessagesAreLocalized() {
    
    Locale locale = new Locale("it");
    Messages messages = Messages.getInstance(locale);
    Assert.assertEquals("Using Italian locale, messages should be in Italian.",
            "Non sono stati trovati errori o potenziali errori.", messages.get(NO_ERRORS_OR_WARNINGS));
    
    // Same test, but let's do it again for another locale.
    locale = new Locale("es");
    messages = Messages.getInstance(locale);
    Assert.assertEquals("Using Spanish locale, messages should be in Spanish.",
            "No se han detectado errores o advertencias.", messages.get(NO_ERRORS_OR_WARNINGS));
    
  }
  
  @Test
  public void ensureMessagesAreCached() {
    
    Messages m1 = Messages.getInstance();
    Messages m2 = Messages.getInstance();
    Assert.assertTrue("Message objects should be the same reference (cached).",
            m1 == m2);
    
    m1 = Messages.getInstance(new Locale("es"));
    m2 = Messages.getInstance(new Locale("es"));
    Assert.assertTrue("Message objects with explicit localization should be same reference (cached).",
            m1 == m2);
    
  }
  
}
