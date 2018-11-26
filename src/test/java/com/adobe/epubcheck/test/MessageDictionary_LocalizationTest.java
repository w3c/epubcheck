package com.adobe.epubcheck.test;

import com.adobe.epubcheck.messages.LocalizedMessageDictionary;
import com.adobe.epubcheck.messages.MessageId;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

public class MessageDictionary_LocalizationTest {
  
  @Test
  public void ensureDefault() {
    LocalizedMessageDictionary messages = new LocalizedMessageDictionary();
    Locale locale = messages.getLocale();
    Locale defaultLocale = Locale.getDefault();
    Assert.assertEquals("Default constructor should provide default locale.",
            locale, defaultLocale);
  }
  
  @Test
  public void ensureDefaultWithAlteredHostLocale() {
    
    Locale newLocale = new Locale("es");
    Locale previousLocale = Locale.getDefault();
    try {
      Locale.setDefault(newLocale);
      
      // should return the default localization
      LocalizedMessageDictionary messages = new LocalizedMessageDictionary(); 
      Locale locale = messages.getLocale();
      Locale defaultLocale = Locale.getDefault();
      Assert.assertEquals( "Messages with default locale should use host Locale.",
          locale, defaultLocale);
      
      System.out.format( "Locales are: %s & %s\n", locale.getLanguage(), defaultLocale.getLanguage());
      
      LocalizedMessageDictionary messagesWithExplicitLocale = new LocalizedMessageDictionary(newLocale);
      
      // Messages should be the same for default locale and explicitly set locale
      Assert.assertEquals( "Messages using an explicit locale same as default should be the same.",
          messagesWithExplicitLocale.getMessage(MessageId.ACC_001).getMessage(), 
          messages.getMessage(MessageId.ACC_001).getMessage());
    } finally {
      // Reset the global host JVM locale
      Locale.setDefault(previousLocale);
    }
  }
  
  @Test
  public void ensureMessagesAreLocalized() {

    // Caution, test is currently brittle
    
    Locale locale = new Locale("it");
    LocalizedMessageDictionary messages = new LocalizedMessageDictionary(locale);
    Assert.assertEquals( "Using Italian locale, message should be Italian (and correct).",
            "Le risorse di tipo XML (o derivate da XML) devono essere documenti XML 1.0 validi. È stata trovata la versione XML: '%1$s'.", 
            messages.getMessage(MessageId.HTM_001).getMessage());
            
    // Test it again for another locale.
    locale = new Locale("es");
    messages = new LocalizedMessageDictionary(locale);
    Assert.assertEquals( "Using Spanish locale, message should be Spanish.",
            "Los recursos con media type basados en XML deben ser documentos XML 1.0 válidos. La versión XML utilizada es: %1$s.", 
            messages.getMessage(MessageId.HTM_001).getMessage());

    
  }
  
  @Test
  public void ensureMessageStringsAreCachedForDefaultLocale() {
    
    LocalizedMessageDictionary m1 = new LocalizedMessageDictionary();
    LocalizedMessageDictionary m2 = new LocalizedMessageDictionary();
    
    // Check reference identity of the two messages to ensure they're cached
    Assert.assertTrue("Message objects should be cached for the default locale.",
            m1.getMessage(MessageId.MED_001) == m2.getMessage(MessageId.MED_001));
    // Check reference identity for the strings as well...
    Assert.assertSame("Message strings should also be cached for the default locale.",
            m1.getMessage(MessageId.MED_001).getMessage(),
            m2.getMessage(MessageId.MED_001).getMessage());
    
  }
  
    @Test
  public void ensureMessageStringsAreCachedForExplicitLocale() {
    
    LocalizedMessageDictionary m1 = new LocalizedMessageDictionary(new Locale("es"));
    LocalizedMessageDictionary m2 = new LocalizedMessageDictionary(new Locale("es"));
    
    // Let's check non-default locale as well.
    // Check reference identity of the two message strings
    Assert.assertTrue("Message objects should be cached for an explicit locale.",
            m1.getMessage(MessageId.ACC_001) == m2.getMessage(MessageId.ACC_001));
    Assert.assertSame("Message strings should also be cached for an explicit locale.",
            m1.getMessage(MessageId.ACC_001).getMessage(), 
            m2.getMessage(MessageId.ACC_001).getMessage());
  
  }
  

  
}
