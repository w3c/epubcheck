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

import java.util.Collections;

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
    testValidateDocument("valid/lorem/lorem-basic", "valid/lorem/lorem-basic.txt");
  }

  @Test
  public void testValidateEPUBMimetype()
  {
    Collections.addAll(expectedErrors, MessageId.PKG_007);
    testValidateDocument("invalid/lorem-mimetype", "invalid/lorem-mimetype.txt");
  }

  @Test
  public void testValidateEPUBBadPathInNCX()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/lorem-ncx-badpath");
  }

  @Test
  public void testValidateEPUBBadNcxPageTargetType()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/ncx-pagetarget-type");
  }

  @Test
  public void testValidateEPUBUidSpaces()
  {
    // ascertain that leading/trailing space in 2.0 id values is accepted
    // issue 163
    testValidateDocument("valid/lorem-uidspaces", "valid/lorem-uidspaces.txt");
  }

  @Test
  public void testValidateEPUB20_circularFallback()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_045, MessageId.OPF_045, MessageId.OPF_045,
        MessageId.OPF_045, MessageId.MED_003);
    testValidateDocument("invalid/fallbacks-circular/", "invalid/fallbacks-circular.txt");
  }

  @Test
  public void testValidateEPUB20_nonResolvingFallback()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_040, MessageId.MED_003);
    testValidateDocument("invalid/fallbacks-nonresolving/");
  }

  @Test
  public void testValidateEPUB20_okFallback()
  {
    testValidateDocument("valid/fallbacks/", "valid/fallbacks.txt");
  }

  @Test
  public void testValidateEPUB20_loremBasicDual()
  {
    testValidateDocument("valid/lorem-basic-dual/", "valid/lorem-basic-dual.txt");
  }

  @Test
  public void testValidateEPUB20_guideWithNcx()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_032);
    testValidateDocument("valid/lorem-dual-guide/", "valid/lorem-dual-guide.txt");
  }

  @Test
  public void testValidateEPUB20_guideBrokenLink()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_032, MessageId.OPF_031, MessageId.RSC_007);
    testValidateDocument("invalid/lorem-dual-guide/", "invalid/lorem-dual-guide.txt");
  }

  @Test
  public void testValidateEPUB20_customNsAttr()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/custom-ns-attr/");
  }

  @Test
  public void testValidateEPUB20_issue205()
  {
    testValidateDocument("valid/issue205/");
  }

  @Test
  public void testValidateEPUB20_issue182()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_034);
    // repeated spine items
    testValidateDocument("invalid/issue182/");
  }

  @Test
  public void testValidateEPUB20_issue256()
  {
    // Ignore .DS_Store, ._DS_Store, Thumbs.db, ehthumbs.db, .svn/, .git/ files
    // in expanded mode. Valid EPUB expected.
    testValidateDocument("valid/issue256/");
  }

  @Test
  public void testValidateEPUB20_issue267()
  {
    testValidateDocument("valid/issue267/", "valid/issue267.txt");
  }

  @Test
  public void testIssue332()
  {
    testValidateDocument("valid/issue332-idspaces");
  }

  @Test
  public void testIssue329_IDSpaces()
  {
    // expectedWarnings.add(MessageId.NCX_004); // Now USAGE
    testValidateDocument("invalid/ncx-uid-spaces");
  }

  @Test
  public void testIssue329_NonMatchingId()
  {
    // expectedWarnings.add(MessageId.NCX_001); // Now USAGE
    testValidateDocument("invalid/ncx-uid-nomatch");
  }

  @Test
  public void testXHTMLExtension()
  {
    Collections.addAll(expectedWarnings, MessageId.HTM_014);
    testValidateDocument("invalid/xhtml-extension");
  }

  @Test
  public void testXHTMLDoctype()
  {
    // 1 error for "FOO" public ID in lorem 1
    // 1 error for HTML5 doctype in lorem2
    Collections.addAll(expectedErrors, MessageId.HTM_004, MessageId.HTM_004);
    testValidateDocument("invalid/xhtml-doctype");
  }
  
  @Test
  public void testMissingResource()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_007);
    testValidateDocument("invalid/missing-resource");
  }
}
