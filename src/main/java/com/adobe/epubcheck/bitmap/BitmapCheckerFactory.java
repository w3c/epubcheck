/*
 * Copyright (c) 2007 Adobe Systems Incorporated
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

package com.adobe.epubcheck.bitmap;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.ContentCheckerFactory;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;

public class BitmapCheckerFactory implements ContentCheckerFactory {

	public ContentChecker newInstance(OCFPackage ocf, Report report,
			String path, String mimeType, String properties,
			XRefChecker xrefChecker, EPUBVersion version) {
		return new BitmapChecker(ocf, report, path, mimeType);
	}

	static private BitmapCheckerFactory instance = new BitmapCheckerFactory();

	static public BitmapCheckerFactory getInstance() {
		return instance;
	}
}
