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

public class Epub20CheckTest {

	private ValidationReport testReport;

	private DocumentValidator epubCheck;

	private GenericResourceProvider resourceProvider;

	private boolean verbose;

	private static String path = "com.adobe.epubcheck.test/testdocs/20/epub/";

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
		testValidateDocument("ContainerNotOPF20.epub", 0, 1); 
	}

	@Test
	public void testValidateEPUBFileInMetaInfNotOPF20() {
		testValidateDocument("MetaInfNotOPF20.epub", 0, 1);
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
}
