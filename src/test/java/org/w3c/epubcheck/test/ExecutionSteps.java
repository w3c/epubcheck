package org.w3c.epubcheck.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.w3c.epubcheck.core.Checker;
import org.w3c.epubcheck.test.TestConfiguration.CheckerMode;
import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.nav.NavChecker;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.ops.OPSChecker;
import com.adobe.epubcheck.overlay.OverlayChecker;
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FileResourceProvider;

import io.cucumber.java.en.When;

public class ExecutionSteps
{

  private final TestConfiguration configuration;

  public ExecutionSteps(TestConfiguration configuration)
  {
    this.configuration = configuration;
  }

  @When("checking EPUB/file/document {string}")
  public void check(String path)
  {
    Locale oldDefaultLocale = Locale.getDefault();
    try
    {
      // Complete configuration and get the test file 
      Locale.setDefault(configuration.getDefaultLocale());
      if (configuration.getMode() == null)
      {
        configuration.setMode(CheckerMode.fromExtension(path));
      }
      File testFile = getEPUBFile(configuration.getBasepath() + path);
      
      // Initialize the report
      configuration.getReport().setEpubFileName(testFile.getAbsolutePath());
      configuration.getReport().initialize();
      
      // Create the checker and run checks
      Checker checker = getChecker(testFile);
      checker.check();
      
      // Finalize the report
      configuration.getReport().generate();
    } finally
    {
      Locale.setDefault(oldDefaultLocale);
    }
  }

  private Checker getChecker(File file)
  {

    ValidationContextBuilder contextBuilder = configuration.getContextBuilder()
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
      return new EpubCheck(file, configuration.getReport(), configuration.getProfile());
    }
  }

  private File getEPUBFile(String path)
  {
    try
    {
      URL url = this.getClass().getResource(path);
      assertThat("Couldn’t find EPUB: " + path, url, is(notNullValue()));
      File file = new File(url.toURI());
      if (file.isDirectory())
      {
        Archive epub = new Archive(file.getPath());
        epub.createArchive();
        return epub.getEpubFile();
      }
      else
      {
        return file;
      }
    } catch (URISyntaxException e)
    {
      throw new IllegalStateException(e);
    }
  }
}
