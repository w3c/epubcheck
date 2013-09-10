package com.adobe.epubcheck.test;

import com.adobe.epubcheck.tool.Checker;
import com.adobe.epubcheck.util.*;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;

public class command_line_Test
{
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  private SecurityManager originalManager;
  private PrintStream originalOut;
  private PrintStream originalErr;


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
  public void static_class_Test()
  {
    //This will create an instance of classes that have nothing but static methods for the sake of code coverage.
    Checker checker = new Checker();
    Assert.assertTrue("Checker string isn't as expected", checker.toString().startsWith("com.adobe.epubcheck.tool.Checker"));

    Messages messages = new Messages();
    Assert.assertTrue("Messages string isn't as expected", messages.toString().startsWith("com.adobe.epubcheck.util.Messages"));

    EpubTypeAttributes attributes = new EpubTypeAttributes();
    Assert.assertTrue("EpubTypeAttributes string isn't as expected", attributes.toString().startsWith("com.adobe.epubcheck.util.EpubTypeAttributes"));

    HandlerUtil handlerUtil = new HandlerUtil();
    Assert.assertTrue("HandlerUtil string isn't as expected", handlerUtil.toString().startsWith("com.adobe.epubcheck.util.HandlerUtil"));

    MetaUtils metaUtils = new MetaUtils();
    Assert.assertTrue("MetaUtils string isn't as expected", metaUtils.toString().startsWith("com.adobe.epubcheck.util.MetaUtils"));

    PathUtil pathUtil = new PathUtil();
    Assert.assertTrue("PathUtil string isn't as expected", pathUtil.toString().startsWith("com.adobe.epubcheck.util.PathUtil"));

    CheckUtil checkUtil = new CheckUtil();
    Assert.assertTrue("CheckUtil string isn't as expected", checkUtil.toString().startsWith("com.adobe.epubcheck.util.CheckUtil"));

    ResourceUtil resourceUtil = new ResourceUtil();
    Assert.assertTrue("ResourceUtil string isn't as expected", resourceUtil.toString().startsWith("com.adobe.epubcheck.util.ResourceUtil"));
  }

  @Test
  public void empty_Test()
  {
    common.runCustomTest("command_line", "empty", 1);
    Assert.assertEquals("Command output not as expected", Messages.ARGUMENT_NEEDED, errContent.toString().trim());
  }

  @Test
  public void help_Test()
  {
    common.runCustomTest("command_line", "help", 1, true, "-?");
    Assert.assertEquals("Command output not as expected", Messages.NO_FILE_SPECIFIED, errContent.toString().trim());
    String expected = Messages.HELP_TEXT.replaceAll("[\\s]+", " ");
    String actual = outContent.toString();
    actual = actual.replaceAll("[\\s]+", " ");
    Assert.assertTrue("Help output isn't as expected", actual.contains(expected));
  }

  @Test
  public void conflicting_output_Test()
  {
    common.runCustomTest("command_line", "conflicting_output", 1, "-o", "foo.xml", "-j", "bar.json");
    Assert.assertEquals("Command output not as expected", Messages.OUTPUT_TYPE_CONFLICT, errContent.toString().trim());
  }

  @Test
  public void SeveritiesUsage_Test()
  {
    runSeverityTest("severity", "command_line", "severity_usage", 1, "-u");
  }

  @Test
  public void SeveritiesWarning_Test()
  {
    runSeverityTest("severity", "command_line", "severity_warning", 1, "-w");
  }

  @Test
  public void SeveritiesError_Test()
  {
    runSeverityTest("severity", "command_line", "severity_error", 1, "-e");
  }

  @Test
  public void SeveritiesFatal_Test()
  {
    runSeverityTest("severity", "command_line", "severity_fatal", 0, "-f");
  }

  @Test
  public void SeveritiesOverrideOk_Test()
  {
    String testName = "severity_overrideOk";
    URL inputUrl = common.class.getResource("command_line");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", "command_line", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideMissingFile_Test()
  {
    String testName = "severity_overrideMissingFile";
    URL inputUrl = common.class.getResource("command_line");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", "command_line", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideBadId_Test()
  {
    String testName = "severity_overrideBadId";
    URL inputUrl = common.class.getResource("command_line");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", "command_line", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideBadSeverity_Test()
  {
    String testName = "severity_overrideBadSeverity";
    URL inputUrl = common.class.getResource("command_line");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", "command_line", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideBadMessage_Test()
  {
    String testName = "severity_overrideBadMessage";
    URL inputUrl = common.class.getResource("command_line");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", "command_line", testName, 1, "-c", configFile, "-u");
  }


  @Test
  public void SeveritiesList_Test()
  {
    //public static void runCustomTest(String epubName, String componentName, String testName, int expectedReturnCode, String... args)
    URL inputUrl = common.class.getResource("command_line");
    String inputPath = inputUrl.getPath();
    String outputPath = inputPath + "/listSeverities" + "_actual_results.txt";
    String expectedUrl = inputPath + "/listSeverities" + "_expected_results.txt";
    common.runCustomTest("command_line", "listSeverities", 0, "--listChecks", outputPath);

    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    File expectedOutput = new File(expectedUrl);
    Assert.assertTrue("Expected file is missing.", expectedOutput.exists());

    try
    {
      common.compareText(expectedOutput, actualOutput);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Test
  public void passonwarnings_Test()
  {
    runExtraCommandLineArgTest("passonwarnings", 0, new String[0]);
  }

  @Test
  public void failonwarnings_Test()
  {
    String[] extraArgs = {"--failonwarnings"};
    runExtraCommandLineArgTest("failonwarnings", 1, extraArgs);
  }


  public static void runExtraCommandLineArgTest(String testName, int expectedReturnCode, String[] extraArgs)
  {
    common.runExpTest("command_line", testName, expectedReturnCode, true, extraArgs);
  }
  public static void runSeverityTest(String epubName, String componentName, String testName, int expectedReturnCode, String... args)
  {
    File actualOutput;
    PrintStream ps = null;
    PrintStream origErr = System.err;
    PrintStream origOut = System.out;
    try
    {
      String[] theArgs = new String[3 + args.length];
      URL inputUrl = common.class.getResource(componentName + "/" + epubName);
      Assert.assertNotNull("Input folder is missing.", inputUrl);
      String inputPath = inputUrl.getPath();
      String outputPath = inputPath + "/../" + testName + "_actual_results.txt";

      theArgs[0] = inputPath;
      theArgs[1] = "-mode";
      theArgs[2] = "exp";
      System.arraycopy(args, 0, theArgs, 3, args.length);

      actualOutput = new File(outputPath);
      ps = new PrintStream(actualOutput);
      System.setErr(ps);
      System.setOut(ps);
      common.runCustomTest(componentName, testName, expectedReturnCode, theArgs);
      System.setErr(origErr);
      System.setOut(origOut);
      ps.flush();
      ps.close();
      ps = null;

      Assert.assertTrue("Output file is missing.", actualOutput.exists());
      URL expectedUrl = common.class.getResource(componentName + "/" + testName + "_expected_results.txt");
      Assert.assertNotNull("Expected file is missing.", expectedUrl);
      File expectedOutput = new File(expectedUrl.getPath());
      Assert.assertTrue("Expected file is missing.", expectedOutput.exists());
      try
      {
        differ d = new differ(expectedOutput, actualOutput, 3);
        Assert.assertTrue("expected file does not match actual file", d.areTheSame());
      }
      catch (Exception ex)
      {
        System.err.println(ex.getMessage());
      }
      File tempFile = new File(testName + ".epub");

      Assert.assertFalse("Temp file left over after test: " + tempFile.getPath(), tempFile.exists());
    }
    catch (FileNotFoundException ex)
    {
      System.err.println("File not found: " + testName + "_actual_results.txt");
    }
    finally
    {
      if (ps != null)
      {
        System.setErr(origErr);
        System.setOut(origOut);
      }
    }
  }

  private static class differ
  {
    File expected;
    File actual;
    int skip;

    public differ(File expected, File actual, int skip)
    {
      this.expected = expected;
      this.actual = actual;
      this.skip = skip;
    }

    public boolean areTheSame()
    {
      BufferedReader aR = null;
      BufferedReader eR = null;
      try
      {
        int lineNumber = 0;
        aR = new BufferedReader(new FileReader(actual));
        eR = new BufferedReader(new FileReader(expected));

        String a;
        String e = null;

        while (((a = aR.readLine()) != null) &&
            ((e = eR.readLine()) != null))
        {
          if (++lineNumber > skip)
          {
            if (a != null && e != null)
            {
              int x1 = a.indexOf(": ");
              int y1 = e.indexOf(": ");
              Assert.assertEquals("lines do not match(" + lineNumber + ")", y1 >= 0 ? e.substring(0, y1) : "", x1 >= 0 ? a.substring(0, x1) : "");

              int x2 = a.lastIndexOf("):");
              int y2 = e.lastIndexOf("):");
              if (x2 != -1 && y2 != -1)
              {
                Assert.assertEquals(a.length() - x2, e.length() - y2);
              }

              if (y1 > 0)
              {
                String x = a.substring(0, x1);
                String y = e.substring(0, y1);
                Assert.assertEquals("lines do not match(" + lineNumber + "): actual:'" + x + "' expected: '" + y + "'", 0, y.compareTo(x));
              }
              else
              {
                Assert.assertEquals(e, a);
              }
              if (y2 > 0)
              {
                String x = a.substring(x2);
                String y = e.substring(y2);
                Assert.assertEquals("lines do not match(" + lineNumber + "): actual:'" + x + "' expected: '" + y + "'", 0, y.compareTo(x));
              }
              else if (!a.contains("com.adobe.epubcheck"))
              {
                Assert.assertEquals("lines do not match(" + lineNumber + "): actual:'" + a + "' expected: '" + e + "'", e, a);
              }
            }
          }
        }
        if (a != null && a.startsWith("Completed command_line test"))
        {
          a = null;
        }
        if (e != null && e.startsWith("Completed command_line test"))
        {
          e = null;
        }
        Assert.assertTrue("files are not the same length", ((a == null || a.length() == 0) && (e == null || e.length() == 0)));
      }
      catch (FileNotFoundException ex)
      {
        Assert.assertTrue("actual file not found", actual.exists());
        Assert.assertTrue("expected file not found", expected.exists());
      }
      catch (IOException io)
      {
        io.printStackTrace();
      }
      finally
      {
        if (aR != null)
        {
          try
          {
            aR.close();
          }
          catch (IOException e1)
          {
            e1.printStackTrace();
          }
        }
        if (eR != null)
        {
          try
          {
            eR.close();
          }
          catch (IOException e2)
          {
            e2.printStackTrace();
          }
        }
      }
      return true;
    }
  }

}
