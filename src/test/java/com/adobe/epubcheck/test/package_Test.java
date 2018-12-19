package com.adobe.epubcheck.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFMockPackage;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.reporting.CheckingReport;
import com.adobe.epubcheck.test.common.TestOutputType;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidationReport;

import junit.framework.Assert;

public class package_Test
{
  private SecurityManager originalManager;
  private Locale defaultLocale;

  @Before
  public void setUp()
    throws Exception
  {
    defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.ENGLISH);
    originalManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown()
    throws Exception
  {
    System.setSecurityManager(this.originalManager);
    Locale.setDefault(defaultLocale);
  }

  @Test
  public void Missing_toc_file_Test()
    throws Exception
  {
    runPackageJsonTest("missing_toc_file", 1);
  }

  @Test
  public void Missing_ncx_file_Test()
    throws Exception
  {
    runPackageJsonTest("missing_ncx_file", 1);
  }

  @Test
  public void missing_mimetype_file_epub3_Test()
    throws Exception
  {
    runPackageJsonTest("missing_mimetype_file_epub3", 1);
  }

  @Test
  public void missing_mimetype_file_epub2_Test()
    throws Exception
  {
    runPackageJsonTest("missing_mimetype_file_epub2", 1);
  }

  @Test
  public void missing_container_file_epub3_Test()
    throws Exception
  {
    runPackageJsonTest("missing_container_file_epub3", 1);
  }

  @Test
  public void missing_container_file_epub2_Test()
    throws Exception
  {
    runPackageJsonTest("missing_container_file_epub2", 1);
  }

  @Test
  public void missing_opf_file_Test()
    throws Exception
  {
    runPackageJsonTest("missing_opf_file", 1);
  }

  @Test
  public void interesting_paths_epub2_Test()
    throws Exception
  {
    runPackageJsonTest("interesting_paths_epub2", 0);
  }

  @Test
  public void interesting_paths_epub2_xml_Test()
    throws Exception
  {
    runPackageXmlTest("interesting_paths_epub2", 0);
  }

  @Test
  public void interesting_paths_epub3_json_Test()
    throws Exception
  {
    runPackageJsonTest("interesting_paths_epub3", 1);
  }

  @Test
  public void interesting_paths_epub3_xml_Test()
    throws Exception
  {
    runPackageXmlTest("interesting_paths_epub3", 1);
  }

  @Test
  public void empty_mimetype_Test()
    throws Exception
  {
    runPackageJsonTest("empty_mimetype", 1);
  }

  @Test
  public void image_types_Test()
    throws Exception
  {
    runPackageJsonTest("image_types", 0);
  }

  @Test
  public void path_resolution_Test()
    throws Exception
  {
    runPackageJsonTest("path_resolution", 0);
  }

  @Test
  public void empty_dir_Test()
    throws Exception
  {
    String[] args = new String[3];
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/empty_dir_actual_results.json";
    String expectedOutputPath = inputPath + "/empty_dir_expected_results.json";
    inputPath += "/empty_dir.epub";
    args[0] = inputPath;
    args[1] = "-j";
    args[2] = outputPath;
    common.runCustomTest("package", "empty_dir", 1, args);
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  @Test
  public void corrupt_file_Test()
    throws Exception
  {
    String[] args = new String[3];
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/corrupt_file_actual_results.json";
    String expectedOutputPath = inputPath + "/corrupt_file_expected_results.json";
    inputPath += "/corrupt_file.epub";
    args[0] = inputPath;
    args[1] = "-j";
    args[2] = outputPath;
    common.runCustomTest("package", "corrupt_file", 1, args);
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  @Test
  public void blank_file_Test()
    throws Exception
  {
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    inputPath += "/blank.epub";
    ValidationReport report = new ValidationReport(inputPath);
    report.initialize();
    File inputEpub = new File(inputPath);
    EpubCheck check = new EpubCheck(inputEpub, report);
    Assert.assertEquals("The file should have generated warnings.", 6, check.doValidate());
    report.generate();
    List<MessageId> fatals = new ArrayList<MessageId>();
    fatals.add(MessageId.PKG_008);
    List<MessageId> errors = new ArrayList<MessageId>();
    errors.add(MessageId.PKG_003);
    Assert.assertEquals("The fatal results do not match", fatals, report.getFatalErrorIds());
    Assert.assertEquals("The error results do not match", errors, report.getErrorIds());
    Assert.assertEquals("The warning results do not match", 0, report.getWarningCount());
  }

  @Test
  public void wrong_type_Test()
    throws Exception
  {
    String[] args = new String[3];
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/wrong_type_actual_results.json";
    String expectedOutputPath = inputPath + "/wrong_type_expected_results.json";
    inputPath += "/PlaceHolder.epub";
    args[0] = inputPath;
    args[1] = "-j";
    args[2] = outputPath;
    common.runCustomTest("package", "wrong_type", 1, args);
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  // @Test There are different results when running through IntelliJ and running
  // with Maven.
  // Different OS's also produce different results.
  public void empty_archive_Test()
    throws Exception
  {
    String[] args = new String[3];
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/empty_archive_actual_results.json";
    String expectedOutputPath = inputPath + "/empty_archive_expected_results.json";
    inputPath += "/empty_archive.epub";
    args[0] = inputPath;
    args[1] = "-j";
    args[2] = outputPath;
    common.runCustomTest("package", "empty_archive", 1, args);
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  @Test
  public void wrong_extension_Test()
    throws Exception
  {
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/wrong_extension_actual_results.json";
    String expectedOutputPath = inputPath + "/wrong_extension_expected_results.json";
    inputPath += "/wrong_extension.zip";
    CheckingReport report = new CheckingReport(inputPath, outputPath);
    report.initialize();
    File inputEpub = new File(inputPath);
    EpubCheck check = new EpubCheck(inputEpub, report);
    org.junit.Assert
        .assertEquals("The file should have generated warnings.", 1, check.doValidate());
    report.generate();
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  @Test
  public void wrong_extension_version3_Test()
          throws Exception
  {
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/wrong_extension_v3_actual_results.json";
    System.out.println(outputPath);
    String expectedOutputPath = inputPath + "/wrong_extension_v3_expected_results.json";
    inputPath += "/wrong_extension_v3.zip";
    CheckingReport report = new CheckingReport(inputPath, outputPath);
    report.initialize();
    File inputEpub = new File(inputPath);
    EpubCheck check = new EpubCheck(inputEpub, report);
    org.junit.Assert
            .assertEquals("The file should have generated info logging.", 0, check.doValidate());
    report.generate();
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  @Test
  public void missing_file_Test()
    throws Exception
  {
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/missing_file_actual_results.json";
    String expectedOutputPath = inputPath + "/missing_file_expected_results.json";
    inputPath += "/no_existence.epub";
    CheckingReport report = new CheckingReport(inputPath, outputPath);
    report.initialize();
    File inputEpub = new File(inputPath);
    EpubCheck check = new EpubCheck(inputEpub, report);
    org.junit.Assert
        .assertEquals("The file should have generated warnings.", 2, check.doValidate());
    report.generate();
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  @Test
  public void missing_opf_epub_file_Test()
    throws Exception
  {
    URL inputUrl = common.class.getResource("package");
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath = inputPath + "/missing_opf_epub_file_actual_results.json";
    String expectedOutputPath = inputPath + "/missing_opf_epub_file_expected_results.json";
    inputPath += "/missing_opf_file";
    OCFPackage ocf = new OCFMockPackage(inputPath);
    CheckingReport report = new CheckingReport(inputPath, outputPath);
    report.initialize();
    ocf.setReport(report);
    OPFChecker opfChecker = new OPFChecker(new ValidationContextBuilder().path("test_single_opf")
        .ocf(ocf).report(report).version(EPUBVersion.VERSION_3).build());
    opfChecker.runChecks();
    report.generate();
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedOutputPath);
    Assert.assertTrue("Expected output file is missing.", expectedOutput.exists());
    common.compareJson(expectedOutput, actualOutput);
  }

  private void runPackageJsonTest(String testName, int expectedReturnCode)
    throws Exception
  {
    common.runExpTest("package", testName, expectedReturnCode, TestOutputType.JSON);
  }

  private void runPackageXmlTest(String testName, int expectedReturnCode)
    throws Exception
  {
    common.runExpTest("package", testName, expectedReturnCode, TestOutputType.XML);
  }

  private static String decodeURLtoString(URL url) {
    try {
      return new File(url.toURI()).getAbsolutePath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e); 
    }
  }
}
