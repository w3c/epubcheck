package com.adobe.epubcheck.test;

import com.adobe.epubcheck.messages.LocalizedMessageDictionary;
import com.adobe.epubcheck.messages.MessageDictionary;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.OverriddenMessageDictionary;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.DefaultReportImpl;
import java.io.File;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

public class OverriddenMessageDictionaryTest
{

  @Test
  public void ensureOverridenMessages()
  {
    String testName = "severity_overrideOk.txt";
    URL inputUrl = common.class.getResource("command_line");
    String inputPath = inputUrl.getPath();
    String overrideFile = inputPath + "/" + testName;

    MessageDictionary md = new LocalizedMessageDictionary();
    OverriddenMessageDictionary omd
            = new OverriddenMessageDictionary(new File(overrideFile), new DefaultReportImpl("test"));

    Assert.assertTrue("Message should have been overridden.",
            omd.getMessage(MessageId.CSS_012).getMessage()
            .contains("This is an overridden message"));
    Assert.assertTrue("Suggestion should be overridden.",
            omd.getMessage(MessageId.CSS_012).getSuggestion()
            .contains("This is an overridden suggestion."));
    Assert.assertEquals("Severity should be overridden.",
            omd.getMessage(MessageId.ACC_015).getSeverity(),
            Severity.USAGE);
    
    Assert.assertFalse("Severity should be different from default.",
            omd.getMessage(MessageId.CSS_021).getSeverity()
            == md.getMessage(MessageId.CSS_021).getSeverity());
    Assert.assertTrue("Message should not be null or empty.",
            omd.getMessage(MessageId.CSS_021).getMessage().isEmpty() == false
            && omd.getMessage(MessageId.CSS_021).getMessage() != null);
    Assert.assertTrue("Message should still match default.",
            omd.getMessage(MessageId.CSS_021).getMessage()
            .contains(md.getMessage(MessageId.CSS_021).getMessage()));
    Assert.assertEquals("Non-overridden message should use default message.",
            omd.getMessage(MessageId.MED_001).getMessage(),
            md.getMessage(MessageId.MED_001).getMessage());

  }
}
