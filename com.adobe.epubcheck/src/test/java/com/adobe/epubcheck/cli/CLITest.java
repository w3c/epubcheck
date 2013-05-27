package com.adobe.epubcheck.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

import org.junit.Test;

import com.adobe.epubcheck.tool.Checker;

public class CLITest {
	private static String epubPath = "/30/epub/";
	private static String expPath = "/30/expanded/";
	private static String singlePath = "/30/single/";
	
	@Test
	public void testNPE() {		
		assertEquals(1, run(null));		
	}
	
	@Test
	public void testValidEPUB() {		
		assertEquals(0, run(new String[]{epubPath + "valid/lorem.epub"}));		
	}
	
	@Test
	public void testValidEPUBArchive() {		
		assertEquals(0, run(new String[]{expPath + "valid/lorem-basic-ncx/", "-mode", "exp", "-save"}));	
		File out = new File("lorem-basic-ncx.epub");
		assertTrue(out.exists());
		if(out.exists()) out.delete();
	}
	
	@Test
	public void testInvalidEPUB() {		
		assertEquals(1, run(new String[]{epubPath + "invalid/lorem-xht-sch-1.epub"}));		
	}
	
	@Test
	public void testValidExp() {		
		assertEquals(0, run(new String[]{expPath + "valid/lorem-basic/", "-mode", "exp"}));		
	}
	
	@Test
	public void testInvalidExp() {		
		assertEquals(1, run(new String[]{expPath + "invalid/lorem-xhtml-rng-1/", "-mode", "exp"}));		
	}
	
	@Test
	public void testValidSingle() {		
		assertEquals(0, run(new String[]{singlePath + "nav/valid/nav001.xhtml", "-mode", "nav"}));		
	}
	
	@Test
	public void testInvalidSingle() {		
		assertEquals(1, run(new String[]{singlePath + "nav/invalid/noTocNav.xhtml", "-mode", "nav"}));		
	}
	
	
	@Test
	public void testValidExtension1() { 
		assertEquals(1, run(new String[]{epubPath + "valid/extension-1.ePub"}));
	}
		
	private int run(String[] args, boolean verbose) {
		PrintStream outOrig = System.out;
		PrintStream errOrig = System.err;
		if(!verbose) {			
			System.setOut(new NullPrintStream());
			System.setErr(new NullPrintStream());
		}
		if (args!=null){
			URL fileURL = this.getClass().getResource(args[0]);
			args[0]=fileURL!=null?fileURL.getPath():args[0];
		}
		int result = Checker.run(args);		
		System.setOut(outOrig);
		System.setErr(errOrig);		
		return result;
	}
	
	private int run(String[] args) {
		return run(args, false);
	}
	
	class NullPrintStream extends PrintStream {
		public NullPrintStream() {
			super(new OutputStream() {				
				@Override
				public void write(int b) throws IOException {
					
				}
			});
		}		
	}
}	
	
