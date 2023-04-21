package org.w3c.epubcheck.test;

import java.util.Locale;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ReportingLevel;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

public class TestConfiguration
{


  public enum CheckerMode
  {
    EPUB,
    MEDIA_OVERLAYS_DOC,
    NAVIGATION_DOC,
    PACKAGE_DOC,
    SVG_CONTENT_DOC,
    XHTML_CONTENT_DOC;

    public static CheckerMode fromExtension(String path)
    {
      int index = path.lastIndexOf(".");
      String ext = (index > 0) ? path.substring(index + 1) : "epub";
      switch (ext)
      {
      case "opf":
        return PACKAGE_DOC;
      case "xhtml":
      case "html":
      case "htm":
        return XHTML_CONTENT_DOC;
      case "svg":
        return SVG_CONTENT_DOC;
      case "smil":
        return MEDIA_OVERLAYS_DOC;
      case "epub":
      default:
        return EPUB;
      }
    }
  }

  private TestReport report = new TestReport();
  private String basepath = "";
  private EPUBVersion version = EPUBVersion.VERSION_3;
  private CheckerMode mode = null;
  private EPUBProfile profile = EPUBProfile.DEFAULT;
  private Locale defaultLocale = Locale.ENGLISH;

  public ValidationContextBuilder getContextBuilder()
  {
    return ValidationContext.test().report(report).version(version).profile(profile);
  }

  @Before("@debug")
  public void beforeDebug()
  {
    report.setVerbose(true);
  }

  @After("@debug")
  public void afterDebug()
  {
    report.setVerbose(false);
  }

  public String getBasepath()
  {
    return basepath;
  }

  public Locale getDefaultLocale()
  {
    return defaultLocale;
  }

  public EPUBVersion getEPUBVersion()
  {
    return version;
  }

  public CheckerMode getMode()
  {
    return mode;
  }

  public EPUBProfile getProfile()
  {
    return profile;
  }

  public TestReport getReport()
  {
    return report;
  }

  public EPUBVersion getVersion()
  {
    return version;
  }

  @Given("EPUBCheck with default settings")
  public void configureDefaults()
  {
    // nothing to do
  }

  @And("(EPUB )test files located at {string}")
  public void setBasepath(String basepath)
  {
    this.basepath = basepath;
  }

  @And("(the )default locale (is )set to ('){locale}(')")
  public void setDefaultLocale(Locale defaultLocale)
  {
    this.defaultLocale = defaultLocale;
  }

  @And("EPUBCheck configured to check EPUB {version} rules")
  public void setEPUBVersion(EPUBVersion version)
  {
    this.version = version;
  }

  @And("EPUBCheck configured to check a(n) {checkerMode}")
  public void setMode(CheckerMode mode)
  {
    this.mode = mode;
  }

  @And("EPUBCheck configured with the ('){profile}(') profile")
  public void setProfile(EPUBProfile profile)
  {
    this.profile = profile;
  }
  
  @And("(the )reporting format (is )set to {}")
  public void setReportingFormat(String format)
  {
    report.setReportingFormat(format);
  }

  @And("(the )reporting level (is )set to {severity}")
  public void setReportingLevel(Severity severity)
  {

    report.setReportingLevel(ReportingLevel.getReportingLevel(severity));
  }

  @And("(the )reporting locale (is )set to ('){locale}(')")
  public void setReportingLocale(Locale locale)
  {
    report.setLocale(locale);
  }

}
