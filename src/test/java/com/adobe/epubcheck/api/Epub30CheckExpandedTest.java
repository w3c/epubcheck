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

public class Epub30CheckExpandedTest extends AbstractEpubCheckTest
{

  public Epub30CheckExpandedTest()
  {
    super("/30/expanded/");
  }

  @Test
  public void testValidateEPUBPLoremBasic()
  {
    testValidateDocument("valid/lorem-basic", "valid/lorem-basic.txt");
  }

  @Test
  public void testValidateEPUBPLoremMultipleRenditions()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_019);
    testValidateDocument("valid/lorem-xrenditions");
  }

  @Test
  public void testValidateEPUBPLoremMultipleRenditionsUnmanifested()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_019, MessageId.OPF_003);
    testValidateDocument("invalid/lorem-xrenditions-unmanifested");
  }

  @Test
  public void testValidateEPUBWastelandBasic()
  {
    testValidateDocument("valid/wasteland-basic", "valid/wasteland-basic.txt");
  }

  @Test
  public void testValidateEPUBLoremAudio()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_056);
    testValidateDocument("valid/lorem-audio", "valid/lorem-audio.txt");
  }

  @Test
  public void testValidateEPUBLoremxhtmlrng1()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/lorem-xhtml-rng-1");
  }

  @Test
  public void testValidateEPUBLoremxhtmlsch1()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/lorem-xhtml-sch-1");
  }

  @Test
  public void testValidateEPUBPLoremBasicMathml()
  {
    testValidateDocument("valid/lorem-basic-mathml");
  }

  @Test
  public void testValidateEPUBPMathmlNoAlt()
  {
    Collections.addAll(expectedWarnings, MessageId.ACC_009);
    testValidateDocument("invalid/lorem-mathml-noalt");
  }

  @Test
  public void testValidateEPUBPLoremMimetype()
  {
    Collections.addAll(expectedErrors, MessageId.PKG_007);
    testValidateDocument("invalid/lorem-mimetype");
  }

  @Test
  public void testValidateEPUBPLoremMimetype2()
  {
    Collections.addAll(expectedErrors, MessageId.PKG_007);
    testValidateDocument("invalid/lorem-mimetype-2");
  }

  @Test
  public void testValidateEPUBPLoremBasicSwitch()
  {
    testValidateDocument("valid/lorem-basic-switch", "valid/lorem-basic-switch.txt");
  }

  @Test
  public void testValidateEPUBPLoremLink()
  {
    testValidateDocument("valid/lorem-link", "valid/lorem-link.txt");
  }

  @Test
  public void testValidateEPUBPLoremForeign()
  {
    testValidateDocument("valid/lorem-foreign", "valid/lorem-foreign.txt");
  }

  @Test
  public void testValidateEPUBPLoremObjectFallbacks()
  {
    testValidateDocument("valid/lorem-object-fallbacks", "valid/lorem-object-fallbacks.txt");
  }

  @Test
  public void testValidateEPUBPLoremBindings()
  {
    testValidateDocument("valid/lorem-bindings", "valid/lorem-bindings.txt");
  }

  @Test
  public void testValidateEPUBPLoremInvalidBindings()
  {
    Collections.addAll(expectedErrors, MessageId.MED_002);
    testValidateDocument("invalid/lorem-bindings");
  }

  @Test
  public void testValidateEPUBPLoremPoster()
  {
    testValidateDocument("valid/lorem-poster", "valid/lorem-poster.txt");
  }

  @Test
  public void testValidateEPUBPLoremSvg()
  {
    testValidateDocument("valid/lorem-svg", "valid/lorem-svg.txt");
  }

  @Test
  public void testValidateEPUBPLoremImage()
  {
    testValidateDocument("valid/lorem-image");
  }

  @Test
  public void testValidateEPUBPLoremSvgHyperlink()
  {
    testValidateDocument("valid/lorem-svg-hyperlink", "valid/lorem-svg-hyperlink.txt");
  }

  @Test
  public void testValidateEPUBPLoremSvgHyperlinkNoTitle()
  {
    Collections.addAll(expectedWarnings, MessageId.ACC_011);
    testValidateDocument("invalid/lorem-svg-hyperlink-notitle");
  }

  @Test
  public void testValidateEPUBPLoremIFrame()
  {
    testValidateDocument("valid/lorem-iframe");
  }

  @Test
  public void testValidateEPUBPInvalidLoremPoster()
  {
    Collections.addAll(expectedErrors, MessageId.MED_001);
    testValidateDocument("invalid/lorem-poster");
  }

  @Test
  public void testValidateEPUBPInvalidLoremForeign()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_014);
    testValidateDocument("invalid/lorem-foreign");
  }

  @Test
  public void testValidateEPUB30_navInvalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/nav-invalid/");
  }

  @Test
  public void testValidateEPUB30_issue134_1()
  {
    // svg in both contentdocs, opf props set right
    testValidateDocument("valid/lorem-svg-dual/", "valid/lorem-svg-dual.txt");
  }

  @Test
  public void testValidateEPUB30_issue134_2()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_014, MessageId.OPF_014);
    // svg in both contentdocs, no opf props set right
    testValidateDocument("invalid/lorem-svg-dual/");
  }

  @Test
  public void testValidateEPUB30_issue134_3()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_014);
    // svg in both contentdocs, only one opf prop set right
    testValidateDocument("invalid/lorem-svg-dual-2/");
  }

  @Test
  public void testValidateEPUB30_CSSImport_valid()
  {
    testValidateDocument("valid/lorem-css-import/", "valid/lorem-css-import.txt");
  }

  @Test
  public void testValidateEPUB30_CSSImport_invalid_1()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_001, MessageId.RSC_001);
    testValidateDocument("invalid/lorem-css-import-1/");
  }

  @Test
  public void testValidateEPUB30_CSSImport_invalid_2()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_008);
    Collections.addAll(expectedWarnings, MessageId.OPF_003);
    testValidateDocument("invalid/lorem-css-import-2/");
  }

  @Test
  public void testValidateEPUB30_CSSURLS_1()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_007);
    // 'imgs/table_header_bg_uni.jpg': referenced resource missing in the
    // package
    testValidateDocument("invalid/lorem-css-urls-1/");
  }

  @Test
  public void testValidateEPUB30_CSSURLS_2()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_027);
    // 'imgs/table_header_bg_uni.jpg': referenced resource missing in the
    // package
    testValidateDocument("invalid/lorem-css-urls-2/");
  }

  @Test
  public void testValidateEPUB30_CSSURLS_3()
  {
    Collections.addAll(expectedWarnings, MessageId.CSS_017);
    // 'imgs/table_header_bg_uni.jpg': referenced resource missing in the
    // package
    testValidateDocument("invalid/lorem-css-urls-3/");
  }

  @Test
  public void testValidateEPUB30_CSSFontFace_valid()
  {
    testValidateDocument("valid/wasteland-otf/", "valid/wasteland-otf.txt");
  }

  @Test
  public void testValidateEPUB30_CSSFontFace_invalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_001, MessageId.RSC_001, MessageId.RSC_001);
    // referenced fonts missing
    testValidateDocument("invalid/wasteland-otf/");
  }

  @Test
  public void testValidateEPUB30_CSSEncoding_invalid()
  {
    Collections.addAll(expectedErrors, MessageId.CSS_003);
    // @charset not utf
    testValidateDocument("invalid/lorem-css-enc/");
  }

  @Test
  public void testValidateEPUB30_CSSMediaType_invalid()
  {
    Collections.addAll(expectedWarnings, MessageId.CSS_010);
    // CSS with declared type 'xhtml/css' should raise a "no fallback" error
    testValidateDocument("invalid/lorem-css-wrongtype/");
  }

  @Test
  public void testValidateEPUB30_remoteAudio_valid()
  {
    // audio element with @src attribute
    testValidateDocument("valid/lorem-remote/", "valid/lorem-remote.txt");
  }

  @Test
  public void testValidateEPUB30_remoteHttpsAudio_valid()
  {
    // remote audio element via HTTPS
    testValidateDocument("valid/lorem-remote-https/");
  }

  @Test
  public void testValidateEPUB30_remoteUrlWithQuery_valid()
  {
    // remote audio element via HTTPS
    testValidateDocument("valid/lorem-remote-queryurl/");
  }

  @Test
  public void testValidateEPUB30_remoteAudioSources_valid()
  {
    // audio element with sources children
    testValidateDocument("valid/lorem-remote-2/");
  }

  @Test
  public void testValidateEPUB30_remoteImg_invalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_006);
    // remote resource of invalid type (img) declared in opf
    testValidateDocument("invalid/lorem-remote/");
  }

  @Test
  public void testValidateEPUB30_remoteImg_invalid2()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_014);
    // remote audio, declared in opf, but missing 'remote-resources' property
    testValidateDocument("invalid/lorem-remote-2/");
  }

  @Test
  public void testValidateEPUB30_remoteAudio_invalid()
  {
    Collections.addAll(expectedErrors, MessageId.MED_002, MessageId.RSC_008);
    // remote audio, not declared in the manifest
    // we should only get one error here:
    // the "no fallback" error is extra since no type info
    // can be retrieved from the manifest...
    testValidateDocument("invalid/lorem-remote-3/");
  }

  @Test
  public void testValidateEPUB30_remoteAudioSources_invalid()
  {
    Collections.addAll(expectedErrors, MessageId.MED_002, MessageId.RSC_008, MessageId.RSC_008);
    // audio element with a list of source children pointing to remote resources
    // not declared in the manifest
    // we should only get two errors here:
    // the "no fallback" error is extra since no type info
    // can be retrieved from the manifest...
    testValidateDocument("invalid/lorem-remote-4/");
  }

  @Test
  public void testValidateEPUB30_circularFallback()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_045, MessageId.OPF_045, MessageId.OPF_045,
        MessageId.OPF_045, MessageId.MED_003);
    testValidateDocument("invalid/fallbacks-circular/");
  }

  @Test
  public void testValidateEPUB30_nonresolvingFallback()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_040, MessageId.MED_003);
    // dupe messages, tbf
    testValidateDocument("invalid/fallbacks-nonresolving/");
  }

  @Test
  public void testValidateEPUB30_okFallback()
  {
    testValidateDocument("valid/fallbacks/", "valid/fallbacks.txt");
  }

  @Test
  public void testValidateEPUB30_svgCoverImage()
  {
    testValidateDocument("valid/svg-cover/", "valid/svg-cover.txt");
  }

  @Test
  public void testValidateEPUB30_svgInSpine()
  {
    // svg in spine, svg cover image
    testValidateDocument("valid/svg-in-spine/", "valid/svg-in-spine.txt");
  }

  @Test
  public void testValidateEPUB30_videoAudioTrigger()
  {
    testValidateDocument("valid/cc-shared-culture/", "valid/cc-shared-culture.txt");
  }

  @Test
  public void testValidateEPUB30_InvalidLinks()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_007, MessageId.RSC_012, MessageId.RSC_012,
        MessageId.RSC_012);
    /*
     * the valid counterpart is in the zipped section
     * 
     * - one broken file ref in navdoc - one broken frag ref in navdoc - one
     * broken internal frag ref in overview - one broken crossdoc frag ref in
     * overview
     */

    testValidateDocument("invalid/epub30-spec/");
  }

  @Test
  public void testValidateEPUB30_basicDual()
  {
    testValidateDocument("valid/lorem-basic-dual/", "valid/lorem-basic-dual.txt");
  }

  @Test
  public void testValidateEPUB30_Base()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_006, MessageId.RSC_006);
    // <base href set, see issue 155
    testValidateDocument("invalid/lorem-basic-dual-base/");
  }

  @Test
  public void testValidateEPUB30_InvalidContainer()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/lorem-container/");
  }

  @Test
  public void testValidateEPUB30_InvalidSignatures()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/lorem-signatures/");
  }

  @Test
  public void testValidateEPUB30_InvalidEncryption()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.PKG_010);
    testValidateDocument("invalid/lorem-encryption/");
  }

  @Test
  public void testValidateEPUB30_customNsAttr()
  {
    testValidateDocument("invalid/custom-ns-attr/");
  }

  @Test
  public void testIssue188()
  {
    // Image file name containing '+'
    testValidateDocument("valid/issue188/");
  }

  @Test
  public void testIssue189()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // element "somebadxhtmlformatting" not allowed here
    testValidateDocument("invalid/issue189/");
  }

  @Test
  public void testIssue198()
  {
    // Collections.addAll(expectedErrors, );

    // also data-* removal
    testValidateDocument("valid/issue198/");
  }

  @Test
  public void testIssue211a()
  {
    // figcaption and scoped styles alt 1
    testValidateDocument("valid/issue211a/");
  }

  @Test
  public void testIssue211b()
  {
    // figcaption and scoped styles alt 2
    testValidateDocument("valid/issue211b/");
  }

  @Test
  public void testIssue225()
  {
    // 2 @href values 0-length and empty after ws norm
    // issue225 asked for warning here, but we give none
    // until we have a compat hint message type; the empty
    // string is a valid URI
    testValidateDocument("valid/issue225/");
  }

  @Test
  public void testIssue226()
  {
    testValidateDocument("valid/issue226/");
  }

  @Test
  public void testIssue237()
  {
    // namespace uri in css is not a remote resource...
    testValidateDocument("valid/issue237/");
  }

  @Test
  public void testIssue249()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_018);
    testValidateDocument("valid/issue249/");
  }

  @Test
  public void testValidateEPUB20_issue267()
  {
    testValidateDocument("valid/issue267/", "valid/issue267.txt");
  }

  @Test
  public void testIssue270()
  {
    testValidateDocument("valid/issue270/");
  }

  @Test
  public void testCollectionPreview()
  {
    testValidateDocument("valid/collections-preview/");
  }

  @Test
  public void testXHTMExtension()
  {
    Collections.addAll(expectedWarnings, MessageId.HTM_014a);
    testValidateDocument("invalid/xhtml-extension");
  }

  @Test
  public void testEdupub_Basic()
  {
    testValidateDocument("valid/edu-basic/", EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupub_NonLinear()
  {
    testValidateDocument("valid/edu-non-linear/", EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubPagination_InvalidNoPageList()
  {
    Collections.addAll(expectedErrors, MessageId.NAV_003);
    testValidateDocument("invalid/edu-pagination-nopagelist/", EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubPagination_InvalidNoPageSource()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_066);
    testValidateDocument("invalid/edu-pagination-nopagesource/", EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubPagination_InvalidWithMicrodata()
  {
    Collections.addAll(expectedWarnings, MessageId.HTM_051);
    testValidateDocument("invalid/edu-microdata/", EPUBProfile.EDUPUB);
  }

  @Test
  public void test_MissingLOx()
  {
    testValidateDocument("valid/non-edu-missing-lox/");
  }

  @Test
  public void testEdupub_MissingLOx()
  {
    Collections.addAll(expectedWarnings, MessageId.NAV_005, MessageId.NAV_006, MessageId.NAV_007,
        MessageId.NAV_008);
    testValidateDocument("invalid/edu-missing-lox/", EPUBProfile.EDUPUB);
  }

}
