package com.adobe.epubcheck.test;

import org.junit.*;

import com.adobe.epubcheck.test.common.TestOutputType;


public class toc_Test
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
  public void Missing_epub_type_Test() throws Exception
  {
    runTocTest("missing_epub_type", 1);
  }

  @Test
  public void fragments_Test() throws Exception
  {
    runTocTest("fragments", 0);
  }

  @Test
  public void invalid_ncx_Test() throws Exception
  {
    runTocTest("invalid_ncx", 1);
  }

  private void runTocTest(String testName, int expectedReturnCode) throws Exception
  {
    common.runExpTest("toc", testName, expectedReturnCode, TestOutputType.JSON);
  }

}
