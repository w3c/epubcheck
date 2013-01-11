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

public class Epub20CheckTest extends AbstractEpubCheckTest {


	public Epub20CheckTest() {
		super("/20/epub/");
	}
	
	@Test
	public void testValidateEPUBvalid20() {
		testValidateDocument("valid/lorem.epub", 0, 0, "valid/lorem.txt");
	}

	@Test
	public void testValidateEPUBInvalid20() {
		testValidateDocument("invalid/lorem-mimetype.epub", 1, 0);
	}

	@Test
	public void testValidateEPUBPageMap20() {
		testValidateDocument("PageMap20.epub", 1, 0);
	}

	@Test
	public void testValidateEPUBNoUniqueId20() {
		testValidateDocument("OPFIllegalElement_UniqueID20.epub", 2, 1);
	}

	@Test
	public void testValidateEPUBOPFIllegalElement20() {
		testValidateDocument("OPFIllegalElement20.epub", 1, 1);
	}

	@Test
	public void testValidateEPUBUnmanifested20() {
		testValidateDocument("Unmanifested20.epub", 0, 3);
	}

	@Test
	public void testValidateEPUBPFileDeclaredInContainerNotOpf20() {
		testValidateDocument("ContainerNotOPF20.epub", 0, 0); 
	}

	@Test
	public void testValidateEPUBFileInMetaInfIgnored() {
		testValidateDocument("MetaInfNotOPF20.epub", 0, 0);
	}

	@Test
	public void testValidateEPUBNullDate20() {
		testValidateDocument("NullDate20.epub", 1, 1);
	}

	@Test
	public void testValidateEPUBNon8601Date20() {
		testValidateDocument("Non8601Date20.epub", 1, 1);
	}

	@Test
	public void testValidateEPUBUnmanifestedGuideItems20() {
		testValidateDocument("UnmanifestedGuideItems20.epub", 2, 1);
	}

	@Test
	public void testValidateEPUBEmptyDir20() {
		testValidateDocument("EmptyDir20.epub", 0, 2);
	}

	@Test
	public void testValidateEPUBPvalid20() {
		testValidateDocument("Test20.epub", 0, 1);
	}
	
	@Test
	public void testValidateEPUBPNoRootFiles() {
		testValidateDocument("/invalid/no-rootfile.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUBPBadOpfNamespace() {
		testValidateDocument("/invalid/bad_opf_namespace.epub", 7, 2);
	}
	
	@Test
	public void testValidateEPUB_mimetypeAndVersion() {
		testValidateDocument("/invalid/mimetypeAndVersion.epub", 2, 0);
	}
	
	@Test
	public void testValidateEPUB_noLinearYes() {
		//+ 3 warnings that dont relate to linear
		testValidateDocument("/invalid/no-linear-yes.epub", 0, 4);
	}
	
	@Test
	public void testValidateEPUB_unusedImages() {
		//4 unused images in subfolder
		testValidateDocument("/invalid/issue89.epub", 2, 6);
	}
	
	@Test
	public void testValidateEPUB_issue138() {
		//warning for empty dc:title
		testValidateDocument("/invalid/issue138.epub", 0, 1);
	}
	
	@Test
	public void testValidateEPUB_ncxDupeID() {
		testValidateDocument("/invalid/ncx-dupe-id.epub", 2, 0);
	}
	
	@Test
	public void testValidateEPUB_unresolvedInternalLink() {
		testValidateDocument("/invalid/unresolved-internal-xhtml-link.epub", 1, 0);
	}

	@Test
	public void testValidateEPUBvalidIssue169() {
		testValidateDocument("valid/issue169.epub", 0, 0, "valid/issue169.txt");
	}
	
	@Test
	public void testValidateEPUBvalidIssue194_1() {
		testValidateDocument("valid/issue194.bad.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUBvalidIssue194_2() {
		testValidateDocument("valid/issue194.good.epub", 0, 0);
	}
	
	@Test
	public void testValidateEPUB30Issue170() { 
		//ncx references not allowed in guide 
		testValidateDocument("invalid/issue170.epub", 1, 0);
	}
}
