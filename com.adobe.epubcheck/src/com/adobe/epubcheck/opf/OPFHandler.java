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

import java.util.Hashtable;
import java.util.Vector;

import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OPFHandler implements XMLHandler {

	XMLParser parser;

	Hashtable itemMapById = new Hashtable();

	Hashtable itemMapByPath = new Hashtable();

	Vector spine = new Vector();
	Vector items = new Vector();

	String path;

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

	public void startElement() {
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
			} else if (name.equals("item")) {
				String id = e.getAttribute("id");
				String href = e.getAttribute("href");
				if (href != null) {
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
				if (href != null) {
					itemMapByPath.put(href, item);
					items.add(item);
				}
			} else if (name.equals("spine")) {
				String idref = e.getAttribute("toc");
				if( idref != null ) {
					toc = (OPFItem) itemMapById.get(idref);
					if( toc == null )
						parser.getReport().error(path, parser.getLineNumber(), "item with id '" + idref + "' not found");
					else
						toc.setNcx(true);
				}
			} else if (name.equals("itemref")) {
				String idref = e.getAttribute("idref");
				if (idref != null) {
					OPFItem item = getItemById(idref);
					if (item != null) {
						spine.add(item);
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
