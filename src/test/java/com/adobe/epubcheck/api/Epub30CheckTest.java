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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;

public class Epub30CheckTest extends AbstractEpubCheckTest
{


  public Epub30CheckTest()
  {
    super("/30/epub/");
  }
  // TODO -- check for fallback cycles

  @Test
  public void testValidateEPUBPFallbackCycle()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_045, MessageId.OPF_044, MessageId.OPF_045, MessageId.OPF_045, MessageId.OPF_045, MessageId.OPF_045);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/fallback-cycle.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBPvalid30()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem.epub", expectedErrors, expectedWarnings, "valid/lorem.txt");
  }

  @Test
  public void testValidateEPUBTestSvg()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/test_svg.epub", expectedErrors, expectedWarnings, "valid/test_svg.txt");
  }

  @Test
  public void testValidateEPUBInvalidNcx()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_012, MessageId.RSC_012);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/invalid-ncx.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBMp3()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/mp3-in-manifest.epub", expectedErrors, expectedWarnings, "valid/mp3-in-manifest.txt");
  }

  @Test
  public void testValidateEPUBInvalidMp3()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_043);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/mp3-in-spine-no-fallback.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBMp3WithFallback()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/mp3-with-fallback.epub", expectedErrors, expectedWarnings, "valid/mp3-with-fallback.txt");
  }

  @Test
  public void testValidateEPUBFontNoFallback()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_043);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/font_no_fallback.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBFontFallbackChain()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/font_fallback_chain.epub", expectedErrors, expectedWarnings, "valid/font_fallback_chain.txt");
  }

  @Test
  public void testValidateEPUBvalid30()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem.epub", expectedErrors, expectedWarnings, "valid/lorem.txt");
  }

  @Test
  public void testValidateEPUB30_xhtmlsch()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    // 1 schematron error from xhtml validation
    testValidateDocument("invalid/lorem-xht-sch-1.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30_xhtmlrng()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    // 1 rng error from xhtml validation
    testValidateDocument("invalid/lorem-xht-rng-1.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30_navInvalid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    // invalid nav issue reported by MattG
    testValidateDocument("invalid/nav-invalid.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30ValidExtension1()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.PKG_016);
    testValidateDocument("valid/extension-1.ePub", expectedErrors, expectedWarnings, "valid/extension-1.txt");
  }

  @Test
  public void testValidateEPUB30CSSProfile()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //issue145; CSS3 pseudoselectors causing css2 lexers to bail out
    testValidateDocument("valid/issue145.epub", expectedErrors, expectedWarnings, "valid/issue145.txt");
  }

  @Test
  public void testValidateEPUB30Issue158()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //bad warning message, this should pass without warnings
    testValidateDocument("valid/issue158.epub", expectedErrors, expectedWarnings, "valid/issue158.txt");
  }

  @Test
  public void testValidateEPUB30Issue137a()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_006);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("invalid/issue137a.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30Issue137b()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_006);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("invalid/issue137b.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30specValid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/epub30-spec.epub", expectedErrors, expectedWarnings, "valid/epub30-spec.txt");
  }

  @Test
  public void testValidateEPUB30Issue203()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.HTM_004);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.HTM_015);
    testValidateDocument("invalid/issue203.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30Issue176()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_001, MessageId.RSC_001, MessageId.RSC_001, MessageId.RSC_001);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.HTM_016, MessageId.HTM_016, MessageId.HTM_016);
    testValidateDocument("invalid/issue176.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30Issue221()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.CSS_008, MessageId.CSS_008, MessageId.RSC_007, MessageId.RSC_007, MessageId.RSC_007, MessageId.RSC_007, MessageId.RSC_007, MessageId.RSC_007);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //syntax error in css that should not mask font-face
    testValidateDocument("invalid/issue221.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30FontObfuscation()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.CSS_017);
    testValidateDocument("valid/font-obfuscation.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30CFI()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/georgia-cfi.epub", expectedErrors, expectedWarnings);
  }

	@Test
	public void testFilenameContainsSpacesIssue239() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/issue239.epub", expectedErrors, expectedWarnings);
	}
	
	@Test
	public void testDuplicateZipEntriesIssue265() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
     List<MessageId> expectedWarnings = new ArrayList<MessageId>();

    // duplicate entries should raise an error
		testValidateDocument("invalid/issue265.epub", expectedErrors, expectedWarnings);
	}
	
	@Test
	public void testDuplicateZipEntriesIssue265b() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.PKG_012, MessageId.OPF_061, MessageId.OPF_003, MessageId.PKG_012);
    // non-unique entry names (after NFC normalization) should raise a warning
		testValidateDocument("invalid/issue265b.epub", expectedErrors, expectedWarnings);
	}
	
	@Test
	public void testDuplicateZipEntriesIssue265c() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_060);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    // non-unique entry names (after case normalization) should raise an error
		testValidateDocument("invalid/issue265c.epub", expectedErrors, expectedWarnings);
	}
	
	@Test
	public void testIssue262() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue262.epub", expectedErrors, expectedWarnings);
	}
	@Test
	public void testIssue271() { 
		// Adobe page template xpgt with correct css fallback
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue271_xpgt_correctFallback.epub", expectedErrors, expectedWarnings);
	}
	
}
