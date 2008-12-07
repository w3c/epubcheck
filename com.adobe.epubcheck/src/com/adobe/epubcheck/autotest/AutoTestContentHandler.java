/*
 * Copyright 2008 Adobe Systems Incorporated
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

package com.adobe.epubcheck.autotest;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.DefaultReportImpl;
import com.adobe.epubcheck.autotest.Result;

/**
 * @author Paul Norton
 * 
 *
 */
public class AutoTestContentHandler implements ContentHandler {
	
	Report report;
	String path;
	public static final String TESTSUITE = "testsuite";
	public static final String TEST = "test";
	public static final String TYPE = "type";
	public static final String TITLE = "title";
	public static final String FILE = "file";
	public static final String DESCRIPTION = "description";
	public static final String RESULTTAG = "result";
	public static final String VALIDITY = "valid";
	public static final String ERRORS = "errors";
	public static final String WARNINGS = "warnings";
 
	
	boolean inFile = false;
	boolean inDesc = false;
	boolean inErrors = false;
	boolean inWarnings = false;
	String urlString = "";
	String zipFile = "";
	String desc = "";
	Result expectedResult;
	Result result;
	int testsRun;
	int testsPassed;
	
	public AutoTestContentHandler(String str) {
		this.path = str;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(inFile) {
			zipFile = this.path;
			zipFile += new String(ch, start, length).trim();
		} else if(inDesc) {
			desc = new String(ch, start, length).trim();
		} else if(inErrors) {
			int errors;
			try {
				errors = Integer.parseInt(new String(ch, start, length));
			} catch (NumberFormatException e) {
				errors = 0;
			}
			expectedResult.setErrors(errors);
		} else if(inWarnings) {
			int warnings;
			try {
				warnings = Integer.parseInt(new String(ch, start, length));
			} catch (NumberFormatException e) {
				warnings = 0;
			}
			expectedResult.setWarnings(warnings);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (name.equals(TESTSUITE)) {
			System.out.println();
			System.out.println("Tests run:" + testsRun);
			System.out.println("Tests passed:" + testsPassed);
		} else if (name.equals(TEST)) {
			System.out.println();
			System.out.println(zipFile);
			Report report = new DefaultReportImpl(zipFile);
			EpubCheck check = new EpubCheck(new File(zipFile), report);
			result.setValid(check.validate());
			result.setErrors(check.errorCount);
			result.setWarnings(check.warningCount);
			if (result.isValid() == expectedResult.isValid()
					&& result.getErrors() == expectedResult.getErrors()
					&& result.getWarnings() == expectedResult.getWarnings()) {
				testsPassed++;
			} else {
				System.out.println("***********************");
				System.out.println("Test Failed");
				System.out.println("\t"+zipFile);
				System.out.println("\t"+desc);
				System.out.println("\tExpectedErrors: "+expectedResult.getErrors());
				System.out.println("\tExpectedWarnings: "+expectedResult.getWarnings());
				System.out.println("***********************");
			}
			testsRun++;
		} else if (name.equals(FILE)) {
			inFile = false;
		} else if (name.equals(DESCRIPTION)) {
			inDesc = false;
		} else if (name.equals(WARNINGS)) {
			inWarnings = false;
		} else if (name.equals(ERRORS)) {
			inErrors = false;
		}

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String target, String data)
			throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator locator) {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String name) throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if (name.equals(TESTSUITE)) {
			testsRun = 0;
			testsPassed = 0;
		} else if(name.equals(TEST)) {
			expectedResult = new Result();
			result = new Result();
			zipFile = "";
			desc = "";
		} else if(name.equals(FILE)) {
			inFile = true;
		} else if(name.equals(DESCRIPTION)) {
			inDesc = true;
		} else if(name.equals(RESULTTAG)) {
				boolean valid = false;
				try {
					valid = atts.getValue("", "valid").equals("true");
				} catch (Exception e) {}
				expectedResult.setValid(valid);
		} else if(name.equals(WARNINGS)) {
			inWarnings = true;
		} else if(name.equals(ERRORS)) {
			inErrors = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

}
