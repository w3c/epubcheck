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

package com.adobe.epubcheck.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.ResourceUtil;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;

public class XMLParser extends DefaultHandler {

	ZipFile zip;

	SAXParser parser;

	Report report;

	String resource;

	Vector contentHandlers = new Vector();

	XMLElement currentElement;

	ContentHandler validatorContentHandler;

	DTDHandler validatorDTDHandler;

	Locator documentLocator;

	static Hashtable systemIdMap;

	static {
		Hashtable map = new Hashtable();
		map.put("http://www.idpf.org/dtds/2007/opf.dtd", ResourceUtil
				.getResourcePath("dtd/opf20.dtd"));
		map.put("http://openebook.org/dtds/oeb-1.2/oeb12.ent", ResourceUtil
				.getResourcePath("dtd/oeb12.dtdinc"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
				ResourceUtil.getResourcePath("dtd/xhtml1-transitional.dtd"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
				ResourceUtil.getResourcePath("dtd/xhtml1-strict.dtd"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent", ResourceUtil
				.getResourcePath("dtd/xhtml-lat1.dtdinc"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent",
				ResourceUtil.getResourcePath("dtd/xhtml-symbol.dtdinc"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent",
				ResourceUtil.getResourcePath("dtd/xhtml-special.dtdinc"));
		map.put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
				ResourceUtil.getResourcePath("dtd/svg11.dtd"));
		map.put("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd",
				ResourceUtil.getResourcePath("dtd/opf20.dtd"));
		map.put("http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd",
				ResourceUtil.getResourcePath("dtd/dtbook-2005-2.dtd"));
		systemIdMap = map;
	}

	public XMLParser(ZipFile zip, String entryName, Report report) {
		this.report = report;
		this.resource = entryName;
		this.zip = zip;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setDTDHandler(this);
			reader.setContentHandler(this);
			reader.setEntityResolver(this);
			reader.setErrorHandler(this);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	public void addXMLHandler(XMLHandler handler) {
		contentHandlers.add(handler);
	}

	public void addValidator(XMLValidator xv) {
		PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
		propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER,
				(ErrorHandler) this);
		Validator validator = xv.schema.createValidator(propertyMapBuilder
				.toPropertyMap());
		validatorContentHandler = validator.getContentHandler();
		validatorDTDHandler = validator.getDTDHandler();
	}

	public void process() {
		try {
			InputStream in = zip.getInputStream(zip.getEntry(resource));
			parser.parse(in, this);
			in.close();
		} catch (IOException e) {
			report.error(null, 0, "I/O error reading " + resource);
		} catch (IllegalArgumentException e) {
			report.error(null, 0, "could not parse " + resource + ": " + e.getMessage() );
		} catch (SAXException e) {
			report.error(resource, 0, e.getMessage());
		}
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		String resourcePath = (String) systemIdMap.get(systemId);
		if (resourcePath == null)
			return null;
		InputStream resourceStream = ResourceUtil
				.getResourceStream(resourcePath);
		InputSource source = new InputSource(resourceStream);
		source.setPublicId(publicId);
		source.setSystemId(systemId);
		return source;
	}

	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
		if (validatorDTDHandler != null)
			validatorDTDHandler.notationDecl(name, publicId, systemId);
	}

	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
		if (validatorDTDHandler != null)
			validatorDTDHandler.unparsedEntityDecl(name, publicId, systemId,
					notationName);
	}

	public void error(SAXParseException ex) throws SAXException {
		report.error(resource, ex.getLineNumber(), ex.getMessage());
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		report.error(resource, ex.getLineNumber(), ex.getMessage());
	}

	public void warning(SAXParseException ex) throws SAXException {
		report.warning(resource, ex.getLineNumber(), ex.getMessage());
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.characters(arg0, arg1, arg2);
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).characters(arg0, arg1,
					arg2);
	}

	public void endDocument() throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.endDocument();
	}

	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.endElement(arg0, arg1, arg2);
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).endElement();
		currentElement = currentElement.getParent();
	}

	public void endPrefixMapping(String arg0) throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.endPrefixMapping(arg0);
	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.ignorableWhitespace(arg0, arg1, arg2);
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).ignorableWhitespace(
					arg0, arg1, arg2);
	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.processingInstruction(arg0, arg1);
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).processingInstruction(
					arg0, arg1);
	}

	public void setDocumentLocator(Locator locator) {
		if (validatorContentHandler != null)
			validatorContentHandler.setDocumentLocator(locator);
		documentLocator = locator;
	}

	public void skippedEntity(String arg0) throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.skippedEntity(arg0);
	}

	public void startDocument() throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.startDocument();
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.startElement(namespaceURI, localName,
					qName, atts);
		int index = qName.indexOf(':');
		String prefix;
		String name;
		if (index < 0) {
			prefix = null;
			name = qName;
		} else {
			prefix = qName.substring(0, index);
			name = qName.substring(index + 1);
		}
		int count = atts.getLength();
		XMLAttribute[] attributes = count == 0 ? null : new XMLAttribute[count];
		for (int i = 0; i < count; i++) {
			String attName = atts.getLocalName(i);
			String attNamespace = atts.getURI(i);
			String attQName = atts.getQName(i);
			int attIndex = attQName.indexOf(':');
			String attPrefix;
			if (attIndex < 0) {
				attPrefix = null;
				attNamespace = null;
			} else {
				attPrefix = attQName.substring(0, attIndex);
			}
			String attValue = atts.getValue(i);
			attributes[i] = new XMLAttribute(attNamespace, attPrefix, attName,
					attValue);
		}
		currentElement = new XMLElement(namespaceURI, prefix, name, attributes,
				currentElement);
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).startElement();
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		if (validatorContentHandler != null)
			validatorContentHandler.startPrefixMapping(arg0, arg1);
	}

	public XMLElement getCurrentElement() {
		return currentElement;
	}

	public Report getReport() {
		return report;
	}

	public int getLineNumber() {
		return documentLocator.getLineNumber();
	}

}
