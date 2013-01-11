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

package com.adobe.epubcheck.ncx;

import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class NCXHandler implements XMLHandler {

	XMLParser parser;

	String path;

	XRefChecker xrefChecker;

	NCXHandler(XMLParser parser, String path, XRefChecker xrefChecker) {
		this.parser = parser;
		this.path = path;
		this.xrefChecker = xrefChecker;
	}

	public void characters(char[] chars, int arg1, int arg2) {
	}

	public void ignorableWhitespace(char[] chars, int arg1, int arg2) {
	}

	public void startElement() {
		XMLElement e = parser.getCurrentElement();
		String ns = e.getNamespace();
		String name = e.getName();
		if (ns.equals("http://www.daisy.org/z3986/2005/ncx/")) {
			if (name.equals("content")) {
				String href = e.getAttribute("src");
				if (href != null) {
					href = PathUtil.resolveRelativeReference(path, href, null);
					if (href.startsWith("http")) {
	                    parser.getReport().info(path, FeatureEnum.REFERENCE, href);
					}
					xrefChecker.registerReference(path, parser.getLineNumber(),
							parser.getColumnNumber(), href,
							XRefChecker.RT_HYPERLINK);
				}

			}
		}
	}

	public void endElement() {
	}

	public void processingInstruction(String arg0, String arg1) {
	}
}
