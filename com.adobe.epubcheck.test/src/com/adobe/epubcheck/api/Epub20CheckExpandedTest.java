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
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.ValidationReport;

public class Epub20CheckExpandedTest {

	private ValidationReport testReport;

	private DocumentValidator epubCheck;

	private boolean verbose;

	private static String path = "com.adobe.epubcheck.test/testdocs/20/expanded/";

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

        Archive epub = new Archive(path + fileName);
        testReport = new ValidationReport(epub.getEpubName());
        epub.createArchive();

        epubCheck = new EpubCheck(epub.getEpubFile(), testReport);

        epubCheck.validate();

        if (this.verbose) {
            this.verbose = false;
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
	
	@Test
	public void testValidateEPUBPLoremBasic() {
		testValidateDocument("valid/lorem/lorem-basic", 0, 0, "valid/lorem/lorem-basic.txt");
	}

	@Test
	public void testValidateEPUBMimetype() {
		testValidateDocument("invalid/lorem-mimetype", 2, 0, "invalid/lorem-mimetype.txt");
	}

	@Test
	public void testValidateEPUBUidSpaces() {
		//ascertain that leading/trailing space in 2.0 id values is accepted
		//issue 163
		testValidateDocument("valid/lorem-uidspaces", 0, 0, "valid/lorem-uidspaces.txt");
	}
	
	@Test
	public void testValidateEPUB20_circularFallback() {
		testValidateDocument("invalid/fallbacks-circular/", 5, 0, "invalid/fallbacks-circular.txt");
	}
	
	@Test
	public void testValidateEPUB20_okFallback() {
		testValidateDocument("valid/fallbacks/", 0, 0, "valid/fallbacks.txt");
	}
	
	@Test
	public void testValidateEPUB20_loremBasicDual() {
		testValidateDocument("valid/lorem-basic-dual/", 0, 0, "valid/lorem-basic-dual.txt");
	}
	
	@Test
	public void testValidateEPUB20_guideWithNcx() {
		testValidateDocument("valid/lorem-dual-guide/", 0, 0, "valid/lorem-dual-guide.txt");
	}
	
	@Test
	public void testValidateEPUB20_guideBrokenLink() {
		testValidateDocument("invalid/lorem-dual-guide/", 2, 0, "invalid/lorem-dual-guide.txt");
	}

}
