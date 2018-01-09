package org.w3.epubcheck.api;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.test.common;
import com.adobe.epubcheck.util.WriterReportImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Test the various constructors for the EpubCheck Object.
 */
public class ApiConstructorsTest
{

  /**
   * Checking if the standard API constructor can take an epub and validate it.
   *
   * @throws Exception
   */
  @Test
  public void StandardConstructorTest() throws Exception
  {
    File epub = getTestEpub();
    EpubCheck check = new EpubCheck(epub);
    Assert.assertEquals("The file should have no errors.", 0, check.doValidate());
  }

  /**
   * Checking if we can apply a PrintWriter to the constructor and get the expected output written
   * to the supplied PrintWriter.
   *
   * @throws Exception
   */
  @Test
  public void PrintWriterConstructorTest() throws Exception
  {
    try {
      File epub = getTestEpub();
      URL expectedUrl = this.getClass().getResource("");
      String outputPath = new File(expectedUrl.toURI()).getAbsolutePath();
      File actualResults = new File(outputPath + "/PrintWriter_Actual.txt");
      File expectedResults = new File(outputPath + "/PrintWriter_Expected.txt");
      FileOutputStream outputStream = new FileOutputStream(actualResults);
      PrintWriter out = new PrintWriter(outputStream);
      EpubCheck check = new EpubCheck(epub, out);
      Assert.assertEquals("The file should have no errors.", 0, check.doValidate());
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

  /**
   * Checking if we can stream a epub to the constructor and use a Report object to
   * summarize the output.
   *
   * @throws Exception
   */
  @Test
  public void InputStreamConstructorTest() throws Exception
  {
    try {
      File epub = getTestEpub();
      URL expectedUrl = this.getClass().getResource("");
      String outputPath = new File(expectedUrl.toURI()).getAbsolutePath();
      File actualResults = new File(outputPath + "/InputStream_Actual.txt");
      File expectedResults = new File(outputPath + "/InputStream_Expected.txt");
      FileOutputStream outputStream = new FileOutputStream(actualResults);
      PrintWriter out = new PrintWriter(outputStream);

      FileInputStream epubStream = new FileInputStream(epub);
      Report report = new WriterReportImpl(out, "Testing 123");
      EpubCheck check = new EpubCheck(epubStream, report, epub.getPath());
      Assert.assertEquals("The file should have generated errors.", 0, check.doValidate());
      out.flush();
      outputStream.close();
      out.close();
      epubStream.close();
      Assert.assertEquals("Errors reported", 0, report.getErrorCount());
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
      URL inputUrl = this.getClass().getResource("../../../../minimal-epub/30/minimal-epub-30.epub");
      String inputPath = new File(inputUrl.toURI()).getAbsolutePath();
      File epub = new File(inputPath);
      Assert.assertTrue("Couldn't find resource: " + inputPath, epub.exists());
      return epub;
    } catch (URISyntaxException e) {
  	  throw new IllegalStateException("Cannot find test file", e);
    }
  }
}
