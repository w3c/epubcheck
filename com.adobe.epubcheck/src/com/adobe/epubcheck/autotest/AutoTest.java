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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author Paul Norton
 *
 */
public class AutoTest {
	
	SAXParser parser;
	String path;
	
	public AutoTest(String str) {
		this.path = str;
	}

	/**
	 * Parse an xml file
	 * 
	 * @param uri
	 *            the uri of the file to parse
	 * @throws IOException
	 * @throws SAXException
	 */
	public void parse(String uri) throws IOException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			AutoTestContentHandler handler = new AutoTestContentHandler(this.path);
			parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(handler);
			reader.parse(uri);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 *        list of arguments of the program: testfile path_to_testdocs [options]
	 * 
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage : AutoTest testfile testdocs_path");
			System.exit(1);
		}
		System.out.println("start");
		String uri = args[0];
		String path = args[1];

		try {
			AutoTest parser = new AutoTest(path);
			parser.parse(uri);
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}

}
