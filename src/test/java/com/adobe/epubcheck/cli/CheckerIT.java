package com.adobe.epubcheck.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.google.common.collect.ObjectArrays;

public class CheckerIT
{

  private static final String[] cmd = new String[] { "java", "-jar", "target/epubcheck.jar" };
  private static String valid30EPUB = "src/test/resources/epub3/02-epub-publication-conformance/files/";

  @Test
  public void testValidEPUB()
  {
    try
    {
      Process process = run(valid30EPUB + "minimal.epub");
      InputStream stderr = process.getErrorStream();
      process.waitFor();
      assertEmpty(stderr);
      assertEquals(0, process.exitValue());
    } catch (Exception e)
    {
      fail(e.getMessage());
    }
  }

  private static Process run(String epub)
  {
    try
    {
      ProcessBuilder builder = new ProcessBuilder(ObjectArrays.concat(cmd, epub));
      builder.directory(new File(CheckerIT.class.getResource(".").toURI().resolve("../../../../../../")));
      return builder.start();
    } catch (Exception e)
    {
      fail(e.getMessage());
      return null;
    }
  }

  private static void assertEmpty(InputStream inputStream)
  {
    try
    {
      if (inputStream.read() == -1) return;
      fail("stream is not empty");
    } catch (IOException e)
    {
      fail(e.getMessage());
    }
  }
}
