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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.test.NoExitSecurityManager;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;

public class OCFCheckerTest
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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
  public void testLoremBasic30Switch()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-basic-switch/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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
    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
  }

  @Test
  public void testLoremBindings30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-bindings/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
  }

  @Test
  public void testLoremFallbacks30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/valid/lorem-object-fallbacks/",
        EPUBVersion.VERSION_3);
    if (0 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertEquals(0, testReport.getErrorCount());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

  // The following tests should all fail, as they point to invalid ePubs
  @Test
  public void testInvalidLoremBasic30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/invalid/lorem-basic-switch/",
        EPUBVersion.VERSION_3);
    if (1 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    // there must be a message error about the missing 'mathml' property
    assertTrue(testReport.errorList.get(0).message
        .contains("The property 'mathml' should be declared in the OPF file."));
    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.OPF_014);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
  }

  @Test
  public void testInvalidLoremBindings30()
  {
    ValidationReport testReport = testOcfPackage("/30/expanded/invalid/lorem-bindings/",
        EPUBVersion.VERSION_3);
    if (1 != testReport.getErrorCount() || 0 != testReport.getWarningCount())
    {
      outWriter.println(testReport);
    }
    assertTrue(
        testReport.errorList.get(0).message.contains("Object element doesn't provide fallback"));
    List<MessageId> errors = new ArrayList<MessageId>();
    Collections.addAll(errors, MessageId.MED_002);
    assertEquals(errors, testReport.getErrorIds());
    assertEquals(0, testReport.getWarningCount());

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
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

    assertTrue(testReport.hasInfoMessage("[format version] 3.0.1"));
  }
}
