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

public class Epub20CheckTest extends AbstractEpubCheckTest
{


  public Epub20CheckTest()
  {
    super("/20/epub/");
  }

  @Test
  public void testValidateEPUBvalid20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/lorem.epub", expectedErrors, expectedWarnings, "valid/lorem.txt");
  }

  @Test
  public void testValidateEPUBInvalid20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.PKG_007);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/lorem-mimetype.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBPageMap20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("PageMap20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBNoUniqueId20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.OPF_030);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("OPFIllegalElement_UniqueID20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBOPFIllegalElement20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("OPFIllegalElement20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBUnmanifested20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055, MessageId.OPF_003, MessageId.OPF_003);
    testValidateDocument("Unmanifested20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBPFileDeclaredInContainerNotOpf20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("ContainerNotOPF20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBFileInMetaInfIgnored()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("MetaInfNotOPF20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBNullDate20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_054);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("NullDate20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBNon8601Date20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_054);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("Non8601Date20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBUnmanifestedGuideItems20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_031, MessageId.RSC_007);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("UnmanifestedGuideItems20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBEmptyDir20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055, MessageId.PKG_014);
    testValidateDocument("EmptyDir20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBPvalid20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("Test20.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBPNoRootFiles()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_024, MessageId.RSC_003);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatalErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedFatalErrors, MessageId.OPF_019);
    testValidateDocument("/invalid/no-rootfile.epub", expectedErrors, expectedWarnings, expectedFatalErrors);
  }

  @Test
  public void testValidateEPUBPBadOpfNamespace()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_024, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.OPF_030);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.OPF_003);
    List<MessageId> expectedFatalErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedFatalErrors, MessageId.OPF_019);
    testValidateDocument("/invalid/bad_opf_namespace.epub", expectedErrors, expectedWarnings, expectedFatalErrors);
  }

  @Test
  public void testValidateEPUB_mimetypeAndVersion()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.PKG_006, MessageId.OPF_024, MessageId.OPF_001);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatalErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedFatalErrors, MessageId.OPF_019);
    testValidateDocument("/invalid/mimetypeAndVersion.epub", expectedErrors, expectedWarnings, expectedFatalErrors);
  }

  @Test
  public void testValidateEPUB_noLinearYes()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_033);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.OPF_003, MessageId.OPF_003);
    //+ 3 warnings that don't relate to linear
    testValidateDocument("/invalid/no-linear-yes.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB_unusedImages()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_031, MessageId.RSC_007);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.PKG_010, MessageId.OPF_003, MessageId.OPF_003, MessageId.PKG_010, MessageId.OPF_003);
    //4 unused images in subfolder
    testValidateDocument("/invalid/issue89.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB_issue138()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    //warning for empty dc:title
    testValidateDocument("/invalid/issue138.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB_ncxDupeID()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("/invalid/ncx-dupe-id.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB_unresolvedInternalLink()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_012);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("/invalid/unresolved-internal-xhtml-link.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBvalidIssue169()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue169.epub", expectedErrors, expectedWarnings, "valid/issue169.txt");
  }

  @Test // STA - This test LOCKS UP!!!!
  public void testValidateEPUBvalidIssue194_1()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.HTM_009);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue194.bad.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUBvalidIssue194_2()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("valid/issue194.good.epub", expectedErrors, expectedWarnings);
  }

  @Test
  public void testValidateEPUB30Issue170()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_032);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //ncx references not allowed in guide
    testValidateDocument("invalid/issue170.epub", expectedErrors, expectedWarnings);
	}
	
	@Test
	public void testMissingFullpathAttributeIssue236() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_001, MessageId.OPF_017, MessageId.OPF_016);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> fatalErrors = new ArrayList<MessageId>();
    //container.xml missing @full-path attribute or @full-path is empty
		// issue 95 / issue 236
		testValidateDocument("invalid/issue236.epub", expectedErrors, expectedWarnings, fatalErrors);
	}
	
	@Test
	public void testFilenameContainsSpacesIssue239() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/filenameSpacesErrorTwice_Issue239.epub", expectedErrors, expectedWarnings);
  }
	
	@Test
	public void testNcxIdIssue313() {
		// ID syntax in NCX files should be checked
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/issue313.epub", expectedErrors, expectedWarnings);
	}

	@Test
	public void testLinkedStylesheetCaseInsensitiveIssue316() {
		// rel="stylesheet" must be checked case-insensitive
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("invalid/issue316.epub", expectedErrors, expectedWarnings);
    List<MessageId> fatalErrors = new ArrayList<MessageId>();
    testValidateDocument("invalid/issue316.epub", expectedErrors, expectedWarnings, fatalErrors);
	}
}
