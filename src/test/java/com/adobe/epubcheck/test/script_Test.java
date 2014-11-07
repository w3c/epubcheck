package com.adobe.epubcheck.test;

import org.junit.*;

import com.adobe.epubcheck.test.common.TestOutputType;

public class script_Test
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
  public void scriptPropertiesTest() throws Exception
  {
    runScriptTest("properties", 1);
  }

  @Test
  public void unused_script_Test() throws Exception
  {
    runScriptTest("unused", 0);
  }

  @Test
  public void eval_script_Test() throws Exception
  {
    runScriptTest("eval", 0);
  }

  @Test
  public void XMLHttpRequest_script_Test() throws Exception
  {
    runScriptTest("XMLHttpRequest", 0);
  }

  @Test
  public void Storage_script_Test() throws Exception
  {
    runScriptTest("storage", 0);
  }

  @Test
  public void epub2_script_Test() throws Exception
  {
    runScriptTest("epub2", 1);
  }

  @Test
  public void epub2_script_xmp_Test() throws Exception
  {
	    common.runExpTest("scripts", "epub2", 1, TestOutputType.XMP);
  }

  private void runScriptTest(String testName, int expectedReturnCode) throws Exception
  {
    common.runExpTest("scripts", testName, expectedReturnCode, TestOutputType.JSON);
  }
}
