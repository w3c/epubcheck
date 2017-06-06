package com.adobe.epubcheck.test;

import com.adobe.epubcheck.tool.Checker;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.outWriter;

import junit.framework.Assert;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class common
{
  public enum TestOutputType { JSON, XML, XMP };
  
  public static void runExpTest(String componentName, String testName, int expectedReturnCode, TestOutputType testOutput)
  {
    runExpTest(componentName, testName, expectedReturnCode, testOutput, false, new String[0]);
  }

  public static void runExpTest(String componentName, String testName, int expectedReturnCode, TestOutputType testOutput, boolean useNullOutputPath, String... extraArgs)
  {
    ArrayList<String> args = new ArrayList<String>();
    String extension = "json";
    switch (testOutput) {
    case JSON : extension = "json"; break;
    case XML : extension = "xml"; break;
    case XMP : extension = "xmp"; break;
    }
    int extraArgsLength = extraArgs != null ? extraArgs.length : 0;
    URL inputUrl = common.class.getResource(componentName + "/" + testName);
    Assert.assertNotNull("Input folder is missing.", inputUrl);
    String inputPath = decodeURLtoString(inputUrl);
    String outputPath =  inputPath + "/../" + testName + (useNullOutputPath ? "check." : "_actual_results.") + extension;
    args.add(inputPath);
    args.add("-mode");
    args.add("exp");
    args.add("-u");
    for (int j = 0; j < extraArgsLength; ++j)
    {
      args.add(extraArgs[j]);
    }
    switch (testOutput) {
    case JSON : args.add("-j"); break;
    case XML : args.add("-o"); break;
    case XMP : args.add("-x"); break;
    }
    if (!useNullOutputPath && outputPath != null && !outputPath.isEmpty())
    {
      args.add(outputPath);
    }

    runCustomTest(componentName, testName, expectedReturnCode, args.toArray(new String[args.size()]));
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    URL expectedUrl = common.class.getResource(componentName + "/" + testName + "_expected_results." + extension);
    Assert.assertNotNull("Expected file is missing.", expectedUrl);
    File expectedOutput = new File(decodeURLtoString(expectedUrl));
    Assert.assertTrue("Expected file is missing.", expectedOutput.exists());
    switch (testOutput) {
    case JSON : compareJson(expectedOutput, actualOutput); break;
    case XML : compareXml(expectedOutput, actualOutput); break;
    case XMP : compareXml(expectedOutput, actualOutput); break;
    }
    File tempFile = new File(testName + ".epub");
    Assert.assertFalse("Temp file left over after test: " + tempFile.getPath(), tempFile.exists());
  }
  
  public static void runEpubTest(String componentName, String testName, int expectedReturnCode, TestOutputType testOutput, boolean useNullOutputPath, String... extraArgs)
  {
    ArrayList<String> args = new ArrayList<String>();
    String extension = "json";
    switch (testOutput) {
    case JSON : extension = "json"; break;
    case XML : extension = "xml"; break;
    case XMP : extension = "xmp"; break;
    }
    int extraArgsLength = extraArgs != null ? extraArgs.length : 0;
    URL inputUrl = common.class.getResource(componentName + "/" + testName);
    Assert.assertNotNull("Input folder is missing.", inputUrl);
    String inputPath = decodeURLtoString(inputUrl);
    // In case of epub input, the input is a file not a directory
    File f = new File(inputPath);
    String outputPath;
    if (f.isDirectory()) {
	    outputPath = inputPath + "/../" + testName + (useNullOutputPath ? "check." : "_actual_results.") + extension;
    } else {
	    outputPath = f.getParent() + "/"+ testName + (useNullOutputPath ? "check." : "_actual_results.") + extension;
    }
    args.add(inputPath);
    args.add("-u");
    for (int j = 0; j < extraArgsLength; ++j)
    {
      args.add(extraArgs[j]);
    }
    switch (testOutput) {
    case JSON : args.add("-j"); break;
    case XML : args.add("-o"); break;
    case XMP : args.add("-x"); break;
    }
    if (!useNullOutputPath && outputPath != null && !outputPath.isEmpty())
    {
      args.add(outputPath);
    }

    runCustomTest(componentName, testName, expectedReturnCode, args.toArray(new String[args.size()]));
    File actualOutput = new File(outputPath);
    Assert.assertTrue("Output file is missing.", actualOutput.exists());
    URL expectedUrl = common.class.getResource(componentName + "/" + testName + "_expected_results." + extension);
    Assert.assertNotNull("Expected file is missing.", expectedUrl);
    File expectedOutput = new File(decodeURLtoString(expectedUrl));
    Assert.assertTrue("Expected file is missing.", expectedOutput.exists());
    switch (testOutput) {
    case JSON : compareJson(expectedOutput, actualOutput); break;
    case XML : compareXml(expectedOutput, actualOutput); break;
    case XMP : compareXml(expectedOutput, actualOutput); break;
    }
    File tempFile = new File(testName + ".epub");
    Assert.assertFalse("Temp file left over after test: " + tempFile.getPath(), tempFile.exists());
  }

  public static void runCustomTest(String componentName, String testName, int expectedReturnCode, String... args)
  {
    runCustomTest(componentName, testName, expectedReturnCode, false, args);
  }

  public static void runCustomTest(String componentName, String testName, int expectedReturnCode, boolean quiet, String... args)
  {
    try
    {
      if (!quiet)
      {
        outWriter.printf("Start %s test('%s')\n", componentName, testName);
      }
      int result = Integer.MAX_VALUE;
      try
      {
        Locale.setDefault(Locale.US);
        Checker.main(args);
      }
      catch (NoExitSecurityManager.ExitException e)
      {
        result = e.status;
      }

      Assert.assertEquals("Return code", expectedReturnCode, result);

    }
    catch (Exception ex)
    {
      System.err.println(Messages.get("there_were_errors"));
      ex.printStackTrace();
      Assert.assertTrue(String.format("Error running %s test('%s')", componentName, testName), false);
    }
    if (!quiet)
    {
      outWriter.printf("Completed %s test('%s')\n", componentName, testName);
    }
  }

  public static void compareText(File expectedOutput, File actualOutput) throws Exception
  {
    BufferedReader expectedReader = new BufferedReader(new FileReader(expectedOutput));
    BufferedReader actualReader = new BufferedReader(new FileReader(actualOutput));
    String expectedLine = expectedReader.readLine();
    while (expectedLine != null)
    {
      String actualLine = actualReader.readLine();
      Assert.assertNotNull("Expected: " + expectedLine + " Actual: null", actualLine);
      actualLine = actualLine.trim();
      expectedLine = expectedLine.trim();
      Assert.assertEquals("Expected: " + expectedLine + " Actual: " + actualLine, expectedLine, actualLine);
      expectedLine = expectedReader.readLine();
    }
    String overflow = actualReader.readLine();
    Assert.assertNull("Expected: null Actual: " + overflow, overflow);
    expectedReader.close();
    actualReader.close();
  }

  public static void compareJson(File expectedOutput, File actualOutput)
  {
    ArrayList<String> ignoreFields = new ArrayList<String>();
    ignoreFields.add("customMessageFileName");
    ignoreFields.add("/checker/checkDate");
    ignoreFields.add("/checker/checkerVersion");
    ignoreFields.add("/checker/elapsedTime");
    ignoreFields.add("/checker/path");
    try
    {
      jsonCompare.compareJsonFiles(expectedOutput, actualOutput, ignoreFields);
    }
    catch (Exception ex)
    {
      System.err.println(Messages.get("there_were_errors"));
      ex.printStackTrace();
      Assert.assertTrue("Error performing the json comparison: ", false);
    }
  }

  public static void compareXml(File expectedOutput, File actualOutput)
  {
    Diff diff;
    try
    {
      FileReader expectedReader = new FileReader(expectedOutput);
      FileReader actualReader = new FileReader(actualOutput);
      diff = new Diff(expectedReader, actualReader);
    }
    catch (Exception ex)
    {
      System.err.println(Messages.get("there_were_errors"));
      ex.printStackTrace();
      Assert.assertTrue("Error performing the xml comparison: ", false);
      return;
    }
    OutputDifferenceListener listener = new OutputDifferenceListener();
    diff.overrideDifferenceListener(listener);
    diff.overrideElementQualifier(new ElementNameAndTextQualifier());
    Assert.assertTrue("There were skipped comparisons.", listener.getSkippedComparisons() == 0);
    if (!diff.similar())
    {
      DetailedDiff details = new DetailedDiff(diff);
      @SuppressWarnings("rawtypes")
      List differences = details.getAllDifferences();
      StringBuilder sb = new StringBuilder();
      for (Object difference : differences)
      {
    	Difference d = (Difference)difference;
    	// Only print the real differences, not the similarities
    	if (!d.isRecoverable()) 
    	{
    		sb.append(" - ").append(difference.toString());
    	}
      }

      Assert.assertTrue("The expected xml was different: " + sb.toString(), diff.similar());
    }
  }

  private static String decodeURLtoString(URL url) {
    try {
      return new File(url.toURI()).getAbsolutePath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e); 
    }
  }
}
