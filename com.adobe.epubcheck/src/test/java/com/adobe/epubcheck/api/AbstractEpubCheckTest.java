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
import java.net.URISyntaxException;
import java.net.URL;

import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;

public abstract class AbstractEpubCheckTest {


	private String basepath;
	
    protected AbstractEpubCheckTest(String basepath) {
		this.basepath = basepath;
	}

	public void testValidateDocument(String fileName, int errors, int warnings) {
        testValidateDocument(fileName, errors, warnings, false);
    }
    
    public void testValidateDocument(String fileName, int errors, int warnings,
    		boolean verbose)  {
    	testValidateDocument(fileName, errors, warnings, null, verbose);
    }
    
    public void testValidateDocument(String fileName, int errors, int warnings, String resultFile) {
        testValidateDocument(fileName, errors, warnings, resultFile, false);
    
    }

    public void testValidateDocument(String fileName, int errors, int warnings, String resultFile, boolean verbose) {
    	DocumentValidator epubCheck;

        ValidationReport testReport = new ValidationReport(fileName);

        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
        	GenericResourceProvider resourceProvider = new URLResourceProvider(fileName);
        	try {
                epubCheck = new EpubCheck(
                        resourceProvider.getInputStream(null), testReport, fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
        	File testFile;
			try {
				testFile = new File(this.getClass().getResource(basepath + fileName).toURI());
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Cannot find test file",e);
			}
        	if (testFile.isDirectory()) {
        		Archive epub = new Archive(testFile.getPath());
        		testReport = new ValidationReport(epub.getEpubName());
        		epub.createArchive();
        		epubCheck = new EpubCheck(epub.getEpubFile(), testReport);
        	} else {
        		epubCheck = new EpubCheck(new File(testFile.getPath()), testReport);
        	}
        }


        epubCheck.validate();

        if (verbose) {
            verbose = false;
            System.out.println(testReport);
        }

        assertEquals(errors, testReport.getErrorCount());
        assertEquals(warnings, testReport.getWarningCount());
        
        if (resultFile != null) {
        	URL fileURL = this.getClass().getResource(basepath + resultFile);
            File f = null;
			try {
				f = new File(fileURL.toURI());
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Cannot find test file",e);
			}
            assertTrue(f.getAbsolutePath() + " doesn't exist", f.exists());
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f), "utf-8"));
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

}
