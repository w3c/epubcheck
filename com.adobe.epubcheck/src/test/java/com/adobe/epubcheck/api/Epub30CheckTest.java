/*
 * Copyright (c) 2011 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.api;

import org.junit.Test;

public class Epub30CheckTest extends AbstractEpubCheckTest {


	public Epub30CheckTest() {
		super("/30/epub/");
	}
    // TODO -- check for fallback cycles
	
	@Test public void testValidateEPUBPFallbackCycle() {
	  testValidateDocument("invalid/fallback-cycle.epub", 6, 0); 
	}
	 
	@Test
	public void testValidateEPUBPvalid30() {
		testValidateDocument("valid/lorem.epub", 0, 0, "valid/lorem.txt");
	}

	@Test
	public void testValidateEPUBTestSvg() {
		testValidateDocument("valid/test_svg.epub", 0, 0, "valid/test_svg.txt");
	}

	@Test
	public void testValidateEPUBInvalidNcx() {
		testValidateDocument("invalid/invalid-ncx.epub", 2, 0);
	}

	@Test
	public void testValidateEPUBMp3() {
		testValidateDocument("valid/mp3-in-manifest.epub", 0, 0, "valid/mp3-in-manifest.txt");
	}

	@Test
	public void testValidateEPUBInvalidMp3() {
		testValidateDocument("invalid/mp3-in-spine-no-fallback.epub", 1, 0);
	}

	@Test
	public void testValidateEPUBMp3WithFallback() {
		testValidateDocument("valid/mp3-with-fallback.epub", 0, 0, "valid/mp3-with-fallback.txt");
	}

	@Test
	public void testValidateEPUBFontNoFallback() {
		testValidateDocument("invalid/font_no_fallback.epub", 1, 0);
	}

	@Test
	public void testValidateEPUBFontFallbackChain() {
		testValidateDocument("valid/font_fallback_chain.epub", 0, 0, "valid/font_fallback_chain.txt");
	}

	@Test
	public void testValidateEPUBvalid30() {
		testValidateDocument("valid/lorem.epub", 0, 0, "valid/lorem.txt");
	}

	@Test
	public void testValidateEPUB30_xhtmlsch() {
		// 1 schematron error from xhtml validation
		testValidateDocument("invalid/lorem-xht-sch-1.epub", 1, 0);
	}

	@Test
	public void testValidateEPUB30_xhtmlrng() {
		// 1 rng error from xhtml validation
		testValidateDocument("invalid/lorem-xht-rng-1.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_navInvalid() {
		// invalid nav issue reported by MattG
		testValidateDocument("invalid/nav-invalid.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30ValidExtension1() { 
		testValidateDocument("valid/extension-1.ePub", 0, 1, "valid/extension-1.txt");
	}
	
	@Test
	public void testValidateEPUB30CSSProfile() { 
		//issue145; CSS3 pseudoselectors causing css2 lexers to bail out
		testValidateDocument("valid/issue145.epub", 0, 0, "valid/issue145.txt");
	}
	
	@Test
	public void testValidateEPUB30Issue158() { 
		//bad warning message, this should pass without warnings
		testValidateDocument("valid/issue158.epub", 0, 0, "valid/issue158.txt");
	}
	
	@Test
	public void testValidateEPUB30Issue137a() { 
		testValidateDocument("invalid/issue137a.epub", 1, 1);
	}
	
	@Test
	public void testValidateEPUB30Issue137b() { 
		testValidateDocument("invalid/issue137b.epub", 1, 1);
	}
	
	@Test
	public void testValidateEPUB30specValid() { 
		testValidateDocument("valid/epub30-spec.epub", 0, 0, "valid/epub30-spec.txt");
	}
	
	@Test
	public void testValidateEPUB30Issue176() { 
		testValidateDocument("invalid/issue176.epub", 4, 0);
	}
	
	@Test
	public void testValidateEPUB30Issue203() { 
		testValidateDocument("invalid/issue203.epub", 2, 0);
	}
	
	@Test
	public void testValidateEPUB30Issue221() { 
		//syntax error in css that should not mask font-face 
		testValidateDocument("invalid/issue221.epub", 6, 1);
	}
	
	@Test
	public void testValidateEPUB30FontObfuscation() { 
		testValidateDocument("valid/font-obfuscation.epub", 0, 0);
	}
	
	@Test
	public void testValidateEPUB30CFI() { 
		//for now, just checking that there are no false negatives
		testValidateDocument("valid/georgia-cfi.epub", 0, 0);
	}
			
}
