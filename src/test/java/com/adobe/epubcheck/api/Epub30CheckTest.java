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

public class Epub30CheckTest extends AbstractEpubCheckTest
{

  public Epub30CheckTest()
  {
    super("/30/epub/");
  }

  @Test
  public void testValidateEPUBPFallbackCycle()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_045, MessageId.OPF_044, MessageId.OPF_045,
        MessageId.OPF_045, MessageId.OPF_045, MessageId.OPF_045);
    testValidateDocument("invalid/fallback-cycle.epub");
  }

  @Test
  public void testValidateEPUBPvalid30()
  {
    testValidateDocument("valid/lorem.epub", "valid/lorem.txt");
  }

  @Test
  public void testValidateEPUBTestSvg()
  {
    testValidateDocument("valid/test_svg.epub", "valid/test_svg.txt");
  }

  @Test
  public void testValidateEPUBInvalidNcx()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_012, MessageId.RSC_012);
    testValidateDocument("invalid/invalid-ncx.epub");
  }

  @Test
  public void testValidateEPUBValidNcx()
  {
    testValidateDocument("valid/valid-ncx.epub");
  }

  @Test
  public void testValidateEPUBMp3()
  {
    testValidateDocument("valid/mp3-in-manifest.epub", "valid/mp3-in-manifest.txt");
  }

  @Test
  public void testValidateEPUBInvalidMp3()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_043);
    testValidateDocument("invalid/mp3-in-spine-no-fallback.epub");
  }

  @Test
  public void testValidateEPUBMp3WithFallback()
  {
    testValidateDocument("valid/mp3-with-fallback.epub", "valid/mp3-with-fallback.txt");
  }

  @Test
  public void testValidateEPUBFontNoFallback()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_043);
    testValidateDocument("invalid/font_no_fallback.epub");
  }

  @Test
  public void testValidateEPUBFontFallbackChain()
  {
    testValidateDocument("valid/font_fallback_chain.epub", "valid/font_fallback_chain.txt");
  }

  @Test
  public void testValidateEPUBvalid30()
  {
    testValidateDocument("valid/lorem.epub", "valid/lorem.txt");
  }

  @Test
  public void testValidateEPUB30_xhtmlsch()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // 1 schematron error from xhtml validation
    testValidateDocument("invalid/lorem-xht-sch-1.epub");
  }

  @Test
  public void testValidateEPUB30_xhtmlrng()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // 1 rng error from xhtml validation
    testValidateDocument("invalid/lorem-xht-rng-1.epub");
  }

  @Test
  public void testValidateEPUB30_navInvalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // invalid nav issue reported by MattG
    testValidateDocument("invalid/nav-invalid.epub");
  }

  @Test
  public void testValidateEPUB30Extension1()
  {
    Collections.addAll(expectedWarnings, MessageId.PKG_016);
    testValidateDocument("invalid/extension-1.ePub", "invalid/extension-1.txt");
  }

  @Test
  public void testValidateEPUB30CSSProfile()
  {
    // issue145; CSS3 pseudoselectors causing css2 lexers to bail out
    testValidateDocument("valid/issue145.epub", "valid/issue145.txt");
  }

  @Test
  public void testValidateEPUB30Issue158()
  {
    // bad warning message, this should pass without warnings
    testValidateDocument("valid/issue158.epub", "valid/issue158.txt");
  }

  @Test
  public void testValidateEPUB30Issue137a()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_006);
    testValidateDocument("invalid/issue137a.epub");
  }

  @Test
  public void testValidateEPUB30Issue137b()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_006);
    testValidateDocument("invalid/issue137b.epub");
  }

  @Test
  public void testValidateEPUB30specValid()
  {
    testValidateDocument("valid/epub30-spec.epub", "valid/epub30-spec.txt");
  }

  @Test
  public void testValidateEPUB30Issue203()
  {
    Collections.addAll(expectedErrors, MessageId.HTM_004);
    testValidateDocument("invalid/issue203.epub");
  }

  @Test
  public void testValidateEPUB30Issue221()
  {
    Collections.addAll(expectedErrors, MessageId.CSS_008, MessageId.RSC_007, MessageId.RSC_007,
        MessageId.RSC_007, MessageId.RSC_007, MessageId.RSC_007, MessageId.RSC_007, MessageId.CSS_020);
    // syntax error in css that should not mask font-face
    testValidateDocument("invalid/issue221.epub");
  }

  @Test
  public void testValidateEPUB30Issue289()
  {
    Collections.addAll(expectedInfos, MessageId.HTM_053);
    testValidateDocument("valid/issue289.epub");
  }

  @Test
  public void testValidateEPUB30FontObfuscation()
  {
    testValidateDocument("valid/font-obfuscation.epub");
  }

  @Test
  public void testValidateEPUB30CFI()
  {
    testValidateDocument("valid/georgia-cfi.epub");
  }

  @Test
  public void testFilenameContainsSpacesIssue239()
  {
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/issue239.epub");
  }

  @Test
  public void testDuplicateZipEntriesIssue265()
  {
    // duplicate entries should raise an error
    Collections.addAll(expectedErrors, MessageId.OPF_060);
    testValidateDocument("invalid/issue265.epub");
  }

  @Test
  public void testDuplicateZipEntriesIssue265b()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_061, MessageId.OPF_003, MessageId.PKG_012,
        MessageId.PKG_012);
    // non-unique entry names (after NFC normalization) should raise a warning
    testValidateDocument("invalid/issue265b.epub");
  }

  @Test
  public void testDuplicateZipEntriesIssue265c()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_060);
    // non-unique entry names (after case normalization) should raise an error
    testValidateDocument("invalid/issue265c.epub");
  }

  @Test
  public void testExtendedFieldofZip()
  {
    Collections.addAll(expectedErrors, MessageId.PKG_005);
    testValidateDocument("invalid/lorem-zip64.epub");
  }

  @Test
  public void testIssue262()
  {
    testValidateDocument("valid/issue262.epub");
  }

  @Test
  public void testIssue271()
  {
    // Adobe page template xpgt with correct css fallback
    testValidateDocument("valid/issue271_xpgt_correctFallback.epub");
  }

  @Test
  public void testEdupub_Pagination()
  {
    testValidateDocument("valid/edupub-pagination.epub", EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupub_PaginationInvalid_NoPageList()
  {
    Collections.addAll(expectedErrors, MessageId.NAV_003);
    testValidateDocument("invalid/edupub-pagination-nopagelist.epub", EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupub_PaginationInvalid_NoPageSource()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_066);
    testValidateDocument("invalid/edupub-pagination-nosource.epub", EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupub_ToCInvalid_NoFullToC()
  {
    // TODO re-enable NAV_004 as a WARNING or ERROR when the spec clearer  
    // Collections.addAll(expectedWarnings, MessageId.NAV_004);
    testValidateDocument("invalid/edupub-toc-missing-branches.epub", EPUBProfile.EDUPUB);
  }

  @Test
  public void testValidateEPUB30_Invalid_Missing_NCX_ref()
  {
    Collections.addAll(expectedUsages, MessageId.OPF_059);
    testValidateDocument("invalid/missing-toc-ncx-ref.epub", true, false);
  }

  @Test
  public void testValidateEPUB30_Invalid_Missing_XHTML_ref()
  {
    Collections.addAll(expectedUsages, MessageId.OPF_058);
    testValidateDocument("invalid/missing-toc-xhtml-ref.epub", true, false);
  }

  @Test
  public void testRenditions()
  {
    testValidateDocument("valid/multiple-renditions.epub");
  }

  @Test
  public void testRenditions_Invalid_NoMetadata()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_019);
    testValidateDocument("invalid/multiple-renditions-nometadata.epub");
  }

  @Test
  public void testRenditions_Invalid_UIDMissingModified()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/multiple-renditions-nodctermsmodified.epub");
  }

  @Test
  public void testRenditions_Invalid_BadMediaQuery()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/multiple-renditions-badmediaquery.epub");
  }

  @Test
  public void testRenditions_Invalid_UndefinedSelection()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/multiple-renditions-undefinedselection.epub");
  }

  @Test
  public void testRenditions_Invalid_NoSelection()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("invalid/multiple-renditions-noselection.epub");
  }

  @Test
  public void testRenditions_Invalid_RenditionValueEmpty()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/issue727_rendition-empty.epub");
  }

  @Test
  public void testEdupubRenditions()
  {
    testValidateDocument("valid/edupub-multiple-renditions.epub");
  }

  @Test
  public void testEdupubRenditions_Invalid_NoPubDCType()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/edupub-multiple-renditions-nodctype-pub.epub");
  }

  @Test
  public void testEdupubRenditions_Invalid_NoRenditionDCType()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/edupub-multiple-renditions-nodctype-rendition.epub");
  }
}
