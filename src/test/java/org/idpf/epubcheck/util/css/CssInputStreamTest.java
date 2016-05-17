package org.idpf.epubcheck.util.css;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class CssInputStreamTest {
	public static final String PATH_TEST_BASE = "/css/";

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
		URL fileURL = this.getClass().getResource(PATH_TEST_BASE + file);
		CssSource cs = new CssSource(fileURL.toString(), fileURL.openStream());
		return cs.getInputStream();
	}
	
}
