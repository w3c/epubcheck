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
    testValidateDocument("valid/lorem.epub", "valid/lorem.txt");
  }

  @Test
  public void testValidateEPUBvalid20WithProfile()
  {
    EPUBProfile profile = EPUBProfile.DICT;
    testValidateDocument("valid/lorem.epub", profile);
  }

  @Test
  public void testValidateEPUBInvalid20()
  {
    Collections.addAll(expectedErrors, MessageId.PKG_007);
    testValidateDocument("invalid/lorem-mimetype.epub");
  }

  @Test
  public void testValidateEPUBPageMap20()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("PageMap20.epub");
  }

  @Test
  public void testValidateEPUBNoUniqueId20()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_030);
    testValidateDocument("OPFIllegalElement_UniqueID20.epub");
  }

  @Test
  public void testValidateEPUBOPFIllegalElement20()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("OPFIllegalElement20.epub");
  }

  @Test
  public void testValidateEPUBUnmanifested20()
  {
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.OPF_003);
    testValidateDocument("Unmanifested20.epub");
  }

  @Test
  public void testValidateEPUBPFileDeclaredInContainerNotOpf20()
  {
    testValidateDocument("ContainerNotOPF20.epub");
  }

  @Test
  public void testValidateEPUBFileInMetaInfIgnored()
  {
    testValidateDocument("MetaInfNotOPF20.epub");
  }

  @Test
  public void testValidateEPUBNullDate20()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_054);
    testValidateDocument("NullDate20.epub");
  }

  @Test
  public void testValidateEPUBNon8601Date20()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_054);
    testValidateDocument("Non8601Date20.epub");
  }

  @Test
  public void testValidateEPUBUnmanifestedGuideItems20()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_031, MessageId.RSC_007);
    testValidateDocument("UnmanifestedGuideItems20.epub");
  }

  @Test
  public void testValidateEPUBEmptyDir20()
  {
    Collections.addAll(expectedWarnings, MessageId.PKG_014);
    testValidateDocument("EmptyDir20.epub");
  }

  @Test
  public void testValidateEPUBPvalid20()
  {
    testValidateDocument("Test20.epub");
  }

  @Test
  public void testValidateEPUBPNoRootFiles()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_003);
    Collections.addAll(expectedFatals, MessageId.OPF_019);
    testValidateDocument("/invalid/no-rootfile.epub");
  }

  @Test
  public void testValidateEPUBPBadOpfNamespace()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.OPF_030);
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.OPF_003);
    Collections.addAll(expectedFatals, MessageId.OPF_019);
    testValidateDocument("/invalid/bad_opf_namespace.epub");
  }

  @Test
  public void testValidateEPUB_mimetypeAndVersion()
  {
    Collections.addAll(expectedErrors, MessageId.PKG_006, MessageId.OPF_001);
    Collections.addAll(expectedFatals, MessageId.OPF_019);
    testValidateDocument("/invalid/mimetypeAndVersion.epub");
  }

  @Test
  public void testValidateEPUB_noLinearYes()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_033);
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.OPF_003, MessageId.OPF_003);
    //+ 3 warnings that don't relate to linear
    testValidateDocument("/invalid/no-linear-yes.epub");
  }

  @Test
  public void testValidateEPUB_unusedImages()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_031, MessageId.RSC_007);
    Collections.addAll(expectedWarnings, MessageId.OPF_003, MessageId.PKG_010, MessageId.OPF_003, MessageId.OPF_003, MessageId.PKG_010, MessageId.OPF_003);
    //4 unused images in subfolder
    testValidateDocument("/invalid/issue89.epub");
  }

  @Test
  public void testValidateEPUB_issue138()
  {
    //warning for empty dc:title
    Collections.addAll(expectedWarnings, MessageId.OPF_055);
    testValidateDocument("/invalid/issue138.epub");
  }

  @Test
  public void testValidateEPUB_ncxDupeID()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("/invalid/ncx-dupe-id.epub");
  }

  @Test
  public void testValidateEPUB_unresolvedInternalLink()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_012);
    testValidateDocument("/invalid/unresolved-internal-xhtml-link.epub");
  }

  @Test
  public void testValidateEPUBvalidIssue169()
  {
    testValidateDocument("valid/issue169.epub", "valid/issue169.txt");
  }

  @Test
  public void testValidateEPUBinvalidIssue194()
  {
    Collections.addAll(expectedErrors, MessageId.HTM_009);
    testValidateDocument("invalid/issue194.bad.epub");
  }

  @Test
  public void testValidateEPUBvalidIssue194()
  {
    testValidateDocument("valid/issue194.good.epub");
  }
  
  @Test
  public void testValidateEPUBinvalidIssue176()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_001, MessageId.RSC_001, MessageId.RSC_001,
        MessageId.RSC_001);
    testValidateDocument("invalid/issue176.epub");
  }

  @Test
  public void testValidateEPUB30Issue170()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_032);
    //ncx references not allowed in guide
    testValidateDocument("invalid/issue170.epub");
	}
	
	@Test
	public void testMissingFullpathAttributeIssue236() {
    Collections.addAll(expectedErrors, MessageId.OPF_017, MessageId.OPF_016);
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    //container.xml missing @full-path attribute or @full-path is empty
		// issue 95 / issue 236
		testValidateDocument("invalid/issue236.epub");
	}
	
	@Test
	public void testFilenameContainsSpacesIssue239() {
    Collections.addAll(expectedWarnings, MessageId.PKG_010);
    testValidateDocument("invalid/filenameSpacesErrorTwice_Issue239.epub");
  }
	
	@Test
	public void testNcxIdIssue313() {
		// ID syntax in NCX files should be checked
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("invalid/issue313.epub");
	}
	
	@Test
	public void testNcxIdEmptyLabel() {
	  testValidateDocument("valid/issue301-ncx-empty-label.epub");
	}

	@Test
	public void testLinkedStylesheetCaseInsensitiveIssue316() {
		// rel="stylesheet" must be checked case-insensitive
	  Collections.addAll(expectedErrors, MessageId.RSC_007);
    testValidateDocument("invalid/issue316.epub");
	}

  @Test
  public void testValidateEPUB_issue21()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_050, MessageId.CHK_008);
    testValidateDocument("/Issue21.epub");
  }
}
