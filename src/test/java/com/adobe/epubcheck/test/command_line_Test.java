package com.adobe.epubcheck.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import junit.framework.Assert;

import com.adobe.epubcheck.test.CommonTestRunner.TestOutputType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.tool.Checker;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.ResourceUtil;

public class command_line_Test
{
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  
  private SecurityManager originalManager;
  private PrintStream originalOut;
  private PrintStream originalErr;
  private final Messages messages = Messages.getInstance();

  @Before
  public void setUp() throws Exception
  {
    this.originalManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @After
  public void tearDown() throws Exception
  {
    System.setSecurityManager(this.originalManager);
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void passonwarnings_Test()
  {
    runExtraCommandLineArgTest("passonwarnings", 0, new String[0]);
  }

  @Test
  public void jsonfile_Test()
  {
    CommonTestRunner.runExpTest("command_line", "jsonfile", 0, TestOutputType.JSON, false, true, new String[0]);
  }

  @Test
  public void xmlfile_Test()
  {
    CommonTestRunner.runExpTest("command_line", "xmlfile", 0, TestOutputType.XML, false, true, new String[0]);
  }

  @Test
  public void xmpfile_Test()
  {
    CommonTestRunner.runExpTest("command_line", "xmlfile", 0, TestOutputType.XMP, false, true, new String[0]);
  }

  @Test
  public void failonwarnings_Test()
  {
    String[] extraArgs = {"--failonwarnings"};
    runExtraCommandLineArgTest("failonwarnings", 1, extraArgs);
  }


  public static void runExtraCommandLineArgTest(String testName, int expectedReturnCode, String[] extraArgs)
  {
    CommonTestRunner.runExpTest("command_line", testName, expectedReturnCode, TestOutputType.JSON, false, false, extraArgs);
  }
  
}
