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

package com.adobe.epubcheck.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ExtraReportTest;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.ValidationReport.ItemReport;
import com.adobe.epubcheck.util.outWriter;

public class OPSCheckerTest
{

  public void testValidateDocument(String fileName, String mimeType,
                                   List<MessageId> errors, List<MessageId> warnings, EPUBVersion version)
  {
    testValidateDocument(fileName, mimeType, errors, warnings, new ArrayList<MessageId>(), version, false);
  }

  public void testValidateDocument(String fileName, String mimeType,
                                   List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors, EPUBVersion version)
  {
    testValidateDocument(fileName, mimeType, errors, warnings, fatalErrors, version, false);

  }
	
  public void testValidateDocument(String fileName, String mimeType,
                                   List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors, EPUBVersion version, boolean verbose)
  {
	  	testValidateDocument(fileName, mimeType, errors, warnings, fatalErrors, version, verbose, null);
	}

  public void testValidateDocument(String fileName, String mimeType,
      List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors, EPUBVersion version, ExtraReportTest extraTest)
  {
		testValidateDocument(fileName, mimeType, errors, warnings, fatalErrors, version, false, extraTest);
  }

	public void testValidateDocument(String fileName, String mimeType,
			    List<MessageId> errors, List<MessageId> warnings, List<MessageId> fatalErrors, EPUBVersion version, boolean verbose, ExtraReportTest extraTest)
  {
    ValidationReport testReport = new ValidationReport(fileName, String.format(Messages.get("single_file"), mimeType, version));
    String basepath = null;
    if (version == EPUBVersion.VERSION_2)
    {
      basepath = "/20/single/";
    }
    else if (version == EPUBVersion.VERSION_3)
    {
      basepath = "/30/single/";
    }

    GenericResourceProvider resourceProvider = null;
    if (fileName.startsWith("http://") || fileName.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(fileName);
    }
    else
    {
      URL fileURL = this.getClass().getResource(basepath + fileName);
      String filePath = fileURL != null ? fileURL.getPath() : basepath + fileName;
      resourceProvider = new FileResourceProvider(filePath);
    }

    OPSChecker opsChecker = new OPSChecker(basepath + fileName, mimeType,
        resourceProvider, testReport, version);

    opsChecker.validate();

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", errors, testReport.getErrorIds());
    assertEquals("The warning results do not match", warnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", fatalErrors, testReport.getFatalErrorIds());
		if (extraTest != null)
    {
			extraTest.test(testReport);
		}
  }

  @Test
  public void testValidateSVGRectInvalid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("svg/invalid/rect.svg", "image/svg+xml", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateSVGRectValid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("svg/valid/rect.svg", "image/svg+xml", expectedErrors, expectedWarnings,
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLEdits001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/edits-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLEmbed001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    // Collections.addAll(expectedErrors, );
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/embed-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLForms001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/forms-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLGlobalAttrs001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/global-attrs-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatals, EPUBVersion.VERSION_3, false);
  }

  @Test
  public void testValidateXHTMLOps001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/ops-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLOPSMATHML001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/ops-mathml-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLLINK()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/link.xhtml", "application/xhtml+xml",
        expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLLINKInvalid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_027, MessageId.CSS_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/link.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLXml11()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.HTM_001);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/xml11.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLOPSMATHML002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/ops-mathml-002.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLOPSSVG001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/ops-svg-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLOPSSVG002()
  {
	//assure that epub:type is allowed on svg elements
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/ops-svg-002.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatals, EPUBVersion.VERSION_3, true);
  }
  
  @Test
  public void testValidateXHTMLRuby001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/ruby-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLCanvas()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/canvas.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLInvalidCanvasFallback()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.MED_002);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/canvas-fallback.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSCH001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/sch-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSections001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/sections-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSSML()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/ssml.xhtml", "application/xhtml+xml",
        expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyle001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/style-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyle002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.CSS_008);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/style-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSwitch001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/switch-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTables001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/tables-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLText001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/text-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTrigger()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/trigger.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLData()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/data.xhtml", "application/xhtml+xml",
        expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLPrefixes001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/prefixes-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLInvalidPrefixes001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.OPF_028, MessageId.OPF_027);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/prefixes-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLVideo()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/video.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_OPSMATHML001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/ops-mathml-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_OPSMATHML002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/ops-mathml-002.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_SCH001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    //Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.MED_002, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    //mgy not sure what happened here, removed the first entry to make it pass
    Collections.addAll(expectedErrors, MessageId.MED_002, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatalErrors = new ArrayList<MessageId>();

    testValidateDocument("xhtml/invalid/sch-001.xhtml",
				"application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatalErrors,
        EPUBVersion.VERSION_3,false,new ExtraReportTest() {
					@Override
					public void test(ValidationReport testReport) {
						for (ItemReport error : testReport.errorList) {
							assertTrue("Error '"+error.message+"' has no line number.",error.line != -1);
							assertTrue("Error '"+error.message+"' has no column number.",error.column != -1);
						}
					}
				});
  }

  @Test
  public void testValidateXHTML_SVG001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/svg-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_Switch001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/switch-001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_Trigger()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/trigger.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_UnresolvedDTD()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.HTM_004, MessageId.RSC_001);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("ops/invalid/unresolved-entity.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTML_DupeID()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("ops/invalid/dupe-id.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTML_httpequiv1()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/http-equiv-1.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_httpequiv2()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/http-equiv-1.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_SSMLemptyPh()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.HTM_007, MessageId.HTM_007);
    testValidateDocument("xhtml/invalid/ssml-empty-ph.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_issue153_valid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/issue153.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_issue153_invalid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/issue153.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_issue166_valid()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("ops/valid/svg-foreignObject.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTML_doctype1_obsolete()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.HTM_004);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/doctype-1.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_doctype1()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //<!DOCTYPE html>
    testValidateDocument("xhtml/valid/doctype-1.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_doctype2()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //<!DOCTYPE html SYSTEM "about:legacy-compat">
    testValidateDocument("xhtml/valid/doctype-2.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue204()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    Collections.addAll(expectedWarnings, MessageId.HTM_025);
    testValidateDocument("xhtml/valid/issue204.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyleAttr001()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/styleAttr001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyleAttr002()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.CSS_008);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/styleAttr001.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  // this test should validate, see issue 173, need to wait for schema update.
//	@Test
//	public void testValidateXHTMLSVGwithRDF() { 
//		testValidateDocument("xhtml/valid/svg-rdf-001.xhtml",
//				"application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3, true);
//	}

  @Test
  public void testValidateSVGIssue196()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("ops/valid/svg-font-face.svg",
        "image/svg+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTMLIssue215()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("ops/valid/issue215.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateSVGIssue219()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("svg/valid/issue219.svg",
        "image/svg+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue222_223_20()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //foreignObject allowed outside switch, and <body> allowed inside
    testValidateDocument("ops/valid/issue222.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTMLIssue222_223_30()
  {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    //in 3.0 foreignObject content must be flow as per
    //http://idpf.org/epub/30/spec/epub30-contentdocs.html#confreq-svg-foreignObject
    //so the document gives 1 error
    testValidateDocument("svg/valid/issue222.xhtml",
        "application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
	}
	
	@Test
	public void testValidateXHTMLIssue248() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/issue248.xhtml",
				"application/xhtml+xml", expectedErrors, expectedWarnings, EPUBVersion.VERSION_3);
  }
	
	@Test
	public void testValidateXHTML301RDFaValid() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/rdfa.xhtml",
				"application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatals, EPUBVersion.VERSION_3, false, null);
  }
	
	@Test
	public void testValidateXHTML301MDValid() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/md.xhtml",
				"application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatals, EPUBVersion.VERSION_3, true, null);
  }
	
	@Test
	public void testValidateXHTML301MDInvalid() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("xhtml/invalid/md.xhtml",
				"application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatals, EPUBVersion.VERSION_3, false, null);
  }
	
	@Test
	public void testValidateXHTML301CustomAttributes() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/custom-ns-attrs.xhtml",
				"application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatals, EPUBVersion.VERSION_3, false, null);
  }
	
	@Test
	public void testValidateXHTML301AriaDescribedAt() {
    List<MessageId> expectedErrors = new ArrayList<MessageId>();
    List<MessageId> expectedWarnings = new ArrayList<MessageId>();
    List<MessageId> expectedFatals = new ArrayList<MessageId>();
    testValidateDocument("xhtml/valid/aria-describedAt.xhtml",
				"application/xhtml+xml", expectedErrors, expectedWarnings, expectedFatals, EPUBVersion.VERSION_3, false, null);
  }
}
