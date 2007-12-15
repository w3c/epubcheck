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

package com.adobe.epubcheck.ops;

import java.util.HashSet;

import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OPSHandler implements XMLHandler {

	XMLParser parser;

	String path;

	HashSet idMap;

	XRefChecker xrefChecker;

	OPSHandler(XMLParser parser, String path, XRefChecker xrefChecker) {
		this.parser = parser;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.idMap = new HashSet();
	}

	private void checkPaint(XMLElement e, String attr) {
		String paint = e.getAttribute(attr);
		if (paint != null && paint.startsWith("url(") && paint.endsWith(")")) {
			String href = paint.substring(4, paint.length() - 1);
			href = PathUtil.resolveRelativeReference(path, href);
			xrefChecker.registerReference(path, parser.getLineNumber(), href,
					XRefChecker.RT_SVG_PAINT);
		}
	}

	private void checkClip(XMLElement e, String attr) {

	}

	private void checkImage(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		if (href != null) {
			href = PathUtil.resolveRelativeReference(path, href);
			xrefChecker.registerReference(path, parser.getLineNumber(), href,
					XRefChecker.RT_IMAGE);
		}
	}

	private void checkObject(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		if (href != null) {
			href = PathUtil.resolveRelativeReference(path, href);
			xrefChecker.registerReference(path, parser.getLineNumber(), href,
					XRefChecker.RT_OBJECT);
		}
	}

	private void checkSymbol(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		if (href != null) {
			href = PathUtil.resolveRelativeReference(path, href);
			xrefChecker.registerReference(path, parser.getLineNumber(), href,
					XRefChecker.RT_SVG_SYMBOL);
		}
	}

	private void checkHRef(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		if (href != null) {
			if (href.startsWith("http://") || href.startsWith("https://")
					|| href.startsWith("ftp://") || href.startsWith("mailto:")
					|| href.startsWith("data:"))
				return;
			try {
				href = PathUtil.resolveRelativeReference(path, href);
			} catch (IllegalArgumentException err) {
				parser.getReport().error(path, parser.getLineNumber(),
						err.getMessage());
				return;
			}
			xrefChecker.registerReference(path, parser.getLineNumber(), href,
					XRefChecker.RT_HYPERLINK);
		}
	}

	public void startElement() {
		XMLElement e = parser.getCurrentElement();
		String id = e.getAttribute("id");
		String ns = e.getNamespace();
		String name = e.getName();
		int resourceType = XRefChecker.RT_GENERIC;
		if (ns != null) {
			if (ns.equals("http://www.w3.org/2000/svg")) {
				if (name.equals("linearGradient")
						|| name.equals("radialGradient")
						|| name.equals("pattern"))
					resourceType = XRefChecker.RT_SVG_PAINT;
				else if (name.equals("clipPath"))
					resourceType = XRefChecker.RT_SVG_CLIP_PATH;
				else if (name.equals("symbol"))
					resourceType = XRefChecker.RT_SVG_SYMBOL;
				else if (name.equals("a"))
					checkHRef(e, "http://www.w3.org/1999/xlink", "href");
				else if (name.equals("use"))
					checkSymbol(e, "http://www.w3.org/1999/xlink", "href");
				else if (name.equals("image"))
					checkImage(e, "http://www.w3.org/1999/xlink", "href");
				checkPaint(e, "fill");
				checkPaint(e, "stroke");
				checkClip(e, "clip");
			} else if (ns.equals("http://www.w3.org/1999/xhtml")) {
				if (name.equals("a"))
					checkHRef(e, null, "href");
				else if (name.equals("img"))
					checkImage(e, null, "src");
				else if (name.equals("object"))
					checkObject(e, null, "data");
				resourceType = XRefChecker.RT_HYPERLINK;
			}
		}
		if (id != null)
			xrefChecker.registerAnchor(path, parser.getLineNumber(), id,
					resourceType);
	}

	public void endElement() {
	}

	public void ignorableWhitespace(char[] chars, int arg1, int arg2) {
	}

	public void characters(char[] chars, int arg1, int arg2) {
	}

	public void processingInstruction(String arg0, String arg1) {
	}

}
