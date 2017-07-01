package com.adobe.epubcheck.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.adobe.epubcheck.tool.EpubChecker;

public class CLITest
{
  private static String epubPath = "/30/epub/";
  private static String expPath = "/30/expanded/";
  private static String singlePath = "/30/single/";

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
	
