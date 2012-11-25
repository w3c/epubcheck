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
package com.adobe.epubcheck.css;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.ContentCheckerFactory;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;

public class CSSCheckerFactory implements ContentCheckerFactory {

	/*
	 * (non-Javadoc)
	 * @see com.adobe.epubcheck.opf.ContentCheckerFactory#newInstance(com.adobe.epubcheck.ocf.OCFPackage, com.adobe.epubcheck.api.Report, java.lang.String, java.lang.String, java.lang.String, com.adobe.epubcheck.opf.XRefChecker, com.adobe.epubcheck.util.EPUBVersion)
	 */
	public ContentChecker newInstance(OCFPackage ocf, Report report,
			String path, String mimeType, String properties,
			XRefChecker xrefChecker, EPUBVersion version) {
	
		return new CSSChecker(ocf, report, path, xrefChecker, version);
	}

	/**
	 * Additional constructor for validating CSS strings (style attributes and elements)
	 */
	public ContentChecker newInstance(OCFPackage ocf, Report report,
			String value, boolean isStyleAttribute, String path, int line, int col, 
			XRefChecker xrefChecker, EPUBVersion version) {
	
		return new CSSChecker(ocf, report, value, isStyleAttribute, path, line, col, xrefChecker, version);
	}
	
	static private CSSCheckerFactory instance = new CSSCheckerFactory();

	static public CSSCheckerFactory getInstance() {
		return instance;
	}

}
