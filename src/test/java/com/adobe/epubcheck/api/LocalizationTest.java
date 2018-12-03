package com.adobe.epubcheck.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.util.FeatureEnum;

public class LocalizationTest
{

  private final static String BASEPATH = "/30/epub/";

  private Locale systemLocale;

  @Before
  public void before()
  {
    systemLocale = Locale.getDefault();
  }

  @After
  public void after()
  {
    Locale.setDefault(systemLocale);
  }

  @Test
  public void testDefaultLocale()
  {
    Locale.setDefault(Locale.ENGLISH);
    TestReport result = check("invalid/lorem-csserror.epub", null);
    assertTrue(result.getFirstErrorMessage().contains("error"));
  }

  @Test
  public void testSetLocale()
  {
    TestReport result = check("invalid/lorem-csserror.epub", Locale.FRENCH);
    assertTrue(result.getFirstErrorMessage().contains("erreur"));
  }
 
  @Test
  public void testNonEnglishDefault()
  {
    Locale.setDefault(Locale.FRENCH);
    TestReport result = check("invalid/lorem-csserror.epub", Locale.ENGLISH);
    assertTrue(result.getFirstErrorMessage().contains("error"));
  }
  
  @Test
  public void testCSSMessages() {
    TestReport result = check("invalid/lorem-csserror.epub", Locale.FRENCH);
    assertTrue(result.getFirstErrorMessage().contains("endroit"));
  }
  
  @Test
  public void testJingMessages() {
    TestReport result = check("invalid/lorem-xht-rng-1.epub", Locale.FRENCH);
    assertTrue(result.getFirstErrorMessage().contains("balise"));
  }
  
  @Test
  public void testJingLocaleIsReset() {
    Locale.setDefault(Locale.ENGLISH);
    check("invalid/lorem-xht-rng-1.epub", Locale.FRENCH);
    TestReport result = check("invalid/lorem-xht-rng-1.epub", null);
    assertFalse(result.getFirstErrorMessage().contains("balise"));
    assertTrue(result.getFirstErrorMessage().contains("allowed"));
  }
  
  private TestReport check(String path, Locale locale)
  {
    File testFile;
    try
    {
      URL url = this.getClass().getResource(BASEPATH + path);
      URI uri = url.toURI();
      testFile = new File(uri);
    } catch (URISyntaxException e)
    {
      throw new IllegalStateException("Cannot find test file", e);
    }
    TestReport report = new TestReport();
    EpubCheck checker = new EpubCheck(testFile, report);
    if (locale != null) checker.setLocale(locale);
    checker.validate();
    return report;
  }

  private static class TestReport extends MasterReport
  {

    private String firstErrorMessage = null;

    public String getFirstErrorMessage()
    {
      return firstErrorMessage;
    }

    @Override
    public void message(Message message, EPUBLocation location, Object... args)
    {
      switch (message.getSeverity())
      {
      case ERROR:
        if (firstErrorMessage == null) firstErrorMessage = message.getMessage(args);
        break;
      default:
        break;
      }
    }

    @Override
    public void info(String resource, FeatureEnum feature, String value)
    {
    }

    @Override
    public int generate()
    {
      return 0;
    }

    @Override
    public void initialize()
    {
    }

  }
}
