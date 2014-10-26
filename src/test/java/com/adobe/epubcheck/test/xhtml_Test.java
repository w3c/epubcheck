package com.adobe.epubcheck.test;

import org.junit.*;

import com.adobe.epubcheck.test.common.TestOutputType;

import java.util.Locale;

public class xhtml_Test
{
  private SecurityManager originalManager;
  private Locale originalLocale;

  @Before
  public void setUp() throws Exception
  {
    this.originalManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
    this.originalLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
  }

  @After
  public void tearDown() throws Exception
  {
    System.setSecurityManager(this.originalManager);
    Locale.setDefault(this.originalLocale);
  }

  @Test
  public void hyperlinksTest() throws Exception
  {
    runXhtmlTest("hyperlinks", 1);
  }

  @Test
  public void nestingTest() throws Exception
  {
    runXhtmlTest("nesting", 0);
  }

  @Test
  public void html5_epub2Test() throws Exception
  {
    runXhtmlTest("html5_epub2", 1);
  }

  @Test
  public void html5_epub3Test() throws Exception
  {
    runXhtmlTest("html5_epub3", 1);
  }

  @Test
  public void html5_deprecated_epub3Test() throws Exception
  {
    runXhtmlTest("html5_deprecated_epub3", 1);
  }

  @Test
  public void lang_Test() throws Exception
  {
    runXhtmlTest("lang", 1);
  }

  @Test
  public void epubcfi_Test() throws Exception
  {
    runXhtmlTest("epubcfi", 0);
  }

  @Test
  public void external_media_test()
  {
    runXhtmlTest("External_media", 0);
  }

  @Test
  public void dtd_test()
  {
    runXhtmlTest("dtd", 1);
  }

  @Test
  public void media_overlays_test()
  {
    runXhtmlTest("media_overlays", 1);
  }

  @Test
  public void lorem_noxmlns_test()
  {
    runXhtmlTest("lorem_noxmlns", 1);
  }

  @Test
  public void lorem_pagemaps1_test()
  {
    runXhtmlTest("lorem_pagemaps1", 1);
  }

  @Test
  public void lorem_pagemaps2_test()
  {
    runXhtmlTest("lorem_pagemaps2", 0);
  }

  @Test
  public void lorem_pagemaps3_test()
  {
    runXhtmlTest("lorem_pagemaps3", 1);
  }

  @Test
  public void accessibility_test()
  {
    runXhtmlTest("accessibility", 1);
  }

  @Test
  public void namespaces_test()
  {
    runXhtmlTest("namespaces", 1);
  }

  @Test
  public void singleline_test()
  {
    runXhtmlTest("singleline", 1);
  }

  private void runXhtmlTest(String testName, int expectedReturnCode)
  {
    common.runExpTest("xhtml", testName, expectedReturnCode, TestOutputType.JSON);
  }
}
