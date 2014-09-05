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

public class Epub20CheckExpandedTest extends AbstractEpubCheckTest
{


  public Epub20CheckExpandedTest()
  {
    super("/20/expanded/");
  }

  @Test
  public void testValidateEPUBPLoremBasic()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem/lorem-basic", expectedErrors, expectedWarnings, "valid/lorem/lorem-basic.txt");
  }

  @Test
  public void testValidateEPUBMimetype()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.PKG_007, MessageId.OPF_022, MessageId.PKG_007, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/lorem-mimetype", expectedErrors, expectedWarnings, "invalid/lorem-mimetype.txt");
  }

  @Test
  public void testValidateEPUBUidSpaces()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //ascertain that leading/trailing space in 2.0 id values is accepted
    //issue 163
    testValidateDocument("valid/lorem-uidspaces", expectedErrors, expectedWarnings, "valid/lorem-uidspaces.txt");
  }

  @Test
  public void testValidateEPUB20_circularFallback()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_045, MessageId.OPF_045, MessageId.OPF_045, MessageId.OPF_045, MessageId.MED_003);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/fallbacks-circular/", expectedErrors, expectedWarnings, "invalid/fallbacks-circular.txt");
  }

  @Test
  public void testValidateEPUB20_okFallback()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/fallbacks/", expectedErrors, expectedWarnings, "valid/fallbacks.txt");
  }

  @Test
  public void testValidateEPUB20_loremBasicDual()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem-basic-dual/", expectedErrors, expectedWarnings, "valid/lorem-basic-dual.txt");
  }

  @Test
  public void testValidateEPUB20_guideWithNcx()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_032);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem-dual-guide/", expectedErrors, expectedWarnings, "valid/lorem-dual-guide.txt");
  }

  @Test
  public void testValidateEPUB20_guideBrokenLink()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_032, MessageId.OPF_031, MessageId.RSC_007);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/lorem-dual-guide/", expectedErrors, expectedWarnings, "invalid/lorem-dual-guide.txt");
  }

  @Test
  public void testValidateEPUB20_customNsAttr()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/custom-ns-attr/", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB20_issue205()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue205/", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB20_issue182()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_034);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //repeated spine items
    testValidateDocument("invalid/issue182/", expectedErrors, expectedWarnings);
  }
	
	@Test
	public void testValidateEPUB20_issue256() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    // Ignore .DS_Store, ._DS_Store, Thumbs.db, ehthumbs.db, .svn/, .git/ files in expanded mode. Valid EPUB expected.
		testValidateDocument("valid/issue256/", expectedErrors, expectedWarnings);
	}

	@Test
	public void testValidateEPUB20_issue267() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue267/", expectedErrors, expectedWarnings, "valid/issue267.txt");
	}
}
