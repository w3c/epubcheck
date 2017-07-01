package com.adobe.epubcheck.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.test.common.TestOutputType;

public class css_Test
{
  private SecurityManager originalManager;

  @Before
  public void setUp() throws Exception
  {
    this.originalManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() throws Exception
  {
    System.setSecurityManager(this.originalManager);
  }

  @Test
  public void transform_Test() throws Exception
  {
    runCSSJsonTest("transform", 0);
  }

  @Test
  public void columns_Test() throws Exception
  {
    runCSSJsonTest("columns", 0);
  }

  @Test
  public void transition_Test() throws Exception
  {
    runCSSJsonTest("transition", 0);
  }

  @Test
  public void keyframe_Test() throws Exception
  {
    runCSSJsonTest("keyframe", 0);
  }

  @Test
  public void font_face_Test() throws Exception
  {
    runCSSJsonTest("font-face", 1);
  }

  @Test
  public void font_face_xmp_Test() throws Exception
  {
    runCSSXmpTest("font-face", 1);
  }

  @Test
  public void font_family_no_src_xml_Test() throws Exception
  {
    runCSSXmlTest("font-family-no-src", 1);
  }

  // @Test
  public void unused_epub3_Test() throws Exception
  {
    runCSSJsonTest("unused_epub3", 1);
  }

  @Test
  public void discouraged_json_Test() throws Exception
  {
    runCSSJsonTest("discouraged", 1);
  }

  @Test
  public void discouraged_xml_Test() throws Exception
  {
    runCSSXmlTest("discouraged", 1);
  }

  // @Test
  public void unused_epub2_Test() throws Exception
  {
    runCSSJsonTest("unused_epub2", 1);
  }

  @Test
  public void multiple_epub3_Test() throws Exception
  {
    runCSSJsonTest("multiple_epub3", 1);
  }

  // @Test
  public void multiple_epub2_Test() throws Exception
  {
    runCSSJsonTest("multiple_epub2", 0);
  }

  @Test
  public void excessive_epub3_Test() throws Exception
  {
    runCSSJsonTest("excessive_epub3", 0);
  }

  @Test
  public void alternate_Test() throws Exception
  {
    runCSSJsonTest("alternate", 1);
  }

  @Test
  public void font_encryption_unknown_Test() throws Exception
  {
    runCSSJsonTest("font_encryption_unknown", 1);
  }

  @Test
  public void font_encryption_adobe_Test() throws Exception
  {
    runCSSJsonTest("font_encryption_adobe", 1);
  }

  @Test
  public void font_encryption_idpf_Test() throws Exception
  {
    runCSSJsonTest("font_encryption_idpf", 0);
  }

  @Test
  public void font_encryption_idpf_xml_Test() throws Exception
  {
    runCSSXmlTest("font_encryption_idpf", 0);
  }

  @Test
  public void font_encryption_idpf_xmp_Test() throws Exception
  {
    runCSSXmpTest("font_encryption_idpf", 0);
  }

  private void runCSSJsonTest(String testName, int expectedReturnCode) throws Exception
  {
    common.runExpTest("css", testName, expectedReturnCode, TestOutputType.JSON);
  }

  private void runCSSXmlTest(String testName, int expectedReturnCode) throws Exception
  {
    common.runExpTest("css", testName, expectedReturnCode, TestOutputType.XML);
  }

  private void runCSSXmpTest(String testName, int expectedReturnCode) throws Exception
  {
    common.runExpTest("css", testName, expectedReturnCode, TestOutputType.XMP);
  }
}
