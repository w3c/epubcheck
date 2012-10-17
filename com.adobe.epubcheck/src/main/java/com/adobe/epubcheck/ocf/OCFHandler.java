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

package com.adobe.epubcheck.ocf;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OCFHandler implements OCFData, XMLHandler {

	private Map<String,List<String>> entries = new HashMap<String, List<String>>();

	XMLParser parser;

	OCFHandler(XMLParser parser) {
		this.parser = parser;
	}

	@Override
	public List<String> getEntries(String mediatype) {
		if (entries.containsKey(mediatype)){
			return Collections.unmodifiableList(entries.get(mediatype));
		} else {
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<String> getEntries() {
		LinkedList<String> result = new LinkedList<String>();
		for (List<String> paths : entries.values()) {
			result.addAll(paths);
		}
		return Collections.unmodifiableList(result);
	}

	public void startElement() {
		XMLElement e = parser.getCurrentElement();
		String ns = e.getNamespace();
		if (e.getName().equals("rootfile") && ns != null
				&& ns.equals("urn:oasis:names:tc:opendocument:xmlns:container")) {
			String mediaType = (e.getAttribute("media-type")!=null)?e.getAttribute("media-type").trim():"unknown";
			String fullPath = e.getAttribute("full-path");
			if (!entries.containsKey(mediaType)){
				entries.put(mediaType, new LinkedList<String>());
			}
			entries.get(mediaType).add(fullPath);
		}
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
