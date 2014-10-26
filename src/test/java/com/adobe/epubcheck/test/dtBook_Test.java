package com.adobe.epubcheck.test;

import org.junit.*;

import com.adobe.epubcheck.test.common.TestOutputType;


public class dtBook_Test
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
  public void Basic_JSON_Test() throws Exception
  {
    runDTBookJsonTest("Basic", 0);
  }


  @Test
  public void Basic_XML_Test() throws Exception
  {
    runDTBookXmlTest("Basic", 0);
  }

  private void runDTBookJsonTest(String testName, int expectedReturnCode) throws Exception
  {
    common.runExpTest("DTBook", testName, expectedReturnCode, TestOutputType.JSON);
  }

  private void runDTBookXmlTest(String testName, int expectedReturnCode) throws Exception
  {
    common.runExpTest("DTBook", testName, expectedReturnCode, TestOutputType.XML);
  }

}
