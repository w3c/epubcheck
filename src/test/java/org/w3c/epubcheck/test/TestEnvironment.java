package org.w3c.epubcheck.test;

import java.util.Locale;

import io.cucumber.java.en.And;

public class TestEnvironment
{

  private String basepath = "";
  private Locale defaultLocale = Locale.ENGLISH;


  public String getBasepath()
  {
    return basepath;
  }

  public Locale getDefaultLocale()
  {
    return defaultLocale;
  }

  @And("(the )(EPUB )test files (are)located at {string}")
  public void setBasepath(String basepath)
  {
    this.basepath = basepath;
  }

  @And("(the )default locale (is )(set to )('){locale}(')")
  public void setDefaultLocale(Locale defaultLocale)
  {
    this.defaultLocale = defaultLocale;
  }

}
