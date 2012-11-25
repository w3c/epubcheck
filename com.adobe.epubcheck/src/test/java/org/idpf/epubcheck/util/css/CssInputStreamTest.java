package org.idpf.epubcheck.util.css;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.idpf.epubcheck.util.css.CssInputStream;
import org.idpf.epubcheck.util.css.CssSource;
import org.junit.Test;

public class CssInputStreamTest {
	public static final String PATH_TEST_BASE = "src/test/resources/css/";
	
	@Test
	public void test10() throws Exception {		
		CssInputStream cis = exec("none.css");
		assertFalse(cis.bom.isPresent());
		assertFalse(cis.charset.isPresent());								
	}
	
	@Test
	public void test20() throws Exception {		
		CssInputStream cis = exec("charset-utf8.css");
		assertFalse(cis.bom.isPresent());
		assertTrue(cis.charset.isPresent());
		assertEquals("UTF-8", cis.charset.get());				
	}
	
	@Test
	public void test30() throws Exception {		
		CssInputStream cis = exec("bom-charset15.css");
		assertTrue(cis.bom.isPresent());
		assertEquals("UTF-8", cis.bom.get());
		assertTrue(cis.charset.isPresent());
		assertEquals("iso-8859-15", cis.charset.get());	
	}
	
	@Test
	public void test40() throws Exception {		
		CssInputStream cis = exec("bom-utf16le.css");
		assertTrue(cis.bom.isPresent());
		assertEquals("UTF-16LE", cis.bom.get());
		assertFalse(cis.charset.isPresent());			
	}
	
	@Test
	public void test50() throws Exception {		
		CssInputStream cis = exec("bom-utf16be.css");
		assertTrue(cis.bom.isPresent());
		assertEquals("UTF-16BE", cis.bom.get());
		assertFalse(cis.charset.isPresent());			
	}

	@Test
	public void test60() throws Exception {		
		CssInputStream cis = exec("bom-utf16be-charset.css");
		assertTrue(cis.bom.isPresent());
		assertEquals("UTF-16BE", cis.bom.get());
		assertTrue(cis.charset.isPresent());	
		assertEquals("UTF-16BE", cis.charset.get());
	}
	
	@Test
	public void test65() throws Exception {		
		CssInputStream cis = exec("bom-utf16le-charset.css");
		assertTrue(cis.bom.isPresent());
		assertEquals("UTF-16LE", cis.bom.get());
		assertTrue(cis.charset.isPresent());	
		assertEquals("UTF-16LE", cis.charset.get());
	}
	
	@Test
	public void test70() throws Exception {		
		CssInputStream cis = exec("charset-malformed.css");
		assertFalse(cis.bom.isPresent());
		assertFalse(cis.charset.isPresent());
	}
	
	@Test
	public void test80() throws Exception {		
		CssInputStream cis = exec("other-rule.css");
		assertFalse(cis.bom.isPresent());
		assertFalse(cis.charset.isPresent());
	}
	
	@Test
	public void test90() throws Exception {		
		CssInputStream cis = exec("charset-empty.css");
		assertFalse(cis.bom.isPresent());
		assertFalse(cis.charset.isPresent());
	}
	
	@Test
	public void test95() throws Exception {		
		CssInputStream cis = exec("other.txt");
		assertFalse(cis.bom.isPresent());
		assertFalse(cis.charset.isPresent());
	}
	
	private CssInputStream exec(String file) throws IOException {
		File f = file(file);
		CssSource cs = new CssSource(f.getName(), new FileInputStream(f));
		return cs.getInputStream();
		
	}
	
	private File file(String file) throws IOException {
		File f = new File(PATH_TEST_BASE + file);
		String abs = f.getCanonicalPath();
		return new File(abs);
	}
}
