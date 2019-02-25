/* 
  Copyright-Only Dedication (based on United States law)
  
  The person or persons who have associated their work with this
  document (the "Dedicators") hereby dedicate whatever copyright they
  may have in the work of authorship herein (the "Work") to the
  public domain.
  
  Dedicators make this dedication for the benefit of the public at
  large and to the detriment of Dedicators' heirs and successors.
  Dedicators intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights
  under copyright law, whether vested or contingent, in the Work.
  Dedicators understand that such relinquishment of all rights
  includes the relinquishment of all rights to enforce (by lawsuit
  or otherwise) those copyrights in the Work.
  
  Dedicators recognize that, once placed in the public domain, the
  Work may be freely reproduced, distributed, transmitted, used,
  modified, built upon, or otherwise exploited by anyone for any
  purpose, commercial or non-commercial, and in any way, including
  by methods that have not yet been invented or conceived.
 */

package com.adobe.epubcheck.ocf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.LinkedList;

import com.adobe.epubcheck.opf.ValidationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;
import com.adobe.epubcheck.util.ExtraReportTest;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.ReportingLevel;
import com.adobe.epubcheck.util.ValidationReport.ItemReport;

public class OCFCheckerTest
{
  
  List<MessageId> expectedErrors = new LinkedList<MessageId>();
  List<MessageId> expectedWarnings = new LinkedList<MessageId>();
  List<MessageId> expectedUsage = new LinkedList<MessageId>();
  List<MessageId> expectedFatals = new LinkedList<MessageId>();
  private final Messages messages = Messages.getInstance();

  private Locale defaultLocale;
  private static final String VERSION_STRING = "[format version] "+EPUBVersion.VERSION_3;
  
  @Before
  public void before() throws Exception
  {
    expectedErrors.clear();
    expectedWarnings.clear();
    expectedFatals.clear();
    expectedUsage.clear();

    defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.ENGLISH);
  }

  @After
  public void after() throws Exception
  {
    Locale.setDefault(defaultLocale);
  }

  public ValidationReport testValidateDocument(String fileName, String mimeType, EPUBVersion version)
  {
    return testValidateDocument(fileName, mimeType, version, false);
  }

  public ValidationReport testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      boolean verbose)
  {
    return testValidateDocument(fileName, mimeType, version, verbose, null);
  }

  public ValidationReport testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      EPUBProfile profile)
  {
    return testValidateDocument(fileName, mimeType, version, profile, false);
  }

  public ValidationReport testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      EPUBProfile profile, boolean verbose)
  {
    return testValidateDocument(fileName, mimeType, version, profile, verbose, null);
  }

  public ValidationReport testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      ExtraReportTest extraTest)
  {
    return testValidateDocument(fileName, mimeType, version, false, extraTest);
  }

  public ValidationReport testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      boolean verbose, ExtraReportTest extraTest)
  {
    return testValidateDocument(fileName, mimeType, version, EPUBProfile.DEFAULT, verbose, extraTest);
  }

  public ValidationReport testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      EPUBProfile profile, boolean verbose, ExtraReportTest extraTest)
  {
    ValidationReport testReport = new ValidationReport(fileName,
        String.format(messages.get("single_file"), mimeType, version, profile));
    testReport.setReportingLevel(ReportingLevel.Usage);
    String basepath = null;
    if (version == EPUBVersion.VERSION_2)
    {
      basepath = "/20/single/";
    }
    else if (version == EPUBVersion.VERSION_3)
    {
      basepath = "/30/single/";
    }

    GenericResourceProvider resourceProvider = null;
    try
    {
      URL fileURL = this.getClass().getResource(basepath + fileName);
      String filePath = fileURL != null ? new File(fileURL.toURI()).getAbsolutePath()
          : basepath + fileName;
      resourceProvider = new FileResourceProvider(filePath);
    } catch (URISyntaxException e)
    {
      throw new IllegalStateException("Cannot find test file", e);
    }

    ValidationContext context = new ValidationContextBuilder().path(basepath + fileName)
            .mimetype(mimeType).resourceProvider(resourceProvider).report(testReport).version(version)
            .profile(profile).build();
    System.out.println(context.path);

    OCFChecker ocfChecker = new OCFChecker(context, true);

    ocfChecker.runChecksSingleFile();

    System.out.println("##########################");
    System.out.println("##########################");
//    if (verbose)
//    {
//      outWriter.println(testReport);
//    }
    outWriter.println(testReport);
    System.out.println("##########################");
    System.out.println("##########################");

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());
    assertEquals("The usage results do not match", expectedUsage,
        testReport.getUsageIds());
    if (extraTest != null)
    {
      extraTest.test(testReport);
    }

    System.out.println("-----------------");
    System.out.println("-----------------");
    System.out.println(testReport.getUsageIds());
    System.out.println(testReport.getInfoIds());
    System.out.println(testReport.getFatalErrorIds());
    System.out.println(testReport.getErrorIds());
    System.out.println(testReport.getWarningIds());
    System.out.println("-----------------");
    System.out.println("-----------------");

    return testReport;
  }

  private ValidationReport testOcfPackage(String fileName, EPUBVersion version)
  {
    OCFPackage ocf = new OCFMockPackage(fileName);

    ValidationReport testReport = new ValidationReport(fileName,
        String.format("Package is being checked as EPUB version %s",
            version == null ? "null" : version.toString()));
    OCFChecker checker = new OCFChecker(
        new ValidationContextBuilder().ocf(ocf).report(testReport).version(version).build());

    checker.runChecks();

    return testReport;
  }

  /**
   * Not a test of the OCFChecker, just a sanity check to be sure the Mock
   * Package provider is working.
   */
  @Test
  public void invalidPath()
  {
    ValidationReport testReport = testOcfPackage("/non-existent/", EPUBVersion.VERSION_2);
    assertEquals(1, testReport.getFatalErrorCount());
  }

  @Test
  public void testLoremBasic20()
  {
    ValidationReport testReport = testOcfPackage("/20/expanded/valid/lorem/lorem-basic/",
        EPUBVersion.VERSION_2);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());
    assertTrue(testReport.hasInfoMessage("[format version] 2.0.1"));
    assertTrue(testReport
        .hasInfoMessage("[unique identifier] urn:uuid:550e8400-e29b-41d4-a716-446667441231"));
  }

  @Test
  public void testLoremBasic30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-basic/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremBasic30Against20()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-basic/",
        EPUBVersion.VERSION_2);
    if (0 == testReport.getErrorCount() || 1 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertTrue(testReport.getErrorCount() > 0);
    List<MessageId> warnings = new ArrayList<MessageId>();
    Collections.addAll(warnings, MessageId.PKG_001);
    assertEquals(warnings, testReport.getWarningIds());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
    assertTrue(testReport
        .hasInfoMessage("[unique identifier] urn:uuid:550e8400-e29b-41d4-a716-446667441231"));
  }

  @Test
  public void testLoremBasic20Against30()
  {
    ValidationReport testReport = testOcfPackage("/20/expanded/valid/lorem/lorem-basic/",
        EPUBVersion.VERSION_3);
    if (0 == testReport.getErrorCount() || 1 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertTrue(testReport.getErrorCount() > 0);
    List<MessageId> warnings = new ArrayList<MessageId>();
    Collections.addAll(warnings, MessageId.PKG_001);
    assertEquals(warnings, testReport.getWarningIds());

    assertTrue(testReport.hasInfoMessage("[format version] 2.0.1"));
    assertTrue(testReport
        .hasInfoMessage("[unique identifier] urn:uuid:550e8400-e29b-41d4-a716-446667441231"));
  }

  @Test
  public void testLoremAudio30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-audio/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());
    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremForeign30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-foreign/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremLink30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-link/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremPoster30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-poster/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremSVG30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-svg/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremHyperlink30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-svg-hyperlink/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremWasteland30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/wasteland-basic/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testLoremMultipleRenditions20()
  {
    ValidationReport testReport = testOcfPackage("/20/expanded/valid/lorem-xrenditions-2ops/",
        EPUBVersion.VERSION_2);
    if (1 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.PKG_013);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());

  }

  @Test
  public void testLoremMultipleRenditionsSingleOPF20()
  {
    ValidationReport testReport = testOcfPackage("/20/expanded/valid/lorem-xrenditions-1ops/",
        EPUBVersion.VERSION_2);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

  }

  @Test
  public void testLoremMultipleRenditions30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/multiple-renditions/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
       outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage("[EPUB renditions count] 2"));
  }

  @Test
  public void testValidCompression()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/ocf-compression/",
        EPUBVersion.VERSION_3);

    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());
  }

  @Test
  public void testInvalidLoremForeign30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/invalid/lorem-foreign/",
        EPUBVersion.VERSION_3);
    if (1 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    // there must be a message error about the missing 'remote-resources'
    // property
    assertTrue(testReport.errorList.get(0).message
        .contains("The property 'remote-resources' should be declared in the OPF file."));
    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.OPF_014);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testInvalidLoremPoster30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/invalid/lorem-poster/",
        EPUBVersion.VERSION_3);
    if (1 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }

    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.MED_001);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testInvalidLoremRNG30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/invalid/lorem-xhtml-rng-1/",
        EPUBVersion.VERSION_3);
    if (1 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertTrue(testReport.errorList.get(0).message.contains("element \"epub:x\" not allowed here"));
    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.RSC_005);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testInvalidLoremSCH30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/invalid/lorem-xhtml-sch-1/",
        EPUBVersion.VERSION_3);

    if (1 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    if (testReport.errorList.size() > 0)
    {
      assertTrue(testReport.errorList.get(0).message
          .contains("The dfn element must not appear inside dfn elements"));
    }
    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.RSC_005);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage(VERSION_STRING));
  }

  @Test
  public void testInvalidCompressionMethod()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/invalid/ocf-compression/",
        EPUBVersion.VERSION_3);

    if (2 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.RSC_005, MessageId.RSC_005);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());
    if (testReport.errorList.size() >= 2)
    {
      assertTrue(testReport.errorList.get(0).message
          .contains("value of attribute \"Method\" is invalid; must be equal to \"0\" or \"8\""));
      assertTrue(testReport.errorList.get(1).message
          .contains("value of attribute \"OriginalLength\" is invalid; must be an integer"));
    }
  }

  // https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-enc-compression
  // https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-encryption.xml

  @Test
  public void testValidEncryptionXML()
  {
    ValidationReport testReport = testValidateDocument("ocf/valid/encryption.xml", "application/encryption+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testInvalidEncryptionXML()
  {
    // Collections.addAll(expectedErrors, MessageId.RSC_020);
    // Collections.addAll(expectedWarnings, MessageId.HTM_025, MessageId.RSC_023, MessageId.RSC_023);
    ValidationReport testReport = testValidateDocument("ocf/invalid/encryption.xml", "application/encryption+xml", EPUBVersion.VERSION_3);
  }
}
