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

public class Epub30CheckExpandedTest extends AbstractEpubCheckTest {


	public Epub30CheckExpandedTest() {
		super("/30/expanded/");
	}

	@Test
	public void testValidateEPUBPLoremBasic() {
		testValidateDocument("valid/lorem-basic", 0, 0, "valid/lorem-basic.txt");
	}
	
	@Test
	public void testValidateEPUBPLoremMultipleRenditions() {
		testValidateDocument("valid/lorem-xrenditions", 0, 0);
	}
	
	@Test
	public void testValidateEPUBPLoremMultipleRenditionsUnmanifested() {
		testValidateDocument("invalid/lorem-xrenditions-unmanifested", 0, 1);
	}

	@Test
	public void testValidateEPUBWastelandBasic() {
		testValidateDocument("valid/wasteland-basic", 0, 0, "valid/wasteland-basic.txt");
	}

	@Test
	public void testValidateEPUBLoremAudio() {
		testValidateDocument("valid/lorem-audio", 0, 0, "valid/lorem-audio.txt");
	}

	@Test
	public void testValidateEPUBLoremxhtmlrng1() {
		testValidateDocument("invalid/lorem-xhtml-rng-1", 1, 0);
	}

	@Test
	public void testValidateEPUBLoremxhtmlsch1() {
		testValidateDocument("invalid/lorem-xhtml-sch-1", 1, 0);
	}

	@Test
	public void testValidateEPUBPLoremBasicMathml() {
		testValidateDocument("invalid/lorem-basic-switch", 1, 0);
	}

	@Test
	public void testValidateEPUBPLoremMimetype() {
		testValidateDocument("invalid/lorem-mimetype", 2, 0);
	}

	@Test
	public void testValidateEPUBPLoremMimetype2() {
		testValidateDocument("invalid/lorem-mimetype-2", 2, 0);
	}
	
	@Test
	public void testValidateEPUBPLoremBasicSwitch() {
		testValidateDocument("valid/lorem-basic-switch", 0, 0, "valid/lorem-basic-switch.txt");
	}

	@Test
	public void testValidateEPUBPLoremLink() {
		testValidateDocument("valid/lorem-link", 0, 0, "valid/lorem-link.txt");
	}

	@Test
	public void testValidateEPUBPLoremForeign() {
		testValidateDocument("valid/lorem-foreign", 0, 0, "valid/lorem-foreign.txt");
	}

	@Test
	public void testValidateEPUBPLoremObjectFallbacks() {
		testValidateDocument("valid/lorem-object-fallbacks", 0, 0, "valid/lorem-object-fallbacks.txt");
	}
	
	@Test
	public void testValidateEPUBPLoremBindings() {
		testValidateDocument("valid/lorem-bindings", 0, 0, "valid/lorem-bindings.txt");
	}

	@Test
	public void testValidateEPUBPLoremInvalidBindings() {
		testValidateDocument("invalid/lorem-bindings", 1, 0);
	}
	
	@Test
	public void testValidateEPUBPLoremPoster() {
		testValidateDocument("valid/lorem-poster", 0, 0, "valid/lorem-poster.txt");
	}

	@Test
	public void testValidateEPUBPLoremSvg() {
		testValidateDocument("valid/lorem-svg", 0, 0, "valid/lorem-svg.txt");
	}
	
	@Test
	public void testValidateEPUBPLoremImage() {
		testValidateDocument("valid/lorem-image", 0, 0);
	}

	@Test
	public void testValidateEPUBPLoremSvgHyperlink() {
		testValidateDocument("valid/lorem-svg-hyperlink", 0, 0, "valid/lorem-svg-hyperlink.txt");
	}

	@Test
	public void testValidateEPUBPInvalidLoremPoster() {
		testValidateDocument("invalid/lorem-poster", 1, 0);
	}

	@Test
	public void testValidateEPUBPInvalidLoremForeign() {
		testValidateDocument("invalid/lorem-foreign", 1, 0);
	}

	@Test
	public void testValidateEPUB30_navInvalid() {
		// invalid nav issuse reported by MattG
		testValidateDocument("invalid/nav-invalid/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_issue134_1() {
		// svg in both contentdocs, opf props set right
		testValidateDocument("valid/lorem-svg-dual/", 0, 0, "valid/lorem-svg-dual.txt");
	}
	
	@Test
	public void testValidateEPUB30_issue134_2() {
		// svg in both contentdocs, no opf props set right
		testValidateDocument("invalid/lorem-svg-dual/", 2, 0);
	}
	
	@Test
	public void testValidateEPUB30_issue134_3() {
		// svg in both contentdocs, only one opf prop set right
		testValidateDocument("invalid/lorem-svg-dual-2/", 1, 0);
	}
			
	@Test
	public void testValidateEPUB30_CSSImport_valid() {		
		testValidateDocument("valid/lorem-css-import/", 0, 0, "valid/lorem-css-import.txt");
	}
	
	@Test
	public void testValidateEPUB30_CSSImport_invalid_1() {		
		testValidateDocument("invalid/lorem-css-import-1/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_CSSImport_invalid_2() {		
		testValidateDocument("invalid/lorem-css-import-2/", 1, 1);
	}
	
	@Test
	public void testValidateEPUB30_CSSURLS_1() {	
		//'imgs/table_header_bg_uni.jpg': referenced resource missing in the package
		testValidateDocument("invalid/lorem-css-urls-1/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_CSSFontFace_valid() {		
		testValidateDocument("valid/wasteland-otf/", 0, 0, "valid/wasteland-otf.txt");
	}
	
	@Test
	public void testValidateEPUB30_CSSFontFace_invalid() {
		//referenced fonts missing
		testValidateDocument("invalid/wasteland-otf/", 3, 0);
	}
	
	@Test
	public void testValidateEPUB30_CSSEncoding_invalid() {
		//@charset not utf
		testValidateDocument("invalid/lorem-css-enc/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_remoteAudio_valid() {		
		// audio element with @src attribute
		testValidateDocument("valid/lorem-remote/", 0, 0, "valid/lorem-remote.txt");
	}
	
	@Test
	public void testValidateEPUB30_remoteAudioSources_valid() {
		// audio element with sources children
		testValidateDocument("valid/lorem-remote-2/", 0, 0);
	}
	
	@Test
	public void testValidateEPUB30_remoteImg_invalid() {
		//remote resource of invalid type (img) declared in opf  
		testValidateDocument("invalid/lorem-remote/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_remoteImg_invalid2() {
		//remote audio, declared in opf, but missing 'remote-resources' property
		testValidateDocument("invalid/lorem-remote-2/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_remoteAudio_invalid() {
		//remote audio, not declared in the manifest
		// we should only get one error here: 
		// the "no fallback" error is extra since no type info
		// can be retrieved from the manifest...
		testValidateDocument("invalid/lorem-remote-3/", 2, 0);
	}
	
	@Test
	public void testValidateEPUB30_remoteAudioSources_invalid() {
		//audio element with a list of source children pointing to remote resources
		// not declared in the manifest
		// we should only get two errors here: 
		// the "no fallback" error is extra since no type info
		// can be retrieved from the manifest...
		testValidateDocument("invalid/lorem-remote-4/", 3, 0);
	}
	
	@Test
	public void testValidateEPUB30_circularFallback() {
		testValidateDocument("invalid/fallbacks-circular/", 5, 0);
	}
	
	@Test
	public void testValidateEPUB30_nonresolvingFallback() {
		//dupe messages, tbf
		testValidateDocument("invalid/fallbacks-nonresolving/", 4, 0);
	}
	
	@Test
	public void testValidateEPUB30_okFallback() {
		testValidateDocument("valid/fallbacks/", 0, 0, "valid/fallbacks.txt");
	}
	
	@Test
	public void testValidateEPUB30_svgCoverImage() {
		testValidateDocument("valid/svg-cover/", 0, 0, "valid/svg-cover.txt");
	}
	
	@Test
	public void testValidateEPUB30_svgInSpine() {
		//svg in spine, svg cover image
		testValidateDocument("valid/svg-in-spine/", 0, 0, "valid/svg-in-spine.txt");
	}
	
	@Test
	public void testValidateEPUB30_videoAudioTrigger() {
		testValidateDocument("valid/cc-shared-culture/", 0, 0, "valid/cc-shared-culture.txt");
	}
	
	@Test
	public void testValidateEPUB30_InvalidLinks() {
		/*
		 * the valid counterpart is in the zipped section
		 * 
		 * - one broken file ref in navdoc
		 * - one broken frag ref in navdoc
		 * - one broken internal frag ref in overview
		 * - one broken crossdoc frag ref in overview
		 */
		
		testValidateDocument("invalid/epub30-spec/", 4, 0);
	}
	
	@Test
	public void testValidateEPUB30_basicDual() {
		testValidateDocument("valid/lorem-basic-dual/", 0, 0, "valid/lorem-basic-dual.txt");
	}
	
	@Test
	public void testValidateEPUB30_Base() {
		//<base href set, see issue 155 
		testValidateDocument("invalid/lorem-basic-dual-base/", 2, 0);
	}
	
	@Test
	public void testValidateEPUB30_InvalidContainer() {
		testValidateDocument("invalid/lorem-container/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_InvalidSignatures() {
		testValidateDocument("invalid/lorem-signatures/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_InvalidEncryption() {
		testValidateDocument("invalid/lorem-encryption/", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_customNsAttr() {
		testValidateDocument("invalid/custom-ns-attr/", 1, 0);
	}
	
	@Test
	public void testIssue188() {
		//Image file name containing '+'
		testValidateDocument("valid/issue188/", 0, 0);
	}
	
	@Test
	public void testIssue189() {
		//element "somebadxhtmlformatting" not allowed here
		testValidateDocument("invalid/issue189/", 1, 0);
	}
	
	@Test
	public void testIssue198() { 
		//also data-* removal
		testValidateDocument("valid/issue198/", 0, 0);
	}
	
	@Test
	public void testIssue211a() { 
		//figcaption and scoped styles alt 1
		testValidateDocument("valid/issue211a/", 0, 0);
	}
	
	@Test
	public void testIssue211b() { 
		//figcaption and scoped styles alt 2
		testValidateDocument("valid/issue211b/", 0, 0);
	}
	
	@Test
	public void testIssue225() { 
		//2 @href values 0-length and empty after ws norm
		//issue225 asked for warning here, but we give none
		//until we have a compat hint message type; the empty
		//string is a valid URI
		testValidateDocument("valid/issue225/", 0, 0);
	}
	
	@Test
	public void testIssue226() { 
		// @href before PDF <object/> pointing to an ID after the object
		testValidateDocument("valid/issue226/", 0, 0);
	}
}
