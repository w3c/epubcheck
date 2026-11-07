package org.w3c.epubcheck.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.w3c.epubcheck.core.Checker;
import org.w3c.epubcheck.test.EPUBCheckConfiguration.CheckerMode;
import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.nav.NavChecker;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.ops.OPSChecker;
import com.adobe.epubcheck.overlay.OverlayChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FileResourceProvider;

import io.cucumber.java.en.When;

public class ExecutionSteps
{

  // some messages contain a random UUID
  private static final Pattern UUID = Pattern
      .compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  private final TestEnvironment environment;
  private final EPUBCheckConfiguration configuration;

  public ExecutionSteps(TestEnvironment environment, EPUBCheckConfiguration configuration)
  {
    this.environment = environment;
    this.configuration = configuration;
  }

  @When("checking EPUB/file/document/directory/file\\ set {string}")
  public void check(String path)
  {
    Locale oldDefaultLocale = Locale.getDefault();
    try
    {
      // Complete configuration and get the test file
      Locale.setDefault(environment.getDefaultLocale());
      if (configuration.getMode() == null)
      {
        configuration.setMode(CheckerMode.fromExtension(path));
      }
      File testFile = getEPUBFile(environment.getBasepath() + path);

      runCheck(testFile, configuration.getReport());

      // compare with sequential results (-Depubcheck.test.parity=true)
      if (Boolean.getBoolean("epubcheck.test.parity"))
      {
        assertParity(testFile, configuration.getReport());
      }
    } finally
    {
      Locale.setDefault(oldDefaultLocale);
    }
  }

  private void runCheck(File testFile, TestReport report)
  {
    report.setEpubFileName(testFile.getAbsolutePath());
    report.initialize();
    Checker checker = getChecker(testFile, report);
    checker.check();
    report.generate();
  }

  private void assertParity(File testFile, TestReport source)
  {
    String previousJobs = System.getProperty("epubcheck.jobs");
    try
    {
      System.setProperty("epubcheck.jobs", "1");
      TestReport sequential = copySettings(source);
      runCheck(testFile, sequential);

      System.setProperty("epubcheck.jobs", "4");
      TestReport concurrent = copySettings(source);
      runCheck(testFile, concurrent);

      assertEquals("sequential and concurrent diagnostics differ for " + testFile,
          messages(sequential, true), messages(concurrent, true));
      assertEquals("sequential and concurrent features differ for " + testFile,
          messages(sequential, false), messages(concurrent, false));
    } finally
    {
      if (previousJobs == null)
      {
        System.clearProperty("epubcheck.jobs");
      }
      else
      {
        System.setProperty("epubcheck.jobs", previousJobs);
      }
    }
  }

  private static TestReport copySettings(TestReport source)
  {
    TestReport copy = new TestReport();
    copy.setMaxOfEachMessage(source.getMaxOfEachMessage());
    copy.setReportingLevel(source.getReportingLevel());
    copy.setLocale(source.getLocale());
    return copy;
  }

  private static List<String> messages(TestReport report, boolean diagnostics)
  {
    List<String> messages = new ArrayList<>();
    for (MessageInfo message : report.getAllMessages())
    {
      if ((message.getId() != null) == diagnostics)
      {
        messages.add(UUID.matcher(message.toString()).replaceAll("UUID"));
      }
    }
    return messages;
  }

  private Checker getChecker(File file, TestReport report)
  {

    ValidationContextBuilder contextBuilder = configuration.getContextBuilder(report)
        .url(URLUtils.toURL(file)).resourceProvider(new FileResourceProvider(file));

    switch (configuration.getMode())
    {
    case MEDIA_OVERLAYS_DOC:
      return new OverlayChecker(
          contextBuilder.mimetype("application/smil+xml").version(EPUBVersion.VERSION_3).build());
    case NAVIGATION_DOC:
      return new NavChecker(
          contextBuilder.mimetype("application/xhtml+xml").version(EPUBVersion.VERSION_3).build());
    case PACKAGE_DOC:
      ValidationContext context = contextBuilder.mimetype("application/oebps-package+xml").build();
      return (context.version == EPUBVersion.VERSION_2) ? new OPFChecker(context)
          : new OPFChecker30(context);
    case SVG_CONTENT_DOC:
      return new OPSChecker(contextBuilder.mimetype("image/svg+xml").build());
    case XHTML_CONTENT_DOC:
      return new OPSChecker(contextBuilder.mimetype("application/xhtml+xml").build());
    case EPUB:
    default:
      return new EpubCheck(file, report, configuration.getProfile());
    }
  }

  private File getEPUBFile(String path)
  {
    try
    {
      URL url = this.getClass().getResource(path);
      assertThat("Couldn’t find EPUB: " + path, url, is(notNullValue()));
      return new File(url.toURI());
    } catch (URISyntaxException e)
    {
      throw new IllegalStateException(e);
    }
  }
}
