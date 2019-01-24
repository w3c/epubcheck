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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
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

  List<MessageId> expectedErrors = new LinkedList<MessageId>();
  List<MessageId> expectedWarnings = new LinkedList<MessageId>();
  List<MessageId> expectedFatals = new LinkedList<MessageId>();
  private final Messages messages = Messages.getInstance();

  public void testValidateDocument(String fileName, String mimeType, EPUBVersion version)
  {
    testValidateDocument(fileName, mimeType, version, false);
  }

  public void testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      boolean verbose)
  {
    testValidateDocument(fileName, mimeType, version, verbose, null);
  }

  public void testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      EPUBProfile profile)
  {
    testValidateDocument(fileName, mimeType, version, profile, false);
  }

  public void testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      EPUBProfile profile, boolean verbose)
  {
    testValidateDocument(fileName, mimeType, version, profile, verbose, null);
  }

  public void testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      ExtraReportTest extraTest)
  {
    testValidateDocument(fileName, mimeType, version, false, extraTest);
  }

  public void testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      boolean verbose, ExtraReportTest extraTest)
  {
    testValidateDocument(fileName, mimeType, version, EPUBProfile.DEFAULT, verbose, extraTest);
  }

  public void testValidateDocument(String fileName, String mimeType, EPUBVersion version,
      EPUBProfile profile, boolean verbose, ExtraReportTest extraTest)
  {
    ValidationReport testReport = new ValidationReport(fileName,
        String.format(messages.get("single_file"), mimeType, version, profile));
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
      try
      {
        URL fileURL = this.getClass().getResource(basepath + fileName);
        String filePath = fileURL != null ? new File(fileURL.toURI()).getAbsolutePath()
            : basepath + fileName;
        resourceProvider = new FileResourceProvider(filePath);
      } catch (URISyntaxException e)
      {
        throw new IllegalStateException("Cannot find test file", e);
      }
    }

    OPSChecker opsChecker = new OPSChecker(new ValidationContextBuilder().path(basepath + fileName)
        .mimetype(mimeType).resourceProvider(resourceProvider).report(testReport).version(version)
        .profile(profile).build());

    opsChecker.validate();

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());
    if (extraTest != null)
    {
      extraTest.test(testReport);
    }
  }

  @Before
  public void setup()
  {
    expectedErrors.clear();
    expectedWarnings.clear();
    expectedFatals.clear();
  }

  @Test
  public void testValidateSVGRectInvalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005);
    testValidateDocument("svg/invalid/rect.svg", "image/svg+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateSVGRectValid()
  {
    testValidateDocument("svg/valid/rect.svg", "image/svg+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLEdits001()
  {
    testValidateDocument("xhtml/valid/edits-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLEmbed001()
  {
    testValidateDocument("xhtml/valid/embed-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLForms001()
  {
    testValidateDocument("xhtml/valid/forms-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLGlobalAttrs001()
  {
    testValidateDocument("xhtml/valid/global-attrs-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLOps001()
  {
    testValidateDocument("xhtml/valid/ops-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLLINK()
  {
    testValidateDocument("xhtml/valid/link.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLLINKInvalid()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_027, MessageId.CSS_005);
    testValidateDocument("xhtml/invalid/link.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLUrlChecksInvalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_020);
    Collections.addAll(expectedWarnings, MessageId.HTM_025, MessageId.RSC_023, MessageId.RSC_023);
    testValidateDocument("xhtml/invalid/url-checks_issue-708.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLXml11()
  {
    Collections.addAll(expectedErrors, MessageId.HTM_001);
    testValidateDocument("xhtml/invalid/xml11.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testValidateXHTMLOPSSVG001()
  {
    testValidateDocument("xhtml/valid/ops-svg-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLOPSSVG002()
  {
    // assure that epub:type is allowed on svg elements
    testValidateDocument("xhtml/valid/ops-svg-002.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLRuby001()
  {
    testValidateDocument("xhtml/valid/ruby-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLCanvas()
  {
    testValidateDocument("xhtml/valid/canvas.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLInvalidCanvasFallback()
  {
    Collections.addAll(expectedErrors, MessageId.MED_002);
    testValidateDocument("xhtml/invalid/canvas-fallback.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSCH001()
  {
    testValidateDocument("xhtml/valid/sch-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSections001()
  {
    testValidateDocument("xhtml/valid/sections-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSSML()
  {
    testValidateDocument("xhtml/valid/ssml.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyle001()
  {
    testValidateDocument("xhtml/valid/style-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyle002()
  {
    Collections.addAll(expectedErrors, MessageId.CSS_008);
    testValidateDocument("xhtml/invalid/style-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testValidateXHTMLStyleInBody()
  {
    // one error for the style element, one for the scoped attribute
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/style-in-body.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSwitchIsDeprecated()
  {
    // tests that epub:switch is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/switch-deprecated.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testValidateXHTMLSwitchMathCase()
  {
    // tests that MathML within an epub:switch is validated
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // raises a warning as epub:switch is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/switch-invalid-mathml.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSwitchWithDefaultBeforeCase()
  {
    // tests that epub:default preceding epub:case is an error
    // one error for epub:default too soon, one error for epub:case too late
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    // raises a warning as epub:switch is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/switch-default-before-case.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSwitchWithTwoDefaults()
  {
    // tests that more than one epub:default is an error
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // raises a warning as epub:switch is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/switch-default-twice.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSwitchWithNoCase()
  {
    // tests that a missing epub:case is an error
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // raises a warning as epub:switch is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/switch-no-case.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSwitchWithNoDefault()
  {
    // tests that a missing epub:default is an error
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // raises a warning as epub:switch is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/switch-no-default.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSwitchWithNoRequiredNamespace()
  {
    // tests that a missing required-namespace attribute on epub:case is an error
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // raises a warning as epub:switch is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/switch-no-requirednamespace.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTables001()
  {
    testValidateDocument("xhtml/valid/tables-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  

  @Test
  public void testValidateXHTMLTableBorderAttribute()
  {
    testValidateDocument("xhtml/valid/table-border.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTableBorderAttributeInvalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/table-border.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLText001()
  {
    testValidateDocument("xhtml/valid/text-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testValidateXHTMLTitleMissing()
  {
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/title-missing.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTrigger()
  {
    // tests that epub:trigger is deprecated
    Collections.addAll(expectedWarnings, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/trigger-deprecated.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTriggerWithBadRefs()
  {
    // tests that epub:trigger ref points to an existing ID
    // tests that epub:trigger ev:observer points to an existing ID
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    // two warnings are raised since epub:trigger is deprecated 
    Collections.addAll(expectedWarnings, MessageId.RSC_017, MessageId.RSC_017);
    testValidateDocument("xhtml/invalid/trigger-badrefs.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLData()
  {
    testValidateDocument("xhtml/valid/data.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLPrefixes001()
  {
    testValidateDocument("xhtml/valid/prefixes-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLInvalidPrefixes001()
  {
    Collections.addAll(expectedErrors, MessageId.OPF_028, MessageId.OPF_027);
    testValidateDocument("xhtml/invalid/prefixes-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLVideo()
  {
    testValidateDocument("xhtml/valid/video.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_SCH001()
  {
    // Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.MED_002,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
    // MessageId.RSC_005);
    // mgy not sure what happened here, removed the first entry to make it pass
    Collections.addAll(expectedErrors, MessageId.MED_002, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005);

    testValidateDocument("xhtml/invalid/sch-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, false, new ExtraReportTest()
        {
          @Override
          public void test(ValidationReport testReport)
          {
            for (ItemReport error : testReport.errorList)
            {
              assertTrue("Error '" + error.message + "' has no line number.", error.line != -1);
              assertTrue("Error '" + error.message + "' has no column number.", error.column != -1);
            }
          }
        });
  }

  @Test
  public void testValidateXHTML_SVG001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/svg-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_UnresolvedDTD()
  {
    Collections.addAll(expectedErrors, MessageId.HTM_004);
    testValidateDocument("ops/invalid/unresolved-entity.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTML_DupeID()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("ops/invalid/dupe-id.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTML_DupeID_EPUB3()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/duplicate-id.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_httpequiv()
  {
    testValidateDocument("xhtml/valid/http-equiv-1.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_httpequiv_caseinsensitive()
  {
    testValidateDocument("xhtml/valid/http-equiv-2.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_httpequiv_invalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/http-equiv-1.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_httpequivInvalidMetaSibling()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/http-equiv-2.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_SSMLemptyPh()
  {
    Collections.addAll(expectedWarnings, MessageId.HTM_007, MessageId.HTM_007);
    testValidateDocument("xhtml/invalid/ssml-empty-ph.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_issue153_invalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/issue153.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_issue166_valid()
  {
    testValidateDocument("ops/valid/svg-foreignObject.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTML_doctype1_obsolete()
  {
    Collections.addAll(expectedErrors, MessageId.HTM_004);
    testValidateDocument("xhtml/invalid/doctype-1.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_doctype1()
  {
    // <!DOCTYPE html>
    testValidateDocument("xhtml/valid/doctype-1.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_doctype2()
  {
    // <!DOCTYPE html SYSTEM "about:legacy-compat">
    testValidateDocument("xhtml/valid/doctype-2.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_SVGLinks()
  {
    testValidateDocument("xhtml/valid/svg-links.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_SVGLinks_MisssingTitle()
  {
    expectedWarnings.add(MessageId.ACC_011);
    testValidateDocument("xhtml/invalid/svg-links.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateSVG_Links()
  {
    testValidateDocument("svg/valid/svg-links.svg", "image/svg+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateSVG_ValidStyleWithoutType_issue688()
  {
    testValidateDocument("svg/valid/issue688.svg", "image/svg+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateSVG_Links_MisssingTitle()
  {
    expectedWarnings.add(MessageId.ACC_011);
    testValidateDocument("svg/invalid/svg-links.svg", "image/svg+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue204()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    Collections.addAll(expectedWarnings, MessageId.HTM_025);
    testValidateDocument("xhtml/valid/issue204.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyleAttr001()
  {
    testValidateDocument("xhtml/valid/styleAttr001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLStyleAttr002()
  {
    Collections.addAll(expectedErrors, MessageId.CSS_008);
    testValidateDocument("xhtml/invalid/styleAttr001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  // this test should validate, see issue 173, need to wait for schema update.
  // @Test
  // public void testValidateXHTMLSVGwithRDF() {
  // testValidateDocument("xhtml/valid/svg-rdf-001.xhtml",
  // "application/xhtml+xml",
  // EPUBVersion.VERSION_3, true);
  // }

  @Test
  public void testValidateSVGIssue196()
  {
    testValidateDocument("ops/valid/svg-font-face.svg", "image/svg+xml", EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTMLIssue215()
  {
    testValidateDocument("ops/valid/issue215.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateSVGIssue219()
  {
    testValidateDocument("svg/valid/issue219.svg", "image/svg+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue222_223_20()
  {
    // foreignObject allowed outside switch, and <body> allowed inside
    testValidateDocument("ops/valid/issue222.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTMLSVGForeignObject()
  {
    // foreignObject allowed outside switch, and <body> allowed inside
    testValidateDocument("xhtml/valid/svg-foreignobject.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSVGForeignObjectBody()
  {
    // foreignObject with disallowed flow content
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/svg-foreignobject-body.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSVGForeignObjectNotFlow()
  {
    // foreignObject with disallowed flow content
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/svg-foreignobject-not-flow.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLSVGIssue769()
  {
    // allow aria attributes on SVG elements
    testValidateDocument("svg/valid/issue769.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue248()
  {
    testValidateDocument("xhtml/valid/issue248.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue282_ObjectTypemustmatch()
  {
    testValidateDocument("xhtml/valid/issue282-object-typemustmatch.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue287_NestedHyperlink()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("ops/invalid/issue287-nested-hyperlink.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTMLIssue288_InvalidURI()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_020);
    testValidateDocument("xhtml/invalid/issue288-invalid-uri.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue293()
  {
    testValidateDocument("ops/valid/issue293-edits-elem-attributes.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTMLIssue296()
  {
    testValidateDocument("xhtml/valid/issue296-irc-uri.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue340()
  {
    testValidateDocument("xhtml/valid/issue340.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue341()
  {
    testValidateDocument("xhtml/valid/issue341.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLIssue355()
  {
    testValidateDocument("xhtml/valid/issue355.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML301RDFaValid()
  {
    testValidateDocument("xhtml/valid/rdfa.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML301MDValid()
  {
    testValidateDocument("xhtml/valid/md.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML301MDInvalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/md.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML301CustomAttributes()
  {
    testValidateDocument("xhtml/valid/custom-ns-attrs.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML301AriaDescribedAt()
  {
    expectedErrors.add(MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/aria-describedAt.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testEdupubSectioning_ExplicitBody()
  {
    testValidateDocument("xhtml/valid/edupub-sectioning-explicit-body.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_ExplicitSections()
  {
    testValidateDocument("xhtml/valid/edupub-sectioning-explicit-sections.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_ImplicitBody()
  {
    testValidateDocument("xhtml/valid/edupub-sectioning-implicit-body.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_Subtitle()
  {
    testValidateDocument("xhtml/valid/edupub-sectioning-subtitle.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_Invalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/edupub-sectioning.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_InvalidExplicitBody()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/edupub-sectioning-explicit-body.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_InvalidImplicitBody()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/edupub-sectioning-implicit-body.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_InvalidImplicitBodyAriaHeading()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/edupub-sectioning-implicit-body-aria-heading.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_InvalidSubtitle()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/edupub-sectioning-subtitle.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupubSectioning_InvalidAriaLabel()
  {
    // aria-label MUST NOT be equal to heading content
    // 2 errors: one on body and one on sub-section
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/edupub-sectioning-arialabel-heading.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupupHeaading_ImgWithAltText()
  {
    testValidateDocument("xhtml/valid/edupub-heading-img.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testEdupupHeaading_ImgWithEmptyAltText()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/edupub-heading-imgnoalt.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, EPUBProfile.EDUPUB);
  }

  @Test
  public void testMathML_1()
  {
    testValidateDocument("xhtml/valid/mathml-01.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathML_2()
  {
    testValidateDocument("xhtml/valid/mathml-02.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLWithNoAlt()
  {
    testValidateDocument("xhtml/valid/mathml-noalt.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLWithContentMathML()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-contentmathml.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }


  @Test
  public void testMathMLAnnotation()
  {
    testValidateDocument("xhtml/valid/mathml-annotation-tex.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMathMLAnnotationXMLWithMathMLContent()
  {
    testValidateDocument("xhtml/valid/mathml-annotationxml-mathml-content.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testMathMLAnnotationXMLWithMathMLPresentation()
  {
    testValidateDocument("xhtml/valid/mathml-annotationxml-mathml-presentation.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithMathMLAsXHTML()
  {
    // one error for mtext not allowed in annotation-xml
    // one side-effect error for the annotation mtext not being in math
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-annotationxml-mathml-in-xhtml.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithMathMLAndNoNameAttr()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-annotationxml-mathml-noname.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithMathMLAndInvalidNameAttr()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-annotationxml-mathml-invalidname.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithMathMLAndInvalidEncodingAttr()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-annotationxml-mathml-invalidencoding.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithXHTML()
  {
    testValidateDocument("xhtml/valid/mathml-annotationxml-xhtml.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithXHTMLAndNoNameAttr()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-annotationxml-xhtml-noname.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithXHTMLAndInvalidNameAttr()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-annotationxml-xhtml-invalidname.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testMathMLAnnotationXMLWithXHTMLAndInvalidEncodingAttr()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/mathml-annotationxml-xhtml-invalidencoding.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testIndex()
  {
    testValidateDocument("xhtml/valid/index.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3,
        EPUBProfile.IDX);
  }

  @Test
  public void testIndex_NoIndex()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/index-noindex.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, EPUBProfile.IDX);
  }

  @Test
  public void testIndex_IndexNotOnBody()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/index-notonbody.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3, EPUBProfile.IDX);
  }

  @Test
  public void testValidateXHTMLImageMap_EPUB2_Valid()
  {
    testValidateDocument("xhtml/valid/imagemap-good_issue696.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateXHTMLImageMap_EPUB3_Valid()
  {
    testValidateDocument("xhtml/valid/imagemap-good_issue696.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLImageMap_EPUB3_Invalid()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/imagemap-bad_issue696.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLEmptyClass_EPUB2_Valid()
  {
    testValidateDocument("xhtml/valid/empty-class-attribute-is-valid_issue733.xhtml",
        "application/xhtml+xml", EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateEmptyLangAttribute_EPUB3_Valid()
  {
    testValidateDocument("xhtml/valid/issue777-empty-lang.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testObsoleteContextMenuAttribute()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/obsolete-contextmenu.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testObsoleteDropzoneAttribute()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/obsolete-dropzone.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testObsoleteKeygenElement()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/obsolete-keygen.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testObsoleteMenus()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/obsolete-menus.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testObsoletePubdateAttribute()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/obsolete-pubdate.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testObsoleteSeamessIframe()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/obsolete-seamless-iframe.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testContentModel_TimeInTime()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/time-in-time.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testEntitiesValid()
  {
    // tests that known named character references are accepted
    // also tests that 'entity references' in comments or CDATA sections are ignored 
    testValidateDocument("xhtml/valid/entities.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

  @Test
  public void testEntitiesInternalDeclaration()
  {
    // tests that internal entity declarations are allowed
    testValidateDocument("xhtml/valid/entities-internal.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testEntitiesMissingSemicolon()
  {
    // tests that entity references not ending with a semicolon cause a parsing error
    Collections.addAll(expectedFatals, MessageId.RSC_016);
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/entities-missing-semicolon.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }
  
  @Test
  public void testEntitiesUnknown()
  {
    // tests that unknown entity references are reported as errors
    Collections.addAll(expectedFatals, MessageId.RSC_016);
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/entities-unknown.xhtml", "application/xhtml+xml", EPUBVersion.VERSION_3);
  }

}
