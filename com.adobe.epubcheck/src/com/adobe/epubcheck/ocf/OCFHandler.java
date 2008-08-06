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

import java.util.HashSet;

import java.net.URLDecoder;

import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OCFHandler implements XMLHandler {

	XMLParser parser;

	static String rootPath;
	
	/** contains the base path to the directory where content.opf is stored (for encryption.xml) */
	String rootBase = new String("");
	
	/** contains encrypted entries (for encryption.xml validation) */
	HashSet encryptedItemsSet = new HashSet();
	
	/** toggle mapping encrypted entries to HashSet (for encryption.xml) */
	private boolean populateEncryptedItems = false;

	OCFHandler(XMLParser parser) {
		this.parser = parser;
	}

	public String getRootPath() {
		return rootPath;
	}

	public HashSet getEncryptedItems() {
		return encryptedItemsSet;
	}
	
	public void setPopulateEnryptedItems(boolean populateEncryptedItems) {
		this.populateEncryptedItems = populateEncryptedItems;
	}
	
	public void setRootBase(String rootBase) {
		this.rootBase = rootBase;
	}
	
	public void startElement() {
		XMLElement e = parser.getCurrentElement();
		String ns = e.getNamespace();
		if (e.getName().equals("rootfile") && ns != null
				&& ns.equals("urn:oasis:names:tc:opendocument:xmlns:container")) {
			String mediaType = e.getAttribute("media-type");
			if (mediaType != null
					&& mediaType.equals("application/oebps-package+xml")) {
				rootPath = e.getAttribute("full-path");
			}
		}
		
		// This code will only be executed if populateEncryptedItems
		// is set to true before the check is run.
		if (populateEncryptedItems) {
			// if the element is <CipherReference>, then the element name
			// is stripped of rootBase, and URLDecoded, and finally put into
			// encryptedItemsSet. 
			if (e.getName().equals("CipherReference")) {
				try {
					String entryName = e.getAttribute("URI");
					entryName = stripPathFromURI(entryName);
					entryName = URLDecoder.decode(entryName, "UTF-8");
					encryptedItemsSet.add(entryName);
				} catch (Exception ex) {
					System.err.println("Error URL-decoding CipherReference entry");
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method strips the rootBase path from the URI passed to it. 
	 * This is useful for removing the directory structure associated with
	 * the encrypted file entry names. In the case the content.opf is in
	 * OEBPS/content.opf, the prefix OEBPS/ will be removed from any 
	 * encrypted entry name before it is placed in the EncryptedItemsSet
	 * @param URI
	 * @return
	 */
	public String stripPathFromURI(String URI) {
		if (URI.startsWith(rootBase))
			return URI.substring(rootBase.length());
		else
			return URI;
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
