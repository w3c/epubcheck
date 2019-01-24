package com.adobe.epubcheck.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.WriterReportImpl;

/**
 * Test the various constructors for the EpubCheck Object.
 */
public class api_Test
{
  
  private Locale defaultLocale;
  
  @Before
  public void before() throws Exception
  {
    defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.ENGLISH);
  }

  @After
  public void after() throws Exception
  {
    Locale.setDefault(defaultLocale);
  }
  
  @Test
  public void EpubCheck1_Test() throws Exception
  {
    File epub = getTestEpub();
    EpubCheck check = new EpubCheck(epub);
    Assert.assertEquals("The file should have generated errors.", 2, 2 & check.doValidate());
  }

  @Test
  public void EpubCheck_PrintWriter_Test() throws Exception
  {
    try {
      File epub = getTestEpub();
      URL expectedUrl = common.class.getResource("api");
      String outputPath = new File(expectedUrl.toURI()).getAbsolutePath();
      File actualResults = new File(outputPath + "/PrintWriter_Actual.txt");
      File expectedResults = new File(outputPath + "/PrintWriter_Expected.txt");
      FileOutputStream outputStream = new FileOutputStream(actualResults);
      PrintWriter out = new PrintWriter(outputStream);
      EpubCheck check = new EpubCheck(epub, out);
      Assert.assertEquals("The file should have generated errors.", 2, 2 & check.doValidate());
      out.flush();
      outputStream.close();
      out.close();
      Assert.assertTrue("The resulting file doesn't exist.", actualResults.exists());
      Assert.assertTrue("The expected file doesn't exist.", expectedResults.exists());
      common.compareText(expectedResults, actualResults);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Cannot find test file", e);
    }
  }

  @Test
  public void EpubCheck_InputStream_Test() throws Exception
  {
    try {
      File epub = getTestEpub();
      URL expectedUrl = common.class.getResource("api");
      String outputPath = new File(expectedUrl.toURI()).getAbsolutePath();
      File actualResults = new File(outputPath + "/InputStream_Actual.txt");
      File expectedResults = new File(outputPath + "/InputStream_Expected.txt");
      FileOutputStream outputStream = new FileOutputStream(actualResults);
      PrintWriter out = new PrintWriter(outputStream);

      FileInputStream epubStream = new FileInputStream(epub);
      Report report = new WriterReportImpl(out, "Testing 123");
      EpubCheck check = new EpubCheck(epubStream, report, epub.getPath());
      Assert.assertEquals("The file should have generated errors.", 2, 2 & check.doValidate());
      out.flush();
      outputStream.close();
      out.close();
      epubStream.close();
      Assert.assertTrue("The resulting file doesn't exist.", actualResults.exists());
      Assert.assertTrue("The expected file doesn't exist.", expectedResults.exists());
      common.compareText(expectedResults, actualResults);
    } catch (URISyntaxException e) {
    	throw new IllegalStateException("Cannot find test file", e);
    }
  }

  private File getTestEpub()
  {
    try {
      URL inputUrl = common.class.getResource("../../../../30/epub/invalid/font_no_fallback.epub");
      String inputPath = new File(inputUrl.toURI()).getAbsolutePath();
      File epub = new File(inputPath);
      Assert.assertTrue("Couldn't find resource: " + inputPath, epub.exists());
      return epub;
    } catch (URISyntaxException e) {
  	  throw new IllegalStateException("Cannot find test file", e);
    }
  }
}
