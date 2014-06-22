package com.adobe.epubcheck.test;

import com.adobe.epubcheck.util.Messages;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test the processing of single files that are not ePubs
 */
public class single_file_Test
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
  public void remote_Test() throws Exception
  {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    common.runCustomTest("command_line", "remote", 1, true, "-mode", "nav", "-v", "3.0", "http://localhost:8080/noexist.nav");

    //The exception string is different on iOS than it is on Windows.
    //This is why we are examining the command line rather than comparing json files.
    String actualErr = errContent.toString();
    Assert.assertTrue("Missing errors message", actualErr.contains(Messages.get("there_were_errors")));
    Assert.assertTrue("Missing message", actualErr.contains("PKG-008"));
    System.setOut(originalOut);
    System.setErr(originalErr);
    outContent.close();
    errContent.close();
  }
}
