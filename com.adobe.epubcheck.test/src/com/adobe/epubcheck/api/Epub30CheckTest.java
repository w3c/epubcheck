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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;

public class Epub30CheckTest {

	private ValidationReport testReport;

	private DocumentValidator epubCheck;

	private GenericResourceProvider resourceProvider;

	private boolean verbose;

	private static String path = "com.adobe.epubcheck.test/testdocs/30/epub/";

	/*
	 * TEST DEBUG FUNCTION
	 */
    public void testValidateDocument(String fileName, int errors, int warnings,
            boolean verbose)  {
        if (verbose)
            this.verbose = verbose;
        testValidateDocument(fileName, errors, warnings);
    }

    public void testValidateDocument(String fileName, int errors, int warnings) {
        testValidateDocument(fileName, errors, warnings, null);
    }
    
    public void testValidateDocument(String fileName, int errors, int warnings, String resultFile, boolean verbose) {
        if (verbose)
            this.verbose = verbose;
        testValidateDocument(fileName, errors, warnings, resultFile);
    
    }

    public void testValidateDocument(String fileName, int errors, int warnings, String resultFile) {
        boolean fromFile = false;

        testReport = new ValidationReport(fileName);

        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            resourceProvider = new URLResourceProvider(fileName);
        } else {
            resourceProvider = new FileResourceProvider(path + fileName);
            fromFile = true;
        }

        if (fromFile)
            epubCheck = new EpubCheck(new File(path + fileName), testReport);
        else
            try {
                epubCheck = new EpubCheck(
                        resourceProvider.getInputStream(null), testReport, path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        epubCheck.validate();

        if (verbose) {
            verbose = false;
            System.out.println(testReport);
        }

        assertEquals(errors, testReport.getErrorCount());
        assertEquals(warnings, testReport.getWarningCount());
        
        if (resultFile != null) {
            File f = new File(path + resultFile);
            assertTrue(f.getAbsolutePath() + " doesn't exist", f.exists());
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f)));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() != 0 && !line.startsWith("#")) { // allow comments
                        assertTrue(line + " not found", testReport.hasInfoMessage(line));
                    }
                }
            } catch (IOException e) { /* IGNORE */
            } finally {
                if (in != null) { try { in.close(); } catch (IOException e) { /* IGNORE */ } } 
            }
        }
    }

    // TODO -- check for fallback cycles
	/*
	 * @Test public void testValidateEPUBPFallbackCycle() {
	 * testValidateDocument("invalid/fallback-cycle.epub", 0, 0, true); }
	 */
	@Test
	public void testValidateEPUBPvalid30() {
		testValidateDocument("valid/lorem.epub", 0, 0, "valid/lorem.txt");
	}

	@Test
	public void testValidateEPUBTestSvg() {
		testValidateDocument("valid/test_svg.epub", 0, 0, "valid/test_svg.txt");
	}

	@Test
	public void testValidateEPUBInvalidNcx() {
		testValidateDocument("invalid/invalid-ncx.epub", 2, 0);
	}

	@Test
	public void testValidateEPUBMp3() {
		testValidateDocument("valid/mp3-in-manifest.epub", 0, 0, "valid/mp3-in-manifest.txt");
	}

	@Test
	public void testValidateEPUBInvalidMp3() {
		testValidateDocument("invalid/mp3-in-spine-no-fallback.epub", 1, 0);
	}

	@Test
	public void testValidateEPUBMp3WithFallback() {
		testValidateDocument("valid/mp3-with-fallback.epub", 0, 0, "valid/mp3-with-fallback.txt");
	}

	@Test
	public void testValidateEPUBFontNoFallback() {
		testValidateDocument("invalid/font_no_fallback.epub", 1, 0);
	}

	@Test
	public void testValidateEPUBFontFallbackChain() {
		testValidateDocument("valid/font_fallback_chain.epub", 0, 0, "valid/font_fallback_chain.txt");
	}

	@Test
	public void testValidateEPUBvalid30() {
		testValidateDocument("valid/lorem.epub", 0, 0, "valid/lorem.txt");
	}

	@Test
	public void testValidateEPUB30_xhtmlsch() {
		// 1 schematron error from xhtml validation
		testValidateDocument("invalid/lorem-xht-sch-1.epub", 1, 0);
	}

	@Test
	public void testValidateEPUB30_xhtmlrng() {
		// 1 rng error from xhtml validation
		testValidateDocument("invalid/lorem-xht-rng-1.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_navInvalid() {
		// invalid nav issue reported by MattG
		testValidateDocument("invalid/nav-invalid.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30ValidExtension1() { 
		testValidateDocument("valid/extension-1.ePub", 0, 1, "valid/extension-1.txt");
	}
	
	@Test
	public void testValidateEPUB30CSSProfile() { 
		//issue145; CSS3 pseudoselectors causing css2 lexers to bail out
		testValidateDocument("valid/issue145.epub", 0, 0, "valid/issue145.txt");
	}
	
	@Test
	public void testValidateEPUB30Issue158() { 
		//bad warning message, this should pass without warnings
		testValidateDocument("valid/issue158.epub", 0, 0, "valid/issue158.txt");
	}
	
	@Test
	public void testValidateEPUB30Issue137a() { 
		testValidateDocument("invalid/issue137a.epub", 2, 1);
	}
	
	@Test
	public void testValidateEPUB30Issue137b() { 
		testValidateDocument("invalid/issue137b.epub", 2, 1);
	}
	
	@Test
	public void testValidateEPUB30specValid() { 
		testValidateDocument("valid/epub30-spec.epub", 0, 0, "valid/epub30-spec.txt");
	}
}
