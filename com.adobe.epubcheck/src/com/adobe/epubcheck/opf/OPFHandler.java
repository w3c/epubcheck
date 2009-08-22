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

package com.adobe.epubcheck.opf;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import java.net.URLDecoder;

import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OPFHandler implements XMLHandler {

	XMLParser parser;

	Hashtable itemMapById = new Hashtable();

	Hashtable itemMapByPath = new Hashtable();
	
	HashSet encryptedItemsSet;

	Vector spine = new Vector();
	Vector items = new Vector();

	String path;
	
	//This string holds the value of the <package> element's unique-identifier attribute
	//that will be used to make sure that the unique-identifier references an existing
	//<dc:identifier> id attribute
	String uniqueIdent;
	
	//This boolean specifies whether or not there has been a <dc:identifier> element
	//parsed that has an id attribute that corresponds with the unique-identifier attribute
	//from the packaging element. The default value is false.
	boolean uniqueIdentExists = false;

	OPFItem toc;
	
	boolean opf12PackageFile = false;

	OPFHandler(XMLParser parser, String path) {
		this.parser = parser;
		this.path = path;
	}

	public boolean getOpf12PackageFile()
	{
		return(opf12PackageFile);
	}
	
	public boolean getOpf20PackageFile()
	{
		return(!opf12PackageFile);
	}
	
	public OPFItem getTOC() {
		return toc;
	}

	public OPFItem getItemById(String id) {
		return (OPFItem) itemMapById.get(id);
	}

	public OPFItem getItemByPath(String path) {
		return (OPFItem) itemMapByPath.get(path);
	}

	public int getSpineItemCount() {
		return spine.size();
	}

	public OPFItem getSpineItem(int index) {
		return (OPFItem) spine.elementAt(index);
	}

	public int getItemCount() {
		return items.size();
	}

	public OPFItem getItem(int index) {
		return (OPFItem) items.elementAt(index);
	}
	
	/**
	 * Checks to see if the unique-identifier attribute of the package element
	 * references an existing DC metadata identifier element's id attribute
	 * 
	 * @return true if there is an identifier with an id attribute that matches
	 * the value of the unique-identifier attribute of the package element. False
	 * otherwise.
	 */
	public boolean checkUniqueIdentExists() {
		return uniqueIdentExists;
	}
	
	public void setEncryptedItemsSet(HashSet encryptedItemsSet) {
		this.encryptedItemsSet = encryptedItemsSet;
	}

	public void startElement() {
		boolean registerEntry = true;
		XMLElement e = parser.getCurrentElement();
		String ns = e.getNamespace();
		if (ns == null || ns.equals("") || ns.equals("http://openebook.org/namespaces/oeb-package/1.0/")
				|| ns.equals("http://www.idpf.org/2007/opf")) {
			String name = e.getName();
			if( name.equals("package") ) {
				if(!ns.equals("http://www.idpf.org/2007/opf"))
				{
					parser.getReport().warning(path, parser.getLineNumber(), "OPF file is using OEBPS 1.2 syntax allowing backwards compatibility");
					opf12PackageFile = true;
				}
				/* This section checks to see the value of the
				 * unique-identifier attribute and stores it 
				 * in the String uniqueIdent or reports an error
				 * if the unique-identifier attribute is missing
				 * or does not have a value
				 */
				String uniqueIdentAttr = e.getAttribute("unique-identifier");
				if ( uniqueIdentAttr != null && !uniqueIdentAttr.equals(""))
				{
					uniqueIdent = uniqueIdentAttr;
				}
				else
				{
					parser.getReport().error(path, parser.getLineNumber(), "unique-identifier attribute in package element must be present and have a value");
				}
			} else if (name.equals("item")) {
				String id = e.getAttribute("id");
				String href = e.getAttribute("href");
				if (href != null) {
					try {
						// if the entry is encrypted per encryption.xml file
						if (encryptedItemsSet != null && encryptedItemsSet.contains(URLDecoder.decode(href, "UTF-8"))) {
							// then do not register the entry (it shouldn't be checked)
							registerEntry = false;
							// if the entry is not required, warn and continue
							if(isNotRequiredContent(href))
								parser.getReport().warning(path, parser.getLineNumber(), href + " is an encrypted non-required entry! Epubcheck will not validate " + href);
							// else (the entry is requried), error and exit (cannot continue with encrypted required content!)
							else {
								parser.getReport().error(path, parser.getLineNumber(), href + " is an encrypted required entry! \nEpubcheck will not validate ePubs with encrypted required content files! Tool will EXIT");
								System.exit(1);
							}
						}
					} catch (Exception ex) {
						System.err.println("Error decoding entry: " + name);
						ex.printStackTrace();
						parser.getReport().error(path, parser.getLineNumber(), ex.getMessage());
					}
						try {
						href = PathUtil.resolveRelativeReference(path, href);
					} catch( IllegalArgumentException ex ) {
						parser.getReport().error(path, parser.getLineNumber(), ex.getMessage());
						href = null;
					}
				}
				String mimeType = e.getAttribute("media-type");
				String fallback = e.getAttribute("fallback");
				String fallbackStyle = e.getAttribute("fallback-style");
				String namespace = e.getAttribute("island-type");
				OPFItem item = new OPFItem(id, href, mimeType, fallback,
						fallbackStyle, namespace, parser.getLineNumber());
				if (id != null) {
					/*
					 * The following error report was made obsolete by the schematron
					 * rule that checks for unique id attribute values across the whole opf file
					 * (see opf.sch and OPFChecker.java)
					 * 
					 * OPFItem prevItem = (OPFItem)itemMapById.get(id);
					 * if( prevItem != null ) {
					 *  	parser.getReport().error(path, parser.getLineNumber(), "item duplicate id, see line " + prevItem.getLineNumber() );
					 * }
					 */
					itemMapById.put(id, item);
				}
				if (href != null && registerEntry) {
					itemMapByPath.put(href, item);
					items.add(item);
				}
			} else if (name.equals("spine")) {
				String idref = e.getAttribute("toc");
				if( idref != null ) {
					toc = (OPFItem) itemMapById.get(idref);
					if( toc == null )
						parser.getReport().error(path, parser.getLineNumber(), "item with id '" + idref + "' not found");
					else {
						toc.setNcx(true);
						if( toc.getMimeType() != null && !toc.getMimeType().equals("application/x-dtbncx+xml") )
							parser.getReport().error(path, parser.getLineNumber(), "toc attribute references resource with non-NCX mime type; \"application/x-dtbncx+xml\" is expected");							
					}
				}
			} else if (name.equals("itemref")) {
				String idref = e.getAttribute("idref");
				if (idref != null) {
					OPFItem item = getItemById(idref);
					if (item != null) {
						spine.add(item);
						item.setInSpine(true);
					} else {
						parser.getReport().error(path, parser.getLineNumber(), "item with id '" + idref + "' not found");						
					}
				}
			}
			else if (name.equals("dc-metadata") || name.equals("x-metadata"))
			{
				if (!opf12PackageFile)
					parser.getReport().error(path, parser.getLineNumber(), "use of deprecated element '" + name + "'");
			}
		}
		else if(ns.equals("http://purl.org/dc/elements/1.1/"))
		{
			// in the DC metadata, when the <identifier> element is parsed, if it has
			// a non-null and non-empty id attribute value that is the same as the
			// value of the unique-identifier attribute of the package element,
			// set uniqueIdentExists = true (to make sure that the unique-identifier
			// attribute references an existing <identifier> id attribute
			String name = e.getName();
			if (name.equals("identifier"))
			{
				String idAttr = e.getAttribute("id");
				if(idAttr != null && !idAttr.equals("") && idAttr.equals(uniqueIdent))
					uniqueIdentExists = true;
			}
		}
	}
	
	/** 
	 * This method is used to check whether the passed href has
	 * and extension that is a required content file inside
	 * the OPF spec. This method returns false if it is requried,
	 * or true if it is not.
	 * 
	 * @param href String to check
	 * @return true if it is not required, false if it is.
	 */
	public boolean isNotRequiredContent(String href) {
		if(href.endsWith(".opf"))
			return false;
		else if(href.endsWith(".html"))
			return false;
		else if (href.endsWith(".ncx"))
			return false;
		else if (href.endsWith(".xpgt"))
			return false;
		else if (href.endsWith(".xhtml"))
			return false;
		else
			return true;
		
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
