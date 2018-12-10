package com.adobe.epubcheck.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.junit.Test;

import com.adobe.epubcheck.tool.EpubChecker;

@Deprecated
public class CLITest
{
  private static String epubPath = "/30/epub/";
  private static String expPath = "/30/expanded/";
  private static String singlePath = "/30/single/";

  private static String epubApiPath = "/com/adobe/epubcheck/test/package/";

	
	@Test
	public void testQuietRun()
  {
  	PrintStream outOrig = System.out;
		CountingOutStream outCount = new CountingOutStream();
		System.setOut(new PrintStream(outCount));
		String epubFilePath = getAbsoluteBasedir(epubPath + "valid/lorem.epub");
    EpubChecker epubChecker = new EpubChecker();
		int result = epubChecker.run(new String[]{ epubFilePath, "--quiet", "--failonwarnings" });
		System.setOut(outOrig);
		assertEquals(0, result);
		// System.err.println("Output [" + outCount.getValue() + "]");
		assertEquals("Output [" + outCount.getValue() + "]", 0, outCount.getCounts());
	}

	@Test
	public void testQuietRunWithOutput()
  {
		final String xmlOutFileName = "outfile4.xml";
		final File xmlOut = new File(xmlOutFileName);
		if(xmlOut.exists()) xmlOut.delete();

		PrintStream outOrig = System.out;
		CountingOutStream outCount = new CountingOutStream();
		System.setOut(new PrintStream(outCount));
		String epubFilePath = getAbsoluteBasedir(epubPath + "valid/lorem.epub");
    EpubChecker epubChecker = new EpubChecker();
    int result = epubChecker.run(new String[]{ epubFilePath, "--quiet", "--out", xmlOutFileName});
		System.setOut(outOrig);
		assertEquals(0, result);
		// System.err.println("Output [" + outCount.getValue() + "]");
		assertEquals("Output [" + outCount.getValue() + "]", 0, outCount.getCounts());

		assertTrue(xmlOut.exists());
		if(xmlOut.exists())
    {
      xmlOut.delete();
    }
  }

  @Test
  public void testInvalidOption()
  {
    /* Make sure an unrecognized option generates an error. */
    assertEquals(1, run(new String[]{epubPath + "valid/lorem.epub", "--invalidoption"}));
  }

        
  @Test
  public void testLocalizationWithValidLocaleAndLocalization()
  {
        PrintStream outOrig = System.out;
        CountingOutStream stream = new CountingOutStream();
        System.setOut(new PrintStream(stream));
        EpubChecker epubChecker = new EpubChecker();
        epubChecker.run(new String[]{
            getAbsoluteBasedir(epubPath + "valid/lorem.epub"),
            "--locale", "fr-FR"
        });
        System.setOut(outOrig);
        assertTrue("Valid Locale should use correct language.", stream.getValue().indexOf("faites") >= 0);
  }
  
   @Test
  public void testLocalizationWithValidLocaleAndNoLocalization()
  {
        Locale temp = Locale.getDefault();
        Locale.setDefault(Locale.FRANCE);
        PrintStream outOrig = System.out;
        CountingOutStream stream = new CountingOutStream();
        System.setOut(new PrintStream(stream));
        EpubChecker epubChecker = new EpubChecker();
        epubChecker.run(new String[]{
            getAbsoluteBasedir(epubPath + "valid/lorem.epub"),
            "--locale", "ar-eg"
        });
        System.setOut(outOrig);
        assertTrue("Valid Locale without translation should fallback to JVM default.", stream.getValue().indexOf("faites en utilisant") >= 0);
        Locale.setDefault(temp);
  }
  
  @Test
  public void testLocalizationWithSkippedLocale()
  {        
        assertEquals("Skipped argument to --lang should fail.", 1, run(new String[]{
            getAbsoluteBasedir(epubPath + "valid/lorem.epub"),
            "--locale", "--bad"
        }));
  }
  
  @Test
  public void testLocalizationWithUnknownLocale()
  {     
        // Rather than attempt to validate locales or match them with available
        // translations, it seems preferrable to follow the pattern that the JDK
        // has set and allow it to naturally fall back to the default (JVM) default.
        Locale previousLocale = Locale.getDefault();
        try {
          Locale.setDefault(Locale.FRANCE);
          PrintStream outOrig = System.out;
          CountingOutStream stream = new CountingOutStream();
          System.setOut(new PrintStream(stream));
          EpubChecker epubChecker = new EpubChecker();
          epubChecker.run(new String[]{
              getAbsoluteBasedir(epubPath + "valid/lorem.epub"),
              "--locale", "foobar"
          });
          System.setOut(outOrig);
          assertTrue("Invalid Locale should use JVM default.", stream.getValue().indexOf("faites en utilisant") >= 0);
        } finally {
          Locale.setDefault(previousLocale);
        }
        
  }
  
  @Test
  public void testLocalizationWithNoLocale()
  {
        assertEquals("--locale with no language tag is clearly an error, fail with message.",
            1, run(new String[]{
            getAbsoluteBasedir(epubPath + "valid/lorem.epub"),
            "--locale"
        }));
  }
  
  private int run(String[] args, boolean verbose)
  {
    PrintStream outOrig = System.out;
    PrintStream errOrig = System.err;
    if (!verbose)
    {
      System.setOut(new NullPrintStream());
      System.setErr(new NullPrintStream());
    }

    if (args != null)
    {
			args[0] = getAbsoluteBasedir(args[0]);
    }
    else
    {
      return 1;
    }
    EpubChecker checker = new EpubChecker();
    int result = checker.run(args);
    System.setOut(outOrig);
    System.setErr(errOrig);
    return result;
  }

  public int run(String[] args)
  {
    return run(args, false);
  }

  private String getAbsoluteBasedir(String base)
  {
	  try {
		  URL fileURL = this.getClass().getResource(base);
		  if(fileURL != null) {
			  String filePath = new File(fileURL.toURI()).getAbsolutePath();
			  return filePath;
		  } else {
			  return base;
		  }
	  } catch (URISyntaxException e) {
		  throw new IllegalStateException("Cannot find test file", e);
	  }
  }

  class CountingOutStream extends OutputStream
  {
		int counts;
		StringBuilder sb = new StringBuilder();
		
    public int getCounts()
    {
      return counts;
    }

    public String getValue()
    {
      return sb.toString();
    }
		
    @Override
    public void write(int b)
    {
      sb.append((char)b);
        counts++;
    }
  }
	
	class NullPrintStream extends PrintStream
  {
		public NullPrintStream()
    {
			super(new OutputStream()
        {
          @Override
          public void write(int b) throws IOException
          {
          }
        });
    }
  }
}	
	
