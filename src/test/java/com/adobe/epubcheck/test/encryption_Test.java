package com.adobe.epubcheck.test;

import org.idpf.epubcheck.common.CommonTestRunner;
import org.idpf.epubcheck.common.CommonTestRunner.TestOutputType;
import org.idpf.epubcheck.common.NoExitSecurityManager;
import org.junit.*;


public class encryption_Test {
  private SecurityManager originalManager;

  @Before
  public void setUp() throws Exception {
    this.originalManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() throws Exception {
    System.setSecurityManager(this.originalManager);
  }

  @Test
  public void epub30_font_obfuscation_encryption_xml_Test() throws Exception {
    CommonTestRunner.runEpubTest("encryption", "epub30_font_obfuscation.epub", 0,
        TestOutputType.XML, false, new String[] {"-w"});
  }

  @Test
  public void epub30_font_obfuscation_encryption_xmp_Test() throws Exception {
    CommonTestRunner.runEpubTest("encryption", "epub30_font_obfuscation.epub", 0,
        TestOutputType.XMP, false, new String[] {"-w"});
  }

  @Test
  public void epub20_minimal_encryption_xml_Test() throws Exception {
    CommonTestRunner.runEpubTest("encryption", "epub20_minimal_encryption.epub", 1,
        TestOutputType.XML, false, new String[] {"-w"});
  }

  @Test
  public void epub20_encryption_binary_content_xml_Test() throws Exception {
    CommonTestRunner.runEpubTest("encryption", "epub20_encryption_binary_content.epub", 1,
        TestOutputType.XML, false, new String[] {"-w"});
  }
}
