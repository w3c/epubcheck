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
import java.util.Locale;

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
  public void testDuplicateID()
  {
    // 2 errors x 2 sets of duplicate IDs
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005);
    testValidateDocument("invalid/duplicate-id");
  }

  @Test
  public void testValidateEPUBPLoremMultipleRenditionsUnmanifested()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_019, MessageId.RSC_017, MessageId.OPF_003);
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
    Collections.addAll(expectedErrors, MessageId.RSC_001);
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
    Collections.addAll(expectedErrors, MessageId.OPF_027, MessageId.CSS_020, MessageId.CSS_020);
    // 'imgs/table_header_bg_uni.jpg': referenced resource missing in the
    // package
    testValidateDocument("invalid/lorem-css-urls-2/");
  }

  @Test
  public void testValidateEPUB30_CSSURLS_3()
  {
    Collections.addAll(expectedWarnings, MessageId.CSS_017);
    Collections.addAll(expectedErrors, MessageId.CSS_020, MessageId.CSS_020);
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
    Collections.addAll(expectedErrors, MessageId.CSS_010);
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
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.MED_003);
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
  public void testValidateEPUB30_svgReferenced()
  {
    // svg referenced from img, object, iframe
    testValidateDocument("valid/svg-referenced/");
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
    testValidateDocument("invalid/lorem-encryption/");
  }

  @Test
  public void testValidateEPUB30_customNsAttr()
  {
    testValidateDocument("invalid/custom-ns-attr/");
  }
  
  /**
   * Also tests locale-independent character case transformations (such as
   * lower-casing). Specifically, in issue 711, when the default locale is set
   * to Turkish, lower-casing resulted in incorrect vocabulary strings (for
   * "PAGE_LIST" enum constant name relevant to the original issue report, as
   * well as for numerous other strings). Therefore, a Turkish locale is set as
   * the default at the beginning of the test (the original locale is restored
   * at the end of the test).
   */
  @Test
  public void testPageList()
  {
    Locale previousLocale = Locale.getDefault();
    try {
      // E.g., tests that I is not lower-cased to \u0131 based on locale's collation rules:
      Locale.setDefault(new Locale("tr", "TR"));
      testValidateDocument("valid/page-list");
    } finally { // restore the original locale
      Locale.setDefault(previousLocale);
    }
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
  public void testIssue305()
  {
    expectedErrors.add(MessageId.OPF_073);
    testValidateDocument("invalid/ncx-external-identifier");
  }

  @Test
  public void testIssue332()
  {
    testValidateDocument("valid/issue332-idspaces");
  }

  @Test
  public void testIssue419()
  {
    testValidateDocument("valid/issue419/");
  }

  @Test
  public void testIssue5()
  {
    testValidateDocument("valid/issue567/");
  }
  
  @Test
  public void testIssue615_langtag() {
    testValidateDocument("valid/issue615-langtags/");
  }
  
  @Test
  public void testIssue922()
  {
    // tests that CSS 'font-size: 0' is accepted
    testValidateDocument("valid/issue922/");
  }

  @Test
  public void testResource_Missing() {
    Collections.addAll(expectedErrors, MessageId.RSC_001);
    testValidateDocument("invalid/resource-missing/");
  }
  
  @Test
  public void testResource_RefInXHTML_Undeclared() {
    Collections.addAll(expectedErrors, MessageId.RSC_007);
    testValidateDocument("invalid/resource-missing-refinxhtml/");
  }

  @Test
  public void testFallback_XPGT_Explicit()
  {
    testValidateDocument("valid/xpgt-explicit-fallback/");
  }

  @Test
  public void testFallback_XPGT_Implicit()
  {
    testValidateDocument("valid/xpgt-implicit-fallback/");
  }

  @Test
  public void testFallback_XPGT_NoFallback()
  {
    Collections.addAll(expectedErrors, MessageId.CSS_010);
    testValidateDocument("invalid/xpgt-no-fallback/");
  }
  
  @Test
  public void testFont_OpenType() {
    testValidateDocument("valid/font-opentype");
  }
  
  @Test
  public void testFont_NonCoreMediaType() {
    testValidateDocument("valid/font-othermediatype");
  }
  
  @Test
  public void testFXL_WithSVG() {
    testValidateDocument("valid/fxl-svg/");
  }
  
  @Test
  public void testFXL_WithSVG_NoViewbox() {
    expectedErrors.add(MessageId.HTM_048);
    testValidateDocument("invalid/fxl-svg-noviewbox/");
  }
  
  @Test
  public void testFXL_WithSVGNotInSpine() {
    testValidateDocument("valid/fxl-svg-notinspine/");
  }
  
  @Test
  public void testLink_MissingResource(){
    Collections.addAll(expectedWarnings, MessageId.RSC_007w);
    testValidateDocument("invalid/link-missing/");
  }

  @Test
  public void testMultipleRenditions()
  {
    testValidateDocument("valid/multiple-renditions");
  }

  @Test
  public void testMultipleRenditions_Mapping_MultipleNavs()
  {
    testValidateDocument("valid/multiple-renditions-mapping-multiplenavs");
  }

  @Test
  public void testMultipleRenditions_Mapping_NotXHTML()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/multiple-renditions-mapping-nonxhtml");
  }

  @Test
  public void testMultipleRenditions_Mapping_MoreThanOne()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // side-effect or more than one mapping: only one is recognized as declared,
    // hence the OPF_003
    Collections.addAll(expectedWarnings, MessageId.OPF_003);
    testValidateDocument("invalid/multiple-renditions-multiple-mappings");
  }

  @Test
  public void testMultipleRenditions_Mapping_NoVersionMeta()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/multiple-renditions-mapping-noversion");
  }

  @Test
  public void testMultipleRenditions_Mapping_NoResourceMap()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/multiple-renditions-mapping-noresourcemap");
  }

  @Test
  public void testMultipleRenditions_Mapping_UnidentifiedNavType()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/multiple-renditions-mapping-untypednav");
  }

  @Test
  public void testPreview_Embedded()
  {
    testValidateDocument("valid/preview-embedded/");
  }

  @Test
  public void testPreview_Embedded_NoManifest()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/preview-embedded-nomanifest/");
  }

  @Test
  public void testPreview_Embedded_NoLinks()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/preview-embedded-nolinks/");
  }

  @Test
  public void testPreview_Embedded_LinkNotContentDoc()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_075);
    testValidateDocument("invalid/preview-embedded-linknoCD/");
  }

  @Test
  public void testPreview_Embedded_LinkWithCFI()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_076);
    testValidateDocument("invalid/preview-embedded-linkcfi/");
  }

  @Test
  public void testPreview_Pub()
  {
    testValidateDocument("valid/preview-pub/");
  }

  @Test
  public void testPreview_Pub_NoType()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/preview-pub-notype/", EPUBProfile.PREVIEW);
  }

  @Test
  public void testPreview_Pub_NoSource()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("invalid/preview-pub-nosource/");
  }

  @Test
  public void testPreview_Pub_SameSourceId()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/preview-pub-samesourceid/");
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
  public void testEdupub_FXL()
  {
    testValidateDocument("valid/edu-fxl/", EPUBProfile.EDUPUB);
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
    testValidateDocument("invalid/edu-missing-lox/", EPUBProfile.EDUPUB);
  }

  @Test
  public void testIdx_SingleFile()
  {
    testValidateDocument("valid/idx-single-file/");
  }

  @Test
  public void testIdx_SingleFile_InvalidIndexContent()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/idx-single-file-badidxcontent/");
  }

  @Test
  public void testIdx_SingleFile_InvalidNoIndex()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_015, MessageId.RSC_005);
    testValidateDocument("invalid/idx-single-file-noindex/");
  }

  @Test
  public void testIdx_WholePub()
  {
    testValidateDocument("valid/idx-whole-pub/");
  }

  @Test
  public void testIdx_WholePub_InvalidIndexContent()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/idx-whole-pub-badidxcontent/");
  }

  @Test
  public void testIdx_WholePub_InvalidNoIndex()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/idx-whole-pub-noindex/");
  }

  @Test
  public void testIdx_Collection()
  {
    testValidateDocument("valid/idx-collection/");
  }

  @Test
  public void testIdx_Collection_InvalidIndexContent()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/idx-collection-badidxcontent/");
  }

  @Test
  public void testIdx_Collection_InvalidNoIndex()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("invalid/idx-collection-noindex");
  }

  @Test
  public void testDataNav_Basic()
  {
    testValidateDocument("valid/data-nav-basic");
  }

  @Test
  public void testDataNav_MoreThanOne()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/data-nav-multiple");
  }

  @Test
  public void testDataNav_NotXHTML()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_012);
    testValidateDocument("invalid/data-nav-notxhtml");
  }

  @Test
  public void testDataNav_InSpine()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_077);
    testValidateDocument("invalid/data-nav-inspine");
  }

  @Test
  public void testDataNav_MissingType()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/data-nav-missing-type");
  }

  @Test
  public void testDataNav_RegionBased()
  {
    testValidateDocument("valid/data-nav-regionbased");
  }

  @Test
  public void testDataNav_RegionBased_NotInDataNav()
  {
    Collections.addAll(expectedErrors, MessageId.HTM_052);
    testValidateDocument("invalid/data-nav-regionbased-notindatanav");
  }

  @Test
  public void testDataNav_RegionBased_NotFXL()
  {
    Collections.addAll(expectedErrors, MessageId.NAV_009);
    testValidateDocument("invalid/data-nav-regionbased-notfxl");
  }

  @Test
  public void testDataNav_RegionBased_Struct()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/data-nav-regionbased-struct");
  }
  
  @Test
  public void testDataNav_RegionBased_ComicsTypes()
  {
      testValidateDocument("valid/data-nav-regionbased-comics");
  }

  @Test
  public void testDict_Single()
  {
    testValidateDocument("valid/dict-single");
  }

  @Test
  public void testDict_Single_NoDictContent()
  {
    expectedErrors.add(MessageId.OPF_078);
    testValidateDocument("invalid/dict-single-nodictcontent");
  }

  @Test
  public void testDict_InvalidDictContent()
  {
    // Two errors: one in Nav Doc, one in regular XHTML Doc
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/dict-invalidcontent");
  }

  @Test
  public void testDict_SearchKeyMap_Invalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("invalid/dict-skm-invalid");
  }

  @Test
  public void testDict_SearchKeyMap_BadExtension()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_080);
    testValidateDocument("invalid/dict-skm-badextension");
  }

  @Test
  public void testDict_SearchKeyMap_LinkDoesntResolve()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_007);
    testValidateDocument("invalid/dict-skm-linktonowhere");
  }

  @Test
  public void testDict_SearchKeyMap_LinkToNonContentDoc()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_021);
    testValidateDocument("invalid/dict-skm-linktocss");
  }

  @Test
  public void testDict_NoDCType()
  {
    // error from schema, because profile is explictly asked
    expectedErrors.add(MessageId.RSC_005);
    // warning from content being detected as dictionary
    expectedWarnings.add(MessageId.OPF_079);
    testValidateDocument("invalid/dict-nodctype", EPUBProfile.DICT);
  }

  @Test
  public void testDict_NoDCTypeButDictContent()
  {
    // Profile not set, but detected as dictionary from epub:type
    expectedWarnings.add(MessageId.OPF_079);
    testValidateDocument("invalid/dict-nodctype-2");
  }

  @Test
  public void testDict_Multiple()
  {
    testValidateDocument("valid/dict-multiple");
  }

  @Test
  public void testDict_Multiple_NoDictContent()
  {
    expectedErrors.add(MessageId.OPF_078);
    testValidateDocument("invalid/dict-multiple-nodictcontent");
  }

  @Test
  public void testDuplicateResources()
  {
    expectedErrors.add(MessageId.OPF_074);
    testValidateDocument("invalid/duplicate-resource");
  }

  @Test
  public void testEncryption_Unknown(){
    expectedErrors.add(MessageId.RSC_004);
    testValidateDocument("invalid/encryption-unknown");
  }
  
  @Test
  public void testOutOfSpineRef()
  {
    expectedErrors.add(MessageId.RSC_011);
    testValidateDocument("invalid/href-outofspine");
  }
  
  @Test
  public void testInvalidCssFontSizeValue()
  {
    Collections.addAll(expectedErrors, MessageId.CSS_020, MessageId.CSS_020, MessageId.CSS_020);
    testValidateDocument("invalid/invalid-css-font-size-value");
  }
  

}
