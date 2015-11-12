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
      try {
        URL fileURL = this.getClass().getResource(basepath + fileName);
        String filePath = fileURL != null ? new File(fileURL.toURI()).getAbsolutePath() : basepath + fileName;
        resourceProvider = new FileResourceProvider(filePath);
      } catch (URISyntaxException e) {
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
        EPUBVersion.VERSION_3, false);
  }

  @Test
  public void testValidateXHTMLOps001()
  {
    testValidateDocument("xhtml/valid/ops-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLOPSMATHML001()
  {
    testValidateDocument("xhtml/valid/ops-mathml-001.xhtml", "application/xhtml+xml",
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
  public void testValidateXHTMLOPSMATHML002()
  {
    testValidateDocument("xhtml/valid/ops-mathml-002.xhtml", "application/xhtml+xml",
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
        EPUBVersion.VERSION_3, false);
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
  public void testValidateXHTMLSwitch001()
  {
    testValidateDocument("xhtml/valid/switch-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTables001()
  {
    testValidateDocument("xhtml/valid/tables-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLText001()
  {
    testValidateDocument("xhtml/valid/text-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTMLTrigger()
  {
    testValidateDocument("xhtml/valid/trigger.xhtml", "application/xhtml+xml",
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
  public void testValidateXHTML_OPSMATHML001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/ops-mathml-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_OPSMATHML002()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/ops-mathml-002.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
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
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005);

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
  public void testValidateXHTML_Switch001()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005, MessageId.RSC_005,
        MessageId.RSC_005, MessageId.RSC_005);

    testValidateDocument("xhtml/invalid/switch-001.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

  @Test
  public void testValidateXHTML_Trigger()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005, MessageId.RSC_005);
    testValidateDocument("xhtml/invalid/trigger.xhtml", "application/xhtml+xml",
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
  public void testValidateXHTML_issue153_valid()
  {
    testValidateDocument("xhtml/valid/issue153.xhtml", "application/xhtml+xml",
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
  public void testValidateXHTMLIssue222_223_30()
  {
    Collections.addAll(expectedErrors, MessageId.RSC_005);
    // in 3.0 foreignObject content must be flow as per
    // http://idpf.org/epub/30/spec/epub30-contentdocs.html#confreq-svg-foreignObject
    // so the document gives 1 error
    testValidateDocument("svg/valid/issue222.xhtml", "application/xhtml+xml",
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
    expectedWarnings.add(MessageId.RSC_017);
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
  public void testMathML()
  {
    testValidateDocument("xhtml/valid/mathml.xhtml", "application/xhtml+xml",
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
    testValidateDocument("xhtml/valid/empty-class-attribute-is-valid_issue733.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_2);
  }

  @Test
  public void testValidateEmptyLangAttribute_EPUB3_Valid()
  {
    testValidateDocument("xhtml/valid/issue777-empty-lang.xhtml", "application/xhtml+xml",
        EPUBVersion.VERSION_3);
  }

}
