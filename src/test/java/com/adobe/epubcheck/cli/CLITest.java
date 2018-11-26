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

public class CLITest
{
  private static String epubPath = "/30/epub/";
  private static String expPath = "/30/expanded/";
  private static String singlePath = "/30/single/";

  private static String epubApiPath = "/com/adobe/epubcheck/test/package/";

  @Test
  public void testNPE()
  {
    assertEquals(1, run(null));
  }

  //@Test // STA - test passes in UI but fails in Maven
  public void testValidEPUB()
  {
    assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub"}));
  }
  
  @Test
  public void testValidEPUBArchive()
  {
    assertEquals(0, run(new String[]{expPath + "valid/lorem-basic-ncx/", "-mode", "exp", "-save"}));
		
		// since issue #255 we need the absolute path to check the saved outfile
		File baseDirParent = new File(getAbsoluteBasedir(expPath + "valid/lorem-basic-ncx/")).getParentFile();
		File out = new File(baseDirParent + File.separator + "lorem-basic-ncx.epub");
		
    assertTrue(out.exists());
    if (out.exists())
    {
      out.delete();
    }
  }

  @Test
  public void testInvalidEPUB()
  {   
    assertEquals(1, run(new String[]{epubPath + "invalid/lorem-xht-sch-1.epub"}));
  }

  @Test
  public void testValidExp()
  {
    assertEquals(0, run(new String[]{expPath + "valid/lorem-basic/", "-mode", "exp"}));
  }

  @Test
  public void testInvalidExp()
  {
    assertEquals(1, run(new String[]{expPath + "invalid/lorem-xhtml-rng-1/", "-mode", "exp"}));
  }

  @Test
  public void testValidSingle()
  {
    assertEquals(0, run(new String[]{singlePath + "nav/valid/nav001.xhtml", "-mode", "nav"},true));
  }

  @Test
  public void testInvalidSingle()
  {
    assertEquals(1, run(new String[]{singlePath + "nav/invalid/noTocNav.xhtml", "-mode", "nav"}));
  }

	@Test
	public void testExtension1()
  {
		assertEquals(0, run(new String[]{epubPath + "invalid/extension-1.ePub"}));
	}

    @Test
    public void testExtension2()
    {
        assertEquals(0, run(new String[]{epubApiPath + "wrong_extension.zip", "--profile", "default"}));
    }

    @Test
    public void testExtension3()
    {
        assertEquals(0, run(new String[]{epubApiPath + "wrong_extension_v3.zip", "--profile", "default"}));
    }

    @Test
    public void testExtension4()
    {
        assertEquals(0, run(new String[]{epubApiPath + "wrong_extension_v3", "--profile", "default"}));
    }

	@Test
	public void testOutputXMLCreation()
  {
		File xmlOut1 = new File("outfile.xml");
		if(xmlOut1.exists()) xmlOut1.delete();
		
		assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub", "-out", "outfile.xml"}));	
		
		assertTrue(xmlOut1.exists());
		if(xmlOut1.exists()) xmlOut1.delete();
	}

  @Test
	public void testOutputXMLCreation_ModeExpanded()
  {
		File xmlOut2 = new File("outfile2.xml");
		if(xmlOut2.exists())
    {
      xmlOut2.delete();
    }
		
		assertEquals(1, run(new String[]{expPath + "invalid/lorem-xhtml-rng-1/", "-mode", "exp", "-out", "outfile2.xml"}));	
		
		assertTrue(xmlOut2.exists());
		if(xmlOut2.exists())
    {
      xmlOut2.delete();
    }
	}
	
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
  public void testHelpRun1()
  {
    assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub", "--help"}));
  }

  @Test
  public void testHelpRun2()
  {
    assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub", "-h"}));
  }

  @Test
  public void testHelpRun3()
  {
    assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub", "-?"}));
  }

  @Test
  public void testVersionRun1()
  {
    assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub", "--version"}));
  }

  @Test
  public void testVersionRun2()
  {
    assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub", "-version"}));
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
	
