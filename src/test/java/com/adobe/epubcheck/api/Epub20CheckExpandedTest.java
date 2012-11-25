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

public class Epub20CheckExpandedTest extends AbstractEpubCheckTest {


	public Epub20CheckExpandedTest() {
		super("/20/expanded/");
	}

	@Test
	public void testValidateEPUBPLoremBasic() {
		testValidateDocument("valid/lorem/lorem-basic", 0, 0, "valid/lorem/lorem-basic.txt");
	}

	@Test
	public void testValidateEPUBMimetype() {
		testValidateDocument("invalid/lorem-mimetype", 2, 0, "invalid/lorem-mimetype.txt");
	}

	@Test
	public void testValidateEPUBUidSpaces() {
		//ascertain that leading/trailing space in 2.0 id values is accepted
		//issue 163
		testValidateDocument("valid/lorem-uidspaces", 0, 0, "valid/lorem-uidspaces.txt");
	}
	
	@Test
	public void testValidateEPUB20_circularFallback() {
		testValidateDocument("invalid/fallbacks-circular/", 5, 0, "invalid/fallbacks-circular.txt");
	}
	
	@Test
	public void testValidateEPUB20_okFallback() {
		testValidateDocument("valid/fallbacks/", 0, 0, "valid/fallbacks.txt");
	}
	
	@Test
	public void testValidateEPUB20_loremBasicDual() {
		testValidateDocument("valid/lorem-basic-dual/", 0, 0, "valid/lorem-basic-dual.txt");
	}
	
	@Test
	public void testValidateEPUB20_guideWithNcx() {
		testValidateDocument("valid/lorem-dual-guide/", 1, 0, "valid/lorem-dual-guide.txt");
	}
	
	@Test
	public void testValidateEPUB20_guideBrokenLink() {
		testValidateDocument("invalid/lorem-dual-guide/", 3, 0, "invalid/lorem-dual-guide.txt");
	}

	@Test
	public void testValidateEPUB20_customNsAttr() {
		testValidateDocument("invalid/custom-ns-attr/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB20_issue205() {
		testValidateDocument("valid/issue205/", 0, 0);
	}
	
	@Test
	public void testValidateEPUB20_issue182() {
		//repeated spine items
		testValidateDocument("invalid/issue182/", 1, 0);
	}
}
